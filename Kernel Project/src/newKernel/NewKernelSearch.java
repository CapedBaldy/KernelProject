package newKernel;

import java.time.Duration;
import kernel.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;

import gurobi.GRBCallback;
import newKernel.KernelAnalyzer;
import utilities.PopulationTools;
import utilities.SamplingTools;

public class NewKernelSearch
{
	private String instPath;
	private String logPath;
	private Configuration config;
	private List<Item> items;
	private ItemSorter sorter;
	private BucketBuilder bucketBuilder;
	private KernelBuilder kernelBuilder;
	private int tlim;
	private Solution bestKernelSolution;
	private Solution bestSolution;
	private List<Bucket> buckets;
	private Kernel kernel;
	private int tlimKernel;
	private int tlimBucket;
	private int numIterations;
	private GRBCallback callback;
	private KernelAnalyzer kernelAnalyzer;
	private EnumeratedDistribution<Bucket> mainDistribution;
	private int candidateThreshold;
	private int mainTabooCounter;
	private int maxBucketsInCandidate;
	private short poorBucketLimit;
	
	private Instant startTime;
	
	public NewKernelSearch(String instPath, String logPath, Configuration config)
	{
		this.instPath = instPath;
		this.logPath = logPath;
		this.config = config;
		bestKernelSolution = new Solution();
		bestSolution= new Solution();
		configure(config);
	}
	
	private void configure(Configuration configuration)
	{
		sorter = config.getItemSorter();
		tlim = config.getTimeLimit();
		bucketBuilder = config.getBucketBuilder();
		kernelBuilder = config.getKernelBuilder();
		tlimKernel = config.getTimeLimitKernel();
		numIterations = config.getNumIterations();
		tlimBucket = config.getTimeLimitBucket();
		candidateThreshold=config.getCandidateThreshold();
		mainTabooCounter=config.getMainTabooCounter();
		maxBucketsInCandidate=config.getMaxBucketsInCandidate();
		poorBucketLimit=config.getPoorBucketLimit();
	}
	
	public Solution start()
	{
		startTime = Instant.now();
		callback = new CustomCallback(logPath, startTime);
		items = buildItems();
		sorter.sort(items);	
		kernel = kernelBuilder.build(items, config);
		buckets = bucketBuilder.build(items.stream().filter(it -> !kernel.contains(it)).collect(Collectors.toList()), config);
		solveKernel();
		solveBuckets();

		
		return bestKernelSolution;
	}

	private List<Item> buildItems()
	{
		Model model = new Model(instPath, logPath, config.getTimeLimit(), config, true); // time limit equal to the global time limit
		model.buildModel();
		model.solve();
		List<Item> items = new ArrayList<>();
		List<String> varNames = model.getVarNames();
		
		for(String v : varNames)
		{
			double value = model.getVarValue(v);
			double rc = model.getVarRC(v); // can be called only after solving the LP relaxation
			Item it = new Item(v, value, rc);
			items.add(it);
		}
		return items;
	}
	
	private void solveKernel()
	{
		Model model = new Model(instPath, logPath, Math.min(tlimKernel, getRemainingTime()), config, false);	
		model.buildModel();
		if(!bestKernelSolution.isEmpty())
			model.readSolution(bestKernelSolution);
		
		List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it)).collect(Collectors.toList());
		model.disableItems(toDisable);
		model.setCallback(callback);
		model.solve();
		if(model.hasSolution() && (model.getSolution().getObj() < bestKernelSolution.getObj() || bestKernelSolution.isEmpty()))
		{
			bestKernelSolution = model.getSolution();
			model.exportSolution();
		}
	}
	
	
	private void solveBuckets(){
		
		double average=0.0;
		boolean changedAverageFlag=false;
		short poorBucketCount=0;
		mainDistribution= new PopulationTools<Bucket>(buckets).getGaussianDistribution(average);
		while(getRemainingTime()>0){
			
			candidateThreshold=config.getCandidateThreshold();
			ArrayList<Candidate> candidatesList = new ArrayList<>();
			
			
			
			//riempimento della pool
			while(candidatesList.size()<candidateThreshold||candidatesList.stream().anyMatch(it->!it.isCompleted())){
				System.out.println("average: "+average);
				System.out.println("bucket inseriti: "+candidatesList.size());
				System.out.println("bucket fuori: "+buckets.size());
					
				if(changedAverageFlag){
					mainDistribution= new PopulationTools<Bucket>(buckets).getGaussianDistribution(average);
					changedAverageFlag=false;
							
				}
				ArrayList<Bucket> tempBucketList= SamplingTools.sampleWithTaboo(candidateThreshold-candidatesList.size(), mainDistribution);
				if(tempBucketList==null){
					
					if(candidateThreshold>2) candidateThreshold-=1;
					else solveEmergencyBucket();
					buckets.stream().filter(bucket -> {
						
						for(Candidate c: candidatesList){
							if(c.getOriginalBucket()==bucket) return false;
						}
						
						return true;
						//break?
					}).forEach(bucket->{
						
						if(bucket.isTaboo()) bucket.setTaboo(false);
					});
					
//					buckets.stream().forEach(bucket -> {
//						
//						if(bucket.isTaboo()) bucket.setTaboo(false);
//						//break?
//					});
					
				}

				if(tempBucketList!=null){
				for(Bucket b: tempBucketList){
					System.out.println(buckets.indexOf(b));
					if(poorBucketCount>=poorBucketLimit){
						average+=10;
						if(average==110.0) average=0;
						poorBucketCount=0;
						changedAverageFlag=true;
						break;
					}
					
					List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it) && !b.contains(it)).collect(Collectors.toList());
					Model model = new Model(instPath, logPath, Math.min(tlimBucket, getRemainingTime()), config, false);	
					model.buildModel();		
					model.disableItems(toDisable);
					model.setCallback(callback);
					System.out.println("Solving extracted bucket: ");
					model.solve();
					
					if(model.hasSolution()){
						if(model.getSolution().getObj()<bestKernelSolution.getObj() || bestKernelSolution.isEmpty()) {
							
							candidatesList.add(new Candidate(b, buckets.indexOf(b), buckets.size(), model.getSolution()));
							b.setTaboo(true);
							System.out.println("Found solution");
						}
						
						
						if(model.getSolution().getObj()<bestSolution.getObj()||bestSolution.isEmpty()) {
							bestSolution=model.getSolution();
							model.exportSolution();
						}
						
					}else if(!model.hasSolution()||model.getSolution().getObj()>=bestKernelSolution.getObj()){
						b.setTaboo(true);
						poorBucketCount++;
						System.out.println("No solution");
					}
	
				}
				}
				for(Candidate c: candidatesList){
					
					if(c.getCurrentVersion().size()==maxBucketsInCandidate) c.setCompleted(true);
					int nextPosition=c.getNextAdiacentPosition();
					
					if(!c.isCompleted()){
						
						
						
						Bucket tempBucket = buckets.get(nextPosition);
						c.addBucket(tempBucket);
						
						List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it) && !c.mergeBuckets().contains(it)).collect(Collectors.toList());
						Model model = new Model(instPath, logPath, Math.min(tlimBucket, getRemainingTime()), config, false);	
						model.buildModel();		
						model.disableItems(toDisable);
						model.setCallback(callback);
						model.solve();
						
						if(!model.hasSolution()||model.getSolution().getObj()>=c.getSol().getObj()) {
							c.undoAddBucket();
							c.setCompleted(true);
						}
						else if(model.hasSolution()&&model.getSolution().getObj()<c.getSol().getObj()){

							c.setSol(model.getSolution());
								
							if(model.getSolution().getObj()<bestSolution.getObj()||bestSolution.isEmpty()) {
								bestSolution=model.getSolution();
								model.exportSolution();
								
							}
						}
						
						
					}
					
				}
				
				
			}
			
			buckets.stream().forEach(bucket -> {
				
				if(bucket.isTaboo()) bucket.setTaboo(false);
				
			});
			
			
			PoolAnalyzer poolAnalyzer = new PoolAnalyzer(this, candidatesList);
			if(poolAnalyzer.initialize()){
				poolAnalyzer.analyzePool();
				
				
				if(!poolAnalyzer.getBestPoolSolution().isEmpty()&&(poolAnalyzer.getBestPoolSolution().getObj()<bestSolution.getObj() || bestSolution.isEmpty())) {
					bestSolution=poolAnalyzer.getBestPoolSolution(); //già esportata in poolanalyzer
					
					if(poolAnalyzer.getItemForKernel()!=null) poolAnalyzer.getItemForKernel().stream().forEach(it->{
						
						kernel.addItem(it);
						for(Bucket b: buckets){
							if(b.contains(it)) b.removeItem(it);
						}
						
						});
					
					Model model = new Model(instPath, logPath, Math.min(tlimKernel, getRemainingTime()), config, false);	
					model.buildModel();
					if(!bestSolution.isEmpty())
						model.readSolution(bestSolution);
					
					List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it)).collect(Collectors.toList());
					model.disableItems(toDisable);
					model.setCallback(callback);
					model.solve();
					if(model.hasSolution() && (model.getSolution().getObj() < bestKernelSolution.getObj() || bestKernelSolution.isEmpty()))
					{
						bestKernelSolution = model.getSolution();
						model.exportSolution();
						
					}
							
					if(model.getSolution().getObj() < bestSolution.getObj())  {
						bestSolution=model.getSolution();
						model.exportSolution();
					}	
					
				}
				
				
				
			}
			}
			
			
				
		}
	
	
	
	
	
	

	public int getRemainingTime()
	{
		return (int) (tlim - Duration.between(startTime, Instant.now()).getSeconds());
	}

	public String getInstPath() {
		return instPath;
	}

	public String getLogPath() {
		return logPath;
	}

	public Configuration getConfig() {
		return config;
	}

	public List<Item> getItems() {
		return items;
	}

	public KernelBuilder getKernelBuilder() {
		return kernelBuilder;
	}

	public int getTlim() {
		return tlim;
	}

	public Solution getBestKernelSolution() {
		return bestKernelSolution;
	}

	public Kernel getKernel() {
		return kernel;
	}

	public int getTlimKernel() {
		return tlimKernel;
	}

	public int getTlimBucket() {
		return tlimBucket;
	}

	public KernelAnalyzer getKernelAnalyzer() {
		return kernelAnalyzer;
	}

	public GRBCallback getCallback() {
		return callback;
	}

	public Solution getBestSolution() {
		return bestSolution;
	}

	public void setBestSolution(Solution bestSolution) {
		this.bestSolution = bestSolution;
	}
	
	
	private void solveEmergencyBucket()
	{
		for(Bucket b : buckets)
		{
			System.out.println("Solving emergency bucket: ");
			List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it) && !b.contains(it)).collect(Collectors.toList());

			Model model = new Model(instPath, logPath, Math.min(tlimBucket, getRemainingTime()), config, false);	
			model.buildModel();
					
			model.disableItems(toDisable);
		//	model.addBucketConstraint(b.getItems()); // can we use this constraint regardless of the type of variables chosen as items?
			
			if(!bestSolution.isEmpty())
			{
					
				model.readSolution(bestSolution);
			}
			
			model.setCallback(callback);
			model.solve();
			
			if(model.hasSolution() && (model.getSolution().getObj() < bestKernelSolution.getObj()  || bestKernelSolution.isEmpty()))
			{
				bestKernelSolution = model.getSolution();
				List<Item> selected = model.getSelectedItems(b.getItems());
				selected.forEach(it -> kernel.addItem(it));
				selected.forEach(it -> b.removeItem(it));
				model.exportSolution();
				
				if(model.getSolution().getObj()<bestSolution.getObj()||bestSolution.isEmpty()) {
					bestSolution=model.getSolution();
					model.exportSolution();
			}
			
			break;

		}	
	}
	
	}
}
package newKernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;

import gurobi.GRB;
import kernel.Item;
import kernel.KernelSearch;
import kernel.Model;
import kernel.Solution;
import utilities.PopulationTools;
import utilities.SamplingTools;

import java.time.Duration;
import java.time.Instant;

public class PoolAnalyzer {
	
	private Bindings bindings;
	private ArrayList<Candidate> candidates;
	private KernelSearch mainProcess;
	private Solution bestSolution;  //best solution relative to the pool, not the main process
	private ArrayList<Item> poolItems;
	private ArrayList<Item> itemsWithBindings;
	private Solution LPSol;
	private EnumeratedDistribution<Item> distribution;
	private Instant startTime;
	private int timeLimit;
	private int wastedIterations; //iterazioni con soluzione minore di bestSolution
	private int wastedIterLimit;
	private double extractionPerc;
	private int activeThreshold;
	private int negativeThreshold;
	private int fixedThreshold;
	private double percThreshold;
	private int poolTabooCounter;
	
	

	
	public PoolAnalyzer(KernelSearch process){
		
		this.mainProcess=process;
		bindings= new Bindings();
		startTime=Instant.now();
		wastedIterations=0;
		timeLimit=mainProcess.getConfig().getPoolTimeLimit();
		wastedIterLimit=mainProcess.getConfig().getWastedIterLimit();
		extractionPerc=mainProcess.getConfig().getExtractionPerc();
		activeThreshold=mainProcess.getConfig().getActiveThreshold();
		negativeThreshold=mainProcess.getConfig().getNegativeThreshold();
		percThreshold=mainProcess.getConfig().getPercThreshold();
		fixedThreshold=mainProcess.getConfig().getFixedThreshold();
		poolTabooCounter=mainProcess.getConfig().getPoolTabooCounter();
		itemsWithBindings= new ArrayList<Item>();
		bestSolution= new Solution();

		
	}


	public ArrayList<Candidate> getPool() {
		return candidates;
	}
	
	
	public void analyzePool(){
		
		int sampleSize= (int) Math.round(poolItems.size()*extractionPerc);
		
		
		
		while(wastedIterations<=wastedIterLimit && !checkStop()){
			
			HashMap<Item,Double> weightMap = distribution.getHashPmf();

			Model model = new Model(mainProcess.getInstPath(), mainProcess.getLogPath(), Math.min(mainProcess.getTlim(), mainProcess.getRemainingTime()), mainProcess.getConfig(),false);
			model.buildModel();
			model.setCallback(mainProcess.getCallback());
			
			ArrayList<Item> samples= SamplingTools.sampleWithTaboo(sampleSize, distribution);
			ArrayList<Item> notSamples= new ArrayList<>(poolItems.stream().filter(it->!samples.contains(it)).collect(Collectors.toList()));
			ArrayList<Item> toDisable = new ArrayList<Item>();
			ArrayList<Item> addedActiveItems= new ArrayList<Item>();
			ArrayList<Item> addedFixedItems = new ArrayList<Item>();			
			
			ArrayList<ItemBinding> internalBindings = bindings.searchInternalBinding(samples, itemsWithBindings);
			
			for(ItemBinding i: internalBindings){
				if(i.negativeBindingCheck(negativeThreshold, percThreshold)){
					if(weightMap.get(i.getItemA())>weightMap.get(i.getItemB())) toDisable.add(i.getItemB());
					if(weightMap.get(i.getItemA())<weightMap.get(i.getItemB())) toDisable.add(i.getItemA());
					if(weightMap.get(i.getItemA())==weightMap.get(i.getItemB())) toDisable.add(new Random().nextBoolean()? i.getItemA(): i.getItemB());

				}
			}
			
			ArrayList<ItemBinding> externalBindings = bindings.searchExternalBinding(samples, notSamples, itemsWithBindings);
			
			for(ItemBinding i: externalBindings){
				
				Item toAdd = (notSamples.contains(i.getItemA()))? i.getItemA() : i.getItemB();
				ArrayList<Double> fixedvalues=i.fixedBindingCheck(toAdd, fixedThreshold);
				
				if(!fixedvalues.isEmpty()) {
					
					model.setFixedValue(toAdd, fixedvalues.get(new Random().nextInt(fixedvalues.size())));
					addedFixedItems.add(toAdd);
				}
				else if(i.activeBindingCheck(activeThreshold, percThreshold)) addedActiveItems.add(toAdd);
			}
			
			
			toDisable.addAll(mainProcess.getItems().stream().filter(it->(!mainProcess.getKernel().contains(it) && !addedActiveItems.contains(it) && !addedFixedItems.contains(it) 
					&& !samples.contains(it))).collect(Collectors.toList()));
			//gli item contenuti in samples che devono essere esclusi sono già in toDisable
			
			model.disableItems(toDisable);
			
			if(!bestSolution.isEmpty()) model.readSolution(bestSolution);
			model.solve();
			
			if(model.hasSolution()){
				
				ArrayList<Item> notFixedActiveItems = new ArrayList<Item>(mainProcess.getItems().stream().
						filter(it->(!toDisable.contains(it)&&!addedFixedItems.contains(it))).collect(Collectors.toList()));
				
				if(bestSolution.isEmpty() || model.getSolution().getObj()>bestSolution.getObj()) bestSolution=model.getSolution();
				SolutionAnalyzer analyzer = new SolutionAnalyzer(bindings, model.getSolution(), LPSol.getObj(), mainProcess.getBestSolution().getObj(), 
						distribution, notFixedActiveItems, itemsWithBindings);
				
				analyzer.updateBindings();
				HashMap<Item,Double> newWeightMap = analyzer.updateWeights();
				
				notFixedActiveItems.stream().forEach(it->it.setTabooCount((short) poolTabooCounter));
				
				distribution= new EnumeratedDistribution<>(newWeightMap);
				wastedIterations=0;
				
				
				
			}else wastedIterations++;
			
			
			
			
			
		}
		
		
		
		
		
	}
		
	boolean initialize(){
		
		//poolItems = new ArrayList<Item>();
		HashSet<Item> tempPoolItems = new HashSet<>();
		//ArrayList<Item> activeItemsInCandidates = new ArrayList<Item>();
		HashSet<Item> tempActiveItemsInCandidates = new HashSet<>(); //uso hashset per evitare duplicati
		
		for(Candidate a: candidates){   //costruisco i bindings iniziali tenendo conto delle soluzioni dei candidati, senza assegnare pesi statici
			SolutionAnalyzer analyzer= new SolutionAnalyzer(bindings, a.getSol(), 0, mainProcess.getBestSolution().getObj(), null, a.mergeBuckets(), itemsWithBindings); //costruisco con LPSOl=0 e distribuzione =null, non devo aggiornare pesi
			analyzer.updateBindings();//assumo che best solution=soluzione coinvolgente solo kernel
			a.mergeBuckets().stream().forEach(it->tempPoolItems.add(it));  //aggiungo i bucket all'hashset, evitando duplicati
			analyzer.getActiveItems().stream().forEach(it->tempActiveItemsInCandidates.add(it));
		}
		
		poolItems= new ArrayList<Item>(tempPoolItems);
		ArrayList<Item> activeItemsInCandidates = new ArrayList<Item>(tempActiveItemsInCandidates);
		
		Model model = new Model(mainProcess.getInstPath(), mainProcess.getLogPath(), Math.min(mainProcess.getTlim(), mainProcess.getRemainingTime()), mainProcess.getConfig(),false); // time limit equal to the global time limit
		model.buildModel();
		List<Item> toDisable= mainProcess.getItems().stream().filter(it->!mainProcess.getKernel().contains(it)&&!poolItems.contains(it)).collect(Collectors.toList());
		model.disableItems(toDisable);
		model.setContinuous(poolItems);
		//model.setCallback(mainProcess.getCallback()); non metto callback, la soluzione è in parte rilassata
		model.solve();
		
		if(!model.hasSolution()){
			
			
			return false;
		}
		
		LPSol=model.getSolution();
		
		ArrayList<Item> activeItemsInLPSol = new ArrayList<Item>(model.getSelectedItems(poolItems)); //confronta con i valori della soluzione del mdoello, non di Item.Xr
		ArrayList<Item> mergedActiveItems= new ArrayList<Item>(activeItemsInLPSol.stream().filter(it->activeItemsInCandidates.contains(it)).collect(Collectors.toList())); //escludo automaticamente gli item contenuti nel kernel
		ArrayList<Item> notMergedActiveItems= new ArrayList<Item>(poolItems.stream().filter(it->!mergedActiveItems.contains(it)).collect(Collectors.toList()));
		notMergedActiveItems.stream().forEach(it->it.setPoolRc(model.getVarRC(it.getName()))); //inserisce i Rc e Xr relativi alla pool
		notMergedActiveItems.stream().forEach(it->it.setPoolXr(model.getVarValue(it.getName())));
		new ItemSorterByPoolValueAndAbsolutePoolRC().sort(notMergedActiveItems);
		
		HashMap<Item,Double> mapForDistribution= new HashMap<>();
		mergedActiveItems.stream().forEach(it->mapForDistribution.put(it, (double) 70)); //inserisco nella map gli item privilegiati
		
		PopulationTools<Item> tools = new PopulationTools<Item>(notMergedActiveItems);
		mapForDistribution.putAll(tools.getLinear()); //inserisco nella map gli item pesati secondo funzione
		
		
		
		distribution= new EnumeratedDistribution<Item>(mapForDistribution);
		
		return true;
		
			
	}
	
	
	boolean checkStop(){
		
		return (int) (timeLimit - Duration.between(startTime, Instant.now()).getSeconds())>0;
	}
	
	

}

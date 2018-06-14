package newKernel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
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
	private NewKernelSearch mainProcess;
	private Solution bestPoolSolution;  //best solution relative to the pool, not the main process
	private ArrayList<Item> poolItems;
	private ArrayList<Item> itemsWithBindings;
	private Solution LPSol;
	private EnumeratedDistribution<Item> poolDistribution;
	private Instant startTime;
	private int poolTimeLimit;
	private int wastedIterations; //iterazioni con soluzione minore di bestSolution
	private int wastedIterLimit;
	private double extractionPerc; //percentuale di item estrarre dalla pool ad ogni iterazione
	private int extractThreshold; //numero di estrazioni oltre la quale si ritiene valido un bound
	private int fixedThreshold;  //numero di volte oltre la quale si ritiene valido un bound di tipo valore fissato
	private double percThreshold; //percentuale oltre la quale si ritiene un bound buono o cattivo
	private int poolTabooCounter; 
	private int negativeThreshold;  //numero di bound oltre il quale si espelle un item
	private int positiveThreshold;  //numero di bound oltre il quale si immette un item
	private int fixedMultipleThreshold; //numero di bound-valore-fissato oltre il quale si fissa un item ad un valore
	
	

	
	public PoolAnalyzer(NewKernelSearch process, ArrayList<Candidate> candidates){
		
		this.mainProcess=process;
		this.candidates=candidates;
		bindings= new Bindings();
		startTime=Instant.now();
		wastedIterations=0;
		poolTimeLimit=mainProcess.getConfig().getPoolTimeLimit();
		wastedIterLimit=mainProcess.getConfig().getWastedIterLimit();
		extractionPerc=mainProcess.getConfig().getExtractionPerc();
		extractThreshold=mainProcess.getConfig().getExtractThreshold();
		percThreshold=mainProcess.getConfig().getPercThreshold();
		fixedThreshold=mainProcess.getConfig().getFixedThreshold();
		poolTabooCounter=mainProcess.getConfig().getPoolTabooCounter();
		itemsWithBindings= new ArrayList<Item>();
		bestPoolSolution= new Solution();
		negativeThreshold=mainProcess.getConfig().getNegativeThreshold();
		positiveThreshold=mainProcess.getConfig().getPositiveThreshold();
		fixedMultipleThreshold=mainProcess.getConfig().getFixedMultipleThreshold();

		
	}


	public ArrayList<Candidate> getPool() {
		return candidates;
	}
	
	
	public void analyzePool(){
		
		int sampleSize= (int) Math.round(poolItems.size()*extractionPerc);
		
		
		
		while(wastedIterations<=wastedIterLimit && checkStop()){
			

			Model model = new Model(mainProcess.getInstPath(), mainProcess.getLogPath(), getRemainingTime(), mainProcess.getConfig(),false);
			model.buildModel();
			model.setCallback(mainProcess.getCallback());
			
			ArrayList<Item> samples= SamplingTools.sampleWithTaboo(sampleSize, poolDistribution);
			ArrayList<Item> notSamples= new ArrayList<>(poolItems.stream().filter(it->!samples.contains(it)).collect(Collectors.toList()));
			ArrayList<Item> toDisable = new ArrayList<Item>();
			ArrayList<Item> addedActiveItems= new ArrayList<Item>();
			ArrayList<Item> addedFixedItems = new ArrayList<Item>();			
			
			ArrayList<ItemBinding> internalBindings = bindings.searchInternalBinding(samples, itemsWithBindings);
			
			

			ArrayList<ItemBinding> SingleNegativeItemBindings= new ArrayList<>(internalBindings.stream().filter(it->it.negativeBindingCheck(extractThreshold, percThreshold)).collect(Collectors.toList()));
			HashMap<Item, Integer> MultipleNegativeItemBindingsCounter = new HashMap<>();
			
			for(ItemBinding i: SingleNegativeItemBindings){
				if(MultipleNegativeItemBindingsCounter.containsKey(i.getItemA())) MultipleNegativeItemBindingsCounter.replace(i.getItemA(), MultipleNegativeItemBindingsCounter.get(i.getItemA())+1);
				else MultipleNegativeItemBindingsCounter.put(i.getItemA(), 1);
				if(MultipleNegativeItemBindingsCounter.containsKey(i.getItemB())) MultipleNegativeItemBindingsCounter.replace(i.getItemB(), MultipleNegativeItemBindingsCounter.get(i.getItemB())+1);
				else MultipleNegativeItemBindingsCounter.put(i.getItemB(), 1);
				
			}
			
			MultipleNegativeItemBindingsCounter.entrySet().stream().filter(it->it.getValue()>=negativeThreshold).forEach(it->toDisable.add(it.getKey()));
			
//			for(ItemBinding i: internalBindings){
//				if(i.negativeBindingCheck(extractThreshold, percThreshold)){
////					if(weightMap.get(i.getItemA())>weightMap.get(i.getItemB())) toDisable.add(i.getItemB());
////					if(weightMap.get(i.getItemA())<weightMap.get(i.getItemB())) toDisable.add(i.getItemA());
////					if(weightMap.get(i.getItemA())==weightMap.get(i.getItemB())) toDisable.add(new Random().nextBoolean()? i.getItemA(): i.getItemB());
//					
//
//				}
//			}
//			
			ArrayList<ItemBinding> externalBindings = bindings.searchExternalBinding(samples, notSamples);
			HashMap<Item, ArrayList<ArrayList<Double>>> valuesArraysForItems = new HashMap<>();
			HashMap<Item, Integer> MultiplePositiveItemBindingsCounter = new HashMap<>();
			
			for (ItemBinding i: externalBindings){
				
				Item toAdd = (notSamples.contains(i.getItemA()))? i.getItemA() : i.getItemB();
				ArrayList<Double> fixedvalues=i.fixedBindingCheck(toAdd, fixedThreshold);
				
				if(!fixedvalues.isEmpty()) {
					if (valuesArraysForItems.containsKey(toAdd)) valuesArraysForItems.get(toAdd).add(fixedvalues);
					else {
						ArrayList<ArrayList<Double>> temp= new ArrayList<>();
						temp.add(fixedvalues);
						valuesArraysForItems.put(toAdd, temp);
					}
				}
				
				if(i.activeBindingCheck(extractThreshold, percThreshold)) {
						if(MultiplePositiveItemBindingsCounter.containsKey(toAdd)) MultiplePositiveItemBindingsCounter.replace(toAdd, MultiplePositiveItemBindingsCounter.get(toAdd)+1);
						else MultiplePositiveItemBindingsCounter.put(toAdd, 1);
				}
				
			}
			
			MultiplePositiveItemBindingsCounter.entrySet().stream().filter(it->it.getValue()>=positiveThreshold).forEach(it->addedActiveItems.add(it.getKey()));
			
			Iterator<Item> iter =valuesArraysForItems.keySet().iterator();	
			
			while(iter.hasNext()){
				HashMap<Double, Short> fixedMultipleValues = new HashMap<>();
				Item entry= iter.next();
				
				for(ArrayList<Double> a:valuesArraysForItems.get(entry)){
					for(Double d: a){
						if(fixedMultipleValues.containsKey(d)) fixedMultipleValues.replace(d, (short)(fixedMultipleValues.get(d)+1));
						else fixedMultipleValues.put(d, (short)1);
					}
				}
				
				try{
				
					Entry<Double, Short> value=fixedMultipleValues.entrySet().stream().filter(it->(it.getValue()>=fixedMultipleThreshold)).sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).findFirst().get();
					model.setFixedValue(entry, value.getKey());
					addedFixedItems.add(entry);
					
				}catch(NoSuchElementException e){}
			}
				
	
			

			
			
//			for(ItemBinding i: externalBindings){
//				
//				Item toAdd = (notSamples.contains(i.getItemA()))? i.getItemA() : i.getItemB();
//				ArrayList<Double> fixedvalues=i.fixedBindingCheck(toAdd, fixedThreshold);
//				
//				if(!fixedvalues.isEmpty()) {
//					
//					model.setFixedValue(toAdd, fixedvalues.get(new Random().nextInt(fixedvalues.size())));
//					addedFixedItems.add(toAdd);
//				}
//				else if(i.activeBindingCheck(extractThreshold, percThreshold)) addedActiveItems.add(toAdd);
//			}
			
			
			toDisable.addAll(mainProcess.getItems().stream().filter(it->(!mainProcess.getKernel().contains(it) && !addedActiveItems.contains(it) && !addedFixedItems.contains(it) 
					&& !samples.contains(it))).collect(Collectors.toList()));
			//gli item contenuti in samples che devono essere esclusi sono già in toDisable
			if(!bestPoolSolution.isEmpty()) model.readSolution(bestPoolSolution);

			model.disableItems(toDisable);

			model.solve();
			
			if(model.hasSolution()){
				
				ArrayList<Item> notFixedActiveItems = new ArrayList<Item>(mainProcess.getItems().stream(). //item attivi e non fissati
						filter(it->(!toDisable.contains(it)&&!addedFixedItems.contains(it))).collect(Collectors.toList()));
				
				if(bestPoolSolution.isEmpty() || model.getSolution().getObj()<bestPoolSolution.getObj()) {
					bestPoolSolution=model.getSolution();
					if(model.getSolution().getObj()<mainProcess.getBestSolution().getObj()) model.exportSolution();
				}
				SolutionAnalyzer analyzer = new SolutionAnalyzer(bindings, model.getSolution(), LPSol.getObj(), mainProcess.getBestSolution().getObj(), 
						poolDistribution, notFixedActiveItems, itemsWithBindings);
				
				analyzer.updateBindings();
				HashMap<Item,Double> newWeightMap = analyzer.updateWeights();
				
				notFixedActiveItems.stream().forEach(it->it.setTabooCount((short) poolTabooCounter));
				
				poolDistribution= new EnumeratedDistribution<>(newWeightMap);
				
				if(model.getSolution().getObj()<mainProcess.getBestSolution().getObj()) wastedIterations=0;
				else wastedIterations++;
				
				
				
			}else wastedIterations++;
			
			
			
			
			
		}
		
		
		poolItems.stream().forEach(it->it.setTabooCount((short)0));
		
		
	}
		
	boolean initialize(){
		
		//poolItems = new ArrayList<Item>();
		HashSet<Item> tempPoolItems = new HashSet<>();
		//ArrayList<Item> activeItemsInCandidates = new ArrayList<Item>();
		HashSet<Item> tempActiveItemsInCandidates = new HashSet<>(); //uso hashset per evitare duplicati
		
		for(Candidate a: candidates){   //costruisco i bindings iniziali tenendo conto delle soluzioni dei candidati, senza assegnare pesi statici
			//SolutionAnalyzer analyzer= new SolutionAnalyzer(bindings, a.getSol(), 0, mainProcess.getBestKernelSolution().getObj(), null, a.mergeBuckets(), itemsWithBindings); //costruisco con LPSOl=0 e distribuzione =null, non devo aggiornare pesi
			//analyzer.updateBindings();//
			a.mergeBuckets().stream().forEach(it->tempPoolItems.add(it));  //aggiungo i bucket all'hashset, evitando duplicati
			a.mergeBuckets().stream().filter(i->(a.getSol().getVarValue(i.getName())>0)).collect(Collectors.toList()).stream().forEach(it->tempActiveItemsInCandidates.add(it));
			//E se un item è attivo in un candidato e non un un altro?
		}
		
		poolItems= new ArrayList<Item>(tempPoolItems);
		ArrayList<Item> activeItemsInCandidates = new ArrayList<Item>(tempActiveItemsInCandidates);
		
		Model model = new Model(mainProcess.getInstPath(), mainProcess.getLogPath(), getRemainingTime(), mainProcess.getConfig(),false); 
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
		if(LPSol.getObj()>=mainProcess.getBestSolution().getObj()) return false;
		
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
		
		
		
		poolDistribution= new EnumeratedDistribution<Item>(mapForDistribution);
		
		return true;
		
			
	}
	
	
	boolean checkStop(){
		
		return (poolTimeLimit - Duration.between(startTime, Instant.now()).getSeconds())>0;
	}
	
	int getRemainingTime(){
		return (int) (poolTimeLimit - Duration.between(startTime, Instant.now()).getSeconds());
	}
	
	ArrayList<Item> getItemForKernel(){
		if(!bestPoolSolution.isEmpty()) return new ArrayList<Item>(poolItems.stream().filter(i->(bestPoolSolution.getVarValue(i.getName())>0)).collect(Collectors.toList()));
		else return null;
	}


	public Solution getBestPoolSolution() {
		return bestPoolSolution;
	}


	

}

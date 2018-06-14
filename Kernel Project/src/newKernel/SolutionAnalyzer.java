package newKernel;
import kernel.Item;
import kernel.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

public class SolutionAnalyzer {
	
	private Bindings bindings;
	private Solution sol;
	private EnumeratedDistribution<Item> distribution;
	private HashMap<Item,Double> weightMap;
	private ArrayList<Item> extractedItems;
	private ArrayList<Item> activeItems;
	private ArrayList<Item> disabledItems;
	private ArrayList<Item> itemsWithBindings;
	private double plObj;
	private double bestObj;
	

	public SolutionAnalyzer(Bindings bindings, Solution sol, double plObj, 
			double bestObj, EnumeratedDistribution<Item> distribution, ArrayList<Item> extractedItems, ArrayList<Item> itemsWithBindings){
		
		this.bindings=bindings;
		this.sol=sol;
		this.distribution=distribution;
		this.extractedItems=extractedItems;
		activeItems= new ArrayList<Item>(extractedItems.stream().filter(i->(sol.getVarValue(i.getName())>0)).collect(Collectors.toList()));
		disabledItems= new ArrayList<Item>(extractedItems.stream().filter(i->(sol.getVarValue(i.getName())==0)).collect(Collectors.toList()));
		this.plObj=plObj;
		this.bestObj=bestObj;
		this.weightMap=distribution.getHashPmf();
		this.itemsWithBindings=itemsWithBindings;
	}
	
	void updateBindings(){
		
		if(sol.getObj()>=bestObj){
			
			
			for(int i=0;i<extractedItems.size();i++ ){
				Item itemA=extractedItems.get(i);
				for(int j=i+1;j<extractedItems.size();j++){
					Item itemB=extractedItems.get(j);
					

					
					if(bindings.searchBinding(itemA,itemB)==null) {
						bindings.getBindings().put(new Pair<Item, Item>(itemA,itemB), new ItemBinding(itemA,itemB));
						itemsWithBindings.add(itemA);
						itemsWithBindings.add(itemB);
					}
					
					ItemBinding bind= bindings.searchBinding(itemA,itemB);
					bind.incrementNegativeCount();
					bind.incrementExtractedCount();
						
					}
				}
		}else{
		
			for(int i=0;i<activeItems.size();i++ ){
				Item itemA=activeItems.get(i);
				for(int j=i+1;j<activeItems.size();j++){
					Item itemB=activeItems.get(j);
					
	
					
					if(bindings.searchBinding(itemA,itemB)==null) {
						bindings.getBindings().put(new Pair<Item, Item>(itemA,itemB), new ItemBinding(itemA,itemB));
						itemsWithBindings.add(itemA);
						itemsWithBindings.add(itemB);
					}
					ItemBinding bind= bindings.searchBinding(itemA,itemB);
					bind.incrementPositiveCount();
					bind.incrementExtractedCount();
					bind.incrementValueCount(itemA, sol.getVarValue(itemA.getName()));
					bind.incrementValueCount(itemB, sol.getVarValue(itemB.getName()));
					
						
					}
				}
			
//			for(int i=0;i<disabledItems.size();i++ ){
//				Item itemA=disabledItems.get(i);
//				for(int j=i+1;j<disabledItems.size();j++){
//					Item itemB=disabledItems.get(j);
//		
//					ItemBinding bind= bindings.searchBinding(itemA,itemB); //se non esiste non occorre aumentare il numero
//					if(bind!=null)
//					bind.incrementExtractedCount();
//					
//					}
//				}
		
		}
	}
	
	
	HashMap<Item,Double> updateWeights(){
		
		double diff100=bestObj-plObj;
		double solObj=sol.getObj();
		double diffX=bestObj-solObj;
		if(diffX>0){
			double perc=diffX/diff100*100;
			
			for(Item a: activeItems){
				weightMap.replace(a, weightMap.get(a)+perc);	
			}
			
			for(Item a: disabledItems){
				weightMap.replace(a, Math.max(0, weightMap.get(a)-perc/2));	
			}
				
		}
		
		
		if(diffX<=0){
			double perc=-diffX/diff100*100;
			
			for(Item a: activeItems){
				weightMap.replace(a, weightMap.get(a)-perc);	
			}
			
			for(Item a: disabledItems){
				weightMap.replace(a, Math.max(0, weightMap.get(a)-perc*2 ));	
			}
				
		}
		
		
		return weightMap;
		
	}

	public ArrayList<Item> getActiveItems() {
		return activeItems;
	}
	
	
		
	
	
}

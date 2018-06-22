package newKernel;
import kernel.Item;
import kernel.Solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

public class SolutionAnalyzer {
	
	private Bindings bindings;
	private Solution sol;
	private HashMap<Item,Double> weightMap;
	private ArrayList<Item> extractedItems;
	private ArrayList<Item> activeItems;
	private ArrayList<Item> disabledItems;
	private ArrayList<Item> itemsWithBindings;
	private double plObj;
	private double bestObj;
	private double kernelObj;
	

	public SolutionAnalyzer(Bindings bindings, Solution sol, double plObj, 
			double bestObj, EnumeratedDistribution<Item> distribution, ArrayList<Item> extractedItems, ArrayList<Item> itemsWithBindings, double kernelObj){
		
		this.bindings=bindings;
		this.sol=sol;
		this.extractedItems=extractedItems;
		activeItems= new ArrayList<Item>(extractedItems.stream().filter(i->(sol.getVarValue(i.getName())>0)).collect(Collectors.toList()));
		disabledItems= new ArrayList<Item>(extractedItems.stream().filter(i->(sol.getVarValue(i.getName())==0)).collect(Collectors.toList()));
		this.plObj=plObj;
		this.bestObj=bestObj;
		this.weightMap=distribution.getHashPmf();
		this.itemsWithBindings=itemsWithBindings;
		this.kernelObj=kernelObj;
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
		System.out.println("bestObj: "+bestObj);
		System.out.println("kernelObj: "+ kernelObj);
		System.out.println("plObj: "+plObj);
		double diff100IfPositive=bestObj-plObj;
		double diff100IfNegative=kernelObj-bestObj;
		double solObj=sol.getObj();
		System.out.println("solObj: "+solObj);
		double diffX=bestObj-solObj;
		//System.out.println("diff100IfPositive: "+diff100IfPositive+"\n"+"diff100IfNegative: "+diff100IfPositive+"\n"+"solObj: "+solObj+"\n"+"diffX: "+diffX+"\n");
		if(diffX>0){
			double perc=diffX/diff100IfPositive*100;
			
			for(Item a: activeItems){
				weightMap.replace(a, weightMap.get(a)+perc);	
			}
			
			for(Item a: disabledItems){
				weightMap.replace(a, Math.max(0.00000000001, weightMap.get(a)-perc/2));	
			}
				
		}
		
		
		if(diffX<=0){
			double perc=-diffX/diff100IfNegative*100;
			System.out.println("Perc :"+perc);
			
			for(Item a: activeItems){
				
				weightMap.replace(a, Math.max(0.00000000001, weightMap.get(a)-perc ));	
			}
			
			for(Item a: disabledItems){
				weightMap.replace(a, Math.max(0.00000000001, weightMap.get(a)-perc*2));	
			}
				
		}
		
		
		return weightMap;
		
	}

	public ArrayList<Item> getActiveItems() {
		return activeItems;
	}
	
	
		
	
	
}

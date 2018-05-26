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
	
	Bindings<PositiveItemBinding> posBindings;
	Bindings<NegativeItemBinding> negBindings;
	Solution sol;
	EnumeratedDistribution<Item> distribution;
	ArrayList<Item> bucketItems;
	ArrayList<Item> activeItems;
	ArrayList<Item> disabledItems;
	double plObj;
	double kernelObj;
	

	public SolutionAnalyzer(Bindings<PositiveItemBinding> posBindings, Bindings<NegativeItemBinding> negBindings, Solution sol, double plObj, 
			double kernelObj, EnumeratedDistribution<Item> distribution, ArrayList<Item> bucketItems){
		
		this.posBindings=posBindings;
		this.negBindings=negBindings;
		this.sol=sol;
		this.distribution=distribution;
		this.bucketItems=bucketItems;
		activeItems= new ArrayList<Item>(bucketItems.stream().filter(i->(sol.getVarValue(i.getName())>0)).collect(Collectors.toList()));
		disabledItems= new ArrayList<Item>(bucketItems.stream().filter(i->(sol.getVarValue(i.getName())==0)).collect(Collectors.toList()));
		this.plObj=plObj;
		this.kernelObj=kernelObj;
	}
	
	void updateBindings(){
		
		if(sol.getObj()<=kernelObj){
			
			
			for(int i=0;i<bucketItems.size();i++ ){
				Item itemA=bucketItems.get(i);
				for(int j=bucketItems.indexOf(itemA)+1;j<bucketItems.size();j++){
					Item itemB=bucketItems.get(j);
					

					
					if(negBindings.searchBinding(itemA,itemB)==null) negBindings.getBindings().put(new Pair<Item, Item>(itemA,itemB), new NegativeItemBinding(itemA,itemB));
					
						
					}
				}
		}
		
		for(int i=0;i<activeItems.size();i++ ){
			Item itemA=activeItems.get(i);
			for(int j=activeItems.indexOf(itemA)+1;j<activeItems.size();j++){
				Item itemB=activeItems.get(j);
				

				
				if(posBindings.searchBinding(itemA,itemB)==null) posBindings.getBindings().put(new Pair<Item, Item>(itemA,itemB), new PositiveItemBinding(itemA,itemB));
				PositiveItemBinding bind= posBindings.searchBinding(itemA,itemB);
				bind.incrementActiveCount();
				bind.incrementExtractedCount();
				bind.incrementValueCount(itemA, sol.getVarValue(itemA.getName()));
				bind.incrementValueCount(itemB, sol.getVarValue(itemB.getName()));
				
					
				}
			}
		
		for(int i=0;i<disabledItems.size();i++ ){
			Item itemA=disabledItems.get(i);
			for(int j=disabledItems.indexOf(itemA)+1;j<disabledItems.size();j++){
				Item itemB=disabledItems.get(j);
	
				PositiveItemBinding bind= posBindings.searchBinding(itemA,itemB);
				if(bind!=null)
				bind.incrementExtractedCount();
				
				}
			}
		
		
	}
	
	
	HashMap<Item,Double> updateWeights(){
		HashMap<Item,Double> map= distribution.getHashPmf();
		double diff100=plObj-kernelObj;
		double solObj=sol.getObj();
		double diffX=solObj-kernelObj;
		if(diff100>0){
			double perc=diffX/diff100*100;
			
			for(Item a: activeItems){
				map.replace(a, map.get(a)+perc);	
			}
			
			for(Item a: disabledItems){
				map.replace(a, Math.max(0, map.get(a)-perc/2));	
			}
				
		}
		
		
//		if(diff100<=0){
//			double perc=-diffX/diff100*100;
//			
//			for(Item a: activeItems){
//				map.replace(a, map.get(a)-perc);	
//			}
//			
//			for(Item a: disabledItems){
//				map.replace(a, Math.max(0, map.get(a)-perc*2 ));	
//			}
//				
//		}
//		
		
		return map;
		
	}
		
	
	
}

package newKernel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import kernel.Item;
import kernel.Solution;

public class KernelAnalyzer {

	HashMap<Item, KernelItemStatistics> itemMap;
	final int TIMES_TO_DROP;

	public KernelAnalyzer(ArrayList<Item> startItemList, int times_to_drop, Solution sol) {
	
		
		TIMES_TO_DROP=times_to_drop;
		itemMap=new HashMap<Item, KernelItemStatistics>();
		update(startItemList, sol);
	}
	
	
	
	ArrayList<Item> analyze(){
		
		ArrayList<Item> itemsToDrop = new ArrayList<Item>();
		Iterator<Entry<Item,KernelItemStatistics>> iter = itemMap.entrySet().iterator();
		
		while(iter.hasNext()){
			Entry<Item,KernelItemStatistics> entry = iter.next();
			if(entry.getValue().getTimesDisabled()>=TIMES_TO_DROP){
				itemsToDrop.add(entry.getKey());
				itemMap.remove(entry.getKey());
			}
		}
		
		return itemsToDrop;
		
		
	}
	
	
	void update(ArrayList<Item> itemsInKernel, Solution sol){
		
		for(Item a: itemsInKernel){
			
			if(!itemMap.containsKey(a)){
				itemMap.put(a, new KernelItemStatistics());
			}
			
			if(sol.getVarValue(a.getName())==0){
					itemMap.get(a).incrementDisabled();
			}
			
				
		}
	}
	
	
	
	
	
	
	
	
}

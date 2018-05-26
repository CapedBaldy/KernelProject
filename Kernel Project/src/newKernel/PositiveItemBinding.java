package newKernel;

import kernel.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class PositiveItemBinding {
	
	short timesActive;
	short timesExtracted;

	Item itemA;
	Item itemB;

	
	HashMap<Double,Short> valuesOfItemA;
	HashMap<Double,Short> valuesOfItemB;
	
	public PositiveItemBinding(Item itemA, Item itemB){
		this.itemA=itemA;
		this.itemB=itemB;

		timesActive=1;
		timesExtracted=1;
		valuesOfItemA= new HashMap<Double,Short>();
		valuesOfItemB= new HashMap<Double,Short>();

	}
	
	
	void incrementActiveCount(){
		timesActive++;
	}
	
	void incrementExtractedCount(){
		timesExtracted++;
	}
	
	double getActivePercentage(){
		return timesActive/timesExtracted;
	}
	
	
	void incrementValueCount(Item item, double value){
		
		if(item==itemA){
			if(valuesOfItemA.get(value)==null) valuesOfItemA.put(value, (short) 0);
			Short count=valuesOfItemA.get(value);
			if(count!=null) count++;
			
		}else {
			if(valuesOfItemB.get(value)==null) valuesOfItemB.put(value, (short) 0);
			Short count=valuesOfItemB.get(value);
			if(count!=null) count++;
			
		}
			
	}
	
	
	boolean activeBindingCheck(int activeThreshold, double percentage){

		if(timesActive>=activeThreshold&&getActivePercentage()>=percentage) return true;
		return false;
		
	}
	
	
	ArrayList<Double> fixedBindingCheck(Item item, int fixedThreshold){
		ArrayList<Double> result = new ArrayList<Double>();
		if(item==itemA){
			Iterator<Double> iter = valuesOfItemA.keySet().iterator();
			while(iter.hasNext()){
				Double temp= iter.next();
				if(valuesOfItemA.get(temp)>=fixedThreshold) result.add(temp);
			}
			
			return result;
			
		}
		else{
			Iterator<Double> iter = valuesOfItemB.keySet().iterator();
			while(iter.hasNext()){
				Double temp= iter.next();
				if(valuesOfItemB.get(temp)>=fixedThreshold) result.add(temp);
			}
			return result;
		}
		
	}

	

}

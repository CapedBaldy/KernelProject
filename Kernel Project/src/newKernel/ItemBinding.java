package newKernel;

import kernel.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ItemBinding {
	
	private short timesPositive;
	private short timesExtracted;
	private short timesNegative;

	private Item itemA;
	private Item itemB;

	
	private HashMap<Double,Short> valuesOfItemA;
	private HashMap<Double,Short> valuesOfItemB;
	
	public ItemBinding(Item itemA, Item itemB){
		this.itemA=itemA;
		this.itemB=itemB;

		timesExtracted=1;
		valuesOfItemA= new HashMap<Double,Short>();
		valuesOfItemB= new HashMap<Double,Short>();

	}
	
	
	void incrementPositiveCount(){
		timesPositive++;
	}
	
	void incrementNegativeCount(){
		timesNegative++;
	}
	
	void incrementExtractedCount(){
		timesExtracted++;
	}
	
	double getPositivePercentage(){
		return timesPositive/timesExtracted;
	}
	
	double getNegativePercentage(){
		return timesNegative/timesExtracted;
	}
	
	
	void incrementValueCount(Item item, double value){
		
		if(item==itemA){
			if(valuesOfItemA.get(value)==null) valuesOfItemA.put(value, (short) 0);
			Short count=valuesOfItemA.get(value);
			count++;
			
		}else {
			if(valuesOfItemB.get(value)==null) valuesOfItemB.put(value, (short) 0);
			Short count=valuesOfItemB.get(value);
			count++;
			
		}
			
	}
	
	
	boolean activeBindingCheck(int extractThreshold, double percentage){

		if(timesPositive>=extractThreshold&&getPositivePercentage()>=percentage) return true;
		return false;
		
	}
	
	boolean negativeBindingCheck(int extractThreshold, double percentage){

		if(timesNegative>=extractThreshold&&getNegativePercentage()>=percentage) return true;
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


	public Item getItemA() {
		return itemA;
	}


	public Item getItemB() {
		return itemB;
	}

	

}

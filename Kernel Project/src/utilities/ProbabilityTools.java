package utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import kernel.Item;


public class ProbabilityTools{

	EnumeratedDistribution<Item> distrib;;
	double total;
	HashMap<Item,Double> indexes;
	List<Item> items;


	public ProbabilityTools(List<Item> items_) {
		
		items=items_;
		total=items.size()/100;
		indexes= new HashMap<Item,Double>();
		for(Item a: items){
			
			indexes.put(a, items.indexOf(a)/total);
		}
		

}



	Double gaussianFunction(double x, double u){
		
		double A=1000.00;
		double C=1/Math.sqrt(2*Math.PI);
		double std=13.00;
		return A*C*(1/std)*Math.exp(-Math.pow((x-u),2)/(2*Math.pow(std,2)));
		/*gaussiana customizzata*/
	}
	
	Double linearFunction(double x){
		
		return -60/100*x+100;
	}


	EnumeratedDistribution<Item> getGaussianDistribution(double u){
		
		double uCorrect=u/total;
		Iterator<Entry<Item, Double>> iter = indexes.entrySet().iterator();
		ArrayList<Pair<Item,Double>> list= new ArrayList<Pair<Item, Double>>();
		
		while(iter.hasNext()){
			Entry<Item,Double> entry=(Entry<Item,Double>) iter.next();
			double value=gaussianFunction(entry.getValue(), u);
			list.add(new Pair<Item, Double>(entry.getKey(), value));
		}
		
		
		return new EnumeratedDistribution<Item>(list);
	}
	
	
	EnumeratedDistribution<Item> getLinearDistribution(){
		
		Iterator<Entry<Item, Double>> iter = indexes.entrySet().iterator();
		ArrayList<Pair<Item,Double>> list= new ArrayList<Pair<Item, Double>>();
		
		while(iter.hasNext()){
			Entry<Item,Double> entry=(Entry<Item,Double>) iter.next();
			double value=linearFunction(entry.getValue());
			list.add(new Pair<Item, Double>(entry.getKey(), value));
		}
		
		return new EnumeratedDistribution<Item>(list);
	}
	
	ArrayList<Item> sampleWithoutReplacement(int sampleSize, EnumeratedDistribution<Item> distrib) {

		ArrayList<Item> result = new ArrayList<Item>();

		for (int i = 0; i < sampleSize; i++) {

			Item sample = distrib.sample();

			while (!result.contains(sample)) {
				sample = distrib.sample();
			}

			result.add(sample);

		}

		return result;

	}

}

package utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import kernel.Item;


public class ProbabilityTools<T>{

	double total;
	HashMap<T,Double> indexes;
	List<T> items;


	public ProbabilityTools(List<T> items_) {
		
		items=items_;
		total=items.size()/100;
		indexes= new HashMap<T,Double>();
		for(T a: items){
			
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


	EnumeratedDistribution<T> getGaussianDistribution(double u){
		
		return getGaussianDistribution(u, indexes);
	}
	
	EnumeratedDistribution<T> getGaussianDistribution(double u, HashMap<T,Double> map){
		
		double uCorrect=u/total;
		Iterator<Entry<T, Double>> iter = map.entrySet().iterator();
		ArrayList<Pair<T,Double>> list= new ArrayList<Pair<T, Double>>();
		
		while(iter.hasNext()){
			Entry<T,Double> entry=(Entry<T,Double>) iter.next();
			double value=gaussianFunction(entry.getValue(), uCorrect);
			list.add(new Pair<T, Double>(entry.getKey(), value));
		}
		
		
		return new EnumeratedDistribution<T>(list);
	}
	
	EnumeratedDistribution<T> getLinearDistribution(){
		
	return getLinearDistribution(indexes);
	}
	
	
EnumeratedDistribution<T> getLinearDistribution(HashMap<T,Double> map){
		
		Iterator<Entry<T, Double>> iter = map.entrySet().iterator();
		ArrayList<Pair<T,Double>> list= new ArrayList<Pair<T, Double>>();
		
		while(iter.hasNext()){
			Entry<T,Double> entry=(Entry<T,Double>) iter.next();
			double value=linearFunction(entry.getValue());
			list.add(new Pair<T, Double>(entry.getKey(), value));
		}
		
		return new EnumeratedDistribution<T>(list);
	}


	
	ArrayList<T> sampleWithoutReplacement(int sampleSize, EnumeratedDistribution<T> distrib) {
		
		

		HashSet<T> result = new HashSet<T>();
		HashMap<T,Double> map = distrib.getHashPmf();
		if (sampleSize > map.size()) return null;
		int updatedSampleSize=sampleSize;

		while(result.size()!=sampleSize){
			
			for (int i = 0; i < updatedSampleSize; i++) {
				result.add(distrib.sample());
			}
			
			updatedSampleSize=sampleSize-result.size();
			Iterator<T> iter=result.iterator();
			while(iter.hasNext()){
				map.remove(iter.next());
			}
			
			distrib= new EnumeratedDistribution<T>(map);
			
			
			
		}

		return new ArrayList<T>(result);

	}
	
	

}

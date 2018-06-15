package utilities;


import java.util.HashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.EnumeratedDistribution;





public class PopulationTools<T>{

	double total;
	HashMap<T,Double> indexes;
	List<T> items;


	public PopulationTools(List<T> items_) {
		
		items=items_;
		total=((double)items.size()-1)/100;
		indexes= new HashMap<T,Double>();
		for(T a: items){
			
			indexes.put(a, items.indexOf(a)/total);
		}
		

}



	private Double gaussianFunction(double x, double u){
		
		double A=1000.00;
		double C=1/Math.sqrt(2*Math.PI);
		double std=13.00;
		return A*C*(1/std)*Math.exp(-Math.pow((x-u),2)/(2*Math.pow(std,2)));
		/*gaussiana customizzata*/
	}
	
	private Double linearFunction(double x){
		
		return ((double)-10)/((double)100)*x+50; 
		//funzione lineare customizzata
	}


	public EnumeratedDistribution<T> getGaussianDistribution(double u){
		
		return getGaussianDistribution(u, indexes);
	}
	

	public EnumeratedDistribution<T> getGaussianDistribution(double u, HashMap<T,Double> map){
		
		return new EnumeratedDistribution<T>(getGaussian(u, map));
	}
	
	
	
	public HashMap<T,Double> getGaussian(double u, HashMap<T,Double> map){
		
		//double uCorrect=u/total;  //normalizza la media
		Iterator<Entry<T, Double>> iter = map.entrySet().iterator();
		HashMap<T,Double> result= new HashMap<T,Double>();
		
		while(iter.hasNext()){
			Entry<T,Double> entry=(Entry<T,Double>) iter.next();
			double value=gaussianFunction(entry.getValue(), u );
			result.put(entry.getKey(), value);
		}
		return result;
	}
	
	
	
	public EnumeratedDistribution<T> getLinearDistribution(){  //ritorna distribuzione lineare usando gli indici della popolazione data nel costruttore
		
	return getLinearDistribution(indexes);
	
	}
	
	
	
	
	public EnumeratedDistribution<T> getLinearDistribution(HashMap<T,Double> map){ //ritorna distribuzione lineare usando i valori di una map arbitraria
				
			return new EnumeratedDistribution<T>(getLinear(map));
		}
	
	
	

	public HashMap<T,Double> getLinear(){
		return getLinear(indexes);
	}
	
	
	
	
	
	public HashMap<T,Double> getLinear(HashMap<T,Double> map){   //calcola y=f(x), dove x è il valore assegnato ogni chiave nell'HashMap
		
		Iterator<Entry<T, Double>> iter = map.entrySet().iterator();
		HashMap<T,Double> result= new HashMap<T,Double>();
		
		while(iter.hasNext()){
			Entry<T,Double> entry=(Entry<T,Double>) iter.next();
			double value=linearFunction(entry.getValue());
			result.put(entry.getKey(), value);
		}
		return result;
	}

	
	
	
	
	
	

}

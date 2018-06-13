package utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import newKernel.Sample;

public class SamplingTools {
	
public static <T> ArrayList<T> sampleWithoutReplacement(int sampleSize, EnumeratedDistribution<T> distrib) {
		
		

		HashSet<T> result = new HashSet<T>();
		HashMap<T,Double> map = distrib.getHashPmf();
		if (sampleSize > map.size()) return null;
		int updatedTSize=sampleSize;

		while(result.size()!=sampleSize){
			
			for (int i = 0; i < updatedTSize; i++) {
				result.add(distrib.sample());
			}
			
			updatedTSize=sampleSize-result.size();
			Iterator<T> iter=result.iterator();
			while(iter.hasNext()){
				map.remove(iter.next());
			}
			
			if(result.size()<sampleSize) distrib= new EnumeratedDistribution<T>(map);
			
			
			
		}

		return new ArrayList<T>(result);

	}



public static <T> ArrayList<T> sampleWithTaboo(int sampleSize, EnumeratedDistribution<T> distrib){
	
	List<Pair<Sample,Double>> list = (List<Pair<Sample,Double>>) (Object) distrib.getPmf();
	
	List<Pair<Sample,Double>> notTabooSamples= list.stream().filter(it->(it.getKey().getTabooCount()==0&&!it.getKey().isTaboo())).collect(Collectors.toList());
	list.stream().filter(it->(it.getKey().getTabooCount()>0)).forEach(it->it.getKey().setTabooCount((short)(it.getKey().getTabooCount()-1))); //abbasso di 1 il conteggio taboo
	
	return sampleWithoutReplacement(sampleSize, new EnumeratedDistribution<T>((List<Pair<T,Double>>)(Object)notTabooSamples));
	
}

}

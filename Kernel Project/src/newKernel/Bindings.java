package newKernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import kernel.Item;

public class Bindings {
	
	private HashMap<Pair<Item,Item>,ItemBinding> bindings;
	
	public Bindings(){
		bindings= new HashMap<Pair<Item,Item>,ItemBinding>();
	}

	public HashMap<Pair<Item, Item>, ItemBinding> getBindings() {
		return bindings;
	}

	
	
	ItemBinding searchBinding(Item itemA, Item itemB){
		
		ItemBinding bind1= bindings.get(new Pair<Item,Item>(itemA,itemB));
		ItemBinding bind2= bindings.get(new Pair<Item,Item>(itemB,itemA));
		
		if(bind1==null&&bind2==null) return null;
		
		ItemBinding bind;
		if(bind1!=null) bind= bind1;
		else bind= bind2;
		
		return bind;
		
		
	}
	
	ArrayList<ItemBinding> searchInternalBinding(ArrayList<Item> samples, ArrayList<Item> itemsWithBindings){
		
		
		ArrayList<ItemBinding> result= new ArrayList<ItemBinding>();
		ArrayList<Item> samplesWithBindings =new ArrayList<>(samples.stream().filter(it->itemsWithBindings.contains(it)).collect(Collectors.toList()));
		
		for(int i=0;i<samplesWithBindings.size();i++ ){
			Item itemA=samplesWithBindings.get(i);
			for(int j=samplesWithBindings.indexOf(itemA)+1;j<samplesWithBindings.size();j++){
				Item itemB=samplesWithBindings.get(j);
				
				ItemBinding bind= searchBinding(itemA,itemB);
				if(bind!=null) result.add(bind);
				
				
				
			}
		}
		
		
		
		
		return result;
		
		
		
	}

	ArrayList<ItemBinding> searchExternalBinding(ArrayList<Item> samples, ArrayList<Item> notSamples, ArrayList<Item> itemsWithBindings){
		
		ArrayList<ItemBinding> result = new ArrayList<ItemBinding>();
		
		Iterator<Pair<Item, Item>> iter =bindings.keySet().iterator();
		while (iter.hasNext()){
			Pair<Item, Item> pair = iter.next();
			if( (samples.contains(pair.getFirst()) && notSamples.contains(pair.getSecond())) || (samples.contains(pair.getSecond()) && notSamples.contains(pair.getFirst()))  ){
				result.add(bindings.get(pair));
			}
			
		}
		
		return result;

	}
	
	
	

}

package newKernel;

import java.util.HashMap;

import org.apache.commons.math3.util.Pair;

import kernel.Item;

public class Bindings<T> {
	
	private HashMap<Pair<Item,Item>,T> bindings;
	
	public Bindings(){
		bindings= new HashMap<Pair<Item,Item>,T>();
	}

	public HashMap<Pair<Item, Item>, T> getBindings() {
		return bindings;
	}

	
	
	T searchBinding(Item itemA, Item itemB){
		
		T bind1= bindings.get(new Pair<Item,Item>(itemA,itemB));
		T bind2= bindings.get(new Pair<Item,Item>(itemB,itemA));
		
		if(bind1==null&&bind2==null) return null;
		
		T bind;
		if(bind1!=null) bind= bind1;
		else bind= bind2;
		
		return bind;
		
		
	}

	
	
	

}

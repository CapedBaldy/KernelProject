package newKernel;

import java.util.Comparator;
import java.util.List;

import kernel.Item;
import kernel.ItemSorter;

public class ItemSorterByPoolValueAndAbsolutePoolRC implements ItemSorter{
	
	public void sort(List<Item> items)
	{
		items.sort(Comparator.comparing(Item::getPoolXr).reversed()
		          .thenComparing(Item::getPoolRc));
	}

}

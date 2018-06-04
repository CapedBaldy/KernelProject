package kernel;
import java.util.ArrayList;
import java.util.List;

import newKernel.Sample;

public class Bucket implements Sample
{
	private List<Item> items;
	private short tabooCount;
	private boolean taboo;

	
	public Bucket()
	{
		items = new ArrayList<>();
		tabooCount=0;
		taboo=false;

	}
	
	public void addItem(Item it)
	{
		items.add(it);
	}
	
	public int size()
	{
		return items.size();
	}
	
	public List<Item> getItems()
	{
		return items;
	}
	
	public boolean contains(Item it)
	{
		return items.stream().anyMatch(it2 -> it2.getName().equals(it.getName()));
	}
	
	public void removeItem(Item it)
	{
		for(int i = 0; i< items.size(); i++)
		{
			Item it2 = items.get(i);
			if(it2.getName().equals(it.getName()))
			{
				items.remove(it2);
				break;
			}
		}
	}

	public short getTabooCount() {
		return tabooCount;
	}

	public void setTabooCount(short tabooCount) {
		this.tabooCount = tabooCount;
	}

	public boolean isTaboo() {
		return taboo;
	}

	public void setTaboo(boolean taboo) {
		this.taboo = taboo;
	}


	
	
}
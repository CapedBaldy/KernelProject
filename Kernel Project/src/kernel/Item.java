package kernel;

import newKernel.Sample;

public class Item implements Sample
{
	private String name;
	private double rc;
	private double xr;
	private double poolXr;
	private double poolRc;
	private short tabooCount;
	private boolean taboo;
	
	public Item(String name, double xr, double rc)
	{
		this.name = name;
		this.xr = xr;
		this.rc = rc;
		tabooCount=0;
		taboo=false;
		poolXr=0;
		poolRc=0;
	}
	
	public String getName()
	{
		return name;
	}
	
	public double getRc()
	{
		return rc;
	}
	
	public double getXr()
	{
		return xr;
	}
	
	public double getAbsoluteRC()
	{
		return Math.abs(rc);
	}

	public short getTabooCount() {
		return tabooCount;
	}

	public void setTabooCount(short tabooCount) {
		this.tabooCount = tabooCount;
	}

	public double getPoolXr() {
		return poolXr;
	}

	public void setPoolXr(double poolXr) {
		this.poolXr = poolXr;
	}

	public double getPoolRc() {
		return poolRc;
	}

	public void setPoolRc(double poolRc) {
		this.poolRc = poolRc;
	}

	public boolean isTaboo() {
		return taboo;
	}

	public void setTaboo(boolean taboo) {
		this.taboo = taboo;
	}
	
	
	
	
	
}
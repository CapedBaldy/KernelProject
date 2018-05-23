package newKernel;

public class ItemStatistics {

	
	short timesDisabled;
	

	
	public ItemStatistics() {

		
		timesDisabled=0;
		
	}


	public int getTimesDisabled() {
		return timesDisabled;
	}


	public void incrementDisabled(){
		++timesDisabled;
	}

	public void setTimesDisabled(short timesDisabled) {
		this.timesDisabled = timesDisabled;
	}



	
}

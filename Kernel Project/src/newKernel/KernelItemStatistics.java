package newKernel;

public class KernelItemStatistics {

	
	short timesDisabled;
	

	
	public KernelItemStatistics() {

		
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

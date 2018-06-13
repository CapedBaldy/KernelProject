package newKernel;
import java.util.ArrayList;

import kernel.Bucket;
import kernel.Item;
import kernel.Solution;

public class Candidate {
	
	private ArrayList<Bucket> currentVersion;
	private ArrayList<Bucket> oldVersion;
	private int startPosition;
	private int arrayLength;
	private int nextAdiacentBucket;
	private int addedBucketsCount;
	private Bucket originalBucket;
	private Solution sol;
	private boolean completed;
	
	public Candidate(Bucket bucket, int position, int arrayLenght, Solution sol){
		currentVersion=new ArrayList<Bucket>();
		currentVersion.add(bucket);
		startPosition=position;
		nextAdiacentBucket=1;
		addedBucketsCount=1; //indice che tiene conto di quale posizione è la prossima da estrarre
		originalBucket=bucket;
		this.sol=sol;
		
	}


	public void addBucket(Bucket bucket){
		oldVersion=currentVersion;
		currentVersion.add(bucket);
		addedBucketsCount++;
		
	}
	
	public int getNextAdiacentPosition(){
		nextAdiacentBucket++;
		

		if(nextAdiacentBucket % 2 == 0){
			if(startPosition==arrayLength-1) return this.getNextAdiacentPosition();
			return startPosition+nextAdiacentBucket/2;
			
		}
		else {
			if(startPosition==0) return this.getNextAdiacentPosition();
			return startPosition-(int)Math.floor(nextAdiacentBucket/2);
		}
	}

	
	public void undoAddBucket(){
		currentVersion=oldVersion;
	}

	public ArrayList<Bucket> getCurrentVersion() {
		return currentVersion;
	}


	public ArrayList<Bucket> getOldVersion() {
		return oldVersion;
	}


	public int getAddedBucketsCount() {
		return addedBucketsCount;
	}


	public Bucket getOriginalBucket() {
		return originalBucket;
	}


	public Solution getSol() {
		return sol;
	}


	public void setSol(Solution sol) {
		this.sol = sol;
	}
	
	
	public ArrayList<Item> mergeBuckets(){
		
		ArrayList<Item> temp= new ArrayList<Item>();
		
		for(Bucket b: currentVersion){
			temp.addAll(b.getItems());
		}
		
		return temp;
	}


	public boolean isCompleted() {
		return completed;
	}


	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	
	
	
	
	
	

}

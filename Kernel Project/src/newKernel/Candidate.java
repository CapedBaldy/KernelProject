package newKernel;
import java.util.ArrayList;

import kernel.Bucket;

public class Candidate {
	
	ArrayList<Bucket> currentVersion;
	ArrayList<Bucket> oldVersion;
	int startPosition;
	int arrayLength;
	int nextAdiacentBucket;
	int addedBucketsCount;
	Bucket originalBucket;
	
	public Candidate(Bucket bucket, int position, int arrayLenght){
		currentVersion=new ArrayList<Bucket>();
		currentVersion.add(bucket);
		startPosition=position;
		nextAdiacentBucket=1;
		addedBucketsCount=1;
		originalBucket=bucket;
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
	
	
	
	
	
	
	

}

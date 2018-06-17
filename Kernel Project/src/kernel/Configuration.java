package kernel;
public class Configuration
{
	private int numThreads;
	private double mipGap;
	private int presolve;
	private int timeLimit;
	private ItemSorter sorter;
	private double kernelSize;
	private double bucketSize;
	private BucketBuilder bucketBuilder;
	private KernelBuilder kernelBuilder;
	private int timeLimitKernel;
	private int numIterations;
	private int timeLimitBucket;
	private int wastedIterLimit;
	private double extractionPerc;
	private int poolTimeLimit;
	private int extractThreshold;
	private int negativeThreshold;
	private int positiveThreshold;
	private int fixedThreshold;
	private double percThreshold;
	private int poolTabooCounter;
	private int fixedMultipleThreshold;
	private int candidateThreshold;
	private int mainTabooCounter;
	private int maxBucketsInCandidate;
	private short poorBucketLimit;
	private int outputFlag;
	private double poolTabooPerc;

	public BucketBuilder getBucketBuilder()
	{
		return bucketBuilder;
	}

	public double getBucketSize()
	{
		return bucketSize;
	}

	public ItemSorter getItemSorter()
	{
		return sorter;
	}

	public KernelBuilder getKernelBuilder()
	{
		return kernelBuilder;
	}

	public double getKernelSize()
	{
		return kernelSize;
	}

	public double getMipGap()
	{
		return mipGap;
	}

	public int getNumIterations()
	{
		return numIterations;
	}

	public int getNumThreads()
	{
		return numThreads;
	}

	public int getPresolve()
	{
		return presolve;
	}

	public int getTimeLimit()
	{
		return timeLimit;
	}

	public int getTimeLimitBucket()
	{
		return timeLimitBucket;
	}

	public int getTimeLimitKernel()
	{
		return timeLimitKernel;
	}

	public void setBucketBuilder(BucketBuilder bucketBuilder)
	{
		this.bucketBuilder = bucketBuilder;
	}

	public void setBucketSize(double bucketSize)
	{
		this.bucketSize = bucketSize;
	}

	public void setItemSorter(ItemSorter sorter)
	{
		this.sorter = sorter;
	}

	public void setKernelBuilder(KernelBuilder kernelBuilder)
	{
		this.kernelBuilder = kernelBuilder;
	}

	public void setKernelSize(double kernelSize)
	{
		this.kernelSize = kernelSize;
	}

	public void setMipGap(double mipGap)
	{
		this.mipGap = mipGap;
	}

	public void setNumIterations(int numIterations)
	{
		this.numIterations = numIterations;
	}

	public void setNumThreads(int numThreads)
	{
		this.numThreads = numThreads;
	}

	public void setPresolve(int presolve)
	{
		this.presolve = presolve;
	}

	public void setTimeLimit(int timeLimit)
	{
		this.timeLimit = timeLimit;
	}

	public void setTimeLimitBucket(int timeLimitBucket)
	{
		this.timeLimitBucket = timeLimitBucket;
	}

	public void setTimeLimitKernel(int timeLimitKernel)
	{
		this.timeLimitKernel = timeLimitKernel;
	}

	public int getWastedIterLimit() {
		return wastedIterLimit;
	}

	public double getExtractionPerc() {
		return extractionPerc;
	}

	public int getPoolTimeLimit() {
		return poolTimeLimit;
	}



	public int getFixedThreshold() {
		return fixedThreshold;
	}

	public void setWastedIterLimit(int wastedIterLimit) {
		this.wastedIterLimit = wastedIterLimit;
	}

	public void setExtractionPerc(double extractionPerc) {
		this.extractionPerc = extractionPerc;
	}

	public void setPoolTimeLimit(int poolTimeLimit) {
		this.poolTimeLimit = poolTimeLimit;
	}



	public void setFixedThreshold(int fixedThreshold) {
		this.fixedThreshold = fixedThreshold;
	}

	public double getPercThreshold() {
		return percThreshold;
	}

	public void setPercThreshold(double percThreshold) {
		this.percThreshold = percThreshold;
	}

	public int getPoolTabooCounter() {
		return poolTabooCounter;
	}

	public void setPoolTabooCounter(int poolTabooCounter) {
		this.poolTabooCounter = poolTabooCounter;
	}

	public int getExtractThreshold() {
		return extractThreshold;
	}

	public void setExtractThreshold(int extractThreshold) {
		this.extractThreshold = extractThreshold;
	}

	public int getNegativeThreshold() {
		return negativeThreshold;
	}

	public int getPositiveThreshold() {
		return positiveThreshold;
	}

	public void setNegativeThreshold(int negativeThreshold) {
		this.negativeThreshold = negativeThreshold;
	}

	public void setPositiveThreshold(int positiveThreshold) {
		this.positiveThreshold = positiveThreshold;
	}

	public int getFixedMultipleThreshold() {
		return fixedMultipleThreshold;
	}

	public void setFixedMultipleThreshold(int fixedMultipleThreshold) {
		this.fixedMultipleThreshold = fixedMultipleThreshold;
	}

	public int getCandidateThreshold() {
		return candidateThreshold;
	}

	public void setCandidateThreshold(int candidateThreshold) {
		this.candidateThreshold = candidateThreshold;
	}

	public int getMainTabooCounter() {
		return mainTabooCounter;
	}

	public void setMainTabooCounter(int mainTabooCounter) {
		this.mainTabooCounter = mainTabooCounter;
	}

	public int getMaxBucketsInCandidate() {
		return maxBucketsInCandidate;
	}

	public void setMaxBucketsInCandidate(int maxBucketsInCandidate) {
		this.maxBucketsInCandidate = maxBucketsInCandidate;
	}

	public short getPoorBucketLimit() {
		return poorBucketLimit;
	}

	public void setPoorBucketLimit(short poorBucketLimit) {
		this.poorBucketLimit = poorBucketLimit;
	}

	public int getOutputFlag() {
		return outputFlag;
	}

	public void setOutputFlag(int outputFlag) {
		this.outputFlag = outputFlag;
	}

	public double getPoolTabooPerc() {
		return poolTabooPerc;
	}

	public void setPoolTabooPerc(double poolTabooPerc) {
		this.poolTabooPerc = poolTabooPerc;
	}
	
	
	
}
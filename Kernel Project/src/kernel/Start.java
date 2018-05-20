package kernel;
public class Start
{
	public static void main(String[] args)
	{
		String pathmps = args[0];
		String pathlog = args[1];
		String pathConfig = args[2];
		Configuration config = ConfigurationReader.read(pathConfig);		
		KernelSearch ks = new KernelSearch(pathmps, pathlog, config);
		ks.start();
	}
}
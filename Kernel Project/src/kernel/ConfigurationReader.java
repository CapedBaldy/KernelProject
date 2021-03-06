package kernel;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationReader
{
	public static Configuration read(String path)
	{
		Configuration config = new Configuration();
		
		List<String> lines = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path)))
        {
        	lines = br.lines().collect(Collectors.toList());
        } catch (IOException e)
		{
			e.printStackTrace();
		}
        
        for(String line : lines)
        {
        	String[] splitLine = line.split("\\s+");
        	
        	switch(splitLine[0])
			{
        		case "THREADS":
        			config.setNumThreads(Integer.parseInt(splitLine[1]));
        			break;
        		case "MIPGAP": 
        			config.setMipGap(Double.parseDouble(splitLine[1]));
        			break;
        		case "PRESOLVE":
        			config.setPresolve(Integer.parseInt(splitLine[1]));
        			break;
        		case "TIMELIMIT":
        			config.setTimeLimit(Integer.parseInt(splitLine[1]));
        			break;
        		case "CANDIDATETHRESHOLD":
        			config.setCandidateThreshold(Integer.parseInt(splitLine[1]));
        			break;
        		case "MAXBUCKETSINCANDIDATE":
        			config.setMaxBucketsInCandidate(Integer.parseInt(splitLine[1]));
        			break;
        		case "POORBUCKETLIMIT":
        			config.setPoorBucketLimit(Short.parseShort(splitLine[1]));
        			break;
        		case "MAINTABOOCOUNTER":
        			config.setMainTabooCounter(Integer.parseInt(splitLine[1]));
        			break;
        		case "POOLTIMELIMIT":
        			config.setPoolTimeLimit(Integer.parseInt(splitLine[1]));
        			break;
        		case "WASTEDITERLIMIT":
        			config.setWastedIterLimit(Integer.parseInt(splitLine[1]));
        			break;
        		case "EXTRACTTHRESHOLD":
        			config.setExtractThreshold(Integer.parseInt(splitLine[1]));
        			break;
        		case "POOLTABOOCOUNTER":
        			config.setPoolTabooCounter(Integer.parseInt(splitLine[1]));
        			break;
        		case "FIXEDTHRESHOLD":
        			config.setFixedThreshold(Integer.parseInt(splitLine[1]));
        			break;
        		case "POSITIVETHRESHOLD":
        			config.setPositiveThreshold(Integer.parseInt(splitLine[1]));
        			break;
        		case "NEGATIVETHRESHOLD":
        			config.setNegativeThreshold(Integer.parseInt(splitLine[1]));
        			break;
        		case "EXTRACTIONPERC":
        			config.setExtractionPerc(Double.parseDouble(splitLine[1]));
        			break;
        		case "POOLTABOOPERC":
        			config.setPoolTabooPerc(Double.parseDouble(splitLine[1]));
        			break;
        		case "PERCTHRESHOLD":
        			config.setPercThreshold(Double.parseDouble(splitLine[1]));
        			break;
        		case "FIXEDMULTIPLETHRESHOLD":
        			config.setFixedMultipleThreshold(Integer.parseInt(splitLine[1]));
        			break;
        			
        		case "SORTER":
        			switch(Integer.parseInt(splitLine[1]))
        			{
        				case 0:
        					config.setItemSorter(new ItemSorterByValueAndAbsoluteRC());
        					break;
        				default:
        					System.out.println("Unrecognized item sorter.");
        			}
        			break;
        		case "KERNELSIZE":
        			config.setKernelSize(Double.parseDouble(splitLine[1]));
        			break;
        		case "BUCKETSIZE":
        			config.setBucketSize(Double.parseDouble(splitLine[1]));
        			break;
        		case "BUCKETBUILDER":
        			switch(Integer.parseInt(splitLine[1]))
        			{
        				case 0:
        					config.setBucketBuilder(new DefaultBucketBuilder());
        					break;
        				default:
        					System.out.println("Unrecognized bucket builder.");
        			}
        			break;
        		case "TIMELIMITKERNEL":
        			config.setTimeLimitKernel(Integer.parseInt(splitLine[1]));
        			break;
        		case "NUMITERATIONS":
        			config.setNumIterations(Integer.parseInt(splitLine[1]));
        			break;
        		case "TIMELIMITBUCKET":
        			config.setTimeLimitBucket(Integer.parseInt(splitLine[1]));
        			break;
        		case "OUTPUTFLAG":
        			config.setOutputFlag(Integer.parseInt(splitLine[1]));
        			break;
        		case "TOTALLP":
        			config.setTotalLP((Integer.parseInt(splitLine[1])));
        			break;
        		case "KERNELBUILDER":
        			switch(Integer.parseInt(splitLine[1]))
        			{
        				case 0:
        					config.setKernelBuilder(new KernelBuilderPositive());
        					break;
        				case 1:
        					config.setKernelBuilder(new KernelBuilderPercentage());
        					break;
        				default:
        					System.out.println("Unrecognized kernel builder.");
        			}
        			break;
        		default:
        			System.out.println("Unrecognized parameter name.");			
			}
        }
        return config;
	}
}
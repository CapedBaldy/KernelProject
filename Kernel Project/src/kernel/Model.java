package kernel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBCallback;

public class Model
{
	private String mpsFilePath;
	private String logPath;
	private int timeLimit;
	private Configuration config;
	private boolean lpRelaxation;
	private GRBEnv env;
	private GRBModel model;
	private boolean hasSolution;
	
	public Model(String mpsFilePath, String logPath, int timeLimit, Configuration config, boolean lpRelaxation)
	{
		this.mpsFilePath = mpsFilePath;
		this.logPath = logPath;
		this.timeLimit = timeLimit;	
		this.config = config;
		this.lpRelaxation = lpRelaxation;
		this.hasSolution = false;
	}
	
	public void buildModel()
	{
		try
		{
			env = new GRBEnv();
			setParameters();
			model = new GRBModel(env, mpsFilePath);
			if(lpRelaxation)
				model = model.relax();
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	private void setParameters() throws GRBException
	{
		env.set(GRB.StringParam.LogFile, logPath+"log.txt");
		env.set(GRB.IntParam.Threads, config.getNumThreads());
		env.set(GRB.IntParam.Presolve, config.getPresolve());
		env.set(GRB.DoubleParam.MIPGap, config.getMipGap());
		if (timeLimit > 0)
			env.set(GRB.DoubleParam.TimeLimit, timeLimit);
	}
	
	public void solve()
	{
		try
		{
			model.optimize();
			if(model.get(IntAttr.SolCount) > 0)
				hasSolution = true;
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public List<String> getVarNames()
	{
		List<String> varNames = new ArrayList<>();
		
		for(GRBVar v : model.getVars())
		{
			try
			{
				varNames.add(v.get(StringAttr.VarName));
			} catch (GRBException e)
			{
				e.printStackTrace();
			}
		}
		return varNames;
	}

	public double getVarValue(String v)
	{
		try
		{
			if(model.get(IntAttr.SolCount) > 0)
			{
				return Math.round(model.getVarByName(v).get(DoubleAttr.X)*(1/config.getMipGap())/(1/(double)config.getMipGap()));
			}
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public double getVarRC(String v)
	{
		try
		{
			if(model.get(IntAttr.SolCount) > 0)
			{
				return Math.round(model.getVarByName(v).get(DoubleAttr.RC)*(1/config.getMipGap())/(1/(double)config.getMipGap()));
			}
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public void disableItems(List<Item> items)
	{
		try
		{
			for(Item it : items)
			{
				model.addConstr(model.getVarByName(it.getName()), GRB.EQUAL, 0, "FIX_VAR_"+it.getName());
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void exportSolution()
	{
		try
		{
			model.write("bestSolution.sol");
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public void readSolution(String path)
	{
		try
		{
			model.read(path);
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public void readSolution(Solution solution)
	{
		try
		{
			for(GRBVar var : model.getVars())
			{
				var.set(DoubleAttr.Start, solution.getVarValue(var.get(StringAttr.VarName)));
			}
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}

	public boolean hasSolution()
	{
		return hasSolution;
	}
	
	public Solution getSolution()
	{
		Solution sol = new Solution();
		
		try
		{
			sol.setObj(model.get(DoubleAttr.ObjVal));
			Map<String, Double> vars = new HashMap<>();
			for(GRBVar var : model.getVars())
			{
				vars.put(var.get(StringAttr.VarName), var.get(DoubleAttr.X));
			}
			sol.setVars(vars);
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
		return sol;
	}
	
	public void addBucketConstraint(List<Item> items)
	{
		GRBLinExpr expr = new GRBLinExpr();
			
		try
		{
			for(Item it : items)
			{
				expr.addTerm(1, model.getVarByName(it.getName()));
			}
			model.addConstr(expr, GRB.GREATER_EQUAL, 1, "bucketConstraint");
		} catch (GRBException e)
		{
			e.printStackTrace();
		}	
	}

	public void addObjConstraint(double obj)
	{
		try
		{
			model.getEnv().set(GRB.DoubleParam.Cutoff, obj);
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public List<Item> getSelectedItems(List<Item> items)
	{
		List<Item> selected = new ArrayList<>();
		for(Item it : items)
		{
			try
			{
				if(Math.round(model.getVarByName(it.getName()).get(DoubleAttr.X)*(1/config.getMipGap()))/(1/config.getMipGap())> 0)
					selected.add(it);
			} catch (GRBException e)
			{
				e.printStackTrace();
			}
		}
		return selected;
	}
	
	public void setCallback(GRBCallback callback)
	{
		model.setCallback(callback);
	}
	
	public void setContinuous(List<Item> list){
		try
		{
			for(Item it : list)
			{
				model.getVarByName(it.getName()).set(GRB.CharAttr.VType, GRB.CONTINUOUS);
				
			}
			model.update();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void setFixedValue(Item item, Double value){
		
		try{
			model.addConstr(model.getVarByName(item.getName()), GRB.EQUAL, value, "FIX_VAR_"+item.getName());
			model.update();
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
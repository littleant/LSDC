package at.ac.tuwien.lsdc.mape;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.actions.Action;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.resources.Resource;

public class SchedulingAgent {
	private static volatile SchedulingAgent instance;
	private Analyser analyser;
	private Planner planner;
	private Executor executor;
	
	List<Action> rmActionList = new LinkedList<Action>();
	
	private SchedulingAgent() {
		analyser = new Analyser();
		//planner = new KISSPlanner();
		planner = new WekaPlanner();
		executor = new Executor();
	}
	
	public static SchedulingAgent getInstance() {
		if (SchedulingAgent.instance == null) {
			synchronized (SchedulingAgent.class) {
				if (SchedulingAgent.instance == null) {
					SchedulingAgent.instance = new SchedulingAgent();
				}
			}
		}
		
		return SchedulingAgent.instance;
	}
	
	public void start() throws NumberFormatException, IOException {
		int i=0;
		while (i<50000) { 
			
			
			//System.out.println ("SchedulingAgent - Tick " + Monitor.getInstance().getGlobalTicks());
			planner.evaluatePastActions();
			Resource problem = analyser.getTopProblem();
			if(problem!=null) {
				System.out.println ("Problem " + problem.getProblemType());
				Action solution = planner.selectAction(problem);
				if(solution!=null){
					System.out.println ("Selected Action " + solution.getClass().getName()); 
					executor.execute(solution);
					System.out.println ("Executed Action " + solution.getClass().getName());
					Monitor.getInstance().logExecution(solution.getProblemResource(), solution, solution.getLocalEvaluation(), Monitor.getInstance().getGlobalTicks());
					//save the action for knowledge aquisition purposes
					planner.getExecutedActions().add(solution);
				}
				else {
					System.out.println("No Solution found");
				}
			}
			Monitor.getInstance().logSystemStatus();
			Monitor.getInstance().nextTick();
			i++;
		}
		
		planner.terminate();
		
	}
}

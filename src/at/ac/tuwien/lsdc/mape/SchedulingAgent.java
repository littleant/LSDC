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
	List<Action> executedActions = new LinkedList<Action>();
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
		while (i<5000) { 
			rmActionList = new LinkedList<Action>();
			//Knowledge aquisition
			for (Action a: executedActions) {
				if(a!=null) {
					boolean evaluated = a.evaluate();
					if(evaluated) {
						rmActionList.add(a); // the action has been evaluated and can be removed
					}
				}
			}
			
			for (Action a: rmActionList) {
				executedActions.remove(a); //remove actions that have been evaluated
			}
			
			//TODO: gst: werden immer Requests generiert??
			//System.out.println ("SchedulingAgent - Tick " + Monitor.getInstance().getGlobalTicks());
			Resource problem = analyser.getTopProblem();
			Action solution = planner.selectAction(problem);
			executor.execute(solution);
			//save the action for knowledge aquisition purposes
			executedActions.add(solution);
			Monitor.getInstance().logSystemStatus();
			Monitor.getInstance().nextTick();
			i++;
		}
		
		planner.terminate();
		
	}
}

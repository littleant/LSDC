package at.ac.tuwien.lsdc.mape;

import java.io.IOException;
import java.util.GregorianCalendar;
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
		
		GregorianCalendar gc = new GregorianCalendar();
		GregorianCalendar gc2 = new GregorianCalendar();
		GregorianCalendar gc3 = new GregorianCalendar();
		GregorianCalendar gc4 = new GregorianCalendar();
		GregorianCalendar gc5 = new GregorianCalendar();
		GregorianCalendar gc6 = new GregorianCalendar();
		GregorianCalendar gc1000 = new GregorianCalendar();
		
		
		GregorianCalendar gcm1 = new GregorianCalendar();
		GregorianCalendar gcm2 = new GregorianCalendar();
		int problemcount = 0;
		while (i<100000) { 
			if(i%1000 ==0){
				System.out.println ("Tick " + i + ", eval (ms): " + (gc2.getTimeInMillis()-gc.getTimeInMillis()) + ", analyze (ms): " + (gc3.getTimeInMillis()-gc2.getTimeInMillis()) + ", plan (ms): " + (gcm2.getTimeInMillis()-gcm1.getTimeInMillis()) + ", logging (ms)" + (gc5.getTimeInMillis()-gc4.getTimeInMillis()) + ", nextTick (ms): " + (gc6.getTimeInMillis()-gc5.getTimeInMillis())  +", gesamt: " + ((new GregorianCalendar()).getTimeInMillis()- gc1000.getTimeInMillis())+ ", problems: "+ problemcount);
				problemcount =0;
				gc1000 = new GregorianCalendar();
			}
			
			gc = new GregorianCalendar();
			planner.evaluatePastActions();
			gc2 = new GregorianCalendar();
			
			Resource problem = analyser.getTopProblem();
			gc3 = new GregorianCalendar();
			
			if(problem!=null) {
				problemcount++;
				gcm1 = new GregorianCalendar();
				Action solution = planner.selectAction(problem);
				gcm2 = new GregorianCalendar();
				//System.out.println("Problem: " + problem.getProblemType());
				if(solution!=null){
					//System.out.println("Solution: " + solution.getClass().getSimpleName());
					executor.execute(solution);
					Monitor.getInstance().logExecution(solution.getProblemResource(), solution, solution.getLocalEvaluation(), Monitor.getInstance().getGlobalTicks());
					//save the action for knowledge aquisition purposes
					planner.getExecutedActions().add(solution);
				}
				else {
					//System.out.println("No Solution found");
				}
			}
			
			gc4 = new GregorianCalendar();
			if(i%100==0){
				Monitor.getInstance().logSystemStatus();
			}
			gc5 = new GregorianCalendar();
			Monitor.getInstance().nextTick();
			gc6 = new GregorianCalendar();
			
			i++;
		}
		
		planner.terminate();
		
	}
}

package at.ac.tuwien.lsdc.mape;

import java.util.LinkedList;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.NEW;

import at.ac.tuwien.lsdc.actions.Action;
import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;
import at.ac.tuwien.lsdc.resources.Resource;

public class KISSPlanner extends Planner {
	List<Class> knownActions = new LinkedList<Class>();
	
	
	public KISSPlanner() {
		Action.setOnlyLearning(true);
		knownActions.add(CreateVmInsertApp.class);
		
		
	}
	
	@Override
	public Action selectAction(Resource problem) {
		int currentFit=10000;
		Action selectedAction = null;
		for (Class ac : knownActions) {
			
			try {
				Action a = (Action) ac.newInstance();
				a.init(problem);
				if (a.preconditions() && calculateFit(a)<currentFit) {
					selectedAction= a;
					currentFit = calculateFit(a);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		return selectedAction;
	}
	
	public void evaluatePastActions(){
		//do nothing here => becaus KISSPlanner
	}
	
	public void terminate(){
		
		for (Class ac: knownActions) {
			try {
				((Action)ac.newInstance()).terminate();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int calculateFit(Action a) {
		//TODO: gst: real Fitness calculation?
		return a.estimate();
	}

}
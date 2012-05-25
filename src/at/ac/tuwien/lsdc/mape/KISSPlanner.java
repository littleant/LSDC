package at.ac.tuwien.lsdc.mape;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.actions.Action;
import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;
import at.ac.tuwien.lsdc.resources.Resource;

public class KISSPlanner extends Planner {
	List<Action> knownActions = new LinkedList<Action>();
	
	public KISSPlanner() {
		knownActions.add(new CreateVmInsertApp());
	}
	
	@Override
	public Action selectAction(Resource problem) {
		int currentFit=10000;
		Action selectedAction = null;
		for (Action a : knownActions) {
			a.init(problem);
			if (a.preconditions() && calculateFit(a)<currentFit) {
				System.out.println("Current Problem: " + problem.getResourceId());
				selectedAction= a;
				currentFit = calculateFit(a);
			}
			
		}
		
		return selectedAction;
	}
	
	private int calculateFit(Action a) {
		//TODO: gst: real Fitness calculation?
		return a.estimate();
	}

}

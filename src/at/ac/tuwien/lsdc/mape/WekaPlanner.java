package at.ac.tuwien.lsdc.mape;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.actions.Action;
import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;
import at.ac.tuwien.lsdc.actions.MoveVm;
import at.ac.tuwien.lsdc.resources.Resource;

public class WekaPlanner extends Planner {
	List<Class> knownActions = new LinkedList<Class>();
	
	public WekaPlanner() {
		Action.setOnlyLearning(Configuration.getInstance().isOnlyLearning());
		knownActions.add(CreateVmInsertApp.class);
		knownActions.add(MoveVm.class);
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
					System.out.println("Current Problem: " + problem.getResourceId());
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

	@Override
	public void terminate() {
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
	
	public int calculateFit(Action a) {
		return a.estimate()+ a.predict();
	}
}

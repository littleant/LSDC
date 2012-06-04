package at.ac.tuwien.lsdc.mape;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.actions.Action;
import at.ac.tuwien.lsdc.resources.Resource;

public abstract class Planner {

	public abstract Action selectAction(Resource resource);
	
	public abstract void terminate();
	
	public abstract void evaluatePastActions();
	
	
	protected List<Action> executedActions = new LinkedList<Action>();
	
	public List<Action> getExecutedActions() {
		return executedActions;
	}

	public void setExecutedActions(List<Action> executedActions) {
		this.executedActions = executedActions;
	}


}
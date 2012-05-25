package at.ac.tuwien.lsdc.mape;

import at.ac.tuwien.lsdc.actions.Action;

public class Executor {
	public void execute(Action action) {
		if (action!=null){
			action.execute();
		}
	}
}

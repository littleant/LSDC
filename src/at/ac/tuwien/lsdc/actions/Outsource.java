package at.ac.tuwien.lsdc.actions;

import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.Resource;

public class Outsource extends Action {
	private App problem;
	
	private static int costs;
	
	public static int getCosts() {
		return costs;
	}

	public static void setCosts(int costs) {
		Outsource.costs = costs;
	}

	@Override
	public void init(Resource problemApp) {
		if (problemApp instanceof App) {
			problem = (App) problemApp;
		}
		
		// set init values
		Outsource.setCosts(Configuration.getInstance().getOutsourceCosts());
	}

	@Override
	public int predict() {
		int ticks = 10;
		return this.problem.getNumberOfSlaViolations(ticks) / (ticks * 3) * 100;
	}

	@Override
	public int estimate() {
		return costs;
	}

	@Override
	public boolean preconditions() {
		return true;
	}

	@Override
	public void execute() {
		// remove the app from the system
		this.problem.getVm().getToRemoveList().add(this.problem);
	}

	@Override
	public boolean evaluate() {
		// nothing to do here
		return true;
	}

	@Override
	public void terminate() {
		// unused
	}
}

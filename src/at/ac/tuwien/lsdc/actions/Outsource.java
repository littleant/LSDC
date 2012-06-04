package at.ac.tuwien.lsdc.actions;

import at.ac.tuwien.lsdc.generator.RequestGenerator;
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
		this.setProblemResource(problemApp);
		this.setProblemType(problemApp.getProblemType());
		
		if (problemApp instanceof App) {
			problem = (App) problemApp;
		}
	}

	@Override
	public int predict() {
		int ticks = 10;
		if (this.problem!=null){
			return this.problem.getNumberOfSlaViolations(ticks) / (ticks * 3) * 100;
		}
		else {
			return 0;
		}
	}

	@Override
	public int estimate() {
		return costs;
	}

	@Override
	public boolean preconditions() {
		if (this.problem !=null) {
			return true;
		}
		return false;
	}

	@Override
	public void execute() {
		// remove the app from the system

		if (this.problem.getVm()!=null) {
			this.problem.getVm().getToRemoveList().add(this.problem);
		}
		
		//remove the app from the request- queue??? 
		if(problem.getOriginalRequest()!=null) {
			RequestGenerator.getInstance().removeRequestFromQueue(problem.getOriginalRequest());
		}
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

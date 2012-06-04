package at.ac.tuwien.lsdc.actions;

import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

/**
 * Action which does nothing, costs nothing but is super important
 */
public class DoNothing extends Action {
	private Resource problem;
	
	@Override
	public void init(Resource problemApp) {
		this.setProblemResource(problemApp);
		this.setProblemType(problemApp.getProblemType());
		this.setProblem(problemApp);
	}

	@Override
	public int predict() {
		// predict how good doing nothing is for the system. How many SLA violations did we have in the past? Predict that we have the same amound in the future.
		int slaViolations = 0;
		
		int ticks = 10;
		if (this.getProblem() instanceof App) {
			App app = (App) this.getProblem();
			slaViolations = app.getNumberOfSlaViolations(ticks);
		} else if (this.getProblem() instanceof VirtualMachine) {
			VirtualMachine vm = (VirtualMachine) this.getProblem();
			slaViolations = vm.getNumberOfSlaViolations(ticks);
		} else if (this.getProblem() instanceof PhysicalMachine) {
			PhysicalMachine pm = (PhysicalMachine) this.getProblem();
			slaViolations = pm.getNumberOfSlaViolations(ticks);
		}
		
		return slaViolations;
	}

	@Override
	public int estimate() {
		// we estimate a cost of 0 for doing nothing. This is pretty accurate.
		return 0;
	}

	@Override
	public boolean preconditions() {
		// preconditions for doing nothing are always met. Surprise.
		return true;
	}

	@Override
	public void execute() {
		// executing nothing... finished!
	}

	@Override
	public boolean evaluate() {
		// nothing to do here
		return true;
	}

	@Override
	public void terminate() {
		// I'll be back!
	}

	public Resource getProblem() {
		return problem;
	}

	public void setProblem(Resource problem) {
		this.problem = problem;
	}
}

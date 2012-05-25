package at.ac.tuwien.lsdc.mape;

import at.ac.tuwien.lsdc.actions.Action;
import at.ac.tuwien.lsdc.resources.Resource;

public abstract class Planner {

	public abstract Action selectAction(Resource problem);
}

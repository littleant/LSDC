package at.ac.tuwien.lsdc.actions;

public abstract class Action {
	
	public abstract int predict();
	
	public abstract int estimate();
	
	public abstract boolean preconditions();
	
}

package at.ac.tuwien.lsdc.actions;

public interface IAction {

	public int estimate();
	
	public int predict(); 
	
	public boolean preconditions();
	
}

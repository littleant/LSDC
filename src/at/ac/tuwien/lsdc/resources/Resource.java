package at.ac.tuwien.lsdc.resources;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class Resource {
	private static int lastPmId=0;
	private static int lastVmId=0;
	private static int lastAppId=0;
	
	protected int suspendedTicks=0;
	private int resourceId=0;
	protected int actionLock = 0;
	
	private String problemType ="";
	
	
	
	public void setNewPmId(){
		lastPmId++;
		this.resourceId=lastPmId;
	}
	
	public void setNewVmId(){
		lastVmId++;
		this.resourceId = lastVmId;
	}
	
	public void setNewAppId() {
		lastAppId++;
		this.resourceId = lastAppId;
	}
	
	public int getResourceId() {
		return resourceId;
	}

	public int getSuspendedTicks() {
		return suspendedTicks;
	}

	public void setSuspendedTicks(int suspendedTicks) {
		this.suspendedTicks = suspendedTicks;
	}

	public int getRunningTicks() {
		return runningTicks;
	}

	public void setRunningTicks(int runningTicks) {
		this.runningTicks = runningTicks;
	}

	protected int runningTicks=0;
	
	//returns the last n elements of an integer list
	public static LinkedList<Integer> getLastEntriesUtil(LinkedList<Integer> input,int maxNoOfEntries, int lastElementId) {
		LinkedList<Integer> out = new LinkedList<Integer>();
		int i=0;
		int t=0;
		int entries=0;
		Iterator<Integer> it = input.descendingIterator();
		while (it.hasNext() && entries<maxNoOfEntries){
			t = it.next();
			if(i >= (input.size()-lastElementId-1)){
				entries++;
				out.addFirst(t);
			}
			i++;
		}
		
		// add zeros for missing values
		if (out.size() < maxNoOfEntries) {
			int max = maxNoOfEntries - out.size();
			for (int j = 0; j < max; j++) {
				out.addFirst(0);
			}
		}
		
		
		return out;
	}
	
	public static LinkedList<Integer> getLastEntriesUtil(LinkedList<Integer> input,int maxNoOfEntries) {
		return getLastEntriesUtil(input, maxNoOfEntries, input.size()-1);
	}
	
	///sums up several lists (memory usage app1 + memory usage app2 +....)
	public static LinkedList<Integer> aggregateValues(LinkedList<LinkedList<Integer>> input ) {
		boolean allFinished = false;
		int i = 1;
		int curVal = 0;
		LinkedList<Integer> aggregated = new LinkedList<Integer>();
		while (allFinished==false) {
			allFinished = true;
			curVal = 0;
			
			for (LinkedList<Integer> li : input) {
				if (li.size()>=i) {
					allFinished=false;
					curVal += li.get(li.size()-i);
				}
			}
			if(allFinished==false){
				aggregated.addFirst(curVal);
			}
			i++;
		}
		return aggregated;
	}
	
	//returns a list filled with 0 - values
	protected LinkedList<Integer> dummyList (int maxEntries)  {
		LinkedList<Integer> vmachine = new LinkedList<Integer>();
		for (int i = 0 ; i<maxEntries; i++) {
			vmachine.add(0);
		}
		
		return vmachine;
	}
	
	
	public abstract Integer getCurrentCpuUsage();
	
	public abstract Integer getCurrentMemoryUsage();
	
	public abstract Integer getCurrentStorageUsage();
	
	public abstract Integer getCurrentCpuAllocation();
	
	public abstract Integer getCurrentMemoryAllocation();
	
	public abstract Integer getCurrentStorageAllocation();
	
	public abstract LinkedList<Integer> getStorageUsageHistory(int maxEntries);
	
	public abstract LinkedList<Integer> getMemoryUsageHistory(int maxEntries);
	
	public abstract LinkedList<Integer> getCpuUsageHistory(int maxEntries);
	
	public abstract LinkedList<Integer> getStorageAllocationHistory(int maxEntries) ;
	
	public abstract LinkedList<Integer> getCpuAllocationHistory(int maxEntries) ;
	
	public abstract LinkedList<Integer> getMemoryAllocationHistory(int maxEntries) ;
	
	//calculate next tick
	abstract public void nextTick();

	public String getProblemType() {
		return problemType;
	}

	public void setProblemType(String problemType) {
		this.problemType = problemType;
	}
	
	public int getActionLock() {
		return actionLock;
	}

	public void setActionLock(int actionLock) {
		this.actionLock = actionLock;
	}
}

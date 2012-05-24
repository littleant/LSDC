package at.ac.tuwien.lsdc.resources;

import java.util.LinkedList;

import at.ac.tuwien.lsdc.mape.Problem;

public class App implements Problem {

	
	public int getCurrentCpuUsage() {
		return 0;
	}
	
	public int getCurrentMemoryUsage() {
		return 0;
	}
	
	public int getCurrentStorageUsage() {
		return 0;
	}
	
	public LinkedList<Integer> getCpuUsageHistory(int maxNumberOfEntries) {
		return null;
	}
	
	public LinkedList<Integer> getMemoryUsageHistory(int maxNumberOfEntries) {
		return null;
	}
	
	public LinkedList<Integer> getStorageUsageHistory(int maxNumberOfEntries) {
		return null;
	}

}

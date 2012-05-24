package at.ac.tuwien.lsdc.resources;

import java.util.LinkedList;

import at.ac.tuwien.lsdc.mape.Problem;

public class VirtualMachine implements Problem {
	private int suspendedTicks;
	
	
	private LinkedList<Integer> allocatedCpu = new LinkedList<Integer>();
	private LinkedList<Integer> allocatedMemory = new LinkedList<Integer>();
	private LinkedList<Integer> allocatedStorage = new LinkedList<Integer>();
	
	private LinkedList<App> apps = new LinkedList<App>();
	
	public VirtualMachine(int initialCpu, int initialMemory, int initialStorage, int startupTime) {
		this.allocatedCpu.addLast(initialCpu);
		this.allocatedMemory.addLast(initialMemory);
		this.allocatedStorage.addLast(initialStorage);
		this.suspendedTicks = startupTime;
	}
	
	private VirtualMachine() {
	}
	
	public Integer getCurrentCpuAllocation() {
		return allocatedCpu.getLast();
	}
	
	public LinkedList<Integer> getCpuAllocationHistory(int maxNumberOfEntries) {
		
	}
	
	//get CPU usage produced by apps
	public Integer getCurrentCpuUsage() {
		int cpuusage =0 ;
		for (App a : apps) {
			cpuusage +=a.getCurrentCpuUsage();
		}
		return cpuusage;
	}
	
	//get memory usage produced by apps
	public Integer getCurrentMemoryUsage() {
		int memusage =0 ;
		for (App a : apps) {
			memusage +=a.getCurrentMemoryUsage();
		}
		return memusage;
	}
	
	//get storage usage produced by apps 
	public Integer getCurrentStorageUsage() {
		int storusage =0 ;
		for (App a : apps) {
			storusage +=a.getCurrentMemoryUsage();
		}
		return storusage;
	}
	
	//memory allocated by the vm
	public Integer getCurrentMemoryAllocation() {
		return allocatedMemory.getLast();
	}
	
	//storage allocated by the vm
	public Integer getCurrentStorageAllocation()  {
		return allocatedStorage.getLast();
	}
	
	
	
}

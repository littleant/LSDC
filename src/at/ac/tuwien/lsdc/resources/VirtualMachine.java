package at.ac.tuwien.lsdc.resources;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.mape.Problem;

public class VirtualMachine extends Resource implements Problem  {

	
	// resources the VM has allocated on the PM
	private LinkedList<Integer> allocatedCpu = new LinkedList<Integer>();
	private LinkedList<Integer> allocatedMemory = new LinkedList<Integer>();
	private LinkedList<Integer> allocatedStorage = new LinkedList<Integer>();
	private PhysicalMachine pm;
	
	public PhysicalMachine getPm() {
		return pm;
	}

	public void setPm(PhysicalMachine pm) {
		this.pm = pm;
	}


	private LinkedList<App> apps = new LinkedList<App>();
	
	public VirtualMachine(int initialCpu, int initialMemory, int initialStorage, int startupTime) {
		setNewVmId();
		this.allocatedCpu.addLast(initialCpu);
		this.allocatedMemory.addLast(initialMemory);
		this.allocatedStorage.addLast(initialStorage);
		this.suspendedTicks = startupTime;
	}
	
	@SuppressWarnings("unused")
	private VirtualMachine() {
	}
	
	
	public Integer getCurrentCpuAllocation() {
		return allocatedCpu.getLast();
	}
	
	//returns the last n cpu - allocation values 
	public LinkedList<Integer> getCpuAllocationHistory(int maxNumberOfEntries) {
		return this.getLastEntriesUtil(allocatedCpu, maxNumberOfEntries);
	}
	
	//returns the last n memory - allocation values
	public LinkedList<Integer> getMemoryAllocationHistory(int maxNumberOfEntries) {
		return this.getLastEntriesUtil(allocatedMemory, maxNumberOfEntries);
	}
	
	//returns the last n storage allocation values
	public LinkedList<Integer> getStorageAllocationHistory(int maxNumberOfEntries) {
		return this.getLastEntriesUtil(allocatedStorage, maxNumberOfEntries);
	}
	
	//returns the last n cpu - usage parameters
	public LinkedList<Integer> getCpuUsageHistory(int maxNumberOfEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		for (App a: apps) {
			values.add(a.getCpuUsageHistory(maxNumberOfEntries));
		}
		
		return this.aggregateValues(values);
	}
	
	//returns the last n memory - usage parameters
	public LinkedList<Integer> getMemoryUsageHistory(int maxNumberOfEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		for (App a: apps) {
			values.add(a.getMemoryUsageHistory(maxNumberOfEntries));
		}
		
		return this.aggregateValues(values);
	}
	
	//returns the last n storage - usage parameters
	public LinkedList<Integer> getStorageUsageHistory(int maxNumberOfEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		for (App a: apps) {
			values.add(a.getStorageUsageHistory(maxNumberOfEntries));
		}
			
		return this.aggregateValues(values);
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
			storusage +=a.getCurrentStorageUsage();
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
	
	
	public void nextTick(){
		if(suspendedTicks>0) {
			suspendedTicks--;
		}
		else {
			
			runningTicks++;
			for (App a: apps) {
				a.nextTick();
			}
		}
	}
	
	public App createApp(App a) {
		this.getApps().add(a);
		a.setVm(this);
		return a;
	}
	
	public void terminate() {
		pm.getVms().remove(this);
	}


	public List<App> getApps() {
		return this.apps;
	}
}

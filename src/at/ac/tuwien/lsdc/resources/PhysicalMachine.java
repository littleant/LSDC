package at.ac.tuwien.lsdc.resources;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.mape.Monitor;

public class PhysicalMachine extends Resource {
	//is the machine running? true=>yes
	
	private boolean isRunning;
	
	private final Integer STARTUPTIME = 20;
	private LinkedList<VirtualMachine> toRemoveList = new LinkedList<VirtualMachine>();

	private LinkedList<VirtualMachine> vms = new LinkedList<VirtualMachine>();
	
	public LinkedList<VirtualMachine> getToRemoveList() {
		return toRemoveList;
	}

	public void setToRemoveList(LinkedList<VirtualMachine> toRemoveList) {
		this.toRemoveList = toRemoveList;
	}

	public PhysicalMachine(){
		setNewPmId();
	}
	
	public LinkedList<VirtualMachine> getVms() {
		return vms;
	}

	public void setVms(LinkedList<VirtualMachine> vms) {
		this.vms = vms;
	}
	
	/**
	 * Retrieves all apps that are deployed on this machine (in VMs)
	 * 
	 * @return deployed apps
	 */
	public List<App> getApps() {
		List<App> apps = new LinkedList<App>();
		List<VirtualMachine> vms = this.getVms();
		for (VirtualMachine vm : vms) {
			apps.addAll(vm.getApps());
		}
		
		return apps;
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	public void setRunning(boolean running) {
		this.isRunning = running;
	}

	public void startMachine() {
		suspendedTicks = STARTUPTIME;
		setActionLock(STARTUPTIME*5);
		isRunning = true;
	}
	
	public void stopMachine() {
		isRunning = false;
	}
	
	@Override
	public void nextTick() {
		
		if (suspendedTicks>0) {
			//System.out.println("PM Suspended");
			suspendedTicks--;
		}
		else if(isRunning==false ){
			//machine is not running
			
		}
		else {
			if(getActionLock()>0){
				actionLock--;
			}
			//System.out.println("PM Running" + this.getResourceId());
			this.toRemoveList = new LinkedList<VirtualMachine>();
			this.runningTicks++;
			
			for (VirtualMachine vm : this.vms) {
				vm.nextTick();
			}

			for (VirtualMachine vm : this.toRemoveList) {
				//System.out.println (Monitor.getInstance().getGlobalTicks() + " Remove VM " + vm.getResourceId() + " from PM " + this.getResourceId());
				this.vms.remove(vm);
			}
		}
		
	}
	
	public void logSlaViolations() {
		for (VirtualMachine vm : vms) {
			for(App a: vm.getApps()){
				a.logSlaViolations();
			}
		}
	}
	
	@Override
	//returns actual cpu - usage of the vms and apps
	public Integer getCurrentCpuUsage() {
		int curcpu =0;
		for (VirtualMachine vm: vms) {
			curcpu +=vm.getCurrentCpuUsage();
		}
		return curcpu;
	}
	
	@Override
	//returns the memory that is actually used by the vms and apps
	public Integer getCurrentMemoryUsage() {
		int curmem=0;
		for(VirtualMachine vm: vms) {
			curmem+=vm.getCurrentMemoryUsage();
		}
		return curmem;
	}
	
	@Override
	//returns the storage that is actually used by the vms and apps
	public Integer getCurrentStorageUsage() {
		int curstorage = 0;
		for (VirtualMachine vm: vms) {
			curstorage += vm.getCurrentStorageUsage();
		}
		return curstorage;
	}
	
	//returns the storage that is currently allocated by the vms
	public Integer getCurrentStorageAllocation() {
		int curstorage = 0;
		for(VirtualMachine vm:vms) {
			curstorage+=vm.getCurrentStorageAllocation();
		}
		return curstorage;
	}
	@Override
	//returns the cpu that is currently allocated by the vms
	public Integer getCurrentCpuAllocation() {
		int curcpu=0;
		for (VirtualMachine vm : vms) {
			curcpu += vm.getCurrentCpuAllocation();
		}
		return curcpu;
		
	}
	
	@Override
	//returns the memory that is currently allocated by the vms
	public Integer getCurrentMemoryAllocation() {
		int curmem=0;
		for (VirtualMachine vm: vms){
			curmem += vm.getCurrentMemoryAllocation();
		}
		return curmem;
	}

	@Override
	//get and aggregate the usage values for all vms
	public LinkedList<Integer> getStorageUsageHistory(int maxEntries) {

		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		values.add(this.dummyList(maxEntries)); // if no vms are running
		for (VirtualMachine vm: vms) {
			values.add(vm.getStorageUsageHistory(maxEntries));
		}
		
		return Resource.aggregateValues(values);
	}

	@Override
	//get and aggregate the usage values for all vms
	public LinkedList<Integer> getMemoryUsageHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		values.add(this.dummyList(maxEntries)); // if no vms are running
		for (VirtualMachine vm: vms) {
			values.add(vm.getMemoryUsageHistory(maxEntries));
		}
		
		return aggregateValues(values);
	}

	@Override
	//get and aggregate the usage values for all vms
	public LinkedList<Integer> getCpuUsageHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		
		
		values.add(this.dummyList(maxEntries)); // if no vms are running
		for (VirtualMachine vm: vms) {
			values.add(vm.getCpuUsageHistory(maxEntries));
			
		}
		
		return aggregateValues(values);
	}

	@Override
	//get and aggregate the allocation values for all vms
	public LinkedList<Integer> getStorageAllocationHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		values.add(this.dummyList(maxEntries)); // if no vms are running
		for (VirtualMachine vm: vms) {
			values.add(vm.getStorageAllocationHistory(maxEntries));
		}
		
		return aggregateValues(values);
	}

	@Override
	//get and aggregate the allocation values for all vms
	public LinkedList<Integer> getCpuAllocationHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		values.add(this.dummyList(maxEntries)); // if no vms are running
		for (VirtualMachine vm: vms) {
			values.add(vm.getCpuAllocationHistory(maxEntries));
		}
		
		return aggregateValues(values);
	}

	@Override
	//get and aggregate the allocation values for all vms
	public LinkedList<Integer> getMemoryAllocationHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		
		values.add(this.dummyList(maxEntries)); // if no vms are running
		for (VirtualMachine vm: vms) {
			values.add(vm.getMemoryAllocationHistory(maxEntries));
		}
		
		return aggregateValues(values);
	}
	
	public VirtualMachine createNewVm(int initialCpu, int initialMemory, int initialStorage, int startupTime) {
		VirtualMachine vm = new VirtualMachine(initialCpu, initialMemory, initialStorage, startupTime);
		vm.setPm(this);
		this.getVms().add(vm);
		return vm;
	}
	
	/**
	 * Gets the number of SLA violations for all apps in all VMs on this PM for the last n ticks.
	 * 
	 * @param ticks How many ticks to look in the past
	 * @return number of violations for the last n ticks
	 */
	public int getNumberOfSlaViolations(int ticks) {
		int violations = 0;
		
		for (VirtualMachine vm : this.getVms()) {
			violations += vm.getNumberOfSlaViolations(ticks);
		}
		
		return violations;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Resource) {
			if (((Resource) o).getResourceId() == this.getResourceId()) {
				return true;
			}
		}
		
		return false;
	}


}


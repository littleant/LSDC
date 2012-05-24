package at.ac.tuwien.lsdc.resources;

import java.util.LinkedList;

import at.ac.tuwien.lsdc.mape.Problem;

public class PhysicalMachine extends Resource implements Problem {
	//is the machine running? true=>yes
	private boolean status;
	private final Integer STARTUPTIME = 20;
	
	private LinkedList<VirtualMachine> vms = new LinkedList<VirtualMachine>();
	
	public boolean isStatus() {
		return status;
	}

	public void startMachine() {
		suspendedTicks = STARTUPTIME;
		status = true;
	}
	
	public void stopMachine() {
		status = false;
	}
	
	@Override
	public void nextTick() {
		if (suspendedTicks>0) {
			suspendedTicks--;
		}
		else if(status==false ){
			//machine is not running
		}
		else {
			runningTicks++;
			for (VirtualMachine vm : vms ) {
				vm.nextTick();
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
		
		for (VirtualMachine vm: vms) {
			values.add(vm.getStorageUsageHistory(maxEntries));
		}
		
		return this.aggregateValues(values);
	}

	@Override
	//get and aggregate the usage values for all vms
	public LinkedList<Integer> getMemoryUsageHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		
		for (VirtualMachine vm: vms) {
			values.add(vm.getMemoryUsageHistory(maxEntries));
		}
		
		return this.aggregateValues(values);
	}

	@Override
	//get and aggregate the usage values for all vms
	public LinkedList<Integer> getCpuUsageHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		
		for (VirtualMachine vm: vms) {
			values.add(vm.getCpuUsageHistory(maxEntries));
		}
		
		return this.aggregateValues(values);
	}

	@Override
	//get and aggregate the allocation values for all vms
	public LinkedList<Integer> getStorageAllocationHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		
		for (VirtualMachine vm: vms) {
			values.add(vm.getStorageAllocationHistory(maxEntries));
		}
		
		return this.aggregateValues(values);
	}

	@Override
	//get and aggregate the allocation values for all vms
	public LinkedList<Integer> getCpuAllocationHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		
		for (VirtualMachine vm: vms) {
			values.add(vm.getCpuAllocationHistory(maxEntries));
		}
		
		return this.aggregateValues(values);
	}

	@Override
	//get and aggregate the allocation values for all vms
	public LinkedList<Integer> getMemoryAllocationHistory(int maxEntries) {
		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
		
		for (VirtualMachine vm: vms) {
			values.add(vm.getMemoryAllocationHistory(maxEntries));
		}
		
		return this.aggregateValues(values);
	}
	
}


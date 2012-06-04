package at.ac.tuwien.lsdc.actions;

import java.util.List;

import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class ChangeVmConfiguration extends Action {
	private VirtualMachine vm;
	private int optimizedCpuAllocation;
	private int optimizedMemoryAllocation;
	private int optimizedStorageAllocation;
	
	private static int configurationChangeCosts;
	private static int topRegion;
	private static int bottomRegion;
	
	// tick count to look in the past
	private final int tickCount = 10;
	
	@Override
	public void init(Resource problem) {
		if (problem instanceof VirtualMachine) {
			this.vm = (VirtualMachine) problem;
		}
		
		// set init values
		ChangeVmConfiguration.setConfigurationChangeCosts(Configuration.getInstance().getVmConfigurationChangeCosts());
		ChangeVmConfiguration.setTopRegion(Configuration.getInstance().getTopRegion());
		ChangeVmConfiguration.setBottomRegion(Configuration.getInstance().getBottomRegion());
	}

	public static int getBottomRegion() {
		return bottomRegion;
	}

	public static void setBottomRegion(int bottomRegion) {
		ChangeVmConfiguration.bottomRegion = bottomRegion;
	}

	@Override
	public int predict() {
		this.calculateBetterAllocationValues();
		
		// decide how urgent a configurationchange is neccessary
		// TODO
			
		return 0;
	}

	/**
	 * Calculates better VM allocation values for the given VM, depending in which zone it's currently in.
	 */
	private void calculateBetterAllocationValues() {
		this.optimizedCpuAllocation = this.calculateOptimizedCpuAllocation(this.tickCount);
		this.optimizedMemoryAllocation = this.calculateOptimizedMemoryAllocation(this.tickCount);
		this.optimizedStorageAllocation = this.calculateOptimizedStorageAllocation(this.tickCount);
	}
	
	/**
	 * Calculates an optimized allocation value for the VM
	 * 
	 * @param ticks Based on the last n ticks
	 * @return The optimized allocation value
	 */
	public int calculateOptimizedCpuAllocation(int ticks) {
		int allocation = this.vm.getCurrentCpuAllocation();
		
		List<Integer> cpuAllocationHistory = this.vm.getCpuAllocationHistory(this.tickCount);
		List<Integer> cpuUsageHistory = this.vm.getCpuUsageHistory(this.tickCount);
		
		int optimizedAllocation = 0;
		
		int topRegionReached = 0;
		int bottomRegionReached = 0;
		
		// calculate how often the VM went into a dangerous zone in the last n ticks (compare allocated to used resources)
		for (int i = 0; i < cpuAllocationHistory.size(); i++) {
			Integer allocated = cpuAllocationHistory.get(i);
			Integer used = cpuUsageHistory.get(i);
			
			// calculate percentage of the used resources vs. the allocated resources
			int ratio = (int) ((used / (float) allocated) * 100);
			if (ratio > ChangeVmConfiguration.topRegion) {
				// need more resources
				topRegionReached++;
			} else if (ratio < ChangeVmConfiguration.bottomRegion) {
				// need less resources
				bottomRegionReached++;
			} else {
				// resource allocation is perfect
				// do nothing
			}
			
			// calculate allocation so that "used" is 95% of the allocation
			optimizedAllocation = (int) ((float) used / ChangeVmConfiguration.topRegion * 100);
			
			if (topRegionReached > 1 || bottomRegionReached > 1) {
				// need to change the allocation
				if (allocation < optimizedAllocation) {
					allocation = optimizedAllocation;
				}
			}
		}
		
		if (allocation > 100) {
			allocation = 100;
		} else if (allocation < 0) {
			allocation = 0;
		}
		
		return allocation;
	}
	
	public int calculateOptimizedMemoryAllocation(int ticks) {
		int allocation = this.vm.getCurrentMemoryAllocation();
		
		// TODO
		
		return allocation;
	}
	
	public int calculateOptimizedStorageAllocation(int ticks) {
		int allocation = this.vm.getCurrentStorageAllocation();
		
		// TODO
		
		return allocation;
	}
	
	@Override
	public int estimate() {
		// TODO: no costs if we shrink the VM?
		return ChangeVmConfiguration.configurationChangeCosts;
	}

	@Override
	public boolean preconditions() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean evaluate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void terminate() {
		// unused
	}

	public static int getConfigurationChangeCosts() {
		return configurationChangeCosts;
	}

	public static void setConfigurationChangeCosts(int configurationChangeCosts) {
		ChangeVmConfiguration.configurationChangeCosts = configurationChangeCosts;
	}

	public static int getTopRegion() {
		return topRegion;
	}

	public static void setTopRegion(int topRegion) {
		ChangeVmConfiguration.topRegion = topRegion;
	}

}

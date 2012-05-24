package at.ac.tuwien.lsdc.resources;

import at.ac.tuwien.lsdc.mape.Problem;

public class PhysicalMachine implements Problem {
	private boolean status;
	private int usedCpu;
	private int usedMemory;
	private int usedStorage;
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public int getUsedCpu() {
		return usedCpu;
	}
	public void setUsedCpu(int usedCpu) {
		this.usedCpu = usedCpu;
	}
	public int getUsedMemory() {
		return usedMemory;
	}
	public void setUsedMemory(int usedMemory) {
		this.usedMemory = usedMemory;
	}
	public int getUsedStorage() {
		return usedStorage;
	}
	public void setUsedStorage(int usedStorage) {
		this.usedStorage = usedStorage;
	}
	
}


package at.ac.tuwien.lsdc.resources;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.mape.Problem;

public class App implements Problem {
	private int ticks;
	private int tickNumber;
	private int extraTicks = 0;
	
	// SLAs
	private int cpu;
	private int memory;
	private int storage;
	
	// currently used resources
	private int usedCpu;
	private int usedMemory;
	private int usedStorage;
	
	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public int getTickNumber() {
		return tickNumber;
	}

	public void setTickNumber(int tickNumber) {
		this.tickNumber = tickNumber;
	}

	public int getExtraTicks() {
		return extraTicks;
	}

	public void setExtraTicks(int extraTicks) {
		this.extraTicks = extraTicks;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getStorage() {
		return storage;
	}

	public void setStorage(int storage) {
		this.storage = storage;
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

	public List<Integer> getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(List<Integer> cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public List<Integer> getMemoryUsage() {
		return memoryUsage;
	}

	public void setMemoryUsage(List<Integer> memoryUsage) {
		this.memoryUsage = memoryUsage;
	}

	public List<Integer> getStorageUsage() {
		return storageUsage;
	}

	public void setStorageUsage(List<Integer> storageUsage) {
		this.storageUsage = storageUsage;
	}

	// used resources per tick
	private List<Integer> cpuUsage;
	private List<Integer> memoryUsage;
	private List<Integer> storageUsage;
	
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

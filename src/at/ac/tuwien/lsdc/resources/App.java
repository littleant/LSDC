package at.ac.tuwien.lsdc.resources;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.mape.Problem;

public class App implements Problem {
	// how many ticks the App will run
	private int ticks;
	// how many ticks the App ran
	private int tickNumber;
	// extra ticks that the App cannot run.
	// If this is > 0 the App is suspendend and the extraTicks should be decremented.
	private int extraTicks = 0;
	
	// SLAs
	private int cpu;
	private int memory;
	private int storage;

	// used resources per tick
	private List<Integer> cpuUsage;
	private List<Integer> memoryUsage;
	private List<Integer> storageUsage;
	
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

	public int getCurrentCpuUsage() {
		return this.getCpuUsage().get(this.getTickNumber());
	}

	public int getCurrentMemoryUsage() {
		return this.getMemoryUsage().get(this.getTickNumber());
	}

	public int getCurrentStorageUsage() {
		return this.getStorageUsage().get(this.getTickNumber());
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
	
	public LinkedList<Integer> getCpuUsageHistory(int maxNumberOfEntries) {
		// TODO
		return null;
	}
	
	public LinkedList<Integer> getMemoryUsageHistory(int maxNumberOfEntries) {
		// TODO
		return null;
	}
	
	public LinkedList<Integer> getStorageUsageHistory(int maxNumberOfEntries) {
		// TODO
		return null;
	}

}

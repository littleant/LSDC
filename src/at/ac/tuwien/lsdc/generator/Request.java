package at.ac.tuwien.lsdc.generator;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.resources.App;

public class Request {
	private int ticks;
	
	// SLAs
	private int start;
	private int cpu;
	private int memory;
	private int storage;
	
	// usage per tick
	private LinkedList<Integer> cpuUsage;
	private LinkedList<Integer> memoryUsage;
	private LinkedList<Integer> storageUsage;
	
	public App createApp() {
		App app = new App(this.cpu, this.memory, this.storage, this.cpuUsage, this.memoryUsage, this.storageUsage);
		
		return app;
	}

	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}
	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
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

	public List<Integer> getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(LinkedList<Integer> cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public List<Integer> getMemoryUsage() {
		return memoryUsage;
	}

	public void setMemoryUsage(LinkedList<Integer> memoryUsage) {
		this.memoryUsage = memoryUsage;
	}

	public List<Integer> getStorageUsage() {
		return storageUsage;
	}

	public void setStorageUsage(LinkedList<Integer> storageUsage) {
		this.storageUsage = storageUsage;
	}
	
	@Override
	public String toString(){
		
		String request= "SLAS: ";

		request += this.cpu + ";" + this.memory + ";" + this.storage + ";";
		
		request += "\nCpus: ";
		for (int i =0;i <this.cpuUsage.size();i++)
			request +=cpuUsage.get(i)+";";
		request += "\nMemory: ";
		for (int i =0;i <this.memoryUsage.size();i++)
			request +=memoryUsage.get(i)+";";
		request += "\nStorage: ";
		for (int i =0;i <this.storageUsage.size();i++)
			request +=storageUsage.get(i)+";";
		
		
		return request;
		
	}

	
}

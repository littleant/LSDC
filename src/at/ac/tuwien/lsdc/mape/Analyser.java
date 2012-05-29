package at.ac.tuwien.lsdc.mape;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.generator.Request;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;

public class Analyser {
	public Resource getTopProblem() {
		// Check SLAs against SLA violation regions (red, orange, green)
		List<PhysicalMachine> pms = Monitor.getInstance().getPms();
		List<App> apps = new LinkedList<App>();
		for (PhysicalMachine pm : pms) {
			apps.addAll(pm.getApps());
		}
		
		// Potential SLA violation regions
		int topRegion = Configuration.getInstance().getTopRegion();
		int bottomRegion = Configuration.getInstance().getBottomRegion();
		
		// check which app is the most critical problem
		App criticalApp = null;
		int criticalAppPercentage = 0;
		
		for (App app : apps) {
			// cpu
			// 100% of SLA = app.getCpu()
			// % of usage, compared to PM = app.getCurrentCpuUsage()
			// percentage of usage, compared to the SLA
			int cpuPercentage = app.getCurrentCpuUsage() / app.getVm().getCurrentCpuAllocation() * 100;
			// check if percentage is higher than previous app
			if (cpuPercentage > criticalAppPercentage && app.getVm().getCurrentCpuAllocation() < app.getCpu()) {
				// replace the top criticl app
				criticalApp = app;
				criticalAppPercentage = cpuPercentage;
			}
			
			// memory
			int memoryPercentage = app.getCurrentMemoryUsage() / app.getVm().getCurrentMemoryAllocation() * 100;
			if (memoryPercentage > criticalAppPercentage && app.getVm().getCurrentMemoryAllocation() < app.getMemory()) {
				// replace the top criticl app
				criticalApp = app;
				criticalAppPercentage = memoryPercentage;
			}
			
			// storage
			int storagePercentage = app.getCurrentStorageUsage() / app.getVm().getCurrentStorageAllocation()* 100;
			if (storagePercentage > criticalAppPercentage && app.getVm().getCurrentStorageAllocation() < app.getStorage()) {
				// replace the top criticl app
				criticalApp = app;
				criticalAppPercentage = storagePercentage;
			}
		}
		
		// Decide what the top problem is
		Resource problem = null;
		
		if (criticalAppPercentage >= topRegion) {
			System.out.println("Top problem is critical app: " + criticalApp.getResourceId());
			problem = criticalApp;
		}
		
		if (problem == null) {
			// Check if there are one or more requests in the queue
			List<Request> requests = RequestGenerator.getInstance().getPendingRequests();
			
			if (requests.size() > 0) {
				// take first request and define it as the top problem
				problem = requests.get(0).createApp();
			}
			
		}
		
		if (problem == null) {
			// take the top problem if there is one that's not in the "green" region
			if (criticalAppPercentage >= bottomRegion) {
				problem = criticalApp;
			}
		}
		
		return problem;
	}
}

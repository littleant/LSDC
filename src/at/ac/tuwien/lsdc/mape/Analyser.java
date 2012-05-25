package at.ac.tuwien.lsdc.mape;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.lsdc.generator.Request;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;

public class Analyser {
	public Problem getTopProblem() {
		// Check SLAs against SLA violation regions (red, orange, green)
		List<PhysicalMachine> pms = Monitor.getInstance().getPms();
		List<App> apps = new LinkedList<App>();
		for (PhysicalMachine pm : pms) {
			apps.addAll(pm.getApps());
		}
		
		// Potential SLA violation regions
		// FIXME: put these values in a properties-file.
		int topRegion = 90;
		int lowestRegion = 75;
		
		// check which app is the most critical problem
		App criticalApp = null;
		int criticalAppPercentage = 0;
		
		for (App app : apps) {
			// cpu
			// 100% of SLA = app.getCpu()
			// % of usage, compared to PM = app.getCurrentCpuUsage()
			// percentage of usage, compared to the SLA
			int cpuPercentage = app.getCurrentCpuUsage() / app.getCpu() * 100;
			// check if percentage is higher than previous app
			if (cpuPercentage > criticalAppPercentage) {
				// replace the top criticl app
				criticalApp = app;
				criticalAppPercentage = cpuPercentage;
			}
			
			// memory
			int memoryPercentage = app.getCurrentMemoryUsage() / app.getMemory() * 100;
			if (memoryPercentage > criticalAppPercentage) {
				// replace the top criticl app
				criticalApp = app;
				criticalAppPercentage = memoryPercentage;
			}
			
			// storage
			int storagePercentage = app.getCurrentStorageUsage() / app.getStorage() * 100;
			if (storagePercentage > criticalAppPercentage) {
				// replace the top criticl app
				criticalApp = app;
				criticalAppPercentage = storagePercentage;
			}
		}
		
		// Decide what the top problem is
		Problem problem = null;
		
		if (criticalAppPercentage >= topRegion) {
			problem = criticalApp;
		}
		
		if (problem == null) {
			// Check if there are one or more requests in the queue
			RequestGenerator requestGenerator = RequestGenerator.getInstance();
			List<Request> requests = requestGenerator.getRequests();
			
			if (requests.size() > 0) {
				// take first request and define it as the top problem
				problem = requests.get(0).createApp();
			}
		}
		
		if (problem == null) {
			// take the top problem if there is one that's not in the "green" region
			if (criticalAppPercentage >= lowestRegion) {
				problem = criticalApp;
			}
		}
		
		return problem;
	}
}

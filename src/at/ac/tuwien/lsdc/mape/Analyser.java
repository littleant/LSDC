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
		// FIXME: put these values in a properties-file. Highest to lowest value!
		List<Integer> regions = new LinkedList<Integer>();
		regions.add(90);
		regions.add(75);
		regions.add(0);
		
		/*
		// check which app is the most critical problem
		App criticalApp = null;
		int criticalAppCapacity = 0;
		*/
		
		Map<App, Integer> appCpuRegions = new HashMap<App, Integer>();
		Map<App, Integer> appMemoryRegions = new HashMap<App, Integer>();
		Map<App, Integer> appStorageRegions = new HashMap<App, Integer>();
		for (App app : apps) {
			// cpu
			// 100% of SLA = app.getCpu()
			// % of usage, compared to PM = app.getCurrentCpuUsage()
			// percentage of usage, compared to the SLA
			int cpuPercentage = app.getCurrentCpuUsage() / app.getCpu() * 100;
			for (Integer region : regions) {
				if (cpuPercentage >= region) {
					// found the matching region
					appCpuRegions.put(app, region);
					
					break;
				}
			}
			
			// memory
			int memoryPercentage = app.getCurrentMemoryUsage() / app.getMemory() * 100;
			for (Integer region : regions) {
				if (memoryPercentage >= region) {
					// found matching region
					appMemoryRegions.put(app, region);
					
					break;
				}
			}
			
			// storage
			int storagePercentage = app.getCurrentStorageUsage() / app.getStorage() * 100;
			for (Integer region : regions) {
				if (storagePercentage >= region) {
					// found matching region
					appMemoryRegions.put(app, region);
					
					break;
				}
			}
		}
		
		// Decide what the top problem is
		Problem problem = null;
		int topRegion = regions.get(0);
		
		// search for a critical CPU problem (top region)
		Iterator<App> i = appCpuRegions.keySet().iterator();
		while (i.hasNext()) {
			App app = i.next();
			if (appCpuRegions.get(app) == topRegion) {
				problem = app;
				
				break;
			}
		}
		
		if (problem == null) {
			// search for a critical memory problem (top region)
			i = appMemoryRegions.keySet().iterator();
			while (i.hasNext()) {
				App app = i.next();
				if (appMemoryRegions.get(app) == topRegion) {
					problem = app;
					
					break;
				}
			}
		}
		
		if (problem == null) {
			// search for a critical storage problem (top region)
			i = appStorageRegions.keySet().iterator();
			while (i.hasNext()) {
				App app = i.next();
				if (appStorageRegions.get(app) == topRegion) {
					problem = app;
					
					break;
				}
			}
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
			// rank CPU, memory and storage according to their regions
			
			
			if (problem == null) {
				// search for a CPU problem
				// TODO
			}
			
			if (problem == null) {
				// search for a memory problem
				// TODO
			}
			
			if (problem == null) {
				// search for a storage problem
				// TODO
			}
		}
		
		return null;
	}
}

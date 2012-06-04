package at.ac.tuwien.lsdc.mape;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.generator.Request;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class Analyser {
	public Resource getTopProblem() {
		String problemType="";
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
			problem = criticalApp;
			problem.setProblemType("slaViolation");
		}
		
		if (problem == null) {
			// Check if there are one or more requests in the queue
			List<Request> requests = RequestGenerator.getInstance().getPendingRequests();
			
			if (requests.size() > 0) {
				// take first request and define it as the top problem
				problem = requests.get(0).createApp();
				problem.setProblemType("newApp");
			}
			
		}
		
		int lowUsageBenchmark = 10;
		//look for PMs that are not really used
		if (problem == null) {
			for (PhysicalMachine pm : Monitor.getInstance().getPms()) {
				if (pm.isRunning() && pm.getCurrentCpuUsage()< lowUsageBenchmark && pm.getCurrentMemoryUsage()< lowUsageBenchmark && pm.getCurrentStorageUsage()< lowUsageBenchmark) {
					if (problem == null) {
						problem = pm; 
						problem.setProblemType("wasteOfResources");
					}
					else if ( (problem.getCurrentCpuUsage()+problem.getCurrentMemoryUsage()+ problem.getCurrentStorageUsage()> pm.getCurrentCpuUsage()+pm.getCurrentMemoryUsage()+pm.getCurrentStorageUsage())) {
						problem = pm; 
						problem.setProblemType("wasteOfResources");
					}
				}
			}
		}
		
		//look for VMs that are over-allocating
		double lowUsagePercentage = 0.2;
		if(problem == null) {
			for (PhysicalMachine pm : Monitor.getInstance().getPms()) {
				if (pm.isRunning()){
					for (VirtualMachine vm: pm.getVms()) {
						if (vm.getCurrentCpuUsage()/vm.getCurrentCpuAllocation()<lowUsagePercentage || vm.getCurrentMemoryUsage()/vm.getCurrentMemoryAllocation()<lowUsagePercentage || vm.getCurrentStorageUsage()/vm.getCurrentStorageAllocation()<lowUsagePercentage) {
							if (problem==null){
								problem = vm;
								problem.setProblemType("wasteOfResources");
							}
							else if (vm.getCurrentCpuUsage()/vm.getCurrentCpuAllocation()+vm.getCurrentMemoryUsage()/vm.getCurrentMemoryAllocation()+vm.getCurrentStorageUsage()/vm.getCurrentStorageAllocation() < problem.getCurrentCpuUsage()/problem.getCurrentCpuAllocation()+problem.getCurrentMemoryUsage()/problem.getCurrentMemoryAllocation()+problem.getCurrentStorageUsage()/problem.getCurrentStorageAllocation()) {
								problem = vm;
								problem.setProblemType("wasteOfResources");
							}
						}
					}
				}
			}
		}
		
		
	   if (problem == null) {
			// take the top problem if there is one that's not in the "green" region
			if (criticalAppPercentage >= bottomRegion) {
				problemType = "MediumCriticalApp";
				problem = criticalApp;
				problem.setProblemType("slaViolation");
			}
		}
		if(problem!=null){  
			Monitor.getInstance().logAnalysis(problem, problem.getProblemType());
		}
		
		return problem;
	}
}

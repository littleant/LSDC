package at.ac.tuwien.lsdc.actions;


import java.util.LinkedList;

import weka.classifiers.trees.J48;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.mape.Monitor;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class CreateVmInsertApp extends Action {
	private static Instances knowledgeBase = null;
	private App app;
	private static int vmStartupCosts = 10;
	private static int pmStartupCosts = 20;
	
	private PhysicalMachine selectedPm = null;
	private boolean preconditionsOk = false;
	private int costs = 0;
	private int waitForEvaluation = 10;
	
	public static Instances getKnowledgeBase() {
		if (knowledgeBase ==null ) {
			try {
				CreateVmInsertApp.knowledgeBase = Action.loadKnowledge(Configuration.getInstance().getKBCreateVmInsertApp());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return knowledgeBase;
	}
	
	@Override
	public int predict() {
		// WEKA inputs: PM resource allocations, App SLAs

		Instances data = CreateVmInsertApp.getKnowledgeBase();
		
		J48 tree = new J48();         // new instance of tree
		try {
			tree.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO: use WEKA to classify
		
		return 100;
	}

	@Override
	public int estimate() {
		return costs;
	}

	@Override
	public boolean preconditions() {
		return preconditionsOk;
	}
	
	@Override
	public double evaluate() {
		if (waitForEvaluation>0) {
			waitForEvaluation--;
		}
		else {
			LinkedList<Integer> cpuallhist = selectedPm.getCpuAllocationHistory(20);
			LinkedList<Integer> cpuusagehist = selectedPm.getCpuUsageHistory(20);	
			int beforeInsertionCount = cpuallhist.size()-10;	
			
			double cpuratio = calculateAllocationUsageRatio(cpuallhist, cpuusagehist, beforeInsertionCount);
			
			LinkedList<Integer> memallhist = selectedPm.getMemoryAllocationHistory(20);
			LinkedList<Integer> memusagehist = selectedPm.getMemoryUsageHistory(20);	
				
			double memoryratio = calculateAllocationUsageRatio(memallhist, memusagehist, beforeInsertionCount);
				
			LinkedList<Integer> storageallhist = selectedPm.getStorageAllocationHistory(20);
			LinkedList<Integer> storageusagehist = selectedPm.getStorageUsageHistory(20);	
				
			double storageratio = calculateAllocationUsageRatio(storageallhist, storageusagehist, beforeInsertionCount);
			
			return cpuratio+memoryratio+storageratio;
			
		}
		return 0;
	}
	
	private double calculateAllocationUsageRatio(LinkedList<Integer> allocation, LinkedList<Integer> usage, int beforeInsertionCount) {
		
		
		
		double ratioBefore =0;
		double ratioAfter = 0;
		for(int i=0; i<allocation.size();i++){ 
			if(i<beforeInsertionCount) {
				ratioBefore += usage.get(i)/allocation.get(i);
			}
			else {
				ratioAfter += usage.get(i)/allocation.get(i);
			}
		}
		return (ratioAfter/10) - (ratioBefore/Math.max(1, beforeInsertionCount));
	}

	
	
	//calculate if an App fits to a pm
	//TODO: gst: use WEKA to calc fit factor!!
	private int calculateFit(App app2, PhysicalMachine pm) {
		int cpuFit = pm.getCurrentCpuAllocation()-app2.getCpu();
		int memFit = pm.getCurrentMemoryAllocation()-app2.getMemory();
		//int storageFit = pm.getCurrentStorageAllocation()-app2.getStorage();
		
		return (100-cpuFit)+(100-memFit);
	}

	@Override
	public void execute() {
		VirtualMachine oldVm = null;
		PhysicalMachine oldPm = null;
		
		//if the App existed before
		if (app.getVm()!=null) {
			oldVm = app.getVm();
			oldPm = oldVm.getPm();
		}
		
		//remove the app from the request- queue??? 
		if(app.getOriginalRequest()!=null) {
			RequestGenerator.getInstance().removeRequestFromQueue(app.getOriginalRequest());
		}
		
		if (selectedPm.isRunning()==false) {
			selectedPm.startMachine();
		}
		
		//TODO: gst: hard - coded startup value!!
		VirtualMachine vm = selectedPm.createNewVm(app.getCpu(), app.getMemory(), app.getStorage(), 10);
		vm.createApp(this.app);
		
		if(oldVm!=null) {
			oldVm.getApps().remove(app);
			//TODO: gst: what happens if the VM is now empty??
			if(oldVm.getApps().size()==0) {
				oldVm.terminate();
			}
		}
	}

	@Override
	public void init(Resource problemApp) {
		this.preconditionsOk=false;
		this.selectedPm=null;
		this.costs=0;
		this.app=null;
		
		CreateVmInsertApp.vmStartupCosts = Configuration.getInstance().getVmStartupCosts();
		CreateVmInsertApp.pmStartupCosts = Configuration.getInstance().getPmStartupCosts();
		
		if (problemApp instanceof App){
			app = (App)problemApp;
			boolean found = false;
			int fitFactor = 0;
			
			for (PhysicalMachine pm : Monitor.getInstance().getPms()) {
				if(pm.isRunning())  {
					if((100-pm.getCurrentCpuAllocation())>=app.getCpu() && (100-pm.getCurrentMemoryAllocation())>=app.getMemory() && (100-pm.getCurrentCpuAllocation())>app.getStorage()) {
						found=true;
						costs = vmStartupCosts;
						
						if(selectedPm == null || calculateFit(app, pm)>fitFactor) {
							selectedPm = pm;
						}
					}
				}
			}
		
		
			//no running machine found => search for a stopped machine
			//TODO: gst: replace by real action
			if (found ==false ) {
				//Start a new machine
			
				for(PhysicalMachine pm : Monitor.getInstance().getPms()){
					if (pm.isRunning()==false) {
						found=true;
						costs += pmStartupCosts;
						selectedPm = pm;
						break;
					}
					
				}
			}
			preconditionsOk = found;
		}
		else {
			preconditionsOk= false;
		}
	}

}

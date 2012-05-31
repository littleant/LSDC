package at.ac.tuwien.lsdc.actions;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.mape.Monitor;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class CreateVmInsertApp extends Action {
	private static Instances knowledgeBase = null;
	private static Classifier classifier = null;
	private static Evaluation evaluation = null;
	private static RandomData randomData = new RandomDataImpl();
	
	private App app;
	private static int vmStartupCosts = 10;
	private static int pmStartupCosts = 20;
	
	private PhysicalMachine selectedPm = null;
	private boolean preconditionsOk = false;
	private int costs = 0;
	private int waitForEvaluation = 10;
	private Instance curInstance;
	private int prediction;
	
	
	public static Instances getKnowledgeBase() {
		if (knowledgeBase ==null ) {
			try {
				//load knowledgebase from file
				CreateVmInsertApp.knowledgeBase = Action.loadKnowledge(Configuration.getInstance().getKBCreateVmInsertApp());
				
				//prediction is also performed therefore the classifier and the evaluator must be instantiated
				if(!isOnlyLearning()) {
					classifier = new MultilayerPerceptron();
					classifier.buildClassifier(CreateVmInsertApp.getKnowledgeBase());
					evaluation = new Evaluation(CreateVmInsertApp.getKnowledgeBase());
					evaluation.crossValidateModel(classifier, knowledgeBase, 10, knowledgeBase.getRandomNumberGenerator(randomData.nextLong(1, 1000)));
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return knowledgeBase;
	}
	
	public void terminate() {
		try {
			Action.saveKnowledge(Configuration.getInstance().getKBCreateVmInsertApp(), CreateVmInsertApp.getKnowledgeBase());
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	@Override
	public int predict() {
		int output = 0;
		if (isOnlyLearning()) { //Randomized predictions for learning
			return randomData.nextInt(0, 100);
		}
		else { //Use WEKA - evaluation for prediction
			//Create new WEKA - instance
			return prediction; 
			
		}			
		
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
	public boolean evaluate() {
		if (curInstance == null) {
			curInstance = createInstance(0); // create a Instance with the past values
		}
		
		if (app.getSuspendedTicks()>0 || app.getVm().getSuspendedTicks()>0 || app.getVm().getPm().getSuspendedTicks()>0) {
			return false;
		}
		else if (waitForEvaluation>0) {
			waitForEvaluation--;
			return false;
		}
		else {
			System.out.println("APP - Running Ticks: " + app.getRunningTicks());
			LinkedList<Integer> cpuusagehist = selectedPm.getCpuUsageHistory(10);	
			
			LinkedList<Integer> memusagehist = selectedPm.getMemoryUsageHistory(10);	
				
			LinkedList<Integer> storageusagehist = selectedPm.getStorageUsageHistory(10);	

			double evaluation =(255- calculateUsageRatio(cpuusagehist, 85) - calculateUsageRatio(memusagehist, 85) - calculateUsageRatio(storageusagehist, 85))/255 ;
			
			//subtract SLA Violations
			evaluation -= (app.getCpuSlaErrorcount()+app.getMemorySlaErrorcount()+app.getStorageSlaErrorcount())/10;
			
			//minimum of 0
			evaluation = Math.max(0, evaluation);
			
			curInstance.setValue(getKnowledgeBase().attribute(63), evaluation);
			getKnowledgeBase().add(curInstance);
		}
		return true;
	}
	
	private int clusterValue (int value) {
		return (int)(Math.ceil(value/10)*10);
	}
	
	private double calculateUsageRatio(LinkedList<Integer> usage, int goal) {
		double result = 0;
		for(int i=0; i<usage.size();i++){ 
			result += Math.abs(85-usage.get(i))/usage.size();
		}
		return result;
	}

	//calculate if an App fits to a pm
	//TODO: gst: use WEKA to calc fit factor!!
	private int calculateFit(App app2, PhysicalMachine pm) {
		int output = 0;
		if (Action.isOnlyLearning()== false ){
			//is space available
			if (app2.getCpu()< (100-pm.getCurrentCpuAllocation()) && app2.getMemory() < (100-pm.getCurrentMemoryAllocation()) && app2.getStorage() < (100-pm.getCurrentCpuAllocation())) {		
				Instance instance = createInstance(Instance.missingValue());
				instance.setDataset(CreateAppInsertIntoVm.getKnowledgeBase());
				
				try {
					output = (int) (evaluation.evaluateModelOnce(classifier, instance) *100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			if (app2.getCpu()< (100-pm.getCurrentCpuAllocation()) && app2.getMemory() < (100-pm.getCurrentMemoryAllocation()) && app2.getStorage() < (100-pm.getCurrentCpuAllocation())) {
				return randomData.nextInt(1, 100);
			}
		}
		return output;
	}

	@Override
	public void execute() {
		VirtualMachine oldVm = null;
		
		//if the App existed before
		if (app.getVm()!=null) {
			oldVm = app.getVm();
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
			int curFit = 0;
			
			for (PhysicalMachine pm : Monitor.getInstance().getPms()) {
				if(pm.isRunning())  {
					if((100-pm.getCurrentCpuAllocation())>=app.getCpu() && (100-pm.getCurrentMemoryAllocation())>=app.getMemory() && (100-pm.getCurrentCpuAllocation())>app.getStorage()) {
						found=true;
						costs = vmStartupCosts;
						curFit = calculateFit(app, pm);
						if(selectedPm == null || curFit>fitFactor) {
							selectedPm = pm;
							prediction = curFit;
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
						prediction  =calculateFit(app, pm);
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
	
	
	private Instance createInstance(double eval) {
		Instance instance = new Instance(64);
		
		LinkedList<Integer> cpuallhist = selectedPm.getCpuAllocationHistory(10);
		LinkedList<Integer> cpuusehist = selectedPm.getCpuUsageHistory(10);

		LinkedList<Integer> memallhist = selectedPm.getMemoryAllocationHistory(10);
		LinkedList<Integer> memusehist = selectedPm.getMemoryUsageHistory(10);
		
			
		LinkedList<Integer> storageallhist = selectedPm.getStorageAllocationHistory(10);
		LinkedList<Integer> storageusehist = selectedPm.getStorageUsageHistory(10);
		
		//CPU/Memory/Storage - Allocation history before the new vm was created
		for (int i = 0; i<10;i++) {
				//cpu allocation
				instance.setValue(getKnowledgeBase().attribute(i), clusterValue(cpuallhist.get(i)));
				//cpu usage
				instance.setValue(getKnowledgeBase().attribute(i+10), clusterValue(cpuusehist.get(i)));
				
				//memory allocation
				instance.setValue(getKnowledgeBase().attribute(i+20),clusterValue( memallhist.get(i)));
				//memory usage
				instance.setValue(getKnowledgeBase().attribute(i+30),clusterValue( memusehist.get(i)));
				
				//storage allocation
				instance.setValue(getKnowledgeBase().attribute(i+40), clusterValue(storageallhist.get(i)));
				//storage usage 
				instance.setValue(getKnowledgeBase().attribute(i+50),clusterValue( storageusehist.get(i)));
		}
		
		//SLAs
		//CPU
		instance.setValue(getKnowledgeBase().attribute(60), clusterValue(app.getCpu()));
		//Memory
		instance.setValue(getKnowledgeBase().attribute(61),clusterValue(app.getMemory()));
		//Storage
		instance.setValue(getKnowledgeBase().attribute(62), clusterValue(app.getStorage()));
		
		//Evaluation
		instance.setValue(getKnowledgeBase().attribute(63), eval);

		return instance;
	}

}

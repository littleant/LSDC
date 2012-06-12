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
	public static Instances knowledgeBase = null;
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
				System.out.println ("Knowledge: " + knowledgeBase.numAttributes());
				
				//prediction is also performed therefore the classifier and the evaluator must be instantiated
				if(!isOnlyLearning()) {
					if(knowledgeBase.numInstances()>0){
						System.out.println("Classify1 data CreateVMInsertApp");
						classifier = new MultilayerPerceptron();
						System.out.println("Classify2 data CreateVMInsertApp");
						classifier.buildClassifier(CreateVmInsertApp.knowledgeBase);
						
						System.out.println("Classify3 data CreateVMInsertApp");
						evaluation = new Evaluation(CreateVmInsertApp.knowledgeBase);
						System.out.println("Classify4 data CreateVMInsertApp");
						evaluation.crossValidateModel(classifier, CreateVmInsertApp.knowledgeBase, 10, knowledgeBase.getRandomNumberGenerator(randomData.nextLong(1, 1000)));
						System.out.println("Classified data CreateVMInsertApp");
					}
					else{
						System.out.println ("No Instancedata for classifier CreateVMInsertApp" );
					}
					
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
			curInstance = createInstance(0, selectedPm); // create a Instance with the past values
		}
		
		if (app.getSuspendedTicks()>0 || app.getVm().getSuspendedTicks()>0 || app.getVm().getPm().getSuspendedTicks()>0) {
			//System.out.println("Wait for SUSPEND");
			return false;
		}
		else if (waitForEvaluation>0) {
			//System.out.println("Wait for EVAL");
			waitForEvaluation--;
			return false;
		}
		else {
			//System.out.println("APP - Running Ticks: " + app.getRunningTicks());
			LinkedList<Integer> cpuusagehist = selectedPm.getCpuUsageHistory(10);	
			
			LinkedList<Integer> memusagehist = selectedPm.getMemoryUsageHistory(10);	
				
			LinkedList<Integer> storageusagehist = selectedPm.getStorageUsageHistory(10);	

			double evaluation =(255- calculateUsageRatio(cpuusagehist, 85) - calculateUsageRatio(memusagehist, 85) - calculateUsageRatio(storageusagehist, 85))/255 ;
			
			//subtract SLA Violations
			evaluation -= (app.getCpuSlaErrorcount()+app.getMemorySlaErrorcount()+app.getStorageSlaErrorcount())/10;
			
			//minimum of 0
			//evaluation = Math.max(0, evaluation);
			Monitor.getInstance().logExecution(app, this, evaluation, this.globalTickExecution);
			curInstance.setValue(getKnowledgeBase().attribute(21), evaluation);
			this.setLocalEvaluation(evaluation);
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
	private int calculateFit(App app2, PhysicalMachine pm) {
		int output = 0;
		if (Action.isOnlyLearning()== false ){
			//is space available
			if (app2.getCpu()< (100-pm.getCurrentCpuAllocation()) && app2.getMemory() < (100-pm.getCurrentMemoryAllocation()) && app2.getStorage() < (100-pm.getCurrentCpuAllocation())) {
				Instance instance = createInstance(Instance.missingValue(), pm);
				instance.setDataset(CreateVmInsertApp.getKnowledgeBase());
				System.out.println ("Instance size: " + instance.numValues() + ", " + classifier);
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
		globalTickExecution = Monitor.getInstance().getGlobalTicks();
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
		VirtualMachine vm = selectedPm.createNewVm(app.getCpu(), app.getMemory(), app.getStorage(), Configuration.getInstance().getVmStartupCosts());
		/*System.out.println (Monitor.getInstance().getGlobalTicks() + " Creating VM: "+ app.getCpu()+", "+ app.getMemory()+", " + app.getStorage()+ ", " + Configuration.getInstance().getVmStartupCosts());
		System.out.println (Monitor.getInstance().getGlobalTicks() + " VM: " + vm.getCurrentCpuAllocation() +", "+ vm.getCurrentMemoryAllocation() + ", " + vm.getCurrentStorageAllocation());
		System.out.println (Monitor.getInstance().getGlobalTicks() + " VM: " + vm.getCpuOverhead() +", "+ vm.getMemoryOverhead() + ", " + vm.getStorageOverhead());
		*/
		vm.createApp(this.app);
		vm.setActionLock(costs);
		
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
		this.setProblemResource(problemApp);
		this.setProblemType(problemApp.getProblemType());
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
					if((100-pm.getCurrentCpuAllocation())>=app.getCpu() && (100-pm.getCurrentMemoryAllocation())>=app.getMemory() && (100-pm.getCurrentStorageAllocation())>app.getStorage()) {
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
	
	
	private Instance createInstance(double eval, PhysicalMachine pm) {
		Instance instance = new Instance(22);
		
		LinkedList<Integer> cpuallhist = pm.getCpuAllocationHistory(3);
		LinkedList<Integer> cpuusehist = pm.getCpuUsageHistory(3);

		LinkedList<Integer> memallhist = pm.getMemoryAllocationHistory(3);
		LinkedList<Integer> memusehist = pm.getMemoryUsageHistory(3);
		
			
		LinkedList<Integer> storageallhist = pm.getStorageAllocationHistory(3);
		LinkedList<Integer> storageusehist = pm.getStorageUsageHistory(3);
		
		//CPU/Memory/Storage - Allocation history before the new vm was created
		for (int i = 0; i<3;i++) {
				//cpu allocation
				instance.setValue(getKnowledgeBase().attribute(i), clusterValue(cpuallhist.get(i)));
				//cpu usage
				instance.setValue(getKnowledgeBase().attribute(i+3), clusterValue(cpuusehist.get(i)));
				
				//memory allocation
				instance.setValue(getKnowledgeBase().attribute(i+6),clusterValue( memallhist.get(i)));
				//memory usage
				instance.setValue(getKnowledgeBase().attribute(i+9),clusterValue( memusehist.get(i)));
				
				//storage allocation
				instance.setValue(getKnowledgeBase().attribute(i+12), clusterValue(storageallhist.get(i)));
				//storage usage 
				instance.setValue(getKnowledgeBase().attribute(i+15),clusterValue( storageusehist.get(i)));
		}
		
		//SLAs
		//CPU
		instance.setValue(getKnowledgeBase().attribute(18), clusterValue(app.getCpu()));
		//Memory
		instance.setValue(getKnowledgeBase().attribute(19),clusterValue(app.getMemory()));
		//Storage
		instance.setValue(getKnowledgeBase().attribute(20), clusterValue(app.getStorage()));
		
		//Evaluation
		instance.setValue(getKnowledgeBase().attribute(21), eval);

		return instance;
	}

	public PhysicalMachine getSelectedPm() {
		return this.selectedPm;
	}
}

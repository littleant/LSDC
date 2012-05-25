package at.ac.tuwien.lsdc.actions;

import at.ac.tuwien.lsdc.mape.Monitor;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class CreateVmInsertApp extends Action {
	private App app;
	//TODO: gst: replace by config file
	private static final int VMSTARTUPCOSTS = 10;
	private static final int PMSTARTUPCOSTS =20;
	
	private VirtualMachine selectedVm;
	private PhysicalMachine selectedPm = null;
	private boolean preconditionsOk = false;
	private int costs = 0;
	
	
	@Override
	public int predict() {
		// TODO: gst: Implement using WEKA
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
		if (app.getVm()!=null) {
			oldVm = app.getVm();
			oldPm = oldVm.getPm();
		}
		
		if (selectedPm.isRunning()!=false) {
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
		if (problemApp instanceof App){
			app = (App)problemApp;
			boolean found = false;
			int fitFactor = 0;
			
			for (PhysicalMachine pm : Monitor.getInstance().getPms()) {
				if(pm.isRunning())  {
					if((100-pm.getCurrentCpuAllocation())>=app.getCpu() && (100-pm.getCurrentMemoryAllocation())>=app.getMemory() && (100-pm.getCurrentCpuAllocation())>app.getStorage()) {
						found=true;
						costs = VMSTARTUPCOSTS;
						
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
						costs += PMSTARTUPCOSTS;
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

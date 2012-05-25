package at.ac.tuwien.lsdc.mape;

import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;

public class Monitor {
	private List<PhysicalMachine> pms = new LinkedList<PhysicalMachine>();
	
	static volatile Monitor instance;
	
	private Monitor() {
		// do nothing
	}
	
	public static Monitor getInstance() {
		if (Monitor.instance == null) {
			synchronized (Monitor.class) {
				if (Monitor.instance == null) {
					Monitor.instance = new Monitor();
				}
			}
		}
		
		return instance;
	}
	
	public void getNewStati() {
		// increment global tick counter in RequestGenerator
		RequestGenerator.getInstance().nextTick();
		
		// updates PMs, which update VMs, which update Apps
		for (PhysicalMachine pm : this.pms) {
			pm.nextTick();
		}
	}

	public List<PhysicalMachine> getPms() {
		return pms;
	}

	public void setPms(List<PhysicalMachine> pms) {
		this.pms = pms;
	}
	
	public void addPm(PhysicalMachine pm) {
		this.pms.add(pm);
	}
}

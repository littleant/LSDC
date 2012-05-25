package at.ac.tuwien.lsdc.mape;

import java.util.List;

import at.ac.tuwien.lsdc.resources.PhysicalMachine;

public class Monitor {
	private List<PhysicalMachine> pms;
	
	static volatile Monitor instance;
	
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
		// TODO
		
		// updates PMs, which update VMs, which update Apps
		// TODO
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

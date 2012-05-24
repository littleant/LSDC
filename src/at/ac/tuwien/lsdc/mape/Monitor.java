package at.ac.tuwien.lsdc.mape;

import java.util.List;

import at.ac.tuwien.lsdc.resources.PhysicalMachine;

public class Monitor {
	private List<PhysicalMachine> pms;
	
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

package at.ac.tuwien.lsdc;

import at.ac.tuwien.lsdc.generator.Request;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.mape.Monitor;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;

public class Lsdc {
	public static void main(String[] args) {
		// add a request to request queue
		Request request = RequestGenerator.getInstance().generateRequest();
		RequestGenerator.getInstance().addRequest(request);
		
		// add new physical machines to our simulation
		Integer pmCount = Configuration.getInstance().getMaxPms();
		for (int i = 0; i < pmCount; i++) {
			Monitor.getInstance().addPm(new PhysicalMachine());
		}
		
		// start mape-loop and simulation
		// TODO
	}
}

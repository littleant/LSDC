package at.ac.tuwien.lsdc.mape;

public class SchedulingAgent {
	private static volatile SchedulingAgent instance;
	
	private SchedulingAgent() {
		// do nothing
	}
	
	public static SchedulingAgent getInstance() {
		if (SchedulingAgent.instance == null) {
			synchronized (SchedulingAgent.class) {
				if (SchedulingAgent.instance == null) {
					SchedulingAgent.instance = new SchedulingAgent();
				}
			}
		}
		
		return SchedulingAgent.instance;
	}
	
	public void start() {
		// TODO: start simulation and scheduling
	}
}

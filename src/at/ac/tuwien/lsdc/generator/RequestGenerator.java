package at.ac.tuwien.lsdc.generator;

import java.util.List;

public class RequestGenerator {
	static volatile RequestGenerator instance;
	
	public static RequestGenerator getInstance() {
		if (RequestGenerator.instance == null) {
			synchronized(RequestGenerator.class) {
				if (RequestGenerator.instance == null) {
					RequestGenerator.instance = new RequestGenerator();
				}
			}
		}
		
		return RequestGenerator.instance;
	}
	
	private List<Request> requests;

	public List<Request> getRequests() {
		return requests;
	}

	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}
	
	public void addRequest(Request request) {
		this.requests.add(request);
	}
}

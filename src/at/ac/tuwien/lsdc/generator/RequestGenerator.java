package at.ac.tuwien.lsdc.generator;

import java.util.List;

public class RequestGenerator {
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

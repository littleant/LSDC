package at.ac.tuwien.lsdc.generator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RequestGenerator {
	static volatile RequestGenerator instance;
	
	int ticks;
	List<Integer> cpu;
	List<Integer> memory;
	List<Integer> storage;
	List<Integer> slas;
	
		
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
	
	
	public void generateRequest() {

		FileReader input = null;
		try {
			input = new FileReader("input.csv");
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
			e.printStackTrace();
		}
		BufferedReader bufRead = new BufferedReader(input);
		String line = null;
		try {
			line = bufRead.readLine();
		} catch (IOException e) {
			System.out.println("Exception while reading");
			e.printStackTrace();
		}

		String[] values = line.split(";");
		System.out.println("Size of the values " + values.length);
		
		cpu = new ArrayList<Integer>();
		memory = new ArrayList<Integer>();
		storage = new ArrayList<Integer>();
		slas = new ArrayList<Integer>();
		
		
		int i;
		for (i = 1; i < 4;i++)
		slas.add(Integer.parseInt(values[i]));
		System.out.println("Size of the slas " + this.slas.size());
		
		for (; i < Integer.parseInt(values[0])+4;i++)
		cpu.add(Integer.parseInt(values[i]));
		System.out.println("Size of the cpus " + this.cpu.size());
		
		for (; i < 2*Integer.parseInt(values[0])+4;i++)
		memory.add(Integer.parseInt(values[i]));
		System.out.println("Size of the memory " + this.memory.size());
		
		
		for (; i < 3*Integer.parseInt(values[0])+4;i++)
		storage.add(Integer.parseInt(values[i]));
		System.out.println("Size of the storage " + this.storage.size());
		

	}
	
	@Override
	public String toString(){
		
		String request= "SLAS: ";

		for (int i =0;i <this.slas.size();i++)
			request +=slas.get(i)+";";

		request += "\nCpus: ";
		for (int i =0;i <this.cpu.size();i++)
			request +=cpu.get(i)+";";
		request += "\nMemory: ";
		for (int i =0;i <this.memory.size();i++)
			request +=memory.get(i)+";";
		request += "\nStorage: ";
		for (int i =0;i <this.storage.size();i++)
			request +=storage.get(i)+";";
		
		
		return request;
		
	}

}

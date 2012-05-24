package at.ac.tuwien.lsdc.csvGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import at.ac.tuwien.lsdc.generator.RequestGenerator;

public class CsvGenerator {

	public static void main(String args[]) throws IOException {

		File f;
		f = new File("input.csv");
		if (!f.exists()) {
			f.createNewFile();
			System.out
					.println("New file \"input.txt\" has been created to the current directory");
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("input.csv"));
			RandomData randomData = new RandomDataImpl();

			// Runtime
			int value = randomData.nextInt(1, 100);
			out.write(value + ";");
			// Slas
			for (int i = 0; i < 3; i++)
				out.write(randomData.nextInt(1, 100) + ";");
			// Cpu
			for (int i = 0; i < value; i++)
				out.write(randomData.nextInt(1, 100) + ";");
			// Memory
			for (int i = 0; i < value; i++)
				out.write(randomData.nextInt(1, 100) + ";");
			// Storage
			for (int i = 0; i < value; i++)
				out.write(randomData.nextInt(1, 100) + ";");

			out.close();
		} catch (IOException e) {
			System.out.println("Excption while writing");
			
		}
		
		RequestGenerator rg = RequestGenerator.getInstance();
		rg.generateRequest();
		System.out.println(rg.toString());

	}

}

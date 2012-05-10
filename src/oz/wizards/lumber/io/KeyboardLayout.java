package oz.wizards.lumber.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

public class KeyboardLayout {
	private Map<String, Integer> mapping = new HashMap<String, Integer>();
	private String filename = "keyboardlayout.txt";
	
	public KeyboardLayout () {
	}

	public KeyboardLayout(String filename) {
		this.filename = filename;
		
		if(new File(filename).exists())
			loadFromFile();
		else
			loadDefaultMapping();
	}

	public void add(String name, int v) {
		mapping.put(name, v);
	}

	public int get(String name) {
		return mapping.get(name);
	}
	
	public void save (String filename) {
		this.filename = filename;
		saveToFile();
	}
	
	private void loadDefaultMapping () {
		this.add("forward", Keyboard.KEY_W);
		this.add("reset", Keyboard.KEY_R);
	}

	private void loadFromFile() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line = "";
			try {
				while ((line = br.readLine()) != null) {
					if (line.startsWith("#"))
						continue;
					String value, key;
					key = line.substring(0, line.indexOf(" = "));
					value = line.substring(line.indexOf(" = ") + 3);
					mapping.put(key, Integer.getInteger(value));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void saveToFile() {
		String outputstr = "# Warning, machine generated.\n";
		for (Map.Entry<String, Integer> e : mapping.entrySet()) {
			outputstr += e.getKey() + " = " + String.valueOf(e.getValue())
					+ "\n";
		}
		outputstr += "# EOF \n";

		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(filename, false));
			bw.write(outputstr);
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}

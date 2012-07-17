package oz.wizards.lumber.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Log {
	static boolean logToFile = false;
	static File file = null;
	static BufferedWriter bw = null;
	static private PrintStream stdSystemOut = null;
	
	public static void enableFileOutput (String logName) {
		stdSystemOut = System.out;
		file = new File("./" + logName + System.currentTimeMillis() + ".log");
		
		try {
			bw = new BufferedWriter(new FileWriter(file));
			logToFile = true;
			
			Log.print(System.getProperty("java.version") + " " + System.getProperty("java.vendor") + " " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version") + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
				PrintStream ps = new PrintStream(new OutputStream() {
					@Override
					public void write(int b) throws IOException {
						Log.output((char)b);
					}
				});
				System.setErr(ps);
				System.setOut(ps);
	}
	
	
	public static void print (String input) {
		Log.output(input);
	}
	
	public static void println (String input) {
		Log.output(input + "\n");
	}
	
	public static void printf (String format, Object ...objects) {
		Log.output(String.format(format, objects));
	}
	
	static void output (char out) {
		if(logToFile) {
			try {
				bw.append(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stdSystemOut.print(out);
	}
	
	static void output (String out) {
		if(logToFile) {
			try {
				bw.append("[" + System.currentTimeMillis() + "] " + out.subSequence(0, out.length()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stdSystemOut.print("[" + System.currentTimeMillis() + "] " + out);
	}
	
	public static void close () {
		if(bw != null) {
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

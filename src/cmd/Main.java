package cmd;
//Larping Studio by ViveTheJoestar
import java.io.File;
import java.io.IOException;

import gui.Program;

public class Main {
	public static String chooseOptionFromArg(Bt4CharaPak pak, String arg) throws IOException {
		boolean isSuccessful = false;
		String operation = "";
		long start = System.currentTimeMillis();
		if (arg.equals("-u") || arg.equals("-ua")) {
			operation = "Unpacking";
			isSuccessful = pak.unpack();
		}
		else if (arg.equals("-r") || arg.equals("-ra")) {
			operation = "Repacking";
			isSuccessful = pak.repack();
		}
		else {
			System.out.println("ERROR: Invalid argument provided!");
			System.exit(3);
		}
		long end = System.currentTimeMillis();
		if (isSuccessful) return "SUCCESS: " + operation + " " + pak.getName() + " complete in " + (end - start) / 1000.0 + " seconds!";
		return null;
	}
	public static String chooseOptionFromArg(Bt4CharaPck pck, String arg) throws IOException {
		boolean isSuccessful = false;
		String operation = "";
		long start = System.currentTimeMillis();
		if (arg.equals("-u")) {
			operation = "Unpacking";
			isSuccessful = pck.split();
		}
		else if (arg.equals("-r")) {
			operation = "Repacking";
			isSuccessful = pck.merge();
		}
		else {
			System.out.println("ERROR: Invalid argument provided!");
			System.exit(3);
		}
		long end = System.currentTimeMillis();
		if (isSuccessful) return "SUCCESS: " + operation + " " + pck.getName() + " complete in " + (end - start) / 1000.0 + " seconds!";
		return null;
	}
	public static void main(String[] args) {
		try {
			String helpText = "USAGE: java -jar larping-studio.jar [option] \"path/to/pak/file/or/folder\"\n";
			helpText += "Replace [option] with one of the following:\n";
			helpText += "-u -> Unpack, -ua -> Unpack All, -r -> Repack, -ra -> Repack All\n";
			if (args.length > 1) {
				File directory = new File(args[1]);
				if (directory.isFile()) {
					Bt4CharaPak pak = new Bt4CharaPak(directory);
					if (pak.isValid()) {
						String msg = chooseOptionFromArg(pak, args[0]);
						if (msg != null) System.out.println(msg);
					}
					else if (directory.getName().toLowerCase().endsWith(".pck")) {
						Bt4CharaPck pck = new Bt4CharaPck(directory);
						String msg = chooseOptionFromArg(pck, args[0]);
						if (msg != null) System.out.println(msg);
					}
					else {
						System.out.println("ERROR: Provided file is NOT a valid character costume (PAK) or parameter container (PCK) file!");
						System.exit(1);
					}
				}
				else if (directory.isDirectory()) {
					File[] pakFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".pak"));
					Bt4CharaPak[] paks = new Bt4CharaPak[pakFiles.length];
					for (int pakCnt = 0; pakCnt < paks.length; pakCnt++) {
						paks[pakCnt] = new Bt4CharaPak(pakFiles[pakCnt]);
						String msg = chooseOptionFromArg(paks[pakCnt], args[0]);
						if (msg != null) System.out.println(msg);
					}
				}
				else {
					System.out.println("ERROR: Provided directory does NOT point to a valid PAK file or a folder with PAK files!");
					System.exit(2);
				}
			}
			else if (args.length > 0) {
				if (args[0].equals("-h")) System.out.println(helpText);
			}
			else Program.run();
		}
		catch (Exception e) {
			System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
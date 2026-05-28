package cmd;
//Larping Studio by ViveTheJoestar
import java.io.File;
import java.io.IOException;

import gui.Program;
import gui.Progress;

public class Main {
	public static double totalTime;
	public static String chooseOptionFromArg(Bt4CharaDump dump, String arg) throws IOException {
		boolean isSuccessful = false;
		long start = System.currentTimeMillis();
		if (arg.equals("-sC")) {
			Progress.fileCnt = 0;
			isSuccessful = dump.splitByColor();
		}
		else if (arg.equals("-sA")) 
			isSuccessful = dump.split(Bt4CharaDump.ALLOWED_FILE_TYPES[0], Bt4CharaDump.ANM_NUM_CONTENTS, Bt4CharaDump.ANM_START_CONTENTS);
		else if (arg.equals("-sP")) isSuccessful = dump.splitByContainer();
		else {
			System.out.println("ERROR: Invalid argument provided!");
			System.exit(3);
		}
		long end = System.currentTimeMillis();
		double time = (end - start) / 1000.0;
		totalTime += time;
		if (isSuccessful) return "SUCCESS: Splitting " + dump.getName() + " complete in " + time + " seconds!";
		return null;
	}
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
		double time = (end - start) / 1000.0;
		totalTime += time;
		if (isSuccessful) return "SUCCESS: " + operation + " " + pak.getName() + " complete in " + time + " seconds!";
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
		double time = (end - start) / 1000.0;
		totalTime += time;
		if (isSuccessful) return "SUCCESS: " + operation + " " + pck.getName() + " complete in " + time + " seconds!";
		return null;
	}
	public static void main(String[] args) {
		try {
			String helpText = "USAGE: java -jar larping-studio.jar [option] \"path/to/pak/file/or/folder\"\n";
			helpText += "Replace [option] with one of the following:\n";
			helpText += "-u -> Unpack, -ua -> Unpack All, -r -> Repack, -ra -> Repack All\n";
			if (args.length > 1) {
				File directory = new File(args[1]);
				String dirName = directory.getName().toLowerCase();
				if (directory.isFile()) {
					Bt4CharaPak pak = new Bt4CharaPak(directory);
					if (dirName.endsWith(".pak")) {
						if (pak.isValid()) {
							String msg = chooseOptionFromArg(pak, args[0]);
							if (msg != null) System.out.println(msg);
						}
					}
					else if (dirName.endsWith(".pck")) {
						Bt4CharaPck pck = new Bt4CharaPck(directory);
						String msg = chooseOptionFromArg(pck, args[0]);
						if (msg != null) System.out.println(msg);
					}
					else if (dirName.endsWith(".dump")) {
						Bt4CharaDump dmp = new Bt4CharaDump(directory);
						String tmpArg = "";
						if (args[0].equals("-u")) {
							if (dirName.contains("costumes")) tmpArg = "-sC";
							else if (dirName.contains("anm")) tmpArg = "-sA";
							else if (dirName.contains("params")) tmpArg = "-sP";
						}
						String msg = chooseOptionFromArg(dmp, tmpArg);
						if (msg != null) System.out.println(msg);
					}
					else {
						System.out.println("ERROR: Provided file is NOT a valid character costume (PAK), parameter container (PCK) or dump file!");
						System.exit(1);
					}
				}
				else if (directory.isDirectory()) {
					File[] pakFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".pak"));
					if (pakFiles.length > 0) {
						Bt4CharaPak[] paks = new Bt4CharaPak[pakFiles.length];
						for (int pakCnt = 0; pakCnt < paks.length; pakCnt++) {
							paks[pakCnt] = new Bt4CharaPak(pakFiles[pakCnt]);
							String msg = chooseOptionFromArg(paks[pakCnt], args[0]);
							if (msg != null) System.out.println("SUCCESS: PAK Folder Operation completed in " + totalTime + " seconds!");
						}
					}
					else {
						File[] pckFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".pck"));
						Bt4CharaPck[] pcks = new Bt4CharaPck[pckFiles.length];
						for (int pckCnt = 0; pckCnt < pcks.length; pckCnt++) {
							pcks[pckCnt] = new Bt4CharaPck(pckFiles[pckCnt]);
							String msg = chooseOptionFromArg(pcks[pckCnt], args[0]);
							if (msg != null) System.out.println("SUCCESS: PCK Folder Operation complete in " + totalTime + " seconds!");
						}
					}
				}
				else {
					System.out.println("ERROR: Provided directory does NOT point to a valid PAK/PCK/dump file or a folder with PAK/PCK/dump files!");
					System.exit(2);
				}
			}
			else if (args.length > 0) {
				if (args[0].equals("-h")) System.out.println(helpText);
			}
			else Program.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
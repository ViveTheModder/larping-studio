package cmd;
//Budokai Tenkaichi 4 Character Dump Class by ViveTheJoestar
//(Dump refers to raw, unorganized data extracted straight from the ISO)
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import gui.Progress;

public class Bt4CharaDump {
	public static final int ANM_NUM_CONTENTS = 440;
	public static final int[] ANM_START_CONTENTS = { 1776, 1792 };
	public static final String[] ALLOWED_FILE_TYPES = { "anm" };
	private File dumpFolder;
	private File costumesCsv;
	private File namesCsv;
	private File parent;
	private RandomAccessFile dump;
	private String name;
	
	public Bt4CharaDump(File f) {
		try {
			dump = new RandomAccessFile(f, "r");
			parent = f.getParentFile();
			name = f.getName();
			dumpFolder = parent.toPath().resolve(name.replace(".dump", "")).toFile();
			costumesCsv = new File("res/chara-costumes.csv");
			namesCsv = new File("res/chara-names.csv");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean split(String fileType, int expectedNumContents, int[] expectedStartContents) throws IOException {
		boolean result = false;
		dumpFolder.mkdir();
		byte[] valBytes = new byte[4];
		int pakCnt = 0, pos = 0;
		String[] names = CsvHandler.getParamNames(namesCsv, false);
		while (pos < dump.length()) {
			dump.seek(pos);
			dump.read(valBytes);
			int numContents = ParamHandler.getVal(valBytes);
			dump.read(valBytes);
			int startContents = ParamHandler.getVal(valBytes);
			dump.seek(pos + ((expectedNumContents + 1) * 4));
			dump.read(valBytes);
			int fileSize = ParamHandler.getVal(valBytes);
			dump.seek(pos);
			boolean startContentsIsValid = false;
			for (int addr: expectedStartContents)
				startContentsIsValid = startContentsIsValid || (addr == startContents);
			if (numContents == expectedNumContents) {
				if (startContentsIsValid) {
					byte[] anmBytes = new byte[fileSize];
					dump.read(anmBytes);
					boolean isOutOfRange = pakCnt >= names.length;
					for (int nameCnt = pakCnt; nameCnt < names.length; nameCnt++) {
						if (names[nameCnt].endsWith("null")) pakCnt++;
						else break;
					}
					String zeroes = "";
					if (pakCnt < 100) zeroes += "0";
					if (pakCnt < 10) zeroes += "0";
					String pakPrefix = isOutOfRange ? zeroes + pakCnt + ".Unknown" : zeroes + names[pakCnt];
					File pakFile = dumpFolder.toPath().resolve(pakPrefix + "_" + fileType + ".pak").toFile();
					RandomAccessFile pak = new RandomAccessFile(pakFile, "rw");
					pak.write(anmBytes);
					pak.close();
					pakCnt++;
					result = true;
					Progress.updateProgLabel(pakFile.getName());
					pos += fileSize;
					int nextNumContents = -1;
					do {
						dump.read(valBytes);
						pos += valBytes.length;
						if (pos >= dump.length()) break;
						int val = ParamHandler.getVal(valBytes);
						if (val == expectedNumContents) {
							nextNumContents = val;
							break;
						}
					} while (nextNumContents != expectedNumContents);
					dump.read(valBytes);
					int nextStartContents = ParamHandler.getVal(valBytes);
					boolean nextStartContentsIsValid = false;
					for (int addr: expectedStartContents)
						nextStartContentsIsValid = nextStartContentsIsValid || (addr == nextStartContents);
					if (nextStartContentsIsValid) pos -= valBytes.length;
				}
			}
		}
		Progress.updateProgLabel("");
		return result;
	}
	public boolean splitByColor() throws IOException {
		boolean result = false;
		dumpFolder.mkdir();
		boolean isDamaged = false;
		byte[] valBytes = new byte[4];
		int charaCnt = 0, colorCnt = 0, initPos = 0;
		int[] validStartContents = { 224, 256, 1024, 1424 };
		int[] costumeArr = CsvHandler.getColumnVals(costumesCsv, 1);
		String[] names = CsvHandler.getParamNames(namesCsv, false);
		int numCostumes = 0;
		for (int costumeCnt = 0; costumeCnt < costumeArr.length; costumeCnt++) {
			costumeArr[costumeCnt] *= 2; //Include damaged costumes
			numCostumes += costumeArr[costumeCnt];
		}
		for (int pakCnt = 0; pakCnt < numCostumes; pakCnt++) {
			colorCnt++;
			String name = names[charaCnt];
			//Reset character and costume counters for the next character
			if (colorCnt > costumeArr[charaCnt]) {
				colorCnt = 1;
				charaCnt++;
				name = names[charaCnt]; //Reset the character name as well
			}
			if (colorCnt > costumeArr[charaCnt] / 2) isDamaged = true;
			else isDamaged = false;
			//Skip characters whose slots are empty/untouched
			if (name.endsWith("null")) {
				colorCnt = 1;
				charaCnt++;
			}
			else {
				int fileSizeOffset = 0;
				dump.seek(initPos);
				dump.read(valBytes);
				int numContents = ParamHandler.getVal(valBytes);
				dump.read(valBytes);
				int startContents = ParamHandler.getVal(valBytes);
				boolean checkBt3StyleCostume = numContents == 252 && startContents == 1024;
				boolean checkBt4StyleCostume = false;
				for (int start: validStartContents)
					checkBt4StyleCostume = checkBt4StyleCostume || (numContents == 52 && startContents == start);
				boolean hasInvalidIndices = false;
				dump.seek(initPos);
				if (checkBt4StyleCostume) {
					for (int offset = 108; offset < 1412; offset += 4) {
						dump.seek(initPos += offset);
						dump.read(valBytes);
						int tempFileSize =  ParamHandler.getVal(valBytes);
						dump.seek(initPos -= offset); //Return to beginning of file
						dump.seek(initPos += tempFileSize); //Go to end of file
						dump.read(valBytes);
						int otherNumContents = ParamHandler.getVal(valBytes);
						dump.read(valBytes);
						int otherStartContents = ParamHandler.getVal(valBytes);
						dump.seek(initPos -= tempFileSize); //Return to beginning of file
						boolean checkForCostumeHeader1 = otherNumContents == 52 || otherNumContents == 252;
						boolean checkForCostumeHeader2 = false;
						for (int start: validStartContents)
							checkForCostumeHeader2 = checkForCostumeHeader2 || (otherStartContents == start);
						boolean checkForPadding = otherNumContents == 0 && otherStartContents == 0;
						if (checkForPadding || (checkForCostumeHeader1 && checkForCostumeHeader2)) {
							fileSizeOffset = offset;
							break;
						}
					}
				}
				else if (checkBt3StyleCostume) fileSizeOffset = 212;
				dump.seek(initPos += fileSizeOffset);
				dump.read(valBytes);
				int fileSize = ParamHandler.getVal(valBytes);
				byte[] pakBytes = new byte[fileSize];
				dump.seek(initPos -= fileSizeOffset);
				dump.read(pakBytes);
				initPos += fileSize;
				//Skip padding until beginning of next file (aka its number of contents) is found
				while (initPos < (int) dump.length()) {
					dump.seek(initPos);
					dump.read(valBytes);
					initPos += valBytes.length;
					int nextNumContents = ParamHandler.getVal(valBytes);
					dump.read(valBytes);
					initPos += valBytes.length;
					int nextStartContents = ParamHandler.getVal(valBytes);
					boolean nextCheckBt4StyleCostume = false;
					for (int start: validStartContents)
						nextCheckBt4StyleCostume = nextCheckBt4StyleCostume || ((nextNumContents == 52 || nextNumContents == 252) && nextStartContents == start);
					if (nextCheckBt4StyleCostume) {
						hasInvalidIndices = false;
						initPos -= 2 * valBytes.length; //Set position back to beginning of file (needed for next iteration)
						break;
					}
					else if (nextNumContents != 0 && nextStartContents != 0)
						hasInvalidIndices = true;
				}
				int costumeCnt = Integer.parseUnsignedInt(name.substring(0, name.indexOf('.')));
				String zeroes = "";
				if (costumeCnt < 100) zeroes += "0";
				if (costumeCnt < 10) zeroes += "0";
				String pakNamePrefix = zeroes + name + "_";
				String pakNameSuffix = isDamaged ? (colorCnt - (costumeArr[charaCnt] / 2)) + "p_dmg.pak" : colorCnt + "p.pak";
				if (hasInvalidIndices) pakNamePrefix = "WRONG." + pakNamePrefix;
				File pakFile = dumpFolder.toPath().resolve(pakNamePrefix + pakNameSuffix).toFile();
				RandomAccessFile pak = new RandomAccessFile(pakFile, "rw");
				pak.setLength(0);
				pak.write(pakBytes);
				pak.close();
				result = true;
				Progress.updateProgBar(numCostumes);
				Progress.updateProgLabel(pakFile.getName());
			}
		}
		return result;
	}
	public boolean splitByContainer() throws IOException {
		boolean result = false;
		dumpFolder.mkdir();
		byte[] valBytes = new byte[4];
		int pakCnt = 0, pos = 0;
		int offset1 = 32, offset2 = 10976;
		String[] names = CsvHandler.getParamNames(namesCsv, false);
		while (pos < dump.length()) {
			dump.seek(pos);
			dump.read(valBytes);
			int numContents = ParamHandler.getVal(valBytes);
			dump.read(valBytes);
			int startHud = ParamHandler.getVal(valBytes);
			dump.read(valBytes);
			int startParams = ParamHandler.getVal(valBytes);
			if (numContents == 3 && startHud == offset1 && startParams == offset2) {
				dump.read(valBytes); //skip start of CMA PAK
				dump.read(valBytes);
				int fileSize = ParamHandler.getVal(valBytes);
				byte[] pakBytes = new byte[fileSize];
				dump.seek(pos);
				dump.read(pakBytes);
				boolean isOutOfRange = pakCnt >= names.length;
				for (int nameCnt = pakCnt; nameCnt < names.length; nameCnt++) {
					if (names[nameCnt].endsWith("null")) pakCnt++;
					else break;
				}
				String zeroes = "";
				if (pakCnt < 100) zeroes += "0";
				if (pakCnt < 10) zeroes += "0";
				String pakPrefix = isOutOfRange ? zeroes + pakCnt + ".Unknown" : zeroes + names[pakCnt];
				File pakFile = dumpFolder.toPath().resolve(pakPrefix + "_container.pak").toFile();
				RandomAccessFile pak = new RandomAccessFile(pakFile, "rw");
				pak.write(pakBytes);
				pak.close();
				pakCnt++;
				result = true;
				Progress.updateProgLabel(pakFile.getName());
				pos += fileSize;
				int nextNumContents = -1;
				do {
					dump.read(valBytes);
					pos += valBytes.length;
					if (pos >= dump.length()) break;
					int val = ParamHandler.getVal(valBytes);
					if (val == 3) {
						dump.read(valBytes);
						pos += valBytes.length;
						int nextStartHud = ParamHandler.getVal(valBytes);
						dump.read(valBytes);
						pos += valBytes.length;
						int nextStartParams = ParamHandler.getVal(valBytes);
						if (nextStartHud == offset1 && nextStartParams == offset2) {
							pos -= 2 * valBytes.length;
							nextNumContents = val;
							break;
						}
					}
				} while (nextNumContents != 3);
				pos -= valBytes.length;
			}
		}
		Progress.updateProgLabel("");
		return result;
	}
	public String getName() {
		return name;
	}
}
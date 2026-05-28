package cmd;
//Budokai Tenkaichi 4 Character PAK Class by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class Bt4CharaPak {
	private boolean valid;
	private int[] offsets;
	private int[] sizes;
	private File pakFolder;
	private File parent;
	private RandomAccessFile pak;
	private String name;
	
	public Bt4CharaPak(File f) {
		try {
			name = f.getName();
			parent = f.getParentFile();
			pak = new RandomAccessFile(f, "rw");
			validate();
			if (valid) {
				int numOffsets = getNumOffsets();
				setOffsets(numOffsets);
				setSizes(numOffsets);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isValid() {
		return valid;
	}
	public String getName() {
		return name;
	}
	public boolean repack() throws IOException {
		String[] allowedExts = { "anm", "cma", "cmr", "dat", "dbt", "mdl", "pak", "pck", "unk" };
		pakFolder = parent.toPath().resolve(name.substring(0, name.length() - 4)).toFile();
		if (pakFolder.isDirectory()) {
			int numParamFiles = 0;
			File[] allParamFiles = pakFolder.listFiles();
			File[] paramFiles = new File[allParamFiles.length];
			for (int fileCnt = 0; fileCnt < allParamFiles.length; fileCnt++) {
				for (String ext: allowedExts) {
					if (allParamFiles[fileCnt].getName().toLowerCase().endsWith("." + ext)) {
						paramFiles[numParamFiles] = allParamFiles[fileCnt];
						numParamFiles++;
						break;
					}
				}
			}
			System.arraycopy(paramFiles, 0, paramFiles, 0, numParamFiles);
			if (paramFiles != null) {
				Arrays.sort(paramFiles);
				int numLastParam = Integer.parseUnsignedInt(paramFiles[paramFiles.length - 1].getName().split("_")[0]);
				int[] paramIds = new int[numLastParam + 1];
				for (int paramCnt = 0; paramCnt < numLastParam + 1; paramCnt++) {
					if (paramCnt < paramFiles.length)
						paramIds[paramCnt] = Integer.parseUnsignedInt(paramFiles[paramCnt].getName().split("_")[0]);
					else paramIds[paramCnt] = paramCnt;
					//Test these out
					String paramName = paramFiles[paramCnt].getName().toLowerCase();
					if (paramName.contains("camera")) paramIds[paramCnt] = paramCnt;
				}
				for (int paramCnt = 0; paramCnt < paramIds.length; paramCnt++) {
					for (int offsetCnt = 0; offsetCnt < offsets.length; offsetCnt++) {
						if (offsetCnt == paramIds[paramCnt]) {
							if (paramCnt < paramFiles.length) {
								RandomAccessFile param = new RandomAccessFile(paramFiles[paramCnt], "r");
								byte[] paramBytes = new byte[(int) param.length()];
								param.read(paramBytes);
								param.close();
								pak.seek((offsetCnt + 1) * 4);
								pak.write(ParamHandler.getValBytes(offsets[paramIds[paramCnt]]));
								pak.seek(offsets[paramIds[paramCnt]]);
								pak.write(paramBytes);
								offsets[paramIds[paramCnt]] = offsets[paramIds[paramCnt]] + paramBytes.length;
							}
							//Do not check the next offsets in line once the offset and parameter file IDs match
							break;
						}
						//The first offset has no predecessor, hence this check (it prevents a negative index)
						else if (offsetCnt > 0) {
							/* Overwrite the offsets between the offset of the parameter ID and the offset of its predecessor
							with the last assigned offset, in order for those in-between files (not present when unpacked) to have a file size of zero */
							if (paramCnt == 0) paramCnt++;
							if (offsetCnt < paramIds[paramCnt] && offsetCnt > paramIds[paramCnt - 1]) {
								pak.seek((offsetCnt + 1) * 4);
								pak.write(ParamHandler.getValBytes(offsets[paramIds[paramCnt - 1]]));
							}
						}
					}
				}
				//Check if PAK file has offsets for 200 empty LPS files (a few of them don't)
				if (offsets.length == 253) {
					for (int paramCnt = 52; paramCnt < 252; paramCnt++) {
						pak.seek((paramCnt + 1) * 4);
						pak.write(ParamHandler.getValBytes(offsets[paramCnt - 1]));
					}
				}
			}
			return true;
		}
		return false;
	}
	public boolean unpack() throws IOException {
		pakFolder = parent.toPath().resolve(name.substring(0, name.length() - 4)).toFile();
		//To prevent the tool from accidentally detecting other files and throwing exceptions:
		if (offsets != null) pakFolder.mkdir();
		else return false;
		boolean includeNum = false;
		int csvNum = -1;
		if (offsets.length == 4) {
			csvNum = name.toLowerCase().contains("camera") ? 0 : 2;
			includeNum = name.toLowerCase().contains("camera") ? true : false;
		}
		else csvNum = 1;
		String[] paramNames = CsvHandler.getParamNames(new File("res/param-names-" + csvNum + ".csv"), includeNum);
		for (int offsetCnt = 0; offsetCnt < offsets.length - 1; offsetCnt++) {
			if (sizes[offsetCnt] != 0) {
				byte[] paramBytes = new byte[sizes[offsetCnt]];
				pak.seek(offsets[offsetCnt]);
				pak.read(paramBytes);
				String paramFileName = "unknown.unk";
				String prefix = "0";
				if (!includeNum) {
					if (offsetCnt > 99) prefix = "";
					if (offsetCnt < 10) prefix = "00";
					prefix += offsetCnt + "_";
				}
				if (offsetCnt < paramNames.length) {
					if (paramNames[offsetCnt] != null) paramFileName = paramNames[offsetCnt];
				}
				File paramFile = pakFolder.toPath().resolve(prefix + paramFileName).toFile();
				RandomAccessFile param = new RandomAccessFile(paramFile, "rw");
				param.write(paramBytes);
				param.close();
			}
		}
		return true;
	}
	
	private int getNumOffsets() throws IOException {
		byte[] valBytes = new byte[4];
		int actualSize = (int) pak.length();
		pak.seek(4);
		pak.read(valBytes);
		int startContents = ParamHandler.getVal(valBytes);
		int posOfLastOffset = startContents;
		for (int pos = startContents - 4; pos >= 4; pos -= 4) {
			pak.seek(pos);
			pak.read(valBytes);
			int offset = ParamHandler.getVal(valBytes);
			if (actualSize == offset) {
				posOfLastOffset = pos;
				break;
			}
		}
		return posOfLastOffset / 4;
	}
	private void setOffsets(int numOffsets) throws IOException {
		offsets = new int[numOffsets];
		byte[] valBytes = new byte[4];
		pak.seek(4);
		for (int offsetCnt = 0; offsetCnt < offsets.length; offsetCnt++) {
			pak.read(valBytes);
			offsets[offsetCnt] = ParamHandler.getVal(valBytes);
		}
	}
	private void setSizes(int numOffsets) {
		sizes = new int[numOffsets - 1];
		for (int sizeCnt = 1; sizeCnt <= sizes.length; sizeCnt++)
			sizes[sizeCnt - 1] = offsets[sizeCnt] - offsets[sizeCnt - 1];
	}
	private void validate() throws IOException {
		byte[] valBytes = new byte[4];
		pak.seek(0);
		pak.read(valBytes);
		int numFiles = ParamHandler.getVal(valBytes);
		pak.seek(212);
		pak.read(valBytes);
		int offset52 = ParamHandler.getVal(valBytes);
		int actualSize = (int) pak.length();
		//BT4 Character Costume PAK: Type 1 (Visual Assets Only)
		if (numFiles == 252) {
			pak.seek(1012);
			pak.read(valBytes);
			int sizeFromPak = ParamHandler.getVal(valBytes);
			if (offset52 == actualSize && sizeFromPak == actualSize) valid = true;
			else valid = false;
		}
		//BT4 Character Costume PAK: Type 2 (Visual Assets Only)
		else if (numFiles == 52) {
			pak.seek(4);
			pak.read(valBytes);
			int lastIndexEntry = ParamHandler.getVal(valBytes) - 4;
			for (int pos = lastIndexEntry; pos >= (numFiles + 1) * 4; pos -= 4) {
				pak.seek(pos);
				pak.read(valBytes);
				int offset = ParamHandler.getVal(valBytes);
				valid = actualSize == offset ? true: false;
				if (valid == true) break;
			}
		}
		//BT4 Character Costume PAK: Type 3 (Gameplay Parameters, HUDs and Cameras)
		else if (numFiles == 3) {
			if (name.toLowerCase().contains("camera")) valid = true;
			else {
				pak.seek(4);
				int[] correctVals = { 32, 10976, 23472 };
				int[] offsets = new int[3];
				for (int offsetCnt = 0; offsetCnt < offsets.length; offsetCnt++) {
					pak.read(valBytes);
					offsets[offsetCnt] = ParamHandler.getVal(valBytes);
					if (offsets[offsetCnt] == correctVals[offsetCnt]) valid = true;
					else {
						valid = false;
						break;
					}
				}
			}
		}
	}
}
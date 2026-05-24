package cmd;
//CSV Handler Class by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class CsvHandler {
	public static String[] getParamNames(File csv, boolean includeNum) throws IOException {
		int lineCnt = 0;
		String[] paramNames = null;
		if (!includeNum) paramNames = new String[getLastId(csv) + 1];
		else paramNames = new String[getNumRows(csv) - 1]; 
		Scanner sc = new Scanner(csv);
		//Validate header
		if (sc.hasNextLine()) {
			if (!sc.nextLine().equals("id,name,ext")) {
				sc.close();
				return null;
			}
		}
		//Read rest of lines
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] lineArr = line.split(",");
			if (lineArr.length == 3) {
				int paramId = Integer.parseInt(lineArr[0]);
				if (paramId < paramNames.length)
					paramNames[paramId] = lineArr[1] + "." + lineArr[2];
				else {
					if (includeNum) {
						paramNames[lineCnt] = lineArr[0] + "_" + lineArr[1] + "." + lineArr[2];
						lineCnt++;
					}
				}
			}
		}
		sc.close();
		return paramNames;
	}
	
	private static int getLastId(File csv) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(csv, "r");
		byte charByte;
		int fileSize = (int) raf.length();
		for (int pos = fileSize - 1; pos >= 0; pos--) {
			raf.seek(pos);
			charByte = raf.readByte();
			if (charByte == 0x0A) {
				byte[] stringBytes = new byte[fileSize - pos];
				raf.read(stringBytes);
				String line = new String(stringBytes);
				raf.close();
				return Integer.parseUnsignedInt(line.split(",")[0]);
			}
		}
		raf.close();
		return -1;
	}
	private static int getNumRows(File csv) throws IOException {
		byte charByte; //The method was rewritten to search for 1 byte instead of 2
		int rowCnt = 0, fileSize = (int) csv.length();
		RandomAccessFile raf = new RandomAccessFile(csv, "r");
		raf.seek(0);
		for (int pos = 0; pos < fileSize; pos++) {
			raf.seek(pos);
			charByte = raf.readByte();
			//Check for Line Feed (originally, Carriage Return was also included, but only Windows uses it)
			if (charByte == 0x0A) rowCnt++;
		}
		raf.close();
		return rowCnt + 1;
	}
}
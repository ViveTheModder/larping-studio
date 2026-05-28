package cmd;
//Budokai Tenkaichi 4 Character Parameter PCK Class by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class Bt4CharaPck {
	private static final int[] PARAM_SIZES = { 256, 8512, 704, 192, 1040, 640, 256, 512, 384 };
	private File parent;
	private File pckFolder;
	private RandomAccessFile pck;
	private String name;
	
	public Bt4CharaPck(File f) {
		try {
			name = f.getName();
			parent = f.getParentFile();
			pck = new RandomAccessFile(f, "rw");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean merge() throws IOException {
		pckFolder = parent.toPath().resolve(name.substring(0, name.length() - 4)).toFile();
		if (pckFolder != null) {
			File[] unpackedFiles = pckFolder.listFiles();
			Arrays.sort(unpackedFiles);
			pck.seek(0);
			for (File f: unpackedFiles) {
				RandomAccessFile raf = new RandomAccessFile(f, "r");
				int fileSize = (int) raf.length();
				byte[] rafBytes = new byte[fileSize];
				raf.read(rafBytes);
				raf.close();
				pck.write(rafBytes);
			}
			pck.close();
			return true;
		}
		return false;
	}
	public boolean split() throws IOException {
		try {
			pckFolder = parent.toPath().resolve(name.substring(0, name.length() - 4)).toFile();
			pckFolder.mkdir();
			pck.seek(0);
			String[] paramNames = CsvHandler.getParamNames(new File("res/param-names-3.csv"), true);
			if (paramNames.length == PARAM_SIZES.length) {
				for (int paramCnt = 0; paramCnt < PARAM_SIZES.length; paramCnt++) {
					byte[] paramBytes = new byte[PARAM_SIZES[paramCnt]];
					pck.read(paramBytes);
					File paramFile = pckFolder.toPath().resolve("0" + paramNames[paramCnt]).toFile();
					RandomAccessFile param = new RandomAccessFile(paramFile, "rw");
					param.write(paramBytes);
					param.close();
				}
				return true;
			}
			return false;
		}
		catch (IOException e) {
			return false;
		}
	}
	public String getName() {
		return name;
	}
}
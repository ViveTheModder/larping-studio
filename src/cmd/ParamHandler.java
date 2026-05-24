package cmd;
//Parameter Handler Class by ViveTheJoestar
public class ParamHandler {
	public static byte[] getValBytes(int val) {
		byte[] valBytes = new byte[4];
		int numBits = 0;
		for (int i = 0; i < 4; i++) {
			/* Literally the steps taken in the getVal() method, but the inverse:
			 * shift right by 8 bits (1 byte) 4 times to get the bytes from left to right. */
			valBytes[i] = (byte) (val >> numBits);
			numBits += 8;
		}
		return valBytes;
	}
	public static int getVal(byte[] intBytes) {
		int numBits = 24, output = 0;
		for (int i = 0; i < 4; i++) {
			/* Steps taken:
			 * 1. Bitwise AND the current byte with 0xFF to make it unsigned
			 * 2. Because the byte is now 32 bits (0xFF is an int), shift left
			 * to set the position in the output for the byte's bits (24-31, 16-23, 8-15, 1-7) 
			 * 3. Bitwise OR the bits to add them to the output */
			output |= ((intBytes[i] & 0xFF) << numBits);
			numBits -= 8; //8 bits = 1 byte
		}
		//If only I knew about this method sooner (RIP LittleEndian "library")
		return Integer.reverseBytes(output);
	}
}
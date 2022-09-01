package utils;

public class ConversionHelper {

	public static byte[] ShortToByte(short[] input) {
		int short_index, byte_index;
		int iterations = input.length;

		byte[] buffer = new byte[input.length * 2];

		short_index = byte_index = 0;

		for (/* NOP */; short_index != iterations; /* NOP */) {
			buffer[byte_index] = (byte) (input[short_index] & 0x00FF);
			buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

			++short_index;
			byte_index += 2;
		}

		return buffer;
	}
	
	public static short[] ByteToShort(byte[] input) {
		int short_index, byte_index;
		int iterations = input.length / 2;
		
		short[] buffer = new short[input.length / 2];
		
		short_index = byte_index = 0;
		
		for(;short_index != iterations;) {
			buffer[short_index] = (short) (((input[byte_index] & 0x00FF) | (input[byte_index + 1] << 8)));
			
			++short_index;
			byte_index += 2;
		}
		
		return buffer;
	}
}

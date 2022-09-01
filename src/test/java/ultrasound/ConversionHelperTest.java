package ultrasound;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import utils.ConversionHelper;

public class ConversionHelperTest {
	
	short [] arrA = {
            0,
                    -700,
                    1000,
                    -1000,
                    2000,
                    -1000,
                    1000,
                    789
        };
	
	byte[] arrAByte = {0, 0, 68, -3, -24, 3, 24, -4, -48, 7, 24, -4, -24, 3, 21, 3};

	@Test
	public void ShortToByteTest() {
		
		
		byte[] res = ConversionHelper.ShortToByte(arrA);
		assertArrayEquals(arrAByte, res);
	}
	
	@Test
	public void ByteToShortTest() {
		short[] res = ConversionHelper.ByteToShort(arrAByte);
		assertArrayEquals(arrA, res);
	}
}

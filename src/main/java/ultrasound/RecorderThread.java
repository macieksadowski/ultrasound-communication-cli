package ultrasound;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.lang3.ArrayUtils;

public class RecorderThread implements Runnable {

	private final CircularBuffer<List<Short>> buffer;

	private int N;
	private int sampleRate;
	private boolean isRunning;

	private AudioFormat format;

	public RecorderThread(CircularBuffer<List<Short>> buffer, int N, int fs) {

		super();

		this.buffer = buffer;
		this.N = N;
		this.sampleRate = fs;

		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		int channels = 1;
		int sampleSize = 16;
		boolean bigEndian = true;
		this.format = new AudioFormat(encoding, (float) sampleRate, sampleSize, channels, (sampleSize / 8) * channels,
				(float) sampleRate, bigEndian);

	}

	public void run() {

		isRunning = true;

		try {
			
			TargetDataLine line = getTargetDataLineForRecord();
			int frameSizeInBytes = format.getFrameSize();
			final int bufferLengthInBytes = N * frameSizeInBytes;
			final byte[] data = new byte[bufferLengthInBytes];
			line.start();

			while (isRunning) {
				
				if ((line.read(data, 0, bufferLengthInBytes)) == -1) {
					break;
				}
		        ByteBuffer bb = ByteBuffer.wrap(data);
		        ShortBuffer sb = bb.asShortBuffer();
		        short[] sa = new short[sb.capacity()];
		        int i=0;
		        while (sb.hasRemaining()) {
		        	sa[i] = sb.get();
		        	i++;
		        }
		        List<Short> list = Arrays.asList(ArrayUtils.toObject(sa));
				buffer.offer(list);
			
//				FileUtil.saveToFile("frag", sa);
				
				boolean communicate = false;
				while(buffer.isFull() && isRunning) {
					if(!communicate) {
						System.out.println("REC - Buffer is full!");
						communicate = true;
					}

					Thread.sleep(100);
				}

			}
			line.stop();
			line.flush();
			line.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

	}

	private TargetDataLine getTargetDataLineForRecord() {
		TargetDataLine line;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			return null;
		}
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
		} catch (final Exception ex) {
			return null;
		}
		return line;
	}
	

	public boolean isRunning() {
		return isRunning;
	}

	public void stop() {

		isRunning = false;
	}
	


}

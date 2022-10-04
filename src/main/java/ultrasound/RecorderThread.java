package ultrasound;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.utils.CircularBuffer;

public class RecorderThread implements Runnable {

	private static final int SAMPLE_SIZE = 16;
	private static final int RECORDING_CHANNELS = 1;
	private static final RecorderLogger LOGGER = RecorderLogger.getInstance();

	private final CircularBuffer<List<Short>> buffer;

	private int N;
	private int sampleRate;
	private boolean isRunning;

	private AudioFormat format;
	
	private boolean communicateOnBufferFullShown;

	public RecorderThread(CircularBuffer<List<Short>> buffer, int N, int fs) {

		super();

		this.buffer = buffer;
		this.N = N;
		this.sampleRate = fs;

		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		boolean bigEndian = true;
		this.format = new AudioFormat(encoding, sampleRate, SAMPLE_SIZE, RECORDING_CHANNELS,
				(SAMPLE_SIZE / Byte.SIZE) * RECORDING_CHANNELS, sampleRate, bigEndian);

	}

	public void run() {

		isRunning = true;

		TargetDataLine line = getTargetDataLineForRecord();
		if (line != null) {
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
				int i = 0;
				while (sb.hasRemaining()) {
					sa[i] = sb.get();
					i++;
				}
				List<Short> list = Arrays.asList(ArrayUtils.toObject(sa));
				buffer.offer(list);
				// FileUtil.saveToFile("frag", sa);
				while (buffer.isFull() && isRunning) {
					onBufferFull();
				}
			}
			line.stop();
			line.flush();
			line.close();
		}
	}
	
	private void onBufferFull() {
		
		if (!communicateOnBufferFullShown) {
			LOGGER.logMessage("Buffer is full!");
			communicateOnBufferFullShown = true;
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private TargetDataLine getTargetDataLineForRecord() {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (AudioSystem.isLineSupported(info)) {
			TargetDataLine line;
			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format);
				return line;
			} catch (LineUnavailableException e) {
				LOGGER.logMessage(e.getMessage());
			}
		}
		return null;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void stop() {
		isRunning = false;
	}

}


package ultrasound;

import java.io.PrintWriter;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Encoder extends AbstractEncoder implements Runnable {

	private String hexData = "";
	private AudioFormat format;

	private PrintWriter out;
	private SourceDataLine dataLine;

	public Encoder(EncoderBuilder builder) {

		super(builder);

	}

	public static class EncoderBuilder extends AbstractEncoderBuilder {

		public EncoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
			super(sampleRate, noOfChannels, firstFreq, freqStep);

		}

		@Override
		public Encoder build() {
			validate();
			Encoder encoder = new Encoder(this);
			return encoder;
		}

		protected final void validate() {
			super.validate();
		}

	}

	/**
	 * Sets hexadecimal data to transmit
	 * 
	 * @param hexData hexadecimal data
	 */
	public void setHexData(String hexData) {
		this.hexData = hexData;
	}

	@Override
	protected void playSound(short[] soundData) {

		int byteLength = soundData.length * 2;
		ByteBuffer bb = ByteBuffer.allocate(byteLength);
		bb.asShortBuffer().put(soundData);

		this.dataLine.write(bb.array(), 0, byteLength);

	}

	/**
	 * Method used to start sound transmission
	 */
	public void run() {

		logMessage("Transmitting message: " + hexData);

		try {

			// construct audio stream in 16bit format with given sample rate
			AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
			int channels = 1;
			int sampleSize = 16;
			boolean bigEndian = true;
			this.format = new AudioFormat(encoding, (float) sampleRate, sampleSize, channels,
					(sampleSize / 8) * channels, (float) sampleRate, bigEndian);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			dataLine = (SourceDataLine) AudioSystem.getLine(info);
			dataLine.open(format);
			dataLine.start();

			transmit(hexData);

			if (this.dataLine != null) {
				this.dataLine.drain();
				this.dataLine.close();
				logMessage("Transmission ended.");
				logMessage("Message: " + hexData);
				logMessage("Bin message: " + getBinaryMessageString());
				logMessage("Bandwidth: " + freq[0][0] + "Hz - " + freq[noOfChannels - 1][1] + "Hz");
				logMessage("Speed rate: " + Math.floor((double) noOfChannels / (tOnePulse + tBreak)) + "b/s");

			}

		} catch (Exception e) {
			e.printStackTrace();
			logMessage(e.toString());
		}
	}

	public void connectToLogOutput(PrintWriter out) {
		this.out = out;
	}

	protected void logMessage(String s) {
		String msg = "ENC - " + s;
		if (out != null) {
			out.println(msg);
		} else {
			System.out.println(msg);
		}

	}
}

package ultrasound;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ultrasound.encoder.AbstractEncoder;
import ultrasound.encoder.AbstractEncoderBuilder;

public final class Encoder extends AbstractEncoder {

	private static final int SAMPLE_SIZE = 16;
	private static final int RECORDING_CHANNELS = 1;

	private SourceDataLine dataLine;

	private Encoder(EncoderBuilder builder) {

		super(builder);

	}

	public static class EncoderBuilder extends AbstractEncoderBuilder {

		public EncoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
			super(sampleRate, noOfChannels, firstFreq, freqStep);

		}

		@Override
		public Encoder build() {
			validate();
			return new Encoder(this);
		}

	}

	@Override
	protected void playSound(short[] soundData) {

		int byteLength = soundData.length * 2;
		ByteBuffer bb = ByteBuffer.allocate(byteLength);
		bb.asShortBuffer().put(soundData);

		this.dataLine.write(bb.array(), 0, byteLength);

	}

	protected void constructAudioStream() throws LineUnavailableException {
		// construct audio stream in 16bit format with given sample rate
		AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(encoding, sampleRate, SAMPLE_SIZE, RECORDING_CHANNELS,
				(SAMPLE_SIZE / Byte.SIZE) * RECORDING_CHANNELS, sampleRate, bigEndian);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		dataLine = (SourceDataLine) AudioSystem.getLine(info);
		dataLine.open(format);
		dataLine.start();
	}

	@Override
	protected void closeAudioStream() throws NullPointerException {
		dataLine.drain();
		dataLine.close();

	}
}

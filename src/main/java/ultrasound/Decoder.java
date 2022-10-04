package ultrasound;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.decoder.AbstractDecoder;
import ultrasound.decoder.AbstractDecoderBuilder;
import ultrasound.utils.CircularBuffer;

public final class Decoder extends AbstractDecoder {
	
	private static final int BUFFER_SIZE = 64;

	private final CircularBuffer<List<Short>> buffer;
	private boolean audioRecorderRunning;
	private RecorderThread recorder;
	private Thread recorderThread;

	private Decoder(DecoderBuilder builder) throws Exception {
		super(builder);

		this.buffer = new CircularBuffer<List<Short>>(BUFFER_SIZE);
		audioRecorderRunning = false;
		recorder = new RecorderThread(buffer, N, sampleRate);

		initializeAudioRecorder();

	}

	public static class DecoderBuilder extends AbstractDecoderBuilder {

		public DecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
				double threshold) {
			super(sampleRate, noOfChannels, firstFreq, freqStep, nfft, threshold);
		}

		@Override
		public Decoder build() {
			try {
				validate();
				return new Decoder(this);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public boolean isAudioRecorderRunning() {
		return audioRecorderRunning;
	}

	protected void startRecording() {
		if (!isAudioRecorderRunning()) {
			initializeAudioRecorder();
		}
		recorderThread.start();
		audioRecorderRunning = true;
	}

	private void initializeAudioRecorder() {
		recorderThread = new Thread(recorder);
		recorderThread.setName("Recorder Thread");
	}

	protected void stopAudioRecorder() {

		recorder.stop();
		try {
			recorderThread.join(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		recorderThread = null;
		audioRecorderRunning = false;
	}

	@Override
	/**
	 * This method returns raw audio data samples, which contain signal to decode.
	 * 
	 * @return short[] audio data samples
	 */
	protected short[] getAudioSamples() throws Exception {

		if (!audioRecorderRunning) {
			throw new Exception("Audio Recorder need to be initialized first!");
		}
		short[] frag = new short[N];
		if (!buffer.isEmpty()) {
			List<Short> list = buffer.poll();
			Short[] arr = list.toArray(new Short[0]);
			frag = ArrayUtils.toPrimitive(arr);
		}
		return frag;

	}

	@Override
	protected void onDataFrameSuccessfullyReceived() {

	}
}

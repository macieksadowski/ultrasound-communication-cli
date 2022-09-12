package ultrasound;

import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public final class Decoder extends AbstractDecoder implements Runnable {

	private final CircularBuffer<List<Short>> buffer;
	protected boolean audioRecorderConnected;
	private RecorderThread recorder;
	private Thread recorderThread;
	private PrintWriter out;

	private Decoder(DecoderBuilder builder) throws Exception {
		super(builder);

		this.buffer = new CircularBuffer<List<Short>>(64);
		audioRecorderConnected = false;

	}

	public static class DecoderBuilder extends AbstractDecoderBuilder {

		public DecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
				double threshold) {
			super(sampleRate, noOfChannels, firstFreq, freqStep, nfft, threshold);

		}

		@Override
		public Decoder build() {
			Decoder decoder;
			try {
				validate();
				decoder = new Decoder(this);
				decoder.connectToAudioRecorder();
				return decoder;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		protected final void validate() {
			super.validate();
		}

	}

	public boolean isAudioRecorderConnected() {
		return audioRecorderConnected;
	}

	protected void startRecording() {
		recorderThread.start();
	}

	protected void closeRecorder() {

		recorder.stop();
		try {
			recorderThread.join(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		recorderThread = null;
	}
	
	public void connectToLogOutput(PrintWriter out) {
		this.out = out;
	}

	@Override
	/**
	 * This method returns raw audio data samples, which contain signal to decode.
	 * 
	 * @return short[] audio data samples
	 */
	protected short[] getAudioSamples() throws Exception {

		if (!audioRecorderConnected) {
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
	protected void logMessage(String s) {
		String msg = "DEC - " + s;
		if (out != null) {
			out.println(msg);
		} else {
			System.out.println(msg);
		}

	}
	
	private void connectToAudioRecorder() {
		recorder = new RecorderThread(buffer, N, sampleRate);
		recorderThread = new Thread(recorder);
		recorderThread.setName("Recorder Thread");
		audioRecorderConnected = true;

	}
}
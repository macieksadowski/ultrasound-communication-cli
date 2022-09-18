package ultrasound;

import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.utils.CircularBuffer;

public final class Decoder extends AbstractDecoder implements Runnable, IDecoder {

	private final CircularBuffer<List<Short>> buffer;
	protected boolean audioRecorderRunning;
	private RecorderThread recorder;
	private Thread recorderThread;
	private PrintWriter out;

	private Decoder(DecoderBuilder builder) throws Exception {
		super(builder);

		this.buffer = new CircularBuffer<List<Short>>(64);
		audioRecorderRunning = false;
		recorder = new RecorderThread(buffer, N, sampleRate);

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
				decoder.startAudioRecorder();
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

	public boolean isAudioRecorderRunning() {
		return audioRecorderRunning;
	}

	protected void startRecording() {
		if(!isAudioRecorderRunning()) {
			startAudioRecorder();
		}
		recorderThread.start();
	}
	
	private void startAudioRecorder() {
		recorderThread = new Thread(recorder);
		recorderThread.setName("Recorder Thread");
		audioRecorderRunning = true;
	}

	protected void stopAudioRecorder() {

		recorder.stop();
		try {
			recorderThread.join(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		recorderThread = null;
		audioRecorderRunning = false;
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
	protected void logMessage(String s) {
		String msg = "DEC - " + s;
		if (out != null) {
			out.println(msg);
		} else {
			System.out.println(msg);
		}

	}

	@Override
	protected void onDataFrameSuccessfullyReceived() {
		
	}
}
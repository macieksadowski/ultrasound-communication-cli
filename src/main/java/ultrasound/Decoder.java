package ultrasound;

import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;

public class Decoder extends AbstractDecoder implements Runnable {

	private final CircularBuffer<List<Short>> buffer;
	private final StopWatch watch;
	protected boolean audioRecorderConnected;
	private boolean isRunning;
	private double elapsedTime;
	private RecorderThread recorder;
	private Thread recorderThread;
	private PrintWriter out;

	public Decoder(DecoderBuilder builder) throws Exception {
		super(builder);

		this.watch = new StopWatch();

		this.isRunning = false;
		this.elapsedTime = 0.0;

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

	private void connectToAudioRecorder() {
		recorder = new RecorderThread(buffer, N, sampleRate);
		recorderThread = new Thread(recorder);
		recorderThread.setName("Recorder Thread");
		audioRecorderConnected = true;

	}

	public boolean isAudioRecorderConnected() {
		return audioRecorderConnected;
	}

    /**
     *
     */
    public void run() {

        logMessage("Decoder started!");
        isRunning = true;
        

        logMessage("RECORDING");
        logMessage("Sampling frequency " + sampleRate + " Hz");
        logMessage("Frame length " + N);
        logMessage("Frequency resolution " + delta_f + "Hz, DFT resolution " + nfft + "Hz");

        //count how many times the while was executed
        int count = 0;
        elapsedTime = 0.0;
        startRecording();
        while (isRunning) {

            try {
                recordFrag = getAudioSamples();

                count++;
                elapsedTime = 1 / delta_f * count;

                watch.start();
                decode();
                watch.stop();
                //if(this.sigBin != null)
                    //logMessage("Data length: " + N + ", execution time: " + watch.getTime());
                watch.reset();
                //callback.updateFigures();

            } catch (Exception e) {
            	logMessage(e.toString());
                e.printStackTrace();
                stopRecording();
                return;
            }
        }
    }

    protected void startRecording() {
        recorderThread.start();
    }

    public void stopRecording() {
    	logMessage("Decoder stopped!");
    	isRunning = false;
        elapsedTime = 0.0;
        recorder.stop();
        try {
			recorderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        recorderThread = null;
    }

    @Override
    /**
     * This method returns raw audio data samples, which contain signal to decode.
     * @return short[] audio data samples
     */
    public short[] getAudioSamples() throws Exception {

        if (!audioRecorderConnected) {
            throw new Exception("Audio Recorder need to be initialized first!");
        }
        short[] frag = new short[N];
        if(!buffer.isEmpty()) {
            List<Short> list = buffer.poll();
            Short[] arr = list.toArray(new Short[0]);
            frag = ArrayUtils.toPrimitive(arr);
        }
        return frag;

    }
    
    public void connectToLogOutput(PrintWriter out) {
    	this.out = out;
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

    public double getElapsedTime() {
        return elapsedTime;
    }

    public boolean isRunning() {
        return isRunning;
    }




}
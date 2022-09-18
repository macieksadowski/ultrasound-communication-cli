package ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import ui.UltrasoundCli.UltrasoundCommand;
import ultrasound.AbstractCoder;
import ultrasound.Decoder;
import ultrasound.Decoder.DecoderBuilder;
import ultrasound.ICoder.CoderMode;

@Command(name = "start-decoder", mixinStandardHelpOptions = true,
description = "Record audio and decode ultrasound signal", version = "1.0")
public class UltrasoundStartDecoderCommand implements Runnable {
	
    @Option(names = {"-sr", "--sample-rate"}, description = "Sample rate")
    int sampleRate;
    
    @Option(names = {"-ch", "--no-of-channels"}, description = "Number of channels")
    int noOfChannels;
    
    @Option(names = {"-fr", "--first-freq"}, description = "First frequency")
    int firstFreq;
	
    @Option(names = {"-st", "--freq-step"}, description = "Frequency step")
    int freqStep;
    
    @Option(names = {"-nfft", "--nfft"}, description = "Size of FFT in form 2^n")
    int nfft;
    
    @Option(names = {"-th", "--threshold"}, description = "Min. amplitude to detect")
    double threshold;
    
    @Option(names = {"-tp", "--t-one-pulse"}, description = "Length of one pulse")
    double tOnePulse;
    
    @Option(names = {"--disable-secded"}, description = "Disable decoding with SECDED Code")
    boolean secdedDisabled = false;
    
    @Option(names = {"-m", "--mode"}, description = "Transmissmion mode: SIMPLE - transmit raw hex data, DATA_FRAME - uses DataFrame for transmission")
    AbstractCoder.CoderMode mode;
    
	
	@ParentCommand UltrasoundCommand parent;
	
	public void run() {
		parent.out.println("Decoding");

		try {
			HashMap<String, String> params = UltrasoundCli.loadParamsProperties();
			if (sampleRate == 0) {
				sampleRate = Integer.valueOf(params.get("sampleRate"));
			}
			if (noOfChannels == 0) {
				noOfChannels = Integer.valueOf(params.get("noOfChannels"));
			}
			if (firstFreq == 0) {
				firstFreq = Integer.valueOf(params.get("firstFreq"));
			}
			if (freqStep == 0) {
				freqStep = Integer.valueOf(params.get("freqStep"));
			}
			if (nfft == 0) {
				nfft = Integer.valueOf(params.get("nfft"));
			}
			if (threshold == 0) {
				threshold = Double.valueOf(params.get("threshold"));
			}
			if (!secdedDisabled) {
				if (params.get("disable-secded") != null) {
					secdedDisabled = Boolean.valueOf(params.get("disable-secded"));
				}
			}

			if (mode == null) {
				if (params.get("disable-secded") != null) {
					try {
						mode = CoderMode.valueOf(params.get("mode"));
					} catch (Exception e) {
					}
				}
			}

			DecoderBuilder decoderBuilder = new DecoderBuilder(sampleRate, noOfChannels, firstFreq, freqStep,
					(int) Math.pow(2, nfft), threshold);
			decoderBuilder.secdedEnabled(!secdedDisabled);
			decoderBuilder.mode(mode);

			if (tOnePulse != 0) {
				decoderBuilder.tOnePulse(tOnePulse);
			} else if (params.get("tOnePulse") != null) {
				decoderBuilder.tOnePulse(Double.valueOf(params.get("tOnePulse") + "d"));
			}

			Decoder decoder = decoderBuilder.build();
			if (decoder != null) {

				parent.setDecoder(decoder);
				Thread decoderThread = new Thread(decoder);
				decoderThread.setName("Decoder Thread");
				parent.setDecoderThread(decoderThread);
				decoderThread.start();

			} else
				throw new Exception("Failed to build Decoder object!");

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

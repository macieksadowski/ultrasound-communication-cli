package ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import ui.UltrasoundCli.UltrasoundCommand;
import ultrasound.AbstractCoder;
import ultrasound.Encoder;
import ultrasound.Encoder.EncoderBuilder;
import ultrasound.ICoder.CoderMode;
import ultrasound.dataframe.DataFrame.DataFrameBuilder;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;

@Command(name = "encode", mixinStandardHelpOptions = true,
description = "Encode and play ultrasound signal", version = "1.0")
public class UltrasoundEncodeCommand implements Runnable {
	
	@Parameters(index = "0", description = "Message to transmit (ASCII)")
	String message;
	
    @Option(names = {"-sr", "--sample-rate"}, description = "Sample rate")
    int sampleRate;
    
    @Option(names = {"-ch", "--no-of-channels"}, description = "Number of channels")
    int noOfChannels;
    
    @Option(names = {"-fr", "--first-freq"}, description = "First frequency")
    int firstFreq;
	
    @Option(names = {"-st", "--freq-step"}, description = "Frequency step")
    int freqStep;
    
    @Option(names = {"-tp", "--t-one-pulse"}, description = "Length of one pulse")
    double tOnePulse;
    
    @Option(names = {"-tb", "--t-break"}, description = "Length of break btw. pulses")
    double tBreak;
    
    @Option(names = {"--disable-secded"}, description = "Disable decoding with SECDED Code")
    boolean secdedDisabled = false;
    
    @Option(names = {"-m", "--mode"}, description = "Transmissmion mode: SIMPLE - transmit raw hex data, DATA_FRAME - uses DataFrame for transmission")
    AbstractCoder.CoderMode mode;
    
	@ParentCommand UltrasoundCommand parent;
	
	public void run() {

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

			if (!secdedDisabled) {
				if (params.get("disable-secded") != null) {
					secdedDisabled = Boolean.valueOf(params.get("disable-secded"));
				}
			}

			if (mode == null) {
				if (params.get("disable-secded") != null) {
					mode = CoderMode.valueOf(params.get("mode"));
				}
			}

			EncoderBuilder encoderBuilder = new EncoderBuilder(sampleRate, noOfChannels, firstFreq, freqStep);

			encoderBuilder.secdedEnabled(!secdedDisabled);

			encoderBuilder.mode(mode);

			if (tOnePulse != 0) {
				encoderBuilder.tOnePulse(tOnePulse);
			} else if (params.get("tOnePulse") != null) {
				encoderBuilder.tOnePulse(Double.valueOf(params.get("tOnePulse") + "d"));
			}

			if (tBreak != 0) {
				encoderBuilder.tBreak(tBreak);
			} else if (params.get("tBreak") != null) {
				encoderBuilder.tBreak(Double.valueOf(params.get("tBreak") + "d"));
			}

			Encoder encoder = encoderBuilder.build();

			byte[] byteMessage = message.getBytes();

			switch (encoder.getMode()) {
			case DATA_FRAME:
				DataFrameBuilder frameBuilder = new DataFrameBuilder(IDataFrame.BROADCAST_ADDRESS, noOfChannels);
				frameBuilder.data(byteMessage);
				frameBuilder.command(IAsciiControlCodes.STX);
				frameBuilder.data(byteMessage);

				encoder.setDataFrame(frameBuilder.build());
				break;
			case SIMPLE:
				StringBuilder hexMessageBuilder = new StringBuilder();
				for (byte b : byteMessage) {
					hexMessageBuilder.append(Integer.toHexString(b));
				}
				encoder.setHexData(hexMessageBuilder.toString());
				break;
			default:
				break;

			}

			encoder.run();

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

package ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jline.TerminalFactory;
import jline.TerminalFactory.Type;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter.ArgumentList;
import jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter;
import jline.internal.Configuration;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.IFactory;
import picocli.shell.jline2.PicocliJLineCompleter;
import ui.UltrasoundDeviceCommands.UltrasoundMasterSendCmdCommand;
import ui.UltrasoundDeviceCommands.UltrasoundMasterSendDataCommand;
import ui.UltrasoundDeviceCommands.UltrasoundMasterStopCommand;
import ui.UltrasoundDeviceCommands.UltrasoundSlaveStartCommand;
import ui.UltrasoundDeviceCommands.UltrasoundSlaveStopCommand;

import ultrasound.Decoder;
import ultrasound.Decoder.DecoderBuilder;
import ultrasound.Encoder.EncoderBuilder;
import ultrasound.ICoder.CoderMode;
import ultrasound.decoder.IDecoder;
import ultrasound.devices.MasterUltrasoundDevice;
import ultrasound.devices.SlaveUltrasoundDevice;
import ultrasound.encoder.IEncoder;
import ultrasound.utils.UltrasoundHelper;

/**
 * Source:
 * https://github.com/remkop/picocli/blob/main/picocli-shell-jline2/README.md
 * 
 * @author M. Sadowski
 *
 */
public class UltrasoundCli {
	
	private static Thread DECODER_THREAD;
	private static Decoder DECODER;
	private static Thread MASTER_THREAD;
	private static MasterUltrasoundDevice MASTER;
	private static Thread SLAVE_THREAD;
	private static SlaveUltrasoundDevice SLAVE;
	
	private static CommandLine cmd;

	@Command(name = "", description = "Ultrasound Cli", footer = { "",
			"Press Ctrl-C to exit." })
	static class UltrasoundCommand implements Runnable {

		final ConsoleReader reader;
		final PrintWriter out;

		public UltrasoundCommand(ConsoleReader reader) {
			this.reader = reader;
			out = new PrintWriter(reader.getOutput());
		}

		public void run() {
			out.println(cmd.getUsageMessage());

		}
		
		public void setDecoder(Decoder decoder) {
			DECODER = decoder;
		}
		
		public Decoder getDecoder() {
			return DECODER;
		}
		
		public void setDecoderThread(Thread _decoderThread) {
			DECODER_THREAD = _decoderThread;
		}
		
		public Thread getDecoderThread() {
			return DECODER_THREAD;
		}
		
		public MasterUltrasoundDevice getMaster() {
			return MASTER;
		}
		
		public SlaveUltrasoundDevice getSlave() {
			return SLAVE;
		}
		
		public void setSlaveThread(Thread _slaveThread) {
			SLAVE_THREAD = _slaveThread;
		}
		
		public Thread getSlaveThread() {
			return SLAVE_THREAD;
		}
		
		public void setMasterThread(Thread _masterThread) {
			MASTER_THREAD = _masterThread;
		}
		
		public Thread getMasterThread() {
			return MASTER_THREAD;
		}

	}
	
	public static void main(String[] args) {

		if (!Help.Ansi.AUTO.enabled() && //
				Configuration.getString(TerminalFactory.JLINE_TERMINAL, TerminalFactory.AUTO).toLowerCase()
						.equals(TerminalFactory.AUTO)) {
			TerminalFactory.configure(Type.NONE);
		}

		try {
			ConsoleReader reader = new ConsoleReader();
			IFactory factory = new CustomFactory(new InteractiveParameterConsumer(reader));
			
			UltrasoundCommand commands = new UltrasoundCommand(reader);
			
			cmd = new CommandLine(commands, factory);
			
			
			cmd.addSubcommand(new ClearScreen());
			
			if(args.length > 0) {
				DeviceType mode = DeviceType.valueOf(args[0]);
				switch(mode) {
				case master:
					initializeAsMasterDevice();
					break;
				case slave:
					Byte adr = null;
					if(args.length > 1) {
						adr = Byte.parseByte(args[1]);
					}
					initializeAsSlaveDevice(adr);
					break;
				}	
			} else {
				initializeDefault();
			}
			
			
			reader.addCompleter(new PicocliJLineCompleter(cmd.getCommandSpec()));

			// start the shell and process input until the user quits with Ctrl-D
			String line;
			while ((line = reader.readLine("> ")) != null) {
				ArgumentList list = new WhitespaceArgumentDelimiter().delimit(line, line.length());
				cmd.execute(list.getArguments());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void initializeAsMasterDevice() {
		
		initializeDevice(DeviceType.master);
		
		cmd.addSubcommand(new UltrasoundMasterSendCmdCommand());
		cmd.addSubcommand(new UltrasoundMasterSendDataCommand());
		cmd.addSubcommand(new UltrasoundMasterStopCommand());
		
	}
	
	public static void initializeAsSlaveDevice(Byte adr) {
		
		initializeDevice(DeviceType.slave, adr);
		
		cmd.addSubcommand(new UltrasoundSlaveStartCommand());
		cmd.addSubcommand(new UltrasoundSlaveStopCommand());
		
	}
	
	public static void initializeDevice(DeviceType type) {
		initializeDevice(type, null);
	}
	
	public static void initializeDevice(DeviceType type, Byte address) {
		try {
			HashMap<String, String> params = UltrasoundCli.loadParamsProperties();

			int sampleRate = Integer.valueOf(params.get("sampleRate"));
			int noOfChannels = Integer.valueOf(params.get("noOfChannels"));
			int firstFreq = Integer.valueOf(params.get("firstFreq"));
			int freqStep = Integer.valueOf(params.get("freqStep"));
			double tOnePulse = Double.valueOf(params.get("tOnePulse") + "d");
			double threshold = Double.valueOf(params.get("threshold"));
			int nfft = Integer.valueOf(params.get("nfft"));
			if(address == null && type == DeviceType.slave) {
				address = UltrasoundHelper.hexToByte(params.get("slaveAddress"));
			}
			
			DecoderBuilder db = new DecoderBuilder(sampleRate, noOfChannels, firstFreq, freqStep, (int) Math.pow(2, nfft), threshold);
			db.mode(CoderMode.DATA_FRAME);
			db.tOnePulse(tOnePulse);
			IDecoder decoder = db.build();
			
			EncoderBuilder eb = new EncoderBuilder(sampleRate, noOfChannels, firstFreq, freqStep);
			eb.mode(CoderMode.DATA_FRAME);
			eb.tOnePulse(tOnePulse);
			IEncoder encoder = eb.build();
			
			
			if(type == DeviceType.master) {
			    MASTER = new MasterUltrasoundDevice(encoder, decoder);
			    
			    if (params.get("masterDecoderTimeout") != null) {
			    	long timeout = (long) (Double.valueOf(params.get("masterDecoderTimeout")) * 1000);
					MASTER.setDecoderTimeout(timeout);
				}
			} else {
				SLAVE = new SlaveUltrasoundDevice(address, encoder, decoder);
			}

			

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void initializeDefault() {
		cmd.addSubcommand(new UltrasoundStartDecoderCommand());
		cmd.addSubcommand(new UltrasoundStopDecoderCommand());
		cmd.addSubcommand(new UltrasoundDecoderClearBuffersCommand());
		cmd.addSubcommand(new UltrasoundEncodeCommand());
		
	}
	
	
	public static HashMap<String, String> loadParamsProperties() throws FileNotFoundException, IOException {
		return loadParamsProperties("params.properties");
	}

	public static HashMap<String, String> loadParamsProperties(String fileName) throws FileNotFoundException, IOException {
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String appConfigPath = rootPath + fileName;

		Properties paramsProps = new Properties();
		paramsProps.load(new FileInputStream(appConfigPath));

		HashMap<String, String> retMap = new HashMap<String, String>();
		for (Map.Entry<Object, Object> entry : paramsProps.entrySet()) {
			retMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}
		return retMap;
	}
	

	public enum DeviceType {
		master,
		slave
	}

	

}

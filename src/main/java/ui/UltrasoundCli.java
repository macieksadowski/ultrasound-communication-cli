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
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import picocli.shell.jline2.PicocliJLineCompleter;
import ultrasound.Decoder;

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

	@Command(name = "", description = "Example interactive shell with completion", footer = { "",
			"Press Ctrl-C to exit." }, subcommands = { UltrasoundStartDecoderCommand.class,UltrasoundStopDecoderCommand.class,
					UltrasoundDecoderClearBuffersCommand.class, UltrasoundEncodeCommand.class, ClearScreen.class })
	static class UltrasoundCommand implements Runnable {

		final ConsoleReader reader;
		final PrintWriter out;
		

		@Spec
		private CommandSpec spec;

		public UltrasoundCommand(ConsoleReader reader) {
			this.reader = reader;
			out = new PrintWriter(reader.getOutput());
		}

		public void run() {
			out.println(spec.commandLine().getUsageMessage());

		}
		
		public void setDecoder(Decoder _decoder) {
			DECODER = _decoder;
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

			// set up the completion
			UltrasoundCommand commands = new UltrasoundCommand(reader);
			CommandLine cmd = new CommandLine(commands, factory);
			reader.addCompleter(new PicocliJLineCompleter(cmd.getCommandSpec()));

			// start the shell and process input until the user quits with Ctrl-D
			String line;
			while ((line = reader.readLine("> ")) != null) {
				ArgumentList list = new WhitespaceArgumentDelimiter().delimit(line, line.length());
				new CommandLine(commands, factory).execute(list.getArguments());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static HashMap<String, String> loadParamsProperties() throws FileNotFoundException, IOException {
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String appConfigPath = rootPath + "params.properties";

		Properties paramsProps = new Properties();
		paramsProps.load(new FileInputStream(appConfigPath));

		HashMap<String, String> retMap = new HashMap<String, String>();
		for (Map.Entry<Object, Object> entry : paramsProps.entrySet()) {
			retMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}
		return retMap;
	}
	



	

}

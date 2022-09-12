package ui;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import ui.UltrasoundCli.UltrasoundCommand;

@Command(name = "clear-decoder", mixinStandardHelpOptions = true,
		description = "Clear decoder's buffers", version = "1.0")
public class UltrasoundDecoderClearBuffersCommand implements Runnable {

	@ParentCommand
	UltrasoundCommand parent;

	public void run() {
		if(parent.getDecoderThread() != null && parent.getDecoder().isRunning()) {
			parent.getDecoder().clearReceivedDataBuffers();
			parent.out.println("Decoder data buffers cleared!");
		} else {
			parent.out.println("Decoder isn't running, nothing to do...");
		}
	}
}
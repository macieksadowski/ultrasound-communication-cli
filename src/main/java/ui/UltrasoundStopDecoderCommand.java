package ui;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import ui.UltrasoundCli.UltrasoundCommand;

@Command(name = "stop-decoder", mixinStandardHelpOptions = true,
		description = "Stop runnind decoder", version = "1.0")
public class UltrasoundStopDecoderCommand implements Runnable {

	@ParentCommand
	UltrasoundCommand parent;

	public void run() {
		if(parent.getDecoderThread() != null && parent.getDecoder().isRunning()) {
			parent.getDecoder().stopRecording();
			try {
				parent.getDecoderThread().join(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			parent.setDecoderThread(null);
			parent.out.println("Decoder stopped!");
		} else {
			parent.out.println("Decoder isn't running, nothing to do...");
		}
	}
}
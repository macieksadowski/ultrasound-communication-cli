package ui;

import java.io.IOException;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import ui.UltrasoundCli.UltrasoundCommand;

/**
 * Command that clears the screen.
 */
@Command(name = "cls", aliases = "clear", mixinStandardHelpOptions = true, description = "Clears the screen", version = "1.0")
class ClearScreen implements Runnable {

	@ParentCommand
	UltrasoundCommand parent;

	public void run() {
		try {
			parent.reader.clearScreen();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}
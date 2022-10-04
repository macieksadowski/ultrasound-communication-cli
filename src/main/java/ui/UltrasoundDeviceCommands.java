package ui;

import java.nio.charset.StandardCharsets;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import ui.UltrasoundCli.UltrasoundCommand;
import ultrasound.dataframe.ControlCodes;
import ultrasound.devices.MasterUltrasoundDevice;

public class UltrasoundDeviceCommands {

	@Command(name = "send-cmd", mixinStandardHelpOptions = true, description = "Send a command", version = "1.0")
	public static class UltrasoundMasterSendCmdCommand implements Runnable {

		@ParentCommand
		UltrasoundCommand parent;

		@Parameters(index = "0", description = "Command to transmit")
		String cmd;

		@Option(names = { "-adr", "--address" }, description = "Receiver's address in format 0x00")
		String adr;

		public void run() {

			MasterUltrasoundDevice master = parent.getMaster();
			if (parent.getMasterThread() == null) {
				parent.setMasterThread(new Thread(master));
				parent.getMasterThread().setName("Master Thread");
				parent.getMasterThread().start();
			}

			Byte command = ControlCodes.getCodeByName(cmd);
			if (command != null) {

				if (adr != null) {
					byte address = Byte.parseByte(adr, 16);
					master.send(address, command.byteValue());
				} else {
					master.sendBroadcast(command.byteValue());
				}
			} else {
				System.err.println("Unknown command");
			}

		}
	}

	@Command(name = "send-data", mixinStandardHelpOptions = true, description = "Send data", version = "1.0")
	public static class UltrasoundMasterSendDataCommand implements Runnable {

		@ParentCommand
		UltrasoundCommand parent;

		@Parameters(index = "0", description = "Data to transmit")
		String dataStr;

		@Option(names = { "-adr", "--address" }, description = "Receiver's address in format 0x00")
		String adr;

		public void run() {

			MasterUltrasoundDevice master = parent.getMaster();
			if (parent.getMasterThread() == null) {
				parent.setMasterThread(new Thread(master));
				parent.getMasterThread().setName("Master Thread");
				parent.getMasterThread().start();
			}

			byte[] data = dataStr.getBytes(StandardCharsets.US_ASCII);
			if (adr != null) {
				byte address = Byte.parseByte(adr, 16);
				master.send(address, data);
			} else {
				master.sendBroadcast(data);
			}

		}
	}

	@Command(name = "stop", mixinStandardHelpOptions = true, description = "Stop master device", version = "1.0")
	public static class UltrasoundMasterStopCommand implements Runnable {

		@ParentCommand
		UltrasoundCommand parent;

		public void run() {

			if (parent.getMasterThread() != null) {
				parent.getMaster().stop();
				try {
					parent.getMasterThread().join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				parent.setMasterThread(null);
			}

		}
	}

	@Command(name = "start", mixinStandardHelpOptions = true, description = "Start listening", version = "1.0")
	public static class UltrasoundSlaveStartCommand implements Runnable {

		@ParentCommand
		UltrasoundCommand parent;

		public void run() {

			parent.setSlaveThread(new Thread(parent.getSlave()));
			parent.getSlaveThread().setName("Slave Thread");
			parent.getSlaveThread().start();

		}
	}

	@Command(name = "stop", mixinStandardHelpOptions = true, description = "Stop listening", version = "1.0")
	public static class UltrasoundSlaveStopCommand implements Runnable {

		@ParentCommand
		UltrasoundCommand parent;

		public void run() {

			if (parent.getSlaveThread() != null) {
				parent.getSlave().stop();
				try {
					parent.getSlaveThread().join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				parent.setSlaveThread(null);
			}

		}
	}

}

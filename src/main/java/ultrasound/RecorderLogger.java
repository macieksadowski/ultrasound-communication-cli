package ultrasound;

import ultrasound.utils.log.Logger;

/**
 * Logger class for {@link RecorderThread}. Singleton class
 */
public final class RecorderLogger extends Logger {

	private static RecorderLogger instance;

	private RecorderLogger() {
		setTag("REC");
	}

	public static synchronized RecorderLogger getInstance() {
		if (instance == null) {
			instance = new RecorderLogger();
		}

		return instance;
	}
}

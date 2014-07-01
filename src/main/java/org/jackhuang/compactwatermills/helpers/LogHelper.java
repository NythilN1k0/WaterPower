
package org.jackhuang.compactwatermills.helpers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jackhuang.compactwatermills.CompactWatermills;
import org.jackhuang.compactwatermills.Reference;

import cpw.mods.fml.common.FMLLog;

public class LogHelper {
	
	private static Logger WatermillLog = LogManager.getLogger(Reference.ModID);
	
	public static void debugLog(Level level, String message) {
		if (! Reference.DEBUG_MODE) {
			return;
		}
		log(level, message);
	}
	
	public static void debugLog(String message) {
		debugLog(Level.INFO, message);
	}
	
	public static void log(Level level, String message) {
		WatermillLog.log(level, message);
	}
	
	public static void log(String message) {
		log(Level.INFO, message);
	}
	
	public static void warn(String message) {
		log(Level.WARN, message);
	}
	
	public static void err(String message) {
		log(Level.ERROR, message);
	}
	
}

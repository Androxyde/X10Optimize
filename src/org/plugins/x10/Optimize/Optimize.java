package org.plugins.x10.Optimize;

import foxtrot.Task;
import foxtrot.Worker;
import java.io.File;
import java.io.FileOutputStream;
import org.adb.AdbUtility;
import org.logger.MyLogger;
import org.plugins.PluginDefaults;
import org.plugins.x10.Optimize.About;
import org.system.CommentedPropertiesFile;
import org.system.Devices;
import org.system.GlobalConfig;
import org.system.Shell;


public class Optimize extends PluginDefaults implements org.plugins.PluginInterface {
	
	public class RunTask extends Task {
		public Object run() {
			try {
				doTask();
			}
			catch (Exception e) {
				MyLogger.getLogger().debug(e.getMessage());
			}
			return null;
		}
	}

	public String getName() {
		return "JIT Installer";
	}

	public void doTask() throws Exception {
		Devices.getCurrent().doBusyboxHelper();
		if (AdbUtility.Sysremountrw()) {
			files.pullWithRename("system/build.prop", Devices.getCurrent().getId()+fsep+"build.prop.orig");
			CommentedPropertiesFile build = new CommentedPropertiesFile();
			build.load(new File(files.getFile(Devices.getCurrent().getId(),"build.prop.orig")));
			build.updateWith(new File(files.getFile(Devices.getCurrent().getId(),"build.prop.merge")));
			build.store(new FileOutputStream(new File(files.getFileDir()+fsep+Devices.getCurrent().getId()+fsep+"build.prop")), "");
			files.push(Devices.getCurrent().getId(),"optimize.tar");
			files.push(Devices.getCurrent().getId(),"build.prop");
			files.delete(Devices.getCurrent().getId()+fsep+"build.prop");
			files.delete(Devices.getCurrent().getId()+fsep+"build.prop.orig");
			Shell pushbuildprop = sfactory.createShell("pushbuildprop");
			pushbuildprop.runRoot();
			Shell optimize = sfactory.createShell("optimize");
			optimize.runRoot();
			MyLogger.info("Optimize finished. Rebooting phone ...");
		}
		else MyLogger.info("Error mounting /system rw");
	}

	public void run() {
		try {
			Worker.post(new RunTask());
		}
		catch (Exception e) {
			MyLogger.error(e.getMessage());
		}
	}

	public void showAbout() {
		About about = new About();
		about.setVisible(true);
	}

}
package com.archer.framework.base;

import com.archer.framework.base.component.ClassContainer;
import com.archer.framework.base.conf.Conf;
import com.archer.framework.base.conf.ConfLoader;
import com.archer.framework.base.util.PlatformUtil;

public class ArcherApplication {
	
	public static void start(String[] args) {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		if(stacks.length < 3) {
			System.err.println("stack trace back can not found method main(String[])");
			System.exit(0);
		}
		if(!PlatformUtil.isWindows() && !PlatformUtil.isLinux()) {
			System.err.println("paltform " + System.getProperty("os.name") + " is not supported.");
			System.exit(0);
		}	
		Conf conf  = ConfLoader.load();
		ClassContainer classes = new ClassContainer(conf, stacks[2].getClassName());
		classes.loadComponents();
	}
}

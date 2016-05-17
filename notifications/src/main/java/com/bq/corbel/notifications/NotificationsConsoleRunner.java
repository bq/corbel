package com.bq.corbel.notifications;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.bq.corbel.lib.cli.console.Console;
import com.bq.corbel.lib.ws.log.LogbackUtils;
import com.bq.corbel.notifications.cli.dsl.NotificationsShell;
import com.bq.corbel.notifications.ioc.NotificationsIoc;

/**
 * @author Alberto J. Rubio
 */
public class NotificationsConsoleRunner extends Console {

	public NotificationsConsoleRunner() {
		super(
				"Welcome to SilkRoad Notifications. Type notifications.help() to start.",
				"notifications", createShell());
	}

	@SuppressWarnings("resource")
	private static NotificationsShell createShell() {
		System.setProperty("conf.namespace", "notifications");
		return new AnnotationConfigApplicationContext(NotificationsIoc.class)
				.getBean(NotificationsShell.class);
	}

	public static void main(String[] args) {
		LogbackUtils.setLogLevel("INFO");
		NotificationsConsoleRunner console = new NotificationsConsoleRunner();
		try {
			if (args.length == 0) {
				console.launch();
			} else {
				console.runScripts(args);
			}
			System.exit(0);
		} catch (Throwable e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

}

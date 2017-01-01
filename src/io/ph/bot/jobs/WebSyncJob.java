package io.ph.bot.jobs;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;

/**
 * Web sync
 * Sync data to the dashboard
 * @author Paul
 *
 */
public class WebSyncJob implements Job {
	public static int messageCount = 0;
	public static int commandCount = 0;
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			long total = Runtime.getRuntime().totalMemory();
			long free = Runtime.getRuntime().freeMemory();
			long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
			URL url = new URL(String.format("http://127.0.0.1:8080/pages/api?"
					+ "msgCount=%d&cmdCount=%d&userCount=%d&memoryUsage=%d&serverCount=%d&hour=%d&min=%d&key=%s",
						messageCount, commandCount,
						Bot.getInstance().getBot().getUsers().size(),
						(int) (((double) total - free) / total * 100), 
						Bot.getInstance().getBot().getGuilds().size(),
						TimeUnit.MILLISECONDS.toHours(uptime),
						TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime)),
						Bot.getInstance().getApiKeys().get("dashboard")));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", 
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
			conn.getInputStream();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if(Bot.getInstance().isDebug())
				LoggerFactory.getLogger(WebSyncJob.class).debug("Web sync offline...");
		} catch (NoAPIKeyException e) {
			//handled before this
		}
		messageCount = 0;
		commandCount = 0;
	}

}

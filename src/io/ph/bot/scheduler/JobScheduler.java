package io.ph.bot.scheduler;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.LoggerFactory;

import io.ph.bot.Bot;
import io.ph.bot.exception.NoAPIKeyException;
import io.ph.bot.jobs.ReminderJob;
import io.ph.bot.jobs.TimedPunishJob;
import io.ph.bot.jobs.TwitchStreamJob;

public class JobScheduler {
	
	public static Scheduler scheduler;
	
	public static void initializeScheduler() {
		try {
			scheduler = new StdSchedulerFactory("resources/config/quartz.properties").getScheduler();
			scheduler.start();
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public static void twitchStreamCheck() {
		JobDetail job = JobBuilder.newJob(TwitchStreamJob.class).withIdentity("twitchJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("twitchJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(180).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public static void remindCheck() {
		JobDetail job = JobBuilder.newJob(ReminderJob.class).withIdentity("reminderJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("reminderJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(15).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public static void punishCheck() {
		JobDetail job = JobBuilder.newJob(TimedPunishJob.class).withIdentity("punishJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("punishJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(20).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public static void initializeEventSchedule() {
		try {
			Bot.getInstance().getApiKeys().get("twitch");
			twitchStreamCheck();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a Twitch.tv API key setup in Bot.properties - Your bot will not have "
					+ "support for Twitch.tv announcements.");
		}
		remindCheck();
		punishCheck();
		
	}
}

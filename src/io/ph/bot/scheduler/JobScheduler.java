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
import io.ph.bot.feed.RedditEventListener;
import io.ph.bot.jobs.ReminderJob;
import io.ph.bot.jobs.StatusChangeJob;
import io.ph.bot.jobs.TimedPunishJob;
import io.ph.bot.jobs.TwitchStreamJob;
import io.ph.bot.jobs.WebSyncJob;

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
	
	private static void twitchStreamCheck() {
		JobDetail job = JobBuilder.newJob(TwitchStreamJob.class).withIdentity("twitchJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("twitchJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(180).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private static void remindCheck() {
		JobDetail job = JobBuilder.newJob(ReminderJob.class).withIdentity("reminderJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("reminderJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(15).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private static void punishCheck() {
		JobDetail job = JobBuilder.newJob(TimedPunishJob.class).withIdentity("punishJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("punishJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private static void webSync() {
		JobDetail job = JobBuilder.newJob(WebSyncJob.class).withIdentity("messageCountsJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("messageCountsJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private static void redditFeed() {
		JobDetail job = JobBuilder.newJob(RedditEventListener.class).withIdentity("redditFeedJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("redditFeedJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(8).repeatForever()).build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private static void statusChange() {
		JobDetail job = JobBuilder.newJob(StatusChangeJob.class).withIdentity("statusChangeJob", "group1").build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("statusChangeJob", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(70).repeatForever()).build();
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
		statusChange();
		try {
			Bot.getInstance().getApiKeys().get("dashboard");
			webSync();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a web dashboard setup. Scheduler will not run dashboard updates");
		}
		try {
			Bot.getInstance().getApiKeys().get("redditkey");
			redditFeed();
		} catch (NoAPIKeyException e1) { 
			LoggerFactory.getLogger(JobScheduler.class).warn("You do not have a reddit client/secret setup. Scheduler will not run reddit feed updates");
		}
	}
}

package io.ph.bot.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import io.ph.bot.State;

public class StatusChangeJob implements Job {
	private static String[] statuses = {
			"www.momobot.io",
			"$help | $info",
			"Persona 6",
			"Final Fantasy XVI",
			"dead"
	};
	private static int index = 0;

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		State.changeBotStatus(statuses[index]);
		if(++index >= statuses.length)
			index = 0;
	}

}

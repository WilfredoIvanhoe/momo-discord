package io.ph.bot.commands.general;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.db.ConnectionPool;
import io.ph.db.SQLUtils;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Set a reminder to be PM'd a message by the bot
 * @author Paul
 *
 */
@CommandData (
		defaultSyntax = "remindme",
		aliases = {"remind"},
		permission = Permission.NONE,
		description = "Designate a time in the future to be PM'd a reminder",
		example = "1d2h It is now 1 day and 2 hours from when I set this reminder!"
		)
public class RemindMe implements Command {

	@Override
	public void executeCommand(IMessage msg) {
		EmbedBuilder em = new EmbedBuilder();
		if(msg.getContent().split(" ").length < 3) {
			em = MessageUtils.commandErrorMessage(msg, "remindme", "#w#d#h#m reminder-message", 
					"*#w#d#h#m* - When you want to be reminded. Need to have at least one time, or any combination\n"
							+ "\tFor example, you can use 3d2h5m to be reminded in 3 days, 2 hours, and 1 minute",
					"*reminder-message* - The message you want to be reminded of");
			em.withFooterText("The reminder is accurate to within 20 seconds");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		String reminderContents = Util.getCommandContents(Util.getCommandContents(msg));
		if(reminderContents.length() > 500) {
			em.withColor(Color.RED).withTitle("Error").withDesc("Maximum reminder length is 500 characters. Yours is " + reminderContents.length());
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		String param = Util.getParam(msg);
		Instant now = Instant.now();
		Instant target = Util.resolveInstantFromString(param);
		
		long months = ChronoUnit.MONTHS.between(LocalDateTime.ofInstant(now, ZoneId.systemDefault()), 
				LocalDateTime.ofInstant(target, ZoneId.systemDefault()));
		if(months > 5) {
			em.withColor(Color.RED).withTitle("Error").withDesc("You can only set reminders up to 6 months in the future");
			MessageUtils.sendMessage(msg.getChannel(), em.build());
			return;
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectionPool.getGlobalDatabaseConnection();
			String sql = "INSERT INTO `global_reminders` (user_id, guild_name, reminder, remind_time) VALUES (?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, msg.getAuthor().getID());
			stmt.setString(2, msg.getGuild().getName());
			stmt.setString(3, reminderContents);
			stmt.setString(4, target.toString());
			stmt.execute();
			
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeQuietly(stmt);
			SQLUtils.closeQuietly(conn);
		}
		em.withColor(Color.GREEN).withTitle("Success")
		.withDesc("You will be reminded at...");
		em.withTimestamp(target.toEpochMilli()).withFooterText("Local time");
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}

}

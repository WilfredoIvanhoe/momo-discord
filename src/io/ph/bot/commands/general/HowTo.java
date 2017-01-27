package io.ph.bot.commands.general;

import java.awt.Color;

import io.ph.bot.Bot;
import io.ph.bot.commands.Command;
import io.ph.bot.commands.CommandData;
import io.ph.bot.model.Permission;
import io.ph.util.MessageUtils;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

@CommandData (
		defaultSyntax = "howto",
		aliases = {"tutorial", "help"},
		permission = Permission.NONE,
		description = "PM a guide to the user, specified by the given input. "
				+ "Doing just the command will give you a list of help topics",
		example = "setup"
		)
public class HowTo implements Command {
	EmbedBuilder em;
	@Override
	public void executeCommand(IMessage msg) {
		em = new EmbedBuilder();
		em.withColor(Color.MAGENTA);
		String s = Util.getCommandContents(msg);
		switch(s.toLowerCase()) {
		case "setup":
			setupMessage(Util.getPrefixForGuildId(msg.getGuild().getID()));
			break;
		case "music":
			setupMusic(Util.getPrefixForGuildId(msg.getGuild().getID()));
			break;
		case "live feeds":
		case "livefeeds":
		case "live":
			setupFeeds(Util.getPrefixForGuildId(msg.getGuild().getID()));
			break;
		case "moderation":
			setupModeration(Util.getPrefixForGuildId(msg.getGuild().getID()));
			break;
		case "roles":
		case "role management":
			setupRoleManagement(Util.getPrefixForGuildId(msg.getGuild().getID()));
			break;
		default:
			defaultMessage(Util.getPrefixForGuildId(msg.getGuild().getID()), msg.getChannel());
			return;
		}
		em.withFooterText(String.format("Current version: %s", Bot.BOT_VERSION));
		MessageUtils.sendPrivateMessage(msg.getAuthor(), em.build());
		em = new EmbedBuilder().withTitle("Success")
				.withDesc("Check your PMs!")
				.withColor(Color.GREEN);
		MessageUtils.sendMessage(msg.getChannel(), em.build());
	}
	
	private void setupRoleManagement(String prefix) {
		em.withTitle("Role management")
		.withDesc("Users with manage role+ permissions can setup and disable *joinable roles*. "
				+ "Joinable roles allow users to join a role to show their flair, whether it's allegiance "
				+ "to a character or to a color")
		.appendField("Creating a joinable role", String.format("You can create a joinable role with "
				+ "`%sjoinablerole name-of-role`. If the role doesn't exist, I will create it for you. "
				+ "If it does exist, I'll use that role. \n"
				+ "`%<sdisablerole name-of-role` will disable this as a joinable role. "
				+ "Note that this will not automatically remove users from this role!\n"
				+ "If you want a listing of roles, you can use `%<srolestats`", prefix), false);
	}
	
	private void setupModeration(String prefix) {
		em.withTitle("Moderation features")
		.withDesc("I provide various moderation functions to both streamline mutes, kicks, and bans, as well as "
				+ "limiting usage on my commands")
		.appendField("Timed mutes and bans", String.format("As well as offering indefinite mutes and bans, "
				+ "you can use the `%smute` and %<sban` commands to temporarily punish a user.\n"
				+ "To do so, use the `temp` parameter with a time in this format: #w#d#h#s. For example, "
				+ "to mute someone for 1 day and 2 hours, do `%<smute temp 1d2h @target`. "
				+ "Same syntax with bans: `%<sban temp 1d2h @target`", prefix), false)
		.appendField("Enabling & disabling commands", String.format("You can enable or disable commands for users "
				+ "with the `%senablecommand` and `%<sdisablecommand`. You can then check "
				+ "the status of your commands with `%<scommandstatus`\n"
				+ "Note: disabled commands can still be used by users with kick+ permissions", prefix), false);
	}
	
	private void setupFeeds(String prefix) {
		em.withTitle("Live feeds")
		.withDesc("This tutorial briefly goes over Twitch.tv, Twitter, and Reddit feeds. You need at least the *kick* permission")
		.appendField("Twitch.tv", String.format("You can register Twitch.tv channels for automatic notifications "
				+ "that trigger when they go online and offline. To do so, use the `%stwitchchannel` "
				+ "in the channel you want to register. Then, use `%<stwitch username` to register. "
				+ "To undo, do `%<sunregistertwitch username`", prefix), false)
		.appendField("Subreddits", String.format("Registering subreddits is straightforward. \n"
				+ "Use the `%sreddit subreddit` command to register a subreddit for notifications. "
				+ "You can then connfigure various features, such as showing all/no nsfw/no images and text previews.\n"
				+ "You can remove a subscription with `%<sremovereddit subreddit`. \n"
				+ "To list all of your subscriptions, use `%<slistreddit`", prefix), false)
		.appendField("Twitter", String.format("Twitter follows the same format as subreddits.\n"
				+ "Use `%stwitter twitter-name` to register a Twitter account.\n"
				+ "To remove it, use `%<sremovetwitter twitter-name`\n"
				+ "To list all of your subscriptions, use `%<stwitterlist`", prefix), false);
	}
	
	private void setupMusic(String prefix) {
		em.withTitle("Music usage")
		.withDesc("This is a quick tutorial on how to use my music features")
		.appendField("Supported sources", "I currently support the following sources: "
				+ "YouTube videos & playlists, direct links, attachments sent through discord, "
				+ ".webm files (such as the music on Themes.moe), Soundcloud, Bandcamp, and more!", false)
		.appendField("Playing music", String.format("Playing music is easy. "
				+ "Use the `%smusic` command with your URL directly afterwards. "
				+ "This will automatically add it to the queue, and, after a short processing period, will play in "
				+ "the designated music voice channel", 
				prefix), false)
		.appendField("Options", String.format("I have various options you can use with the `%smusic` command.\n"
				+ "`%<smusic skip` adds a vote to skip the song\n"
				+ "`%<smusic now` shows the current song and timestamp\n"
				+ "`%<smusic next` shows the current queue\n"
				+ "`%<smusic stop` allows moderators to kill the queue\n"
				+ "`%<smusic volume #` allows moderators to change the volume", 
				prefix), false);
	}
	private void setupMessage(String prefix) {
		em.withTitle("Basic setup")
		.withDesc("These are the first three commands you should be doing.")
		.appendField("Mute setup", String.format("Muting requires me to create a special role. "
				+ "This role will have special permission overrides for every channel, preventing them from sending messages. "
				+ "To do this, make sure you have the Manage Server role and do `%ssetup`", 
				prefix), false)
		.appendField("Basic configuration", String.format("Various features I provide can be configured in one go. "
				+ "To do this, type in `%sconfigure`. The steps are self explanatory.",
				prefix), false)
		.appendField("Music", String.format("Last but not least is music. To set this up, do `%ssetupmusic`. "
				+ "Then, if you want music announcements for when a new song is playing, do `%<smusicchannel` in a "
				+ "designated channel.", 
				prefix), false);
	}
	private void defaultMessage(String prefix, IChannel channel) {
		em.withTitle("How To options")
		.withColor(Color.MAGENTA)
		.withDesc(String.format("Do `%showto` with a topic afterwards, i.e. `%<showto setup`", prefix))
		.appendField("Options", "setup, moderation, role management, live feeds, music", true);
		MessageUtils.sendMessage(channel, em.build());
	}
}

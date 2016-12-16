package io.ph.bot.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Event for when a user is unmuted
 * @author Paul
 *
 */
public class UserUnmutedEvent extends Event {
	private final IUser user;
	private final IGuild guild;
	
	public UserUnmutedEvent(IUser target, IGuild guild) {
		this.user = target;
		this.guild = guild;
	}
	
	/**
	 * Muted user
	 * @return IUser of target
	 */
	public IUser getUser() {
		return this.user;
	}
	
	/**
	 * Guild this was performed in
	 * @return IGuild
	 */
	public IGuild getGuild() {
		return this.guild;
	}
}

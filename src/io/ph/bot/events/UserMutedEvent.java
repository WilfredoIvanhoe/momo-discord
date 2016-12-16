package io.ph.bot.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Event for when a user is muted
 * @author Paul
 *
 */
public class UserMutedEvent extends Event {
	private final IUser muter;
	private final IUser user;
	private final IGuild guild;
	
	public UserMutedEvent(IUser muter, IUser target, IGuild guild) {
		this.muter = muter;
		this.user = target;
		this.guild = guild;
	}
	
	/**
	 * Person that muted the target
	 * @return IUser of muter
	 */
	public IUser getMuter() {
		return this.muter;
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

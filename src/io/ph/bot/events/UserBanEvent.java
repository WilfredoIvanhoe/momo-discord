package io.ph.bot.events;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Event for when a user is banned
 * Upgrade from internal ban event by adding the banner
 * @author Paul
 *
 */
public class UserBanEvent extends Event {
	private final IUser banner;
	private final IUser user;
	private final IGuild guild;
	
	public UserBanEvent(IUser banner, IUser target, IGuild guild) {
		this.banner = banner;
		this.user = target;
		this.guild = guild;
	}
	
	/**
	 * Person that banned the target
	 * @return IUser of muter
	 */
	public IUser getBanner() {
		return this.banner;
	}
	
	/**
	 * Banned user
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

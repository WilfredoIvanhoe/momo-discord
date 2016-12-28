package io.ph.bot.commands;

import io.ph.bot.model.Permission;
import io.ph.util.Util;
import sx.blah.discord.handle.obj.IMessage;

public interface Command {
	/**
	 * Execute this command
	 * @param msg Original IMessage. Can infer guild, user, etc off of this
	 */
	public void executeCommand(IMessage msg);
	
	/**
	 * Check if user has permissions
	 * @param msg Original message
	 * @return True if good to go, false if not
	 */
	public default boolean hasPermissions(IMessage msg) {
		return Util.userHasPermission(msg.getAuthor(), msg.getGuild(), getPermission());
	}
	
	/**
	 * Get permission required
	 * @return Permission required
	 */
	public default Permission getPermission() {
		return this.getClass().getAnnotation(CommandData.class).permission();
	}
	
	/**
	 * Get default command syntax
	 * @return Default syntax
	 */
	public default String getDefaultCommand() {
		return this.getClass().getAnnotation(CommandData.class).defaultSyntax();
	}
	
	/**
	 * Get aliases of command
	 * @return Aliases of this command
	 */
	public default String[] getAliases() {
		return this.getClass().getAnnotation(CommandData.class).aliases();
	}
	
	/**
	 * Get description
	 * @return Description of command
	 */
	public default String getDescription() {
		return this.getClass().getAnnotation(CommandData.class).description();
	}
	
	/**
	 * Get example. Do not include the default command, as that is automatically appended
	 * If you have multiple examples, use a \n linebreak to designate. Again, do not include the command itself
	 * @return Example in annotation
	 */
	public default String getExample() {
		return this.getClass().getAnnotation(CommandData.class).example();
	}
}

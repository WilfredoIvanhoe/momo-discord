package io.ph.bot.model;

public enum Permission {
	NONE("No permissions"),
	KICK("Kick"),
	BAN("Ban"),
	MANAGE_ROLES("Manage roles"),
	MANAGE_CHANNELS("Manage channels"),
	MANAGE_SERVER("Manage server"),
	ADMINISTRATOR("Administrator"),
	BOT_OWNER("Bot owner");
	
	private String readable;
	private Permission(String readable) {
		this.readable = readable;
	}
	
	@Override
	public String toString() {
		return this.readable;
	}

}

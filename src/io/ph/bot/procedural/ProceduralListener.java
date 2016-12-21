package io.ph.bot.procedural;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class ProceduralListener {
	private static ProceduralListener instance;
	/**
	 * Observers are identified by the User ID that started
	 */
	private Map<String, ProceduralCommand> observers = ExpiringMap.builder()
			.expiration(5, TimeUnit.MINUTES)
			.expirationPolicy(ExpirationPolicy.CREATED)
			.build();

	public void addListener(IUser u, ProceduralCommand c) {
		observers.put(new String(u.getID()), c);
	}
	public void removeListener(IUser u) {
		observers.remove(u.getID());
	}

	public void update(IMessage msg) {
		if(observers.get(msg.getAuthor().getID()) != null)
			observers.get(msg.getAuthor().getID()).step(msg);
	}

	public static ProceduralListener getInstance() {
		if(instance == null)
			instance = new ProceduralListener();
		return instance;
	}
}

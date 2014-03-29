package net.mcshockwave.Minigames.Handlers;

import java.util.Random;

import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;

import org.bukkit.World;
import org.bukkit.event.Listener;

public interface IMinigame extends Listener {
	
	public World w = Minigames.getDefaultWorld();
	public Random rand = new Random();
	public Minigames plugin = Minigames.ins;
	
	public abstract void onGameStart();
	public abstract void onGameEnd();
	public abstract void onPlayerDeath(DeathEvent e);

}

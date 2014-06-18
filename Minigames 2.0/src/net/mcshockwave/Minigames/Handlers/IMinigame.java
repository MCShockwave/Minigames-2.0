package net.mcshockwave.Minigames.Handlers;

import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.World;
import org.bukkit.event.Listener;

import java.util.Random;

public interface IMinigame extends Listener {
	
	public World w = Multiworld.getGame();
	public Random rand = new Random();
	public Minigames plugin = Minigames.ins;
	
	public abstract void onGameStart();
	public abstract void onGameEnd();
	public abstract void onPlayerDeath(DeathEvent e);

}

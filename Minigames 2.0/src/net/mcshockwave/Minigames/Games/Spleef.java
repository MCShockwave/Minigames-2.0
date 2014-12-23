package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Spleef implements IMinigame {

	public long			invin				= 0;
	public final int	invincibilityTime	= 10;

	@Override
	public void onGameStart() {
		Minigames.showDefaultSidebar();
		
		for (Player p : Minigames.getOptedIn()) {
			p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
			if (p.getGameMode() != GameMode.SURVIVAL) {
				p.setGameMode(GameMode.SURVIVAL);
			}
		}
		Minigames.broadcast("You have %s seconds of invincibility!", invincibilityTime);
		invin = System.currentTimeMillis() + (invincibilityTime * 1000);
	}

	@Override
	public void onGameEnd() {
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Block b = event.getClickedBlock();
		if (b != null && Minigames.alivePlayers.contains(p.getName()) && b.getType() == Material.SNOW_BLOCK) {
			if (invin < System.currentTimeMillis()) {
				killBlock(b);
			} else {
				Minigames.send(ChatColor.RED, p, "You still have %s seconds of invincibility left!",
						(invin - System.currentTimeMillis()) / 1000);
			}
		}
	}

	public void killBlock(final Block b) {
		b.setType(Material.AIR);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				b.setType(Material.SNOW_BLOCK);
			}
		}, 300);
	}

}

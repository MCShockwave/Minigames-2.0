package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Minotaur implements IMinigame {

	Material[]			chests		= { Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
			Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.WOOD_SWORD, Material.STONE_SWORD,
			Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.IRON_CHESTPLATE };

	public BukkitTask	refill		= null;

	Location			startArea	= new Location(w, 1039, 111, 39);
	Location			endArea		= new Location(w, 961, 111, -39);

	public void refillChests() {
		int minX = Math.min(startArea.getBlockX(), endArea.getBlockX()), minZ = Math.min(startArea.getBlockZ(),
				endArea.getBlockZ());
		int maxX = Math.max(startArea.getBlockX(), endArea.getBlockX()), maxZ = Math.max(startArea.getBlockZ(),
				endArea.getBlockZ());

		int y = startArea.getBlockY();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				Block b = w.getBlockAt(new Location(w, x, y, z));
				if (b.getType() == Material.CHEST) {
					Chest c = (Chest) b.getState();

					c.getInventory().clear();

					for (int i = 0; i < rand.nextInt(6); i++) {
						c.getInventory().setItem(rand.nextInt(27), new ItemStack(chests[rand.nextInt(chests.length)]));
					}
				}
			}
		}
	}

	/*
	 * public void sendToRandom(Player p) { int minX =
	 * Math.min(startArea.getBlockX(), endArea.getBlockX()), minZ =
	 * Math.min(startArea.getBlockZ(), endArea.getBlockZ()); int maxX =
	 * Math.max(startArea.getBlockX(), endArea.getBlockX()), maxZ =
	 * Math.max(startArea.getBlockZ(), endArea.getBlockZ()); while (true) { int
	 * x = rand.nextInt(maxX - minX) + minX; int z = rand.nextInt(maxZ - minZ) +
	 * minZ; Location l = new Location(w, x, 112, z);
	 * 
	 * if (l.getBlock().getType() == Material.AIR && l.add(0, -1,
	 * 0).getBlock().getType() == Material.AIR && l.add(0, -2,
	 * 0).getBlock().getType() != Material.AIR) { p.teleport(l); break; } } }
	 */

	public Player getMino() {
		for (OfflinePlayer op : Game.Minotaur.getTeam("The Minotaur").team.getPlayers()) {
			if (op.isOnline()) {
				return (Player) op;
			}
		}
		return null;
	}

	@Override
	public void onGameStart() {
		refillChests();

		Minigames.broadcast(ChatColor.RED, "%s is the minotaur!", getMino().getName());

		PlayerInventory pi = getMino().getInventory();
		pi.setHelmet(new ItemStack(Material.IRON_HELMET));
		pi.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		pi.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		pi.setBoots(new ItemStack(Material.IRON_BOOTS));
		pi.setItem(0, new ItemStack(Material.DIAMOND_SWORD));

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				for (Player p : Minigames.getOptedIn()) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 49));
					Minigames.send(ChatColor.RED, p, "You have %s seconds of invincibility!", "30");
				}
			}
		}, 20l);

		refill = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.YELLOW + "Chests have been refilled!");
				refillChests();
			}
		}, 600, 1200);
	}

	@Override
	public void onGameEnd() {
		refill.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if (getMino() == e.p) {
			Bukkit.broadcastMessage(ChatColor.RED + "The Minotaur Died!");
		} else {
			Minigames.broadcastDeath(e.p, e.k, "%s killed themselves", "%s was killed by the Minotaur (%s)");
		}
	}

}

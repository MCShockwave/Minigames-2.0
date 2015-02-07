package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

public class Minotaur implements IMinigame {

	Material[]			chests	= { Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS,
			Material.LEATHER_BOOTS, Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
			Material.DIAMOND_SWORD, Material.IRON_CHESTPLATE };

	public BukkitTask	refill	= null;

	public void refillChests() {
		for (Chunk c : Multiworld.getGame().getLoadedChunks()) {
			for (BlockState bs : c.getTileEntities()) {
				if (bs instanceof Chest) {
					Chest ch = (Chest) bs;

					ch.getBlockInventory().clear();

					for (int i = 0; i < rand.nextInt(6); i++) {
						ch.getBlockInventory().setItem(rand.nextInt(27),
								new ItemStack(chests[rand.nextInt(chests.length)]));
					}

					ch.update(true);
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

	public long	startTime	= 0;

	@Override
	public void onGameStart() {
		Minigames.showDefaultSidebar();

		Minigames.broadcast(ChatColor.RED, "%s is the minotaur!", getMino().getName());

		PlayerInventory pi = getMino().getInventory();
		if (Minigames.getOptedIn().size() > 12) {
			pi.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
			pi.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
			pi.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		} else {
			pi.setHelmet(new ItemStack(Material.IRON_HELMET));
			pi.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			pi.setBoots(new ItemStack(Material.IRON_BOOTS));
		}
		pi.setItem(0, new ItemStack(Material.DIAMOND_SWORD));

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				startTime = System.currentTimeMillis();
				for (Player p : Minigames.getOptedIn()) {
					Minigames.send(ChatColor.RED, p, "You have %s seconds of invincibility!", "30");
				}
			}
		}, 20l);

		refill = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.YELLOW + "Chests have been refilled!");
				refillChests();
			}
		}, 100, 600);
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

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && System.currentTimeMillis() < startTime + 30000) {
			event.setCancelled(true);
		}
	}

	@Override
	public void giveKit(Player p) {
	}

	@Override
	public Object determineWinner(Game g) {
		return g.getTeam("Humans").team;
	}

}

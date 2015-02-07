package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ghostbusters implements IMinigame {

	public GameTeam getGhosts() {
		return Game.Ghostbusters.getTeam("Ghosts");
	}

	public GameTeam getHumans() {
		return Game.Ghostbusters.getTeam("Humans");
	}

	@Override
	public void onGameStart() {
	}

	public void giveKit(Player p) {
		ItemStack[] items;
		if (Game.getTeam(p).color == getGhosts().color) {
			items = new ItemStack[] { new ItemStack(Material.DIAMOND_SWORD),
					new ItemStack(Material.POTION, 4, (short) 16428), new ItemStack(Material.POTION, 1, (short) 8229),
					new ItemStack(Material.POTION, 1, (short) 8229), new ItemStack(Material.POTION, 1, (short) 8229) };
			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
		} else {
			items = new ItemStack[] { new ItemStack(Material.WOOD_SWORD), new ItemStack(Material.GOLDEN_APPLE, 2) };
			p.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
			p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			p.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
		}

		p.getInventory().addItem(items);
	}

	@Override
	public void onGameEnd() {
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if (e.gt != null) {
			if (e.gt == getGhosts()) {
				Minigames.broadcastDeath(e.p, e.k, "%s was exterminated", "%s was ghostbusted by %s");
			} else {
				Minigames.broadcastDeath(e.p, e.k, "%s died of natural causes", "%s had their soul stolen by %s");
			}
		}
	}

	@EventHandler
	public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			if (event.getRegainReason() == RegainReason.SATIATED && Game.getTeam((Player) e) == getHumans()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		final Player p = event.getPlayer();
		final ItemStack it = event.getItem();
		if (it.getType() == Material.GOLDEN_APPLE) {
			Bukkit.getScheduler().runTask(plugin, new Runnable() {
				public void run() {
					p.removePotionEffect(PotionEffectType.ABSORPTION);
					p.removePotionEffect(PotionEffectType.REGENERATION);
					p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 170, 1));
				}
			});
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (Minigames.alivePlayers.contains(e.getPlayer().getName())) {
			if (e.getTo().getY() >= 125
					&& (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.STAINED_CLAY)) {
				Player p = e.getPlayer();
				Location l = p.getWorld().getHighestBlockAt(e.getFrom()).getLocation();
				l.setPitch(p.getLocation().getPitch());
				l.setYaw(p.getLocation().getYaw());
				e.setTo(l);
				MCShockwave.send(e.getPlayer(), "Do not climb the %s!", "hills");
			}
		}
	}

	@Override
	public Object determineWinner(Game g) {
		return null;
	}
}

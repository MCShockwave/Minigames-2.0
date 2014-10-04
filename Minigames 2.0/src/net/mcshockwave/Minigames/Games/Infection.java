package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Infection implements IMinigame {

	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);

			if (Minigames.hasItem(p, ShopItem.Floater)) {
				p.setAllowFlight(true);
			}
		}
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		Minigames.milkPlayer(p);
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000000, 1));
		// if IsHuman
		if (Game.getTeam(p) != null && Game.getTeam(p).color == ChatColor.YELLOW) {
			p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
			p.getInventory().addItem(new ItemStack(Material.BOW));
			p.getInventory().setItem(28, new ItemStack(Material.ARROW, 64));
		} else {
			p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
			p.getInventory().setHelmet(
					ItemMetaUtils.setLeatherColor(new ItemStack(Material.LEATHER_HELMET), Color.LIME));
		}
	}

	@Override
	public void onGameEnd() {
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if (e.gt == null || e.gt.team == null)
			return;
		if (e.gt.color == ChatColor.YELLOW && e.gt.team.getPlayers().size() <= 2) {
			for (OfflinePlayer op : e.gt.team.getPlayers()) {
				if (op.isOnline() && op.getPlayer() != e.p) {
					Minigames.stop(op.getPlayer());
					return;
				}
			}
		}
		if (e.gt.color == ChatColor.YELLOW && e.gt.team.getPlayers().size() < 2) {
			Minigames.stop(null);
			return;
		}

		if (e.gt != null && e.gt.color == ChatColor.YELLOW) {
			e.gt.team.removePlayer(e.p);
			Minigames.broadcastDeath(e.p, e.k, "%s was infected", "%s was infected by %s");
			Bukkit.getScoreboardManager().getMainScoreboard().getTeam("Zombies").addPlayer(e.p);
		}
		giveKit(e.p);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (ee instanceof Player && de instanceof Player) {
			Player p = (Player) ee;
			Player d = (Player) de;

			if (Minigames.hasItem(d, ShopItem.Biter)) {
				if (Game.getTeam(d).color == ChatColor.YELLOW) {
					event.setDamage(event.getDamage() + 1);
				}
				if (Game.getTeam(d).color == ChatColor.GREEN) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
				}
			}
		}
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();

		if (p.getGameMode() != GameMode.CREATIVE && Minigames.alivePlayers.contains(p.getName()) && event.isFlying()) {
			event.setCancelled(true);
			p.setFlying(false);
			if (p.getLocation().getBlock().getType() != Material.VINE
					&& (p.getLocation().add(0, -1, 0).getBlock().getType() != Material.AIR || p.getLocation()
							.add(0, -2, 0).getBlock().getType() != Material.AIR)) {
				p.setVelocity(p.getVelocity().add(new Vector(0, 1.5, 0)));
				p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 3, 1);
			} else
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.1, 0)));
		}

		if (p.getGameMode() != GameMode.CREATIVE && p.isFlying() && Minigames.alivePlayers.contains(p.getName())) {
			p.setFlying(false);
		}
	}

	public HashMap<Player, Long>	lurker	= new HashMap<>();

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player p = event.getPlayer();

		if (event.isSneaking() && Minigames.alivePlayers.contains(p.getName())) {
			if (Minigames.hasItem(p, ShopItem.Lurker)) {

				if (!lurker.containsKey(p) || lurker.containsKey(p) && lurker.get(p) < System.currentTimeMillis()) {
					lurker.remove(p);
					lurker.put(p, System.currentTimeMillis() + 30000);

					p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
				}

			}
		}
	}

}

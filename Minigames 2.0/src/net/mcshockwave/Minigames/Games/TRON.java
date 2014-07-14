package net.mcshockwave.Minigames.Games;

import java.util.HashMap;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class TRON implements IMinigame {

	HashMap<Block, Byte>		wool	= new HashMap<Block, Byte>();

	HashMap<Player, BukkitTask>	lmt		= new HashMap<Player, BukkitTask>();

	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			setMoveTime(p, 15);
			Minigames.send(ChatColor.RED, p, "Don't stop moving or you will %s!", "die");
			p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000000, 0));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 2));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onGameEnd() {
		for (Block b : wool.keySet()) {
			b.setData(wool.get(b));
		}
		for (BukkitTask bt : lmt.values()) {
			bt.cancel();
		}
		lmt.clear();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was killed", "%s was killed by %s");
		e.p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000000, 0));
		e.p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 2));
	}

	public void setMoveTime(final Player p, int time) {
		if (lmt.containsKey(p)) {
			lmt.get(p).cancel();
			lmt.remove(p);
		}

		lmt.put(p, Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				if (!Minigames.alivePlayers.contains(p.getName())) {
					return;
				}
				p.damage(p.getMaxHealth());
			}
		}, time * 20));
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		final Block w = p.getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (Minigames.alivePlayers.contains(p.getName()) && w.getType() == Material.WOOL) {
			byte tc = getTeamColor(p);
			if (w.getData() == 15 || w.getData() == 9) {
				if (!wool.containsKey(w)) {
					wool.put(w, w.getData());
				}
				setMoveTime(p, 3);
				w.setData(tc);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						w.setData(wool.get(w));
						wool.remove(w);
					}
				}, 200);
			} else if (w.getData() != getTeamColor(p)) {
				p.damage(p.getMaxHealth());
			}
		}
	}

	public byte getTeamColor(Player p) {
		GameTeam gt = Game.getTeam(p);
		if (gt != null) {
			if (gt.color == ChatColor.GREEN) {
				return 5;
			}
			if (gt.color == ChatColor.YELLOW) {
				return 4;
			}
		}
		return 15;
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
	}

}

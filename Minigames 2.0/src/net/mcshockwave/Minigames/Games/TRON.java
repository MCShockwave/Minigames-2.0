package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class TRON implements IMinigame {

	HashMap<Block, Byte>		blocks			= new HashMap<Block, Byte>();
	HashMap<Block, String>		blockPlayers	= new HashMap<Block, String>();

	HashMap<Player, BukkitTask>	lmt				= new HashMap<Player, BukkitTask>();

	@Override
	public void onGameStart() {
		Minigames.showDefaultSidebar();

		for (Player p : Minigames.getOptedIn()) {
			setMoveTime(p, 15);
			Minigames.send(ChatColor.RED, p, "Don't stop moving or you will %s!", "die");
			if (Game.hasElement("night-vision")) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
			}
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
		}
	}

	@Override
	public void onGameEnd() {
		for (BukkitTask bt : lmt.values()) {
			bt.cancel();
		}
		lmt.clear();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was killed", "%s was killed by %s");
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

		if (Minigames.alivePlayers.contains(p.getName()) && w != null) {
			byte tc = getTeamColor(p);
			// list.contains doesn't work
			if (w.getData() != 4 && w.getData() != 5) {
				blocks.put(w, w.getData());
				blockPlayers.put(w, p.getName());
				setMoveTime(p, 3);
				w.setData(tc);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						w.setData(blocks.get(w));
						blocks.remove(w);
						blockPlayers.remove(w);
					}
				}, 200);
			} else if (w.getData() != getTeamColor(p)) {
				Player k = null;
				if (blockPlayers.containsKey(w)) {
					String kn = blockPlayers.get(w);
					k = Bukkit.getPlayer(kn) == null ? null : Bukkit.getPlayer(kn);
				}
				if (k != null) {
					p.damage(p.getMaxHealth(), k);
				} else
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

}

package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaserTag implements IMinigame {

	private Location	yb1			= new Location(Minigames.getDefaultWorld(), -1475, 105, 2);
	private Location	yb2			= new Location(Minigames.getDefaultWorld(), -1475, 105, -2);
	private Location	gb1			= new Location(Minigames.getDefaultWorld(), -1525, 105, 2);
	private Location	gb2			= new Location(Minigames.getDefaultWorld(), -1525, 105, -2);

	private int			startp		= 100;

	Scoreboard			s;
	Score				yscore, gscore;
	Objective			o;

	ArrayList<String>	cooldown	= new ArrayList<String>();

	@SuppressWarnings("deprecation")
	@Override
	public void onGameStart() {
		cooldown.clear();
		yb1.getBlock().setType(Material.GLOWSTONE);
		yb2.getBlock().setType(Material.GLOWSTONE);
		gb1.getBlock().setType(Material.GLOWSTONE);
		gb2.getBlock().setType(Material.GLOWSTONE);
		s = Bukkit.getScoreboardManager().getMainScoreboard();
		o = s.registerNewObjective("Points", "dummy");
		o.setDisplayName(ChatColor.LIGHT_PURPLE + "Points Left");
		gscore = o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Green"));
		yscore = o.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + "Yellow"));
		gscore.setScore(startp);
		yscore.setScore(startp);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
		}
	}

	@Override
	public void onGameEnd() {
		o.unregister();
		cooldown.clear();
		yb1.getBlock().setType(Material.GLOWSTONE);
		yb2.getBlock().setType(Material.GLOWSTONE);
		gb1.getBlock().setType(Material.GLOWSTONE);
		gb2.getBlock().setType(Material.GLOWSTONE);
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		if (cooldown.contains(p.getName())) {
			cooldown.remove(p.getName());
		}
		if (getLives(e.gt) < 0) {
			Minigames.broadcastDeath(e.p, e.k, "%s was tagged for the final point!",
					"%s was tagged by %s for the final point!");
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInt(final PlayerInteractEvent e) {
		if (e.getPlayer().getItemInHand().getType() == Material.DIAMOND_HOE) {
			if (e.getAction() != Action.RIGHT_CLICK_AIR) {
				return;
			}
			if (cooldown.contains(e.getPlayer().getName())) {
				return;
			}
			if (Game.getTeam(e.getPlayer()).spawn.distance(e.getPlayer().getLocation()) < 5) {
				Minigames.send(e.getPlayer(), "You can not %s while in your base!", ChatColor.RED + "shoot");
				return;
			}
			cooldown.add(e.getPlayer().getName());
			Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
				public void run() {
					cooldown.remove(e.getPlayer().getName());
				}
			}, 30L);
			Player p = e.getPlayer();
			p.getWorld().playSound(p.getLocation(), Sound.BLAZE_HIT, 5, 10);
			BlockIterator bi = new BlockIterator(p.getEyeLocation(), 0D, 50);
			while (bi.hasNext()) {
				Block bl = bi.next();

				if (bl.getType() != Material.AIR) {
					break;
				}

				boolean bool = true;
				for (Entity ent : p.getNearbyEntities(50, 50, 50)) {
					if (ent instanceof Player) {
						Player pla = (Player) ent;
						Location end_loc = ent.getLocation();
						double distance = bl.getLocation().distanceSquared(end_loc);
						if (distance <= 4 && Minigames.alivePlayers.contains(pla.getName())
								&& Game.getTeam(pla).spawn.distanceSquared(pla.getLocation()) > 5 * 5) {
							bool = false;
							if (Game.getTeam(p) == Game.getTeam(pla)) {
								break;
							}
							pla.damage(pla.getHealth());
							addLives(Game.getTeam(pla), -1);
							checkPoints(Game.getTeam(pla));
							break;
						}
					}
				}
				if (!bool) {
					break;
				}
				PacketUtils.playParticleEffect(ParticleEffect.FIREWORKS_SPARK, bl.getLocation().add(0.5, 0.5, 0.5), 0,
						0.1f, 3);

				List<Block> bll = p.getLineOfSight(null, 50);
				final Block b = bll.get(bll.size() - 1);

				boolean bool1 = true;
				for (Block block : bll) {
					for (Entity ent : p.getNearbyEntities(50, 50, 50)) {
						if (ent instanceof Player) {
							Player pla = (Player) ent;
							double distance = block.getLocation().distanceSquared(ent.getLocation());
							if (distance <= 4 && Minigames.alivePlayers.contains(pla.getName())
									&& Game.getTeam(pla).spawn.distanceSquared(pla.getLocation()) > 5 * 5) {
								bool1 = false;
								GameTeam gt = Game.getTeam(pla);
								if (Game.getTeam(p) != gt) {
									pla.damage(pla.getHealth());
									addLives(Game.getTeam(pla), -1);
									checkPoints(Game.getTeam(pla));
								}
								break;
							} else {
								bool1 = true;
							}
						}
					}
				}
				if (bool1) {
					GameTeam gt = Game.getTeam(p);

					if (b.getType() == Material.GLOWSTONE) {
						if (gt.color == ChatColor.GREEN) {
							if (LocUtils.isSame(yb1, b.getLocation())) {
								yb1.getBlock().setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the §e§oYellow§7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										yb1.getBlock().setType(Material.GLOWSTONE);
									}
								}, 200l);
								for (GameTeam t : Game.Laser_Tag.teams) {
									if (t != gt) {
										addLives(t, -5);
										checkPoints(t);
									}
								}
							}
							if (LocUtils.isSame(yb2, b.getLocation())) {
								yb2.getBlock().setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the §e§oYellow§7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										yb2.getBlock().setType(Material.GLOWSTONE);
									}
								}, 200l);
								for (GameTeam t : Game.Laser_Tag.teams) {
									if (t != gt) {
										addLives(t, -5);
										checkPoints(t);
									}
								}
							}
						}
						if (gt.color == ChatColor.YELLOW) {
							if (LocUtils.isSame(gb1, b.getLocation())) {
								gb1.getBlock().setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the §a§oGreen§7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										gb1.getBlock().setType(Material.GLOWSTONE);
									}
								}, 200l);
								for (GameTeam t : Game.Laser_Tag.teams) {
									if (t != gt) {
										addLives(t, -5);
										checkPoints(t);
									}
								}
							}
							if (LocUtils.isSame(gb2, b.getLocation())) {
								gb2.getBlock().setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the §a§oGreen§7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										gb2.getBlock().setType(Material.GLOWSTONE);
									}
								}, 200l);
								for (GameTeam t : Game.Laser_Tag.teams) {
									if (t != gt) {
										addLives(t, -5);
										checkPoints(t);
									}
								}
							}
						}
					}

				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			((Player) event.getDamager()).sendMessage("§cNo pvp!");
			event.setCancelled(true);
		}
	}

	private void giveItems(Player p) {
		p.getInventory().clear();
		p.getInventory().addItem(new ItemStack(Material.DIAMOND_HOE));
	}

	private void addLives(GameTeam gt, int add) {
		setLives(gt, getLives(gt) + add);
	}

	private void setLives(GameTeam gt, int set) {
		if (gt.color == ChatColor.YELLOW) {
			yscore.setScore(set);
		}
		if (gt.color == ChatColor.GREEN) {
			gscore.setScore(set);
		}
		List<Integer> nums = Arrays.asList(new Integer[] { 150, 100, 75, 50, 25, 10, 5, 4, 3, 2, 1, 0 });
		if (nums.contains(set)) {
			Minigames.broadcast(ChatColor.LIGHT_PURPLE, "%s has %s points left!", gt.color + gt.name, set);
		}
	}

	private int getLives(GameTeam gt) {
		if (gt.color == ChatColor.YELLOW) {
			return yscore.getScore();
		} else if (gt.color == ChatColor.GREEN) {
			return gscore.getScore();
		}
		return 0;
	}

	private void checkPoints(GameTeam t) {
		if (getLives(t) <= 0) {
			GameTeam[] teams = Game.Laser_Tag.teams;
			if (t == teams[0]) {
				Minigames.stop(teams[1].team);
			}
			if (t == teams[1]) {
				Minigames.stop(teams[0].team);
			}
		}
	}
}

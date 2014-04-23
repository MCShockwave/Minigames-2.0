package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockIterator;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.Currency.LevelUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.minecraft.server.v1_7_R2.PacketPlayOutWorldParticles;

public class LaserTag implements IMinigame {
	
	private Location yb1 = new Location(Minigames.getDefaultWorld(), -1475, 104, 2);
	private Location yb2 = new Location(Minigames.getDefaultWorld(), -1475, 104, -2);
	private Location gb1 = new Location(Minigames.getDefaultWorld(), -1525, 104, 2);
	private Location gb2 = new Location(Minigames.getDefaultWorld(), -1525, 104, -2);
	
	private int startp = 120;
	private int gint = 120, yint = 120;
	
	Scoreboard s;
	Score yscore, gscore;
	Objective o;
	
	ArrayList<String> cooldown = new ArrayList<String>();
	
	@Override
	public void onGameStart() {
		cooldown.clear();
		yb1.getBlock().setType(Material.REDSTONE_LAMP_OFF);
		yb2.getBlock().setType(Material.REDSTONE_LAMP_OFF);
		gb1.getBlock().setType(Material.REDSTONE_LAMP_OFF);
		gb2.getBlock().setType(Material.REDSTONE_LAMP_OFF);
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
		yb1.getBlock().setType(Material.REDSTONE_LAMP_OFF);
		yb2.getBlock().setType(Material.REDSTONE_LAMP_OFF);
		gb1.getBlock().setType(Material.REDSTONE_LAMP_OFF);
		gb2.getBlock().setType(Material.REDSTONE_LAMP_OFF);
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		if (cooldown.contains(p.getName())) {
			cooldown.remove(p.getName());
		}
		if (getLives(e.gt) < 0) {
			Minigames.broadcastDeath(e.p, e.k, "%s was tagged for the final point!", "%s was tagged by %s for the final point!");
		}
		if (e.k != null) {
			Player k = e.k;
			Random rand = new Random();
			if (rand.nextInt(SQLTable.Settings.getInt("Setting", "XPChance", "Value")) == 0) {
				int minXp = SQLTable.Settings.getInt("Setting", "XPMin", "Value");
				int maxXp = SQLTable.Settings.getInt("Setting", "XPMax", "Value");
				LevelUtils.addXP(k, rand.nextInt(maxXp - minXp) + minXp, "killing " + p.getName(), true);
			}
		}
		PacketUtils.sendPacketGlobally(p.getEyeLocation(), 50, PacketUtils.generateParticles(ParticleEffect.LAVA, p.getEyeLocation(), 0, 1, 50));
		p.getWorld().playSound(p.getEyeLocation(), Sound.CHICKEN_EGG_POP, 1, 0);
		PlayerRespawnEvent pre = new PlayerRespawnEvent(p, p.getWorld().getSpawnLocation(), false);
		Bukkit.getPluginManager().callEvent(pre);
		p.teleport(pre.getRespawnLocation());
		addLives(e.gt, -1);
		GameTeam[] gta = Game.Laser_Tag.teams;
		for (GameTeam gt : gta) {
			if (gt == e.gt) {
				checkPoints(gt);
				break;
			}
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
			}, 2 * 20L);
			Player p = e.getPlayer();
			p.getWorld().playSound(p.getLocation(), Sound.BLAZE_HIT, 5, 10);
			BlockIterator bi = new BlockIterator(p.getEyeLocation(), 0D, 50);
			while (bi.hasNext()) {
				if (bi.next().getType() != Material.AIR) {
					break;
				}
				
				boolean bool = true;
			    for (Entity ent : p.getNearbyEntities(50, 50, 50)) {
			    	if (ent instanceof Player) {
			    		Location end_loc = ent.getLocation();
			    		int distance = (int) bi.next().getLocation().distance(end_loc);
			    		if(distance <= 1 && Minigames.getOptedIn().contains(ent)){
			    			bool = false;
			    			break;
			    		}
			    	}
			    }
			    if (!bool) {
			    	break;
			    }
				Location l = bi.next().getLocation();
				for (Player pl : Bukkit.getOnlinePlayers()) {
					((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldParticles("fireworksSpark", 
							(float) l.getX(),
							(float) l.getY(),
							(float) l.getZ(),
							0,0,0,
							0, 5));
				}
				
				List<Block> bll = p.getLineOfSight(null, 50);
				final Block b = bll.get(bll.size() - 1);
				
				boolean bool1 = true;
				for (Block block : bll) {
					for (Entity ent : p.getNearbyEntities(50, 50, 50)) {
						if (ent instanceof Player) {
							Player pla = (Player) ent;
							int distance = (int) block.getLocation().distance(ent.getLocation());
							if (distance <= 1 && Minigames.getOptedIn().contains(ent) && Game.getTeam(pla).spawn.distance(pla.getLocation()) < 5) {
								bool1 = false;
								GameTeam gt = Game.getTeam(pla);
								if (Game.getTeam(p) != gt) {
									this.onPlayerDeath(new DeathEvent(pla, p, DamageCause.CUSTOM, gt.team, gt));
								}
								break;
							} else {
								bool1 = true;
							}
						}
					}
				}
				if (bool1) {
					if (b.getType() == p.getTargetBlock(null, 50).getType()) {
						if (b.getType() == Material.REDSTONE_LAMP_OFF) {
							b.setType(Material.REDSTONE_BLOCK);
							GameTeam pt = Game.getTeam(p);
							for (GameTeam t : Game.Laser_Tag.teams) {
								if (t.color == ChatColor.GREEN && pt.color == ChatColor.GREEN) {
									if (b.getLocation() == yb1 || b.getLocation() == yb2) {
										Minigames.broadcast(ChatColor.RED, "%s has hit the " + ChatColor.YELLOW + "Yellow" + ChatColor.RESET + " team's base!", p.getName());
										if (b.getLocation() == yb1) {
											yb2.getBlock().setType(Material.REDSTONE_BLOCK);
										} else if (b.getLocation() == yb2) {
											yb1.getBlock().setType(Material.REDSTONE_BLOCK);
										}
										Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
											public void run() {
												yb1.getBlock().setType(Material.REDSTONE_LAMP_OFF);
												yb2.getBlock().setType(Material.REDSTONE_LAMP_OFF);
											}
										}, 5 * 20L);
										addLives(Game.getTeam(Game.Laser_Tag, "Yellow"), -5);
										checkPoints(Game.getTeam(Game.Laser_Tag, "Yellow"));
										break;
									}
								} else if (t.color == ChatColor.YELLOW && pt.color == ChatColor.YELLOW) {
									if (b.getLocation() == gb1 || b.getLocation() == gb2) {
										Minigames.broadcast(ChatColor.RED, "%s has hit the " + ChatColor.GREEN + "Green" + ChatColor.RESET + " team's base!", p.getName());
										if (b.getLocation() == gb1) {
											gb2.getBlock().setType(Material.REDSTONE_BLOCK);
										} else if (b.getLocation() == gb2) {
											gb1.getBlock().setType(Material.REDSTONE_BLOCK);
										}
										Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
											public void run() {
												gb1.getBlock().setType(Material.REDSTONE_LAMP_OFF);
												gb2.getBlock().setType(Material.REDSTONE_LAMP_OFF);
											}
										}, 5 * 20L);
										checkPoints(Game.getTeam(Game.Laser_Tag, "Green"));
										addLives(Game.getTeam(Game.Laser_Tag, "Green"), -5);
										break;
									}
								}
							}
							break;
						}
					}
				}
			}
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
			yint = set;
			yscore.setScore(set);
		}
		if (gt.color == ChatColor.GREEN) {
			yint = set;
			gscore.setScore(set);
		}
		List<Integer> nums = Arrays.asList(new Integer[] { 150, 100, 75, 50, 25, 10, 5, 4, 3, 2, 1, 0 });
		if (nums.contains(set)) {
			Minigames.broadcast(ChatColor.LIGHT_PURPLE, "%s has %s points left!", gt.color + gt.name, set);
		}
	}
	
	private int getLives(GameTeam gt) {
		if (gt.color == ChatColor.YELLOW) {
			return yint;
		} else if (gt.color == ChatColor.GREEN) {
			return gint;
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

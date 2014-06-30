package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;

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
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaserTag implements IMinigame {

	public Block		yb1, yb2, gb1, gb2;

	private int			startp		= 100;

	GameScore			yscore, gscore;

	ArrayList<String>	cooldown	= new ArrayList<String>();

	@Override
	public void onGameStart() {
		yb1 = Game.getBlock("yellow-base-1");
		yb2 = Game.getBlock("yellow-base-2");
		gb1 = Game.getBlock("green-base-1");
		gb2 = Game.getBlock("green-base-2");

		cooldown.clear();
		yb1.setType(Material.REDSTONE_LAMP_OFF);
		yb2.setType(Material.REDSTONE_LAMP_OFF);
		gb1.setType(Material.REDSTONE_LAMP_OFF);
		gb2.setType(Material.REDSTONE_LAMP_OFF);

		yscore = Sidebar.getNewScore("�ePoints Left", startp);
		gscore = Sidebar.getNewScore("�aPoints Left", startp);

		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
		}
	}

	@Override
	public void onGameEnd() {
		cooldown.clear();
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
			if (Game.getSpawn(Game.getTeam(e.getPlayer())).distanceSquared(e.getPlayer().getLocation()) < 5 * 5) {
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
								&& Game.getSpawn(Game.getTeam(pla)).distanceSquared(pla.getLocation()) > 5 * 5) {
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
									&& Game.getSpawn(Game.getTeam(pla)).distanceSquared(pla.getLocation()) > 5 * 5) {
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

					if (b.getType() == Material.REDSTONE_LAMP_OFF) {
						if (gt.color == ChatColor.GREEN) {
							if (LocUtils.isSame(yb1.getLocation(), b.getLocation())) {
								yb1.setType(Material.REDSTONE_BLOCK);
								yb2.setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the �e�oYellow�7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										yb1.setType(Material.REDSTONE_LAMP_OFF);
										yb2.setType(Material.REDSTONE_LAMP_OFF);
									}
								}, 200l);
								for (GameTeam t : Game.Laser_Tag.teams) {
									if (t != gt) {
										addLives(t, -5);
										checkPoints(t);
									}
								}
							}
							if (LocUtils.isSame(yb2.getLocation(), b.getLocation())) {
								yb2.setType(Material.REDSTONE_BLOCK);
								yb1.setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the �e�oYellow�7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										yb2.setType(Material.REDSTONE_LAMP_OFF);
										yb1.setType(Material.REDSTONE_LAMP_OFF);
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
							if (LocUtils.isSame(gb1.getLocation(), b.getLocation())) {
								gb1.setType(Material.REDSTONE_BLOCK);
								gb2.setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the �a�oGreen�7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										gb1.setType(Material.REDSTONE_LAMP_OFF);
										gb2.setType(Material.REDSTONE_LAMP_OFF);
									}
								}, 200l);
								for (GameTeam t : Game.Laser_Tag.teams) {
									if (t != gt) {
										addLives(t, -5);
										checkPoints(t);
									}
								}
							}
							if (LocUtils.isSame(gb2.getLocation(), b.getLocation())) {
								gb2.setType(Material.REDSTONE_BLOCK);
								gb1.setType(Material.REDSTONE_BLOCK);
								Minigames.broadcast(gt.color, "%s has hit the �a�oGreen�7 base!", p.getName());
								Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
									public void run() {
										gb2.setType(Material.REDSTONE_LAMP_OFF);
										gb1.setType(Material.REDSTONE_LAMP_OFF);
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
			((Player) event.getDamager()).sendMessage("�cNo pvp!");
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
			yscore.setVal(set);
		}
		if (gt.color == ChatColor.GREEN) {
			gscore.setVal(set);
		}
		List<Integer> nums = Arrays.asList(new Integer[] { 150, 100, 75, 50, 25, 10, 5, 4, 3, 2, 1, 0 });
		if (nums.contains(set)) {
			Minigames.broadcast(ChatColor.LIGHT_PURPLE, "%s has %s points left!", gt.color + gt.name, set);
		}
	}

	private int getLives(GameTeam gt) {
		if (gt.color == ChatColor.YELLOW) {
			return yscore.getVal();
		} else if (gt.color == ChatColor.GREEN) {
			return gscore.getVal();
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

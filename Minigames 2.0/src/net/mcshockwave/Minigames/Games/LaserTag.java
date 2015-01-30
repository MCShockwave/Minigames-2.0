package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.CooldownUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.PreGame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;
import net.mcshockwave.Minigames.Utils.LaserTagMapGenerator;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class LaserTag implements IMinigame {

	public Block		yb1, yb2, gb1, gb2;

	public int			neededPoints	= 0, maxCharge = 1000, maxBaseHealth = 50;
	public BukkitTask	charge			= null;

	GameScore			yscore, gscore, needed;

	@Override
	public void onGameStart() {
		neededPoints = Minigames.getOptedIn().size() * 100;

		yb1 = Game.getBlock("yellow-base-1");
		yb2 = Game.getBlock("yellow-base-2");
		gb1 = Game.getBlock("green-base-1");
		gb2 = Game.getBlock("green-base-2");

		for (Block b : new Block[] { yb1, yb2, gb1, gb2 }) {
			baseHealth.put(b, maxBaseHealth);
		}

		needed = Sidebar.getNewScore("§c - NEEDED -", neededPoints);
		yscore = Sidebar.getNewScore("§eYellow Points", 0);
		gscore = Sidebar.getNewScore("§aGreen Points", 0);

		charge = new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				for (Player p : Minigames.getOptedIn()) {
					if (!Minigames.alivePlayers.contains(p.getName()))
						continue;
					if (p.getLevel() <= 0) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2));
					} else {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
					}
					int lvlSet = p.getLevel();
					if (lvlSet > 0) {
						for (Block b : Game.getTeam(p).color == ChatColor.GREEN ? new Block[] { yb1, yb2 }
								: new Block[] { gb1, gb2 }) {
							if (b.getLocation().distanceSquared(p.getLocation()) < 10 * 10) {
								lvlSet -= 1;
							}
						}
					}
					Block on = p.getLocation().clone().add(0, -1, 0).getBlock();
					if (on.getType() == Material.STAINED_CLAY
							&& on.getData() == (Game.getTeam(p).color == ChatColor.GREEN ? 5 : 4)) {
						lvlSet += rand.nextInt(10) + 10;
					}
					if (lvlSet > maxCharge) {
						lvlSet = maxCharge;
					}
					if (lvlSet <= 0) {
						lvlSet = 0;
						if (p.getLevel() > 0) {
							disableMsgs(p, null);
						}
					}
					p.setLevel(lvlSet);
				}
			}
		}.runTaskTimer(plugin, 1, 1);

		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
			p.setLevel(maxCharge);
		}
	}

	public void disableMsgs(Player d, Player k) {
		d.getWorld().playSound(d.getLocation(), Sound.WITHER_SPAWN, 2, 2);
		Minigames.broadcastDeath(d, k, "%s was disabled from standing around an enemy base", "%s was disabled by %s");
		Minigames.send(ChatColor.RED, d, "You are %s! You cannot shoot! Go back to your base to %s!", "disabled",
				"recharge");
		if (k != null) {
			addPoints(Game.getTeam(k), 20);
		}
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
		ItemStack it = event.getItem();
		Action a = event.getAction();
		if (!CooldownUtils.isOnCooldown("LT-Shooter", p.getName()) && a.name().contains("RIGHT_CLICK") && it != null
				&& it.getType() == Material.DIAMOND_HOE) {
			event.setCancelled(true);
			if (p.getLevel() <= 0) {
				Minigames.send(ChatColor.RED, p, "You cannot shoot while %s!", "disabled");
				return;
			}
			Location st = p.getEyeLocation();
			Vector vel = p.getEyeLocation().getDirection().normalize();
			double spr = 50;
			vel.add(new Vector(rand.nextGaussian() / spr, rand.nextGaussian() / spr, rand.nextGaussian() / spr));

			st.getWorld().playSound(st, Sound.BLAZE_HIT, 2, 2);
			int rep = 0;
			int maxDis = 500;
			while (st.getBlock().getType().isTransparent() && ++rep < maxDis) {
				st.add(vel);
				PacketUtils.playParticleEffect(ParticleEffect.MAGIC_CRIT, st, 0.05f, 0.05f, 2);
				for (Player c : Minigames.getOptedIn()) {
					if (!Minigames.alivePlayers.contains(c.getName()) || Game.getTeam(c) != null
							&& Game.getTeam(p) != null && Game.getTeam(p).color == Game.getTeam(c).color) {
						continue;
					}
					double rad = 1;
					if (c.getLocation().distanceSquared(st) < rad * rad
							|| c.getEyeLocation().distanceSquared(st) < rad * rad) {
						rep = maxDis;
						if (c.getLevel() > 0) {
							c.damage(0);
							int dmg = rand.nextInt(50) + 150;
							int lvlSet = c.getLevel() - dmg;
							if (lvlSet <= 0) {
								disableMsgs(c, p);
								lvlSet = 0;
							} else
								c.getWorld().playSound(c.getLocation(), Sound.BLAZE_HIT, 2, 0);
							c.setLevel(lvlSet);
						} else {
							p.sendMessage("§cPlayer is disabled!");
							p.playSound(p.getLocation(), Sound.BLAZE_DEATH, 10, 2);
						}
					}
				}
			}
			final Block hit = st.getBlock();
			if (hit.getType() == Material.BEACON && baseHealth.containsKey(hit)) {
				int hp = baseHealth.get(hit);
				if (hp > 0
						&& (Game.getTeam(p).color == ChatColor.GREEN ? (hit.equals(yb1) || hit.equals(yb2)) : (hit
								.equals(gb1) || hit.equals(gb2)))) {
					hp--;
					hit.getWorld().playEffect(hit.getLocation(), Effect.STEP_SOUND, Material.BEACON);
					hit.getWorld().playSound(hit.getLocation(), Sound.WITHER_HURT, 10, 2);
					if (hp <= 0) {
						Minigames.broadcast(Game.getTeam(p).color, "%s destroyed the %s base!", p.getName(),
								(Game.getTeam(p).color == ChatColor.GREEN ? "§e§oYellow" : "§a§oGreen"));
						hit.setType(Material.OBSIDIAN);
						addPoints(Game.getTeam(p), 200);
						hp = 0;
						CooldownUtils.addCooldown("LT-Base", hit.getLocation().toString(), 600, new Runnable() {
							public void run() {
								hit.setType(Material.BEACON);
								baseHealth.remove(hit);
								baseHealth.put(hit, maxBaseHealth);
							}
						});
					}
					baseHealth.remove(hit);
					baseHealth.put(hit, hp);
				}
			}
			CooldownUtils.addCooldown("LT-Shooter", p.getName(), 10);
		}
	}

	public HashMap<Block, Integer>	baseHealth	= new HashMap<>();

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			((Player) event.getDamager()).sendMessage("§cNo melee!");
			event.setCancelled(true);
		}
	}

	public void giveItems(Player p) {
		p.getInventory().clear();
		p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.DIAMOND_HOE), "§fLaser Gun"));
	}

	public void addPoints(GameTeam gt, int add) {
		setPoints(gt, getPoints(gt) + add);
	}

	public void setPoints(GameTeam gt, int set) {
		if (set / 100 > (gt.color == ChatColor.GREEN ? gscore.getVal() : yscore.getVal()) / 100) {
			Minigames.broadcast(gt.color, "%s has reached %s points!", gt.name, (set / 100) * 100);
		}
		if (gt.color == ChatColor.YELLOW) {
			yscore.setVal(set);
		}
		if (gt.color == ChatColor.GREEN) {
			gscore.setVal(set);
		}
		checkPoints(gt);
	}

	public int getPoints(GameTeam gt) {
		if (gt.color == ChatColor.YELLOW) {
			return yscore.getVal();
		} else if (gt.color == ChatColor.GREEN) {
			return gscore.getVal();
		}
		return 0;
	}

	public void checkPoints(GameTeam t) {
		if (getPoints(t) >= neededPoints) {
			GameTeam[] teams = Game.Laser_Tag.teams;
			if (t == teams[0]) {
				Minigames.stop(teams[0].team);
			}
			if (t == teams[1]) {
				Minigames.stop(teams[1].team);
			}
		}
	}

	@PreGame
	public void generateMap() {
		if (Minigames.currentMap.name.equalsIgnoreCase("Random")) {
			Bukkit.broadcastMessage("§6Starting generation of map...");
			LaserTagMapGenerator.generate(Multiworld.getGame().getSpawnLocation().clone().add(0, -1, 0), 75);
			Bukkit.broadcastMessage("§aDone!");
		}
	}
}

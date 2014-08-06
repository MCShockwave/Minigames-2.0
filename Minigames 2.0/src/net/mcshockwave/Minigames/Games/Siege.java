package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Siege implements IMinigame {

	Villager			yv			= null;
	Villager			gv			= null;

	BukkitTask			bt			= null;

	public GameScore	yhp			= null;
	public GameScore	ghp			= null;

	int					startHealth	= 50;

	public BukkitTask	btc			= null;

	@Override
	public void onGameStart() {
		yhp = Sidebar.getNewScore("§eYellow King", startHealth);
		ghp = Sidebar.getNewScore("§aGreen King", startHealth);

		final Location yl = Game.getLocation("yellow-king");
		final Location gl = Game.getLocation("green-king");

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				yl.getChunk().load();
				gl.getChunk().load();

				yv = (Villager) Multiworld.getGame().spawnEntity(yl, EntityType.VILLAGER);
				gv = (Villager) Multiworld.getGame().spawnEntity(gl, EntityType.VILLAGER);

				yv.setCustomName(ChatColor.YELLOW + "Yellow King");
				yv.setCustomNameVisible(true);
				yv.setMaxHealth(startHealth);
				yv.setHealth(startHealth);

				gv.setCustomName(ChatColor.GREEN + "Green King");
				gv.setCustomNameVisible(true);
				gv.setMaxHealth(startHealth);
				gv.setHealth(startHealth);
			}
		}, 10l);

		bt = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				if (yv.isValid() || !yv.isDead()) {
					yv.teleport(yl);
				}
				if (gv.isValid() || !gv.isDead()) {
					gv.teleport(gl);
				}
			}
		}, 20l, 10l);

		btc = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (FallingBlock fb : catapult) {
					if (fb.isDead() || !fb.isValid()) {
						continue;
					}

					fb.getWorld().playSound(fb.getLocation(), Sound.FIZZ, 4, 1);

					PacketUtils.playParticleEffect(ParticleEffect.FLAME, fb.getLocation(), 0.5f, 0.05f, 4);
					PacketUtils.playParticleEffect(ParticleEffect.FIREWORKS_SPARK, fb.getLocation(), 0.5f, 0.05f, 4);

					if (fb.getTicksLived() < 10) {
						continue;
					}

					int[] ch = { -2, -1, 0, 1, 2 };

					for (int x : ch) {
						for (int y : ch) {
							for (int z : ch) {
								Block b = fb.getLocation().getBlock().getRelative(x, y, z);
								if (b.getType() != Material.AIR) {
									explode(fb.getLocation().getBlock(), fb);
									catapult.remove(fb);
									fb.remove();
								}
							}
						}
					}
				}
			}
		}, 1, 1);

		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);
		}
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
		Minigames.milkPlayer(p);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
	}

	@Override
	public void onGameEnd() {
		try {
			yv.remove();
		} catch (Exception e) {
		}
		try {
			gv.remove();
		} catch (Exception e) {
		}
		btc.cancel();
		bt.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if (e.gt.color == ChatColor.YELLOW && (yv.isDead() || !yv.isValid())) {
			Minigames.broadcastDeath(e.p, e.k, "%s was eliminated", "%s was eliminated by %s");
			Minigames.setDead(e.p, false);
		} else if (e.gt.color == ChatColor.GREEN && (gv.isDead() || !gv.isValid())) {
			Minigames.broadcastDeath(e.p, e.k, "%s was eliminated", "%s was eliminated by %s");
			Minigames.setDead(e.p, false);
		} else {
			giveKit(e.p);
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity e = event.getEntity();

		if (e instanceof Villager) {
			Villager v = (Villager) e;
			if (v == yv) {
				Minigames.broadcastAll(Minigames.getBroadcastMessage(ChatColor.YELLOW,
						"The %s Villager has died!\n%s can no longer respawn!", "Yellow", "Yellow"));
				yhp.remove();
			}
			if (v == gv) {
				Minigames.broadcastAll(Minigames.getBroadcastMessage(ChatColor.GREEN,
						"The %s Villager has died!\n%s can no longer respawn!", "Green", "Green"));
				ghp.remove();
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (de instanceof Player) {
			Player d = (Player) de;

			if (ee instanceof Villager) {
				final Villager v = (Villager) ee;
				if (v == yv && Game.getTeam(d) != null && Game.getTeam(d).color == ChatColor.YELLOW) {
					event.setCancelled(true);
				}
				if (v == gv && Game.getTeam(d) != null && Game.getTeam(d).color == ChatColor.GREEN) {
					event.setCancelled(true);
				}

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						if (v == yv) {
							yhp.setVal((int) yv.getHealth());
						}
						if (v == gv) {
							ghp.setVal((int) gv.getHealth());
						}
					}
				}, 1);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (Minigames.alivePlayers.contains(e.getPlayer().getName())) {
			if (e.getTo().getY() >= 120
					&& (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SNOW_BLOCK
							|| e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SNOW || e.getTo()
							.getBlock().getRelative(BlockFace.DOWN).getType() == Material.STONE)) {

				Player p = e.getPlayer();
				Location l = p.getWorld().getHighestBlockAt(e.getFrom()).getLocation();
				l.setPitch(p.getLocation().getPitch());
				l.setYaw(p.getLocation().getYaw());
				e.setTo(l);
				MCShockwave.send(e.getPlayer(), "Do not climb the %s!", "mountains");
			}
		}
	}

	public String[][]	cata	= { { "XXXX#", "XXX#X", "XX#XX", "X#XXX", "#XXXX" },
			{ "XX#XX", "X#XXX", "X#XXX", "X#XXX", "#XXXX" }, { "X#XXX", "X#XXX", "X#XXX", "X#XXX", "X#XXX" } };

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Action a = event.getAction();
		final Block b = event.getClickedBlock();

		if (Minigames.alivePlayers.contains(p.getName()) && a == Action.RIGHT_CLICK_BLOCK && b != null
				&& b.getType() == Material.STONE_BUTTON) {
			Button btn = (Button) b.getState().getData();
			final BlockFace rel = btn.getAttachedFace();
			final BlockFace rot = rotate(rel);
			final Block wl = b.getRelative(rel);
			if (wl.getType() == Material.WOOL && wl.getData() == 5) {
				wl.setData((byte) 15);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						wl.setData((byte) 5);
						set(wl, cata[0], rel, rot);
					}
				}, 600);
				wl.getWorld().playSound(wl.getLocation(), Sound.DOOR_OPEN, 10, 0);

				SchedulerUtils util = SchedulerUtils.getNew();
				for (final String[] c : cata) {
					util.add(new Runnable() {
						public void run() {
							set(wl, c, rel, rot);
						}
					});
					util.add(5);
				}
				util.add(new Runnable() {
					public void run() {
						Location shoot = wl.getRelative(rel.getModX(), 4, rel.getModZ()).getLocation();

						FallingBlock fb = shoot.getWorld().spawnFallingBlock(shoot, Material.COBBLESTONE, (byte) 0);
						fb.setDropItem(false);
						fb.setFireTicks(Integer.MAX_VALUE);

						Vector vel = new Vector(-rot.getModX() * 2, 1.1, -rot.getModZ() * 2);
						double spr = 0.4;
						Vector velSpr = vel.clone().add(
								new Vector(rand.nextGaussian() * spr, rand.nextGaussian() * (spr / 4), rand
										.nextGaussian() * spr));

						fb.setVelocity(velSpr);

						catapult.add(fb);
					}
				});
				util.execute();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void set(Block origin, String[] setTo, BlockFace rel, BlockFace rot) {
		int x = origin.getLocation().getBlockX() + rel.getModX();
		int y = origin.getLocation().getBlockY() + setTo.length;
		int z = origin.getLocation().getBlockZ() + rel.getModZ();

		for (int i = 0; i < setTo.length; i++) {
			String s = setTo[i];
			for (int i2 = 0; i2 < s.length(); i2++) {
				Block set = origin.getWorld().getBlockAt(x + (rot.getModX() * i2), y - i - 1, z + (rot.getModZ() * i2));

				char bl = s.charAt(i2);
				if (bl == '#') {
					set.setType(Material.WOOD);
					set.setData((byte) 0);
				} else {
					set.setType(Material.AIR);
				}
			}
		}
	}

	public BlockFace rotate(BlockFace bf) {
		switch (bf) {
			case NORTH:
				return BlockFace.EAST;
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.NORTH;
			default:
				return null;
		}
	}

	public static ArrayList<FallingBlock>	catapult	= new ArrayList<>();

	public void explode(Block b, FallingBlock cr) {
		if (!catapult.contains(cr)) {
			return;
		}

		Location l = b.getLocation();
		l.getWorld().createExplosion(l, 6);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList()) {
			if (rand.nextInt(4) == 0) {
				final FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation(),
						b.getType() == Material.GRASS ? Material.DIRT : b.getType(), (byte) 0);
				fb.setDropItem(false);
				fb.setVelocity(Vector.getRandom().multiply(2).subtract(Vector.getRandom()).add(new Vector(0, 0.5, 0)));
				new BukkitRunnable() {
					public void run() {
						for (Entity e : fb.getNearbyEntities(2, 2, 2)) {
							if (e instanceof Damageable
									&& (e instanceof Player && Minigames.alivePlayers.contains(((Player) e).getName()) || !(e instanceof Player))) {
								((Damageable) e).damage(e.getTicksLived() / 2, fb);
							}
						}

						if (!fb.isValid() || fb.isDead()) {
							cancel();
						}
					}
				}.runTaskTimer(plugin, 3, 3);
			}
		}
	}

}

package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Button;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.ItemUtils;
import net.mcshockwave.Minigames.worlds.Multiworld;

public class Boarding implements IMinigame {

	int gr = 50, yr = 50;

	GameScore gs, ys;

	HashMap<Player, Long> lastFireTime = new HashMap<Player, Long>();
	private final int reloadTime = 10;

	@Override
	public void onGameStart() {
		Game g = Game.Boarding;
		int rein = Math.min(g.teams[0].team.getSize() * 5, g.teams[1].team.getSize() * 5);
		gr = rein;
		yr = rein;

		gs = Sidebar.getNewScore("§aReinforcements", gr);
		ys = Sidebar.getNewScore("§eReinforcements", yr);

		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);
		}
	}

	public void giveKit(Player p) {
		if (!Minigames.alivePlayers.contains(p.getName()))
			return;
		Minigames.clearInv(p);
		PlayerInventory pi = p.getInventory();
		pi.addItem(ItemMetaUtils.setItemName(new ItemStack(Material.IRON_SWORD), ChatColor.RESET + "Steel Sword"));
		pi.addItem(ItemMetaUtils.setItemName(new ItemStack(Material.IRON_AXE), ChatColor.RESET + "Musket"));
		pi.setItem(8, new ItemStack(Material.SULPHUR, 2));
		pi.setItem(7, ItemMetaUtils.setItemName(new ItemStack(Material.BUCKET), "§bFire Extinguisher"));
		if (Minigames.hasItem(p, ShopItem.Buccaneer)) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 0));
			p.setAllowFlight(true);
		}
		if (Game.hasElement("potion-effects") && Game.getString("potion-effects").equalsIgnoreCase("true")) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000000, 3));
		}
	}

	@Override
	public void onGameEnd() {
		lastFireTime.clear();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		giveKit(e.p);

		lastFireTime.remove(e.p);
		lastFireTime.put(e.p, System.currentTimeMillis() + 2000);

		if (e.k != null) {
			addReinforcements(e.gt, -1);
		}

		if (getReinforcements(e.gt) < 0) {
			GameTeam[] teams = Game.Boarding.teams;
			if (e.gt == teams[0]) {
				Minigames.stop(teams[1].team);
			}
			if (e.gt == teams[1]) {
				Minigames.stop(teams[0].team);
			}
		}

		if (e.k != null) {
			if (Minigames.hasItem(e.k, ShopItem.Privateer)) {
				e.k.setHealth(e.k.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
			if (Minigames.hasItem(e.k, ShopItem.Scavenger)) {
				if (e.k.getInventory().getItem(8) == null) {
					e.k.getInventory().setItem(8, new ItemStack(Material.SULPHUR, 1));
				} else {
					e.k.getInventory().addItem(new ItemStack(Material.SULPHUR, 1));
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity pe = event.getEntity();
		Entity de = event.getDamager();

		if (pe instanceof Player) {
			Player p = (Player) pe;

			if (!Minigames.alivePlayers.contains(p.getName())) {
				return;
			}

			if (de instanceof Arrow && Minigames.hasItem(p, ShopItem.Privateer)) {
				event.setDamage(event.getDamage() / 7.5);
			}

			if (de instanceof Player) {
				Player d = (Player) de;

				if (Minigames.hasItem(d, ShopItem.Buccaneer)) {
					event.setDamage(event.getDamage() + 1);
				}
			}

			if (!p.isBlocking()) {
				return;
			}

			if (de instanceof Player) {

				PacketUtils.playBlockParticles(new MaterialData(Material.IRON_BLOCK), p.getEyeLocation());
				p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1.5f);

				event.setDamage(event.getDamage() * 0.15);

			} else if (de instanceof Arrow) {
				event.setDamage(event.getDamage() * 0.33);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();
		final Block b = event.getClickedBlock();

		if (it != null && it.getType() == Material.IRON_AXE && a.name().contains("RIGHT_CLICK")) {
			event.setCancelled(true);

			if (!lastFireTime.containsKey(p)) {
				lastFireTime.put(p, (long) 0);
			}
			if (ItemUtils.hasItems(p, new ItemStack(Material.SULPHUR, 1))) {
				if (lastFireTime.get(p) < System.currentTimeMillis()) {
					p.getInventory().removeItem(new ItemStack(Material.SULPHUR, 1));
					p.updateInventory();

					Arrow ar = p.launchProjectile(Arrow.class);
					ar.setVelocity(ar.getVelocity().multiply(10));

					p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, 1, 0);
					Location play = p.getEyeLocation().add(p.getLocation().getDirection());
					play.getWorld().playEffect(play, Effect.SMOKE, 0);

					lastFireTime.remove(p);
					lastFireTime.put(p, System.currentTimeMillis() + (reloadTime * 1000));
					int reloadTimeCus = reloadTime - (Minigames.hasItem(p, ShopItem.Scavenger) ? 2 : 0);
					Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
						public void run() {
							Minigames.send(ChatColor.GREEN, p, "Musket is now %s!", "reloaded");
						}
					}, reloadTimeCus * 20);
				} else {
					Minigames.send(ChatColor.RED, p, "Musket is %s!", "reloading");
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
				}
			} else {
				Minigames.send(ChatColor.RED, p, "Musket is %s!", "out of ammo");
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
			}
		}

		if (Minigames.alivePlayers.contains(p.getName()) && a == Action.RIGHT_CLICK_BLOCK && b != null
				&& b.getType() == Material.STONE_BUTTON) {
			Button btn = (Button) b.getState().getData();
			BlockFace rel = btn.getAttachedFace();
			final Block wl = b.getRelative(rel);
			if (wl.getType() == Material.WOOL && wl.getData() == 5) {
				wl.setData((byte) 15);
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						wl.setData((byte) 5);
					}
				}, 600);

				Vector vel = new Vector(0, 0.9, rel.getModZ() * 2);

				Location ch = wl.getLocation();
				ch.add(new Vector(0, 0, rel.getModZ() * 2));
				int check = 10;
				for (int i = -check; i <= check; i++) {
					boolean norsou = (rel == BlockFace.NORTH || rel == BlockFace.SOUTH);
					Location cur = ch.clone().add(norsou ? i : 0, 0, norsou ? 0 : i);
					if (cur.getBlock().getType() == Material.COAL_BLOCK) {
						TNTPrimed tnt = (TNTPrimed) cur.getWorld().spawnEntity(cur.clone().add(0, 0, rel.getModZ()),
								EntityType.PRIMED_TNT);
						tnt.setFuseTicks(60);
						double spr = 0.2;
						Vector velSpr = vel.clone()
								.add(new Vector(rand.nextGaussian() * spr, 0.2, rand.nextGaussian() * spr));
						if (Game.hasElement("cannon-power")) {
							velSpr.multiply(Game.getDouble("cannon-power"));
						}
						tnt.setVelocity(velSpr);
						tnt.setFireTicks(Integer.MAX_VALUE);
						tnt.setIsIncendiary(true);

						cur.getWorld().playSound(cur, Sound.ENTITY_WITHER_SHOOT, 10, 2);
					}
				}
			}
		}

		if (Minigames.alivePlayers.contains(p.getName()) && a.name().contains("RIGHT_CLICK") && it != null
				&& it.getType() == Material.BUCKET) {
			Location[] bs = rayCast(p.getEyeLocation(), p.getLocation().getDirection(), 500, true);
			final Location tl = bs[bs.length - 1];
			int count = 0;
			for (final Location l : bs) {
				count++;
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						PacketUtils.playParticleEffect(Particle.EXPLOSION_NORMAL, l, 0, 0.01f, 2);
					}
				}, count);
			}
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					int rad = 2;
					PacketUtils.playParticleEffect(Particle.DRIP_WATER, tl, rad, 0F, 50);
					tl.getWorld().playEffect(tl, Effect.EXTINGUISH, 0);
					for (int x = tl.getBlockX() - rad; x < tl.getBlockX() + rad; x++) {
						for (int y = tl.getBlockY() - rad; y < tl.getBlockY() + rad; y++) {
							for (int z = tl.getBlockZ() - rad; z < tl.getBlockZ() + rad; z++) {
								Block b = tl.getWorld().getBlockAt(x, y, z);
								if (b.getType() == Material.FIRE) {
									b.setType(Material.AIR);
								}
							}
						}
					}
				}
			}, count);
		}

		if (Game.hasElement("enable-large-cannons")
				&& Game.getString("enable-large-cannons").equalsIgnoreCase("true")) {
			if (Minigames.alivePlayers.contains(p.getName()) && a == Action.RIGHT_CLICK_BLOCK && b != null
					&& b.getType() == Material.STONE_BUTTON) {
				Button bt = (Button) b.getState().getData();
				final Block beh = b.getRelative(bt.getAttachedFace());
				final int dir = bt.getAttachedFace() == BlockFace.NORTH ? -1 : 1;
				Location l = b.getLocation();
				Block finB = null;
				if (beh.getType() == Material.BEDROCK) {
					beh.setType(Material.SOUL_SAND);
					final ArrayList<Block> blocks = new ArrayList<>();
					SchedulerUtils ut = SchedulerUtils.getNew();
					ut.add(new Runnable() {
						public void run() {
							b.getWorld().playSound(b.getLocation(), Sound.ENTITY_WITHER_SPAWN, 10, 0.6f);
						}
					});
					for (int i = 0; i < 12; i++) {
						int j = i * dir;
						for (int x = -3; x <= 3; x++) {
							for (int y = -3; y <= 3; y++) {
								final Block check = Multiworld.getGame().getBlockAt(l.getBlockX() + x,
										l.getBlockY() + y, l.getBlockZ() + j);
								if (check.getType() == Material.STAINED_CLAY) {
									ut.add(new Runnable() {
										public void run() {
											check.setData((byte) 4);
										}
									});
									finB = check;
									blocks.add(check);
								}
							}
						}
						ut.add(2);
					}
					final Block fin = finB;
					ut.add(new Runnable() {
						public void run() {
							if (fin != null) {
								fin.getWorld().playSound(fin.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 10, 2);

								Vector vel = new Vector((rand.nextDouble() * 2 - 1) / 2, 0, dir);
								Location start = fin.getLocation().add(0.5, 0.5, 0.5);
								for (int i = 0; i < 200; i++) {
									PacketUtils.playParticleEffect(Particle.FIREWORKS_SPARK, start, 0, 0.2f, 3);
									start.add(vel);
									if (!start.getBlock().getType().isTransparent()) {
										start.getWorld().createExplosion(start, 12);
										break;
									}
								}
							}

							for (Block bl : blocks) {
								bl.setData((byte) 14);
							}
						}
					});
					ut.add(600);
					ut.add(new Runnable() {
						public void run() {
							beh.setType(Material.BEDROCK);
						}
					});

					ut.execute();
				}
			}
		}
	}

	public static final Material[] nobreak = { Material.IRON_BLOCK, Material.DOUBLE_STEP, Material.IRON_FENCE,
			Material.STAINED_GLASS, Material.REDSTONE_LAMP_ON, Material.REDSTONE_BLOCK };

	public void setReinforcements(GameTeam gt, int set) {
		if (gt.color == ChatColor.YELLOW) {
			yr = set;
			ys.setVal(set);
		}
		if (gt.color == ChatColor.GREEN) {
			gr = set;
			gs.setVal(set);
		}
		List<Integer> display = Arrays.asList(new Integer[] { 50, 40, 30, 20, 15, 10, 5, 4, 3, 2, 1, 0 });
		if (display.contains(set)) {
			Minigames.broadcast(gt.color, "%s has %s reinforcements left!", gt.name, set);
		}
	}

	public void addReinforcements(GameTeam gt, int add) {
		setReinforcements(gt, getReinforcements(gt) + add);
	}

	public int getReinforcements(GameTeam gt) {
		if (gt.color == ChatColor.YELLOW) {
			return yr;
		} else if (gt.color == ChatColor.GREEN) {
			return gr;
		}
		return 0;
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();

		if (p.getGameMode() != GameMode.CREATIVE && Minigames.alivePlayers.contains(p.getName()) && event.isFlying()) {
			event.setCancelled(true);
			p.setFlying(false);
			if (p.getLocation().add(0, -1, 0).getBlock().getType() != Material.AIR
					|| p.getLocation().add(0, -2, 0).getBlock().getType() != Material.AIR) {
				p.setVelocity(p.getVelocity().add(new Vector(0, 1.5, 0)));
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 3, 1);
			} else
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.1, 0)));
		}

		if (p.getGameMode() != GameMode.CREATIVE && p.isFlying() && Minigames.alivePlayers.contains(p.getName())) {
			p.setFlying(false);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList().toArray(new Block[0])) {
			if (Arrays.asList(nobreak).contains(b.getType())) {
				event.blockList().remove(b);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (Game.hasElement("potion-effects") && Game.getString("potion-effects").equalsIgnoreCase("true")
				&& event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
	}

	public Location[] rayCast(Location start, Vector vec, int distance, boolean grav) {
		ArrayList<Location> cast = new ArrayList<>();
		Location s = start.clone();
		Vector v = vec.clone();

		for (int i = 0; i < distance; i++) {
			s = s.add(v);
			if (grav) {
				v.add(new Vector(0, -0.025F, 0));
			}
			if (!s.getBlock().getType().isTransparent()) {
				break;
			}
			cast.add(s.clone());
		}

		return cast.toArray(new Location[0]);
	}

	@Override
	public Object determineWinner(Game g) {
		if (gs.getVal() > ys.getVal()) {
			return g.getTeam("Green").team;
		} else if (ys.getVal() > gs.getVal()) {
			return g.getTeam("Yellow").team;
		}
		return null;
	}
}

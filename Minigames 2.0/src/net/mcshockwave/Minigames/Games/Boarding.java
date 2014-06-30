package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.ItemUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Button;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Boarding implements IMinigame {

	int						gr				= 50, yr = 50;

	GameScore				gs, ys;

	HashMap<Player, Long>	lastFireTime	= new HashMap<Player, Long>();
	private final int		reloadTime		= 10;

	@Override
	public void onGameStart() {
		Game g = Game.getInstance(this);
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
			GameTeam[] teams = Game.getInstance(this).teams;
			if (e.gt == teams[0]) {
				Minigames.stop(teams[1].team);
			}
			if (e.gt == teams[1]) {
				Minigames.stop(teams[0].team);
			}
		}

		if (e.k != null) {
			if (Minigames.hasItem(e.k, ShopItem.Privateer)) {
				e.k.setHealth(e.k.getMaxHealth());
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

				PacketUtils.sendPacketGlobally(p.getLocation(), 20,
						PacketUtils.generateBlockParticles(Material.IRON_BLOCK, 0, p.getEyeLocation()));
				p.getWorld().playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1.5f);

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
		Block b = event.getClickedBlock();

		if (it != null && it.getType() == Material.IRON_AXE && a.name().contains("RIGHT_CLICK")) {
			event.setCancelled(true);

			if (!lastFireTime.containsKey(p)) {
				lastFireTime.put(p, (long) 0);
			}
			if (lastFireTime.get(p) < System.currentTimeMillis()) {
				if (ItemUtils.hasItems(p, new ItemStack(Material.SULPHUR, 1))) {
					p.getInventory().removeItem(new ItemStack(Material.SULPHUR, 1));
					p.updateInventory();

					Arrow ar = p.launchProjectile(Arrow.class);
					ar.setVelocity(ar.getVelocity().multiply(10));

					p.getWorld().playSound(p.getLocation(), Sound.ZOMBIE_WOOD, 1, 0);
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
					Minigames.send(ChatColor.RED, p, "Musket is %s!", "out of ammo");
					p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
				}
			} else {
				Minigames.send(ChatColor.RED, p, "Musket is %s!", "reloading");
				p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
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
					Location cur = ch.clone().add(i, 0, 0);
					if (cur.getBlock().getType() == Material.COAL_BLOCK) {
						TNTPrimed tnt = (TNTPrimed) cur.getWorld().spawnEntity(cur.clone().add(0, 0, rel.getModZ()),
								EntityType.PRIMED_TNT);
						tnt.setFuseTicks(40);
						double spr = 0.2;
						Vector velSpr = vel.clone().add(
								new Vector(rand.nextGaussian() * spr, rand.nextGaussian(), rand.nextGaussian() * spr));
						tnt.setVelocity(velSpr);
						tnt.setFireTicks(Integer.MAX_VALUE);
						tnt.setIsIncendiary(true);

						cur.getWorld().playSound(cur, Sound.WITHER_SHOOT, 10, 2);
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
						PacketUtils.playParticleEffect(ParticleEffect.EXPLODE, l, 0, 0.01f, 2);
					}
				}, count);
			}
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					int rad = 2;
					PacketUtils.playParticleEffect(ParticleEffect.DRIP_WATER, tl, rad, 0F, 50);
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
	}

	public static final Material[]	nobreak	= { Material.IRON_BLOCK, Material.DOUBLE_STEP, Material.IRON_FENCE };

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
				p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 3, 1);
			} else
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.1, 0)));
		}

		if (p.getGameMode() != GameMode.CREATIVE && p.isFlying() && Minigames.alivePlayers.contains(p.getName())) {
			p.setFlying(false);
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
}

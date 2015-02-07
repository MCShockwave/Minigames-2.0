package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.FireworkLaunchUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.LocUtils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Airships implements IMinigame {

	public void onGameStart() {
		Minigames.showDefaultSidebar();

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				Minigames.broadcast("You will get your %s in 10 seconds! Spread out!", "arrow");
			}
		}, 5);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				for (Player p : Minigames.getOptedIn()) {
					if (!Minigames.alivePlayers.contains(p.getName()))
						continue;
					p.getInventory().addItem(new ItemStack(Material.ARROW));
				}
				Minigames.broadcast("%s given out! Fight!", "Arrows");
			}
		}, 205);

		Minigames.timer.add(Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (Player p : Minigames.getOptedIn()) {
					if (Minigames.alivePlayers.contains(p.getName())
							&& p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR
							&& (p.getLocation().getY() % 1) <= 0.2 && !p.isFlying()) {

						p.getWorld().createExplosion(p.getLocation(), 3);
						for (int i = 0; i < 10; i++) {

							Location l = LocUtils.addRand(p.getLocation(), 8, 4, 8);
							FireworkLaunchUtils.playFirework(l, Color.BLACK, Color.GRAY);

						}

						p.damage(p.getMaxHealth());
					}

					if (p.getHealth() <= 6) {
						PacketUtils
								.playParticleEffect(ParticleEffect.FLAME, p.getLocation().add(0, 1, 0), 0, 0.05f, 10);
					}
				}
			}
		}, 1, 2));
	}

	public void onGameEnd() {
		for (Player p : Minigames.getOptedIn()) {
			p.setAllowFlight(false);
			p.removePotionEffect(PotionEffectType.NIGHT_VISION);
			p.setFireTicks(0);
		}
	}

	public void onPlayerDeath(DeathEvent e) {
		e.p.setAllowFlight(false);
		e.p.setFireTicks(0);

		Minigames.broadcastDeath(e.p, e.k, "%s crashed into the ground", "%s was killed by %s");
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			event.setCancelled(true);
		}

		if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Player p = (Player) event.getEntity();
			Arrow a = (Arrow) event.getDamager();

			if (a.getShooter() instanceof Player) {
				Player d = (Player) a.getShooter();

				if (Minigames.hasItem(d, ShopItem.Venom) && rand.nextInt(4) == 0) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity e = event.getEntity();
		if (event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
			return;
		}
		if (e instanceof Player && event.getCause() != DamageCause.CUSTOM) {
			Player p = (Player) e;

			if (!Minigames.alivePlayers.contains(p.getName())) {
				event.setCancelled(true);
				return;
			}

			double health = p.getHealth();
			double dam = event.getDamage();

			if (health - dam <= 0) {
				event.setCancelled(true);
				p.setHealth(20f);
				p.setAllowFlight(false);

				Minigames.send(p, "Your %s has been destroyed!", "engine");

				FireworkLaunchUtils.playFirework(p.getEyeLocation(), Color.RED, Color.ORANGE);
			}
		}
	}

	@EventHandler
	public void onPlayerRegen(EntityRegainHealthEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (it.getType() == Material.BOW && a.name().contains("RIGHT_CLICK")
				&& p.hasPotionEffect(PotionEffectType.CONFUSION)) {
			event.setCancelled(true);
			Minigames.send(p, "Your %s has been disabled!", "weapon");
		}

		if (it.getType() == Material.TNT && a == Action.RIGHT_CLICK_AIR) {
			p.setItemInHand(null);

			final TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.PRIMED_TNT);
			tnt.setVelocity(p.getLocation().getDirection().multiply(2));

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					tnt.getWorld().playSound(tnt.getLocation(), Sound.EXPLODE, 10, 1);
					for (int i = 0; i < 100; i++) {
						Arrow a = tnt.getWorld().spawnArrow(tnt.getLocation(),
								new Vector(rand.nextGaussian() * 2, rand.nextFloat(), rand.nextGaussian() * 2),
								rand.nextInt(5), 0);
						float spread = 8;
						float x = (rand.nextFloat() - rand.nextFloat()) / spread;
						float y = rand.nextFloat() / spread;
						float z = (rand.nextFloat() - rand.nextFloat()) / spread;
						a.setVelocity(new Vector(x, y, z).multiply(15));
					}
					tnt.remove();
				}
			}, 20l);
		}

		if (it.getType() == Material.NETHER_STAR && a == Action.RIGHT_CLICK_AIR) {
			p.setItemInHand(null);

			p.getWorld().playSound(p.getLocation(), Sound.FIZZ, 3, 1);

			int ra = 8;
			for (Entity e : p.getNearbyEntities(ra, ra, ra)) {
				if (e instanceof Player) {
					Player h = (Player) e;
					if (!Minigames.alivePlayers.contains(h.getName()))
						continue;

					h.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList()) {
			FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation(),
					b.getType() == Material.GRASS ? Material.DIRT : b.getType(), (byte) 0);
			fb.setDropItem(false);
			fb.setVelocity(Vector.getRandom().multiply(2).subtract(Vector.getRandom()).add(new Vector(0, 0.5, 0)));
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		Location to = event.getTo();

		if (Minigames.alivePlayers.contains(p.getName())) {
			if (to.getY() > Game.getDouble("max-y")) {
				if (p.isFlying()) {
					p.setFlying(false);
				}
			}
		}
	}

	@Override
	public void giveKit(Player p) {
		p.getInventory().addItem(
				ItemMetaUtils.addEnchantment(new ItemStack(Material.BOW), Enchantment.ARROW_INFINITE, 1));
		p.setAllowFlight(true);
		p.setFlying(true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 0));
		if (Minigames.hasItem(p, ShopItem.Demoman)) {
			p.getInventory().setItem(8, new ItemStack(Material.TNT));
		}
		if (Minigames.hasItem(p, ShopItem.Jammer)) {
			p.getInventory().setItem(8, new ItemStack(Material.NETHER_STAR));
		}
	}

	@Override
	public Object determineWinner(Game g) {
		return null;
	}

}

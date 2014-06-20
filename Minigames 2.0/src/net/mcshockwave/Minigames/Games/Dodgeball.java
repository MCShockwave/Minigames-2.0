package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;
import java.util.HashMap;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Shop.ShopItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Dodgeball implements IMinigame {

	Vector[]			drop		= { new Vector(-16, 116, -201), new Vector(-11, 116, -201),
			new Vector(-7, 116, -201), new Vector(-2, 116, -201), new Vector(2, 116, -201), new Vector(7, 116, -201),
			new Vector(11, 116, -201), new Vector(16, 116, -201) };

	BukkitTask			sn			= null;

	public ArrayList<Player>	canBeHit	= new ArrayList<>();

	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			if (Minigames.hasItem(p, ShopItem.Athlete)) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000000, 1));
			}
			if (Minigames.hasItem(p, ShopItem.Catcher)) {
				canBeHit.add(p);
			}
		}
		sn = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				dropDodgeballs();
			}
		}, 20, 600);
	}

	@Override
	public void onGameEnd() {
		sn.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was killed", "%s was hit by %s");
	}

	public void dropDodgeballs() {
		for (Vector v : drop) {
			Item i = w.dropItem(new Location(w, v.getBlockX() + 0.5, v.getY(), v.getBlockZ() + 0.5), new ItemStack(
					Material.SNOW_BALL));
			i.setVelocity(new Vector());
		}
		Minigames.broadcast("%s have been dropped!", "Dodgeballs");
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Snowball) {
			if (event.getEntity() instanceof Player) {
				Player p = (Player) event.getEntity();
				if (canBeHit.contains(p)) {
					canBeHit.remove(p);
					event.setDamage(0f);
					p.getWorld().playSound(p.getLocation(), Sound.HURT_FLESH, 1, 1);
					return;
				}
				event.setDamage(20f);
				for (ItemStack it : p.getInventory().getContents()) {
					if (it != null && it.getType() == Material.SNOW_BALL) {
						for (int i = 0; i < it.getAmount(); i++) {
							w.dropItem(event.getEntity().getLocation(), new ItemStack(Material.SNOW_BALL));
						}
					}
				}
				Minigames.clearInv(p);
			}
		} else {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		Location l = p.getLocation();
		GameTeam gt = Game.getTeam(p);
		if (gt != null) {
			if (gt.color == ChatColor.GREEN && l.getZ() <= -200) {
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.25, 1)));
			}
			if (gt.color == ChatColor.YELLOW && l.getZ() >= -200) {
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.25, -1)));
			}
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile e = event.getEntity();
		if (event.getEntity().getType() == EntityType.SNOWBALL) {
			BlockIterator iterator = new BlockIterator(e.getWorld(), e.getLocation().toVector(), e.getVelocity()
					.normalize(), 0, 4);
			Block hitBlock = null;

			while (iterator.hasNext()) {
				hitBlock = iterator.next();
				if (hitBlock.getType() != Material.AIR)
					break;
			}
			if (hitBlock != null && hitBlock.getType() != Material.AIR) {
				event.getEntity().getWorld()
						.dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.SNOW_BALL));
			}
		}
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.SNOWBALL) {
			if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
				Player t = (Player) event.getEntity().getShooter();

				if (Minigames.hasItem(t, ShopItem.Catcher)) {
					event.getEntity().setVelocity(
							event.getEntity()
									.getVelocity()
									.add(new Vector(rand.nextGaussian() / 5, rand.nextGaussian() / 5, rand
											.nextGaussian() / 5)));
				}
			}
		}
	}

	HashMap<Player, Long>	magCool	= new HashMap<>();

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player p = event.getPlayer();

		if (event.isSneaking() && Minigames.alivePlayers.contains(p.getName())) {
			if (Minigames.hasItem(p, ShopItem.Magician)) {

				if (!magCool.containsKey(p) || magCool.containsKey(p) && magCool.get(p) < System.currentTimeMillis()) {
					magCool.remove(p);
					magCool.put(p, System.currentTimeMillis() + 10000);

					for (Entity e : p.getNearbyEntities(50, 50, 50)) {
						if (e instanceof Snowball) {
							e.getWorld().dropItemNaturally(e.getLocation(), new ItemStack(Material.SNOW_BALL));
							e.getWorld().playEffect(e.getLocation(), Effect.ENDER_SIGNAL, 0);
							e.remove();
						}
					}
					
					p.getWorld().playSound(p.getLocation(), Sound.PORTAL_TRIGGER, 5, 2);
				}

			}
		}
	}

}

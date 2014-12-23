package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dodgeball implements IMinigame {

	static final int	dropCount	= 8;

	BukkitTask			sn			= null;

	ArrayList<Player>	canBeHit	= new ArrayList<>();

	public void onGameStart() {
		Minigames.showDefaultSidebar();

		for (Player p : Minigames.getOptedIn()) {
			if (Minigames.hasItem(p, ShopItem.Athlete)) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000000, 1));
			}
			if (Minigames.hasItem(p, ShopItem.Catcher)) {
				canBeHit.add(p);
			}
			p.getInventory().setItem(8, getClay(5, Game.getTeam(p)));
			if (p.getGameMode() != GameMode.SURVIVAL) {
				p.setGameMode(GameMode.SURVIVAL);
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
		if (e.k != null) {
			e.k.getInventory().addItem(getClay(3, Game.getTeam(e.k)));
		}
	}

	public ItemStack getClay(int count, GameTeam te) {
		if (te != null) {
			short data = (short) (te.color == ChatColor.GREEN ? 5 : 4);
			return new ItemStack(Material.STAINED_CLAY, count, data);
		} else {
			return new ItemStack(Material.AIR);
		}
	}

	public void dropDodgeballs() {
		for (int index = 1; index <= dropCount; index++) {
			Location l = Game.getLocation("dodgeball-" + index);
			Item i = Multiworld.getGame().dropItem(l, new ItemStack(Material.SNOW_BALL));
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
							Multiworld.getGame().dropItem(event.getEntity().getLocation(),
									new ItemStack(Material.SNOW_BALL));
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
			if (gt.color == ChatColor.GREEN && l.getZ() <= Game.getDouble("border-z")) {
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.25, 1)));
			}
			if (gt.color == ChatColor.YELLOW && l.getZ() >= Game.getDouble("border-z")) {
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.25, -1)));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile e = event.getEntity();
		if (event.getEntity().getType() == EntityType.SNOWBALL) {
			BlockIterator iterator = new BlockIterator(e.getWorld(), e.getLocation().toVector(), e.getVelocity()
					.normalize(), 0, 4);
			Block hit = null;

			List<Block> aff = new ArrayList<>();
			while (iterator.hasNext()) {
				hit = iterator.next();
				if (hit.getType() != Material.AIR) {
					break;
				}
			}
			if (hit != null && hit.getType() != Material.AIR) {
				event.getEntity().getWorld()
						.dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.SNOW_BALL));
			}

			aff.add(hit);
			if (e.getShooter() != null && e.getShooter() instanceof Player) {
				for (BlockFace bf : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
						BlockFace.UP, BlockFace.DOWN }) {
					if (rand.nextInt(3) == 0)
						continue;
					aff.add(hit.getRelative(bf));
				}
			}
			for (Block hb : aff) {
				if (hb.getType() == Material.SNOW_BLOCK || hb.getType() == Material.SNOW
						|| (hb.getType() == Material.STAINED_CLAY && (hb.getData() == 5 || hb.getData() == 4))) {
					hb.getWorld().playEffect(hb.getLocation(), Effect.STEP_SOUND, hb.getType());
					hb.setType(Material.AIR);
				}
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

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		event.setCancelled(false);
	}

}

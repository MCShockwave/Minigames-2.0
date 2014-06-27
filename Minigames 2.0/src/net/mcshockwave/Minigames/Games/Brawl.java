package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Brawl implements IMinigame {

	public Player	b1		= null, b2 = null;

	Location		l1		= new Location(Multiworld.getGame(), 301.5, 134, -780, 270, 0);
	Location		l2		= new Location(Multiworld.getGame(), 306.5, 134, -780, 90, 0);

	public long		invin	= 0;

	@Override
	public void onGameStart() {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				selectRandoms();
			}
		}, 50);
	}

	@Override
	public void onGameEnd() {
		b1 = null;
		b2 = null;
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if (b1 == e.p || b2 == e.p) {
			Minigames.broadcastDeath(e.p, e.k, "%s fell off the tower", "%s was knocked off the tower by %s");
			if (e.p == b1) {
				b2.teleport(Game.getFFASpawn());
			} else {
				b1.teleport(Game.getFFASpawn());
			}
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					selectRandoms();
				}
			}, 2);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
	}

	public Player getRandom() {
		Player ret = null;
		int times = 0;
		while (ret == null) {
			ret = Minigames.getOptedIn().get(rand.nextInt(Minigames.getOptedIn().size()));

			if (ret == b1 || ret == b2 || !Minigames.alivePlayers.contains(ret.getName())) {
				ret = null;
			}
			times++;
			if (times > 10000) {
				break;
			}
		}
		return ret;
	}

	public void selectRandoms() {
		if (b1 != null && b2 != null) {
			if (Minigames.alivePlayers.contains(b1.getName())) {
				b1.setAllowFlight(false);
				Minigames.clearInv(b1);
			}
			if (Minigames.alivePlayers.contains(b2.getName())) {
				b2.setAllowFlight(false);
				Minigames.clearInv(b2);
			}
		}

		b1 = null;
		b2 = null;

		b1 = getRandom();
		b2 = getRandom();

		if (b1 == null || b2 == null) {
			return;
		}

		invin = System.currentTimeMillis() + 3000;

		b1.teleport(l1);
		b2.teleport(l2);

		specAbil(b1);
		specAbil(b2);

		PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20, 10);
		b1.addPotionEffect(slow);
		b2.addPotionEffect(slow);

		Minigames.broadcast("%s VS. %s", b1.getName(), b2.getName());
	}

	public void specAbil(Player p) {
		if (Minigames.hasItem(p, ShopItem.Ruthless)) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.STICK), "§rWooden Pole"));
		}
		if (Minigames.hasItem(p, ShopItem.Panther)) {
			p.setAllowFlight(true);
		}
		if (Minigames.hasItem(p, ShopItem.Outcast)) {
			p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity e = event.getEntity();
		Entity d = event.getDamager();

		if ((b1 == e && b2 == d || b1 == d && b2 == e) && System.currentTimeMillis() >= invin) {
			if (Minigames.hasItem((Player) d, ShopItem.Outcast)
					&& ((Player) d).getItemInHand().getType() == Material.WOOD_SWORD) {
				event.setDamage(1);
			} else
				event.setDamage(0);

			if (Minigames.hasItem((Player) d, ShopItem.Ruthless)
					&& ((Player) d).getItemInHand().getType() == Material.STICK) {
				((Player) e).removePotionEffect(PotionEffectType.CONFUSION);
				((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
			}
		} else {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();

		if (p.getGameMode() != GameMode.CREATIVE && Minigames.alivePlayers.contains(p.getName()) && event.isFlying()) {
			event.setCancelled(true);
			p.setVelocity(p.getVelocity().add(new Vector(0, 2, 0)));
			p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 3, 1);
			p.setAllowFlight(false);
		}
	}

}

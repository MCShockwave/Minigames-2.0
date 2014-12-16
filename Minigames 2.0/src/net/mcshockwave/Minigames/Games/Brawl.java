package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class Brawl implements IMinigame {

	public Player	b1		= null, b2 = null;

	public long		invin	= 0;
	
	public ArrayList<String> selection = new ArrayList<>();

	@Override
	public void onGameStart() {
		Minigames.showDefaultSidebar();
		
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				selectRandoms();
			}
		}, 50);
	}
	
	public void resetSelectionList() {
		for (String s : Minigames.alivePlayers) {
			selection.add(s);
		}
	}

	@Override
	public void onGameEnd() {
		b1 = null;
		b2 = null;
		selection.clear();
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
		if (selection.contains(e.p.getName())) {
			selection.remove(e.p.getName());
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
			if (selection.size() < 1) {
				resetSelectionList();
			}
			
			String name = selection.get(rand.nextInt(selection.size()));
			ret = Bukkit.getPlayer(name);
			
			if (ret == null && name != null) {
				selection.remove(name);
			}

			if (ret == b1 || ret == b2 || !Minigames.alivePlayers.contains(ret.getName())) {
				ret = null;
			}
			if (++times > 10000) {
				break;
			}
		}
		selection.remove(ret.getName());
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

		b1.teleport(Game.getLocation("brawl-spawn-1"));
		b2.teleport(Game.getLocation("brawl-spawn-2"));

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

}

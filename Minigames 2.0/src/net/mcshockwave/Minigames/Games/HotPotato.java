package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HotPotato implements IMinigame {

	List<Player>						potPl	= new ArrayList<Player>();

	HashMap<Item, Player>				thrPo	= new HashMap<Item, Player>();

	public HashMap<String, GameScore>	hp		= new HashMap<>();

	@Override
	public void onGameStart() {
		int max = 3;
		int size = Minigames.getOptedIn().size();
		for (int i = 0; i < (size > max ? max : size > 1 ? size - 1 : size); i++) {
			Player p = Game.getRandomPlayer();
			if (!potPl.contains(p)) {
				selectNewPlayer(p, null);
			} else {
				i--;
			}
		}

		for (Player p : Minigames.getOptedIn()) {
			updateHP(p);
		}
	}

	public void updateHP(Player p) {
		if (hp.containsKey(p.getName())) {
			hp.get(p.getName()).setVal((int) p.getHealth());
		} else {
			hp.put(p.getName(), Sidebar.getNewScore("§o" + p.getName(), (int) p.getHealth()));
		}
	}

	public void updateHPForAll() {
		for (String n : hp.keySet()) {
			if (Minigames.alivePlayers.contains(n) && Bukkit.getPlayer(n) != null) {
				updateHP(Bukkit.getPlayer(n));
			}
		}
	}

	@Override
	public void onGameEnd() {
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, null, "%s burned to death", "");
		if (potPl.contains(e.p)) {
			selectNewPlayer(Game.getRandomPlayer(), e.p);
			e.p.setFireTicks(0);
		}

		hp.get(e.p.getName()).remove();
	}

	public void selectNewPlayer(Player p, Player old) {
		if (p == old) {
			selectNewPlayer(Game.getRandomPlayer(), old);
			return;
		}

		if (old != null) {
			potPl.remove(old);
			old.setFireTicks(0);
		}

		potPl.add(p);
		p.getInventory().addItem(new ItemStack(Material.BAKED_POTATO));

		Minigames.send(p, "Throw the potato by %s with it in your hand!", "right-clicking");
		p.setFireTicks(99999999);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (a.name().contains("RIGHT_CLICK")) {

			if (it.getType() == Material.BAKED_POTATO) {
				event.setCancelled(true);
				event.setUseItemInHand(Result.DENY);
				it.setType(Material.AIR);
				p.setItemInHand(it);
				final Item pro = p.getWorld().dropItem(
						p.getEyeLocation().add(p.getLocation().getDirection().multiply(2)),
						new ItemStack(Material.BAKED_POTATO));
				pro.setVelocity(p.getLocation().getDirection());
				pro.setPickupDelay(0);
				thrPo.put(pro, p);

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						if ((!pro.isDead() || pro.isValid()) && Minigames.alivePlayers.contains(p.getName())) {
							p.getInventory().addItem(new ItemStack(Material.BAKED_POTATO));
							Minigames.send(p, "You missed!");
							pro.remove();
							thrPo.remove(pro);
						}
					}
				}, 60L);
			}

		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		Item i = event.getItem();

		if (thrPo.containsKey(i) && Minigames.alivePlayers.contains(p.getName())) {
			event.setCancelled(true);
			Player t = thrPo.get(i);

			if (potPl.contains(p) || t == p) {
				return;
			}

			i.remove();
			selectNewPlayer(p, t);

		}

	}

	@EventHandler
	public void onPlayerRegen(EntityRegainHealthEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FIRE_TICK) {
			event.setDamage(2f);
		}
		if (event.getEntity() instanceof Player) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					updateHP((Player) event.getEntity());					
				}
			}, 1);
		}
	}
}

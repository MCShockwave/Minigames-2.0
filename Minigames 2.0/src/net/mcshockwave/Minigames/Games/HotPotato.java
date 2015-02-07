package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.CooldownUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HotPotato implements IMinigame {

	public boolean hasPotato(Player p) {
		return p.getInventory().contains(Material.BAKED_POTATO);
	}

	@Override
	public void onGameStart() {
		int max = 3;
		int size = Minigames.getOptedIn().size();
		for (int i = 0; i < (size > max ? max : size > 1 ? size - 1 : size); i++) {
			Player p = Game.getRandomPlayer();
			if (!hasPotato(p)) {
				selectNewPlayer(p, null);
			} else {
				i--;
			}
		}

		Minigames.showDefaultSidebar();
	}

	@Override
	public void onGameEnd() {
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, null, "%s burned to death", "");
		if (hasPotato(e.p)) {
			selectNewPlayer(getNearestPlayerTo(e.p), e.p);
			e.p.setFireTicks(0);
		}
	}

	public void selectNewPlayer(Player p, Player old) {
		if (p.equals(old)) {
			return;
		}

		if (old != null) {
			old.setFireTicks(0);
			old.getInventory().clear();
			old.removePotionEffect(PotionEffectType.SPEED);
		}

		p.getInventory().addItem(new ItemStack(Material.BAKED_POTATO));
		p.getInventory().setItem(17, new ItemStack(Material.BAKED_POTATO));

		Minigames.send(p, "Get rid of the potato by %s another player!", "left-clicking");
		p.setFireTicks(Integer.MAX_VALUE);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
	}

	@EventHandler
	public void onPlayerRegen(EntityRegainHealthEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			event.setCancelled(true);

			Player p = (Player) event.getEntity();
			Player d = (Player) event.getDamager();

			if (Minigames.alivePlayers.contains(p.getName())) {
				if (!CooldownUtils.isOnCooldown("HotPotato", p.getName())) {
					if (hasPotato(p)) {
						return;
					}

					CooldownUtils.addCooldown("HotPotato", d.getName(), 60);
					selectNewPlayer(p, d);
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FIRE_TICK) {
			event.setDamage(2f);
		}
	}

	public Player getNearestPlayerTo(Player p) {
		double dis = -1;
		Player r = null;
		for (String cn : Minigames.alivePlayers) {
			Player c = Bukkit.getPlayer(cn);
			if (c != null && !hasPotato(c)) {
				double cdis = c.getLocation().distanceSquared(p.getLocation()); // DoUEvnCDis
				if (!c.equals(p) && dis == -1 || cdis < dis) {
					dis = cdis;
					r = c;
				}
			}
		}
		return r;
	}

	@Override
	public void giveKit(Player p) {
	}

	@Override
	public Object determineWinner(Game g) {
		return null;
	}
}

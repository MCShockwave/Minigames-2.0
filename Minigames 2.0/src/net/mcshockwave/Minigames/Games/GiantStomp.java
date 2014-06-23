package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Utils.ShockwaveUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class GiantStomp implements IMinigame {

	public Giant		giant		= null;

	public BukkitTask	giantTask;

	boolean				stomping	= false;

	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
		}

		// TODO set to giantstomp game object
		final Location spawn = Game.Airships.lobby;

		giant = (Giant) spawn.getWorld().spawnEntity(spawn, EntityType.GIANT);
		giant.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10));

		// Yet another Minigames 1.0 port
		giantTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				if (giant.getLocation().getBlockX() != spawn.getBlockX()
						&& giant.getLocation().getBlockZ() != spawn.getBlockZ()) {
					Location tp = spawn.clone();
					tp.setY(giant.getLocation().getY());
					giant.teleport(tp);
				}
				if (rand.nextInt(10) == 1 && !stomping) {
					stomping = true;
					giant.setVelocity(new Vector(0, 2, 0));
				}
			}
		}, 0L, 2L);
	}

	private void giveItems(Player p) {
		Minigames.clearInv(p);
		Minigames.milkPlayer(p);
		p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
	}

	@Override
	public void onGameEnd() {
		giant.remove();
		giantTask.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was stomped out", "%s was murdered by %s");
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.GIANT) {
			event.setCancelled(true);
			if (event.getCause() == DamageCause.FALL && stomping) {
				stomping = false;
				ShockwaveUtils.makeShockwave(giant.getLocation(), 25);
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Giant || e.getEntity() instanceof Giant) {
			e.setCancelled(true);
		}
	}

}

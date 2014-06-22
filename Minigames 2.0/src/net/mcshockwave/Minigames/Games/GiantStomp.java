package net.mcshockwave.Minigames.Games;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Utils.LocUtils;

public class GiantStomp implements IMinigame {

	public Entity g = null;

	@SuppressWarnings("deprecation")
	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);

			Location giantspawn = new Location(Minigames.getDefaultWorld(),
					1300, 106, -200);

			Minigames.getDefaultWorld().spawnCreature(giantspawn,
					EntityType.GIANT);
			for (LivingEntity ent : Minigames.getDefaultWorld()
					.getLivingEntities()) {
				if (ent.getType().equals(EntityType.GIANT)) {
					g = ent;
				}
				((Giant) g).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20000, 255), true);
				((Giant) g).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20000, 255), true);

			}
			{

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						jump();
					}
				}, 6 * 20);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void giveItems(Player p) {
		p.getInventory().clear();
		p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
		p.updateInventory();

	}

	@Override
	public void onGameEnd() {
		((Giant) g).setHealth(0);

	}

	@Override
	public void onPlayerDeath(DeathEvent e) {

		e.p.setVelocity(new Vector(0, 0, 0));

		Minigames.broadcastDeath(e.p, e.k, "%s was stomped out",
				"%s was murdered by %s");
	}

	public void jump() {
		if (Minigames.alivePlayers.size() > 1) {
			g.setVelocity(new Vector(0, 0, 0));
			Vector d = ((Giant) g).getLocation().toVector().setY(4.0D)
					.normalize();
			g.setVelocity(d);
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					for (String s : Minigames.alivePlayers) {
						Bukkit.getPlayer(s).setVelocity(
								LocUtils.getVelocity(null,
										Bukkit.getPlayer(s).getLocation())
										.multiply(0.5));
					}
				}
			}, 3 * 20);
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					jump();
				}
			}, 8 * 20);

		}
	}

	@EventHandler
	public void onPlayerDamageEvent(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {

		} else {
			e.setCancelled(true);
		}
	}


}
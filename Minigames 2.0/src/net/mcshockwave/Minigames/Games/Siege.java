package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

public class Siege implements IMinigame {

	Villager	yv	= null;
	Villager	gv	= null;

	Location	yl	= new Location(Minigames.getDefaultWorld(), 453.5, 117.5, 195, 90, 0);
	Location	gl	= new Location(Minigames.getDefaultWorld(), 275.5, 117.5, 156, 270, 0);

	BukkitTask	bt	= null;

	@Override
	public void onGameStart() {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				yv = (Villager) w.spawnEntity(yl, EntityType.VILLAGER);
				gv = (Villager) w.spawnEntity(gl, EntityType.VILLAGER);

				yv.setCustomName(ChatColor.YELLOW + "Yellow King");
				yv.setCustomNameVisible(true);
				yv.setMaxHealth(50);
				yv.setHealth(50);

				gv.setCustomName(ChatColor.GREEN + "Green King");
				gv.setCustomNameVisible(true);
				gv.setMaxHealth(50);
				gv.setHealth(50);
			}
		}, 10l);

		bt = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				if (yv.isValid() || !yv.isDead()) {
					yv.teleport(yl);
				}
				if (gv.isValid() || !gv.isDead()) {
					gv.teleport(gl);
				}
			}
		}, 20l, 10l);

		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);
		}
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
		Minigames.milkPlayer(p);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
	}

	@Override
	public void onGameEnd() {
		yv.remove();
		gv.remove();
		bt.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if (e.gt.color == ChatColor.YELLOW && (yv.isDead() || !yv.isValid())) {
			Minigames.broadcastDeath(e.p, e.k, "%s was eliminated", "%s was eliminated by %s");
			Minigames.setDead(e.p, false);
		} else if (e.gt.color == ChatColor.GREEN && (gv.isDead() || !gv.isValid())) {
			Minigames.broadcastDeath(e.p, e.k, "%s was eliminated", "%s was eliminated by %s");
			Minigames.setDead(e.p, false);
		} else {
			giveKit(e.p);
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity e = event.getEntity();

		if (e instanceof Villager) {
			Villager v = (Villager) e;
			if (v == yv) {
				Minigames.broadcastAll(Minigames.getBroadcastMessage(ChatColor.YELLOW,
						"The %s Villager has died!\n%s can no longer respawn!", "Yellow", "Yellow"));
			}
			if (v == gv) {
				Minigames.broadcastAll(Minigames.getBroadcastMessage(ChatColor.GREEN,
						"The %s Villager has died!\n%s can no longer respawn!", "Green", "Green"));
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity ee = event.getEntity();
		Entity de = event.getDamager();

		if (de instanceof Player) {
			Player d = (Player) de;

			if (ee instanceof Villager) {
				Villager v = (Villager) ee;
				if (v == yv && Game.getTeam(d) != null && Game.getTeam(d).color == ChatColor.YELLOW) {
					event.setCancelled(true);
				}
				if (v == gv && Game.getTeam(d) != null && Game.getTeam(d).color == ChatColor.GREEN) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (Minigames.alivePlayers.contains(e.getPlayer().getName())) {
			if (e.getTo().getY() >= 120
					&& (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SNOW_BLOCK
							|| e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SNOW || e.getTo()
							.getBlock().getRelative(BlockFace.DOWN).getType() == Material.STONE)) {
				e.setTo(e.getFrom());
				MCShockwave.send(e.getPlayer(), "Do not climb the %s!", "mountains");
			}
		}
	}

}

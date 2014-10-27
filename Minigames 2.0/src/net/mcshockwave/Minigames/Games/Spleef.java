package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Spleef implements IMinigame {

	public long			invin				= 0;
	public List<Block>	dest				= new ArrayList<Block>();
	public final int	invincibilityTime	= 10;
	private Location    corner1             = new Location(Minigames.getDefaultWorld(), 770, 105, 30);
	private Location    corner2             = new Location(Minigames.getDefaultWorld(), 830, 105, -29);
	private Random      r                   = new Random();

	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
			if (Minigames.hasItem(p, ShopItem.TNT_Bomb)) {
				p.getInventory().setItem(8, ItemMetaUtils.setItemName(new ItemStack(Material.TNT), ChatColor.RED + "TNT Bomb"));
			}
			if (Minigames.hasItem(p, ShopItem.Teleporter)) {
				p.getInventory().setItem(8, ItemMetaUtils.setItemName(new ItemStack(Material.GHAST_TEAR), ChatColor.LIGHT_PURPLE + "Teleporter"));
			}
		}
		Minigames.broadcast("You have %s seconds of invincibility!", invincibilityTime);
		invin = System.currentTimeMillis() + (invincibilityTime * 1000);
	}

	@Override
	public void onGameEnd() {
		for (Block b : dest) {
			b.setType(Material.SNOW_BLOCK);
		}
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Block b = event.getClickedBlock();
		if (b != null && Minigames.alivePlayers.contains(p.getName()) && b.getType() == Material.SNOW_BLOCK) {
			if (invin < System.currentTimeMillis()) {
				killBlock(b);
			} else {
				Minigames.send(ChatColor.RED, p, "You still have %s seconds of invincibility left!",
						(invin - System.currentTimeMillis()) / 1000);
			}
		}
		if (!event.getAction().name().contains("RIGHT")) {
			return;
		}
		if (event.getItem().getType() == Material.TNT) {
			p.setItemInHand(null);
			TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.PRIMED_TNT);
			tnt.setVelocity(p.getLocation().getDirection().multiply(2.0));
			tnt.setFuseTicks((int) 20l);
		}
		if (event.getItem().getType() == Material.GHAST_TEAR) {
			p.setItemInHand(null);
			int xdif = corner2.getBlockX() - corner1.getBlockX();
			int zdif = -corner2.getBlockZ() + (corner1.getBlockZ());
			int tax = r.nextInt(xdif);
			int taz = r.nextInt(zdif);
			p.teleport(new Location(Minigames.getDefaultWorld(), corner1.getBlockX() + tax, 105, corner2.getBlockZ() + taz, p.getLocation().getYaw(), p.getLocation().getPitch()));
			p.setFireTicks(0);
			MCShockwave.send(ChatColor.GREEN, p, "%s", "§lWhoooooosh!");
		}
		if (event.getItem().getType() == Material.NETHER_STAR) {
			p.setItemInHand(null);
			MCShockwave.broadcast(ChatColor.DARK_PURPLE, "%s", "§l" + p.getName() + ": May death rain upon them!");
			for (Player pl : Bukkit.getOnlinePlayers()) {
				pl.playSound(pl.getEyeLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
			}
			SchedulerUtils u = SchedulerUtils.getNew();
			for (int i = 0; i < 6; i++) {
				final Location m = LocUtils.addRand(Game.Spleef.spawn, 25, 0, 25).add(0, 50, 0);
				u.add(new Runnable() {
					public void run() {
						m.getWorld().spawnEntity(m, EntityType.PRIMED_TNT);
					}
				});
				u.add(r.nextInt(10));
			}
			u.execute();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent e) {
		for (Block b : e.blockList()) {
			if (b.getType() == Material.SNOW_BLOCK) {
				killBlock(b);
			}
		}
		e.blockList().clear();
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
		if (event.getCause() == DamageCause.ENTITY_EXPLOSION) {
			event.setCancelled(true);
		}
	}
	
	public void killBlock(final Block b) {
		dest.add(b);
		b.setType(Material.AIR);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				b.setType(Material.SNOW_BLOCK);
				dest.remove(b);
			}
		}, 200);
	}
}

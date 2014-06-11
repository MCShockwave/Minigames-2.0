package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class StormTheCastle implements IMinigame {

	Location	beaconPlace	= new Location(Minigames.getDefaultWorld(), 2578, 106, -3);

	BukkitTask	holders		= null;

	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
		}
		holders = new BukkitRunnable() {
			public void run() {
				for (Player p : Game.Storm_The_Castle.getTeam("Barbarians").getPlayers()) {
					if (p.getInventory().contains(Material.BEACON)) {
						Item i = p.getWorld().dropItem(p.getEyeLocation(), new ItemStack(Material.BEACON));
						i.setPickupDelay(Short.MAX_VALUE);
						i.setTicksLived(6000 - 20);
						i.setVelocity(new Vector(0, 0.5, 0));
					}
				}
			}
		}.runTaskTimer(Minigames.ins, 10, 10);
	}

	@Override
	public void onGameEnd() {
		holders.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		GameTeam gt = Game.getTeam(p);
		if (gt == Game.Storm_The_Castle.getTeam("Knights")) {
			p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.BEACON));
			Minigames.broadcastDeath(p, e.k, "%s killed themselves and dropped a beacon!",
					"%s was killed by %s and dropped a beacon!");
			giveItems(p);
		} else if (gt == Game.Storm_The_Castle.getTeam("Barbarians")) {
			if (p.getInventory().contains(Material.BEACON)) {
				Minigames.broadcastDeath(p, e.k, "%s killed themselves and lost a beacon",
						"%s was killed by %s and lost a beacon");
				giveItems(p);
			}
		} else {
			Minigames.broadcastDeath(p, e.k, "%s killed themselves", "%s was killed by %s");
			giveItems(p);
		}
	}

	private void giveItems(Player p) {
		GameTeam gt = Game.getTeam(p);
		PlayerInventory pi = p.getInventory();
		if (gt.team == Game.Storm_The_Castle.getTeam("Knights")) {
			pi.setHelmet(new ItemStack(Material.IRON_HELMET));
			pi.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			pi.setBoots(new ItemStack(Material.IRON_BOOTS));
			pi.addItem(new ItemStack(Material.IRON_SWORD));
		} else if (gt.team == Game.Storm_The_Castle.getTeam("Barbarians")) {
			pi.setHelmet(new ItemStack(Material.LEATHER_HELMET));
			pi.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
			pi.setBoots(new ItemStack(Material.LEATHER_BOOTS));
			pi.addItem(new ItemStack(Material.STONE_AXE));
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		GameTeam gt = Game.getTeam(p);
		if (gt == Game.Storm_The_Castle.getTeam("Knights")) {
			e.setCancelled(true);
			e.getItem().remove();
			Minigames.broadcast(ChatColor.GOLD, "A %s was recovered by the knights!", "beacon");
		} else if (gt == Game.Storm_The_Castle.getTeam("Barbarians")) {
			Minigames.broadcast(ChatColor.GOLD, p.getName() + " has picked up a %s!", "beacon");
			Minigames.send(p, "You have a %s! Go place it on the %s to win!", "beacon", "gold block");
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		Block against = e.getBlockAgainst();
		if (Game.getTeam(p).name.equalsIgnoreCase("Barbarians") && LocUtils.isSame(b.getLocation(), beaconPlace)
				&& against.getType() == Material.GOLD_BLOCK) {
			Minigames.stop(Game.getTeam(p).team);
		}
	}

}

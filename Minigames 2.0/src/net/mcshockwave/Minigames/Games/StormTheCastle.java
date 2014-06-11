package net.mcshockwave.Minigames.Games;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

public class StormTheCastle implements IMinigame {
	
	private boolean beaconDropped = false;
	
	Location beaconPlace = new Location(Minigames.getDefaultWorld(), 2578.5, 106, -2.5);
	
	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
		}
	}
	
	@Override
	public void onGameEnd() {
		beaconDropped = false;
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		GameTeam gt = Game.getTeam(p);
		if (gt.team == Game.getTeam(Game.Storm_The_Castle, "Knights")) {
			if (!beaconDropped) {
				p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.BEACON));
				Minigames.broadcastDeath(p, e.k, "%s killed themselves and dropped the " + ChatColor.GOLD + " beacon " + ChatColor.RESET + "!",
						"%s was killed by %s and dropped the " + ChatColor.GOLD + " beacon " + ChatColor.RESET + "!");
				giveItems(p);
				beaconDropped = true;
				return;
			}
		} else if (gt.team == Game.getTeam(Game.Storm_The_Castle, "Barbarians")) {
			if (p.getInventory().contains(Material.BEACON)) {
				Minigames.broadcastDeath(p, e.k, "%s killed themselves and lost the beacon!", "%s was killed by %s and lost the beacon!");
				giveItems(p);
				beaconDropped = false;
			}
		} else {
			Minigames.broadcastDeath(p, e.k, "%s killed themselves.", "%s was killed by %s.");
			giveItems(p);
		}
	}
	
	private void giveItems(Player p) {
		GameTeam gt = Game.getTeam(p);
		PlayerInventory pi = p.getInventory();
		if (gt.team == Game.getTeam(Game.Storm_The_Castle, "Knights")) {
			pi.setHelmet(new ItemStack(Material.IRON_HELMET));
			pi.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			pi.setBoots(new ItemStack(Material.IRON_BOOTS));
			pi.addItem(new ItemStack(Material.IRON_SWORD));
		} else if (gt.team == Game.getTeam(Game.Storm_The_Castle, "Barbarians")) {
			pi.setHelmet(new ItemStack(Material.LEATHER_HELMET));
			pi.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
			pi.setBoots(new ItemStack(Material.LEATHER_BOOTS));
			pi.addItem(new ItemStack(Material.STONE_AXE));
		}
	}
	
	@EventHandler
	public void ItemPickup(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		GameTeam gt = Game.getTeam(p);
		if (gt.team == Game.getTeam(Game.Storm_The_Castle, "Knights")) {
			e.setCancelled(true);
			e.getItem().remove();
			Minigames.broadcast(ChatColor.GOLD, "The %s was recovered by the knights!", "beacon");
			beaconDropped = false;
		} else if (gt.team == Game.getTeam(Game.Storm_The_Castle, "Barbarians")) {
			Minigames.broadcast(ChatColor.GOLD, p.getName() + " has picked up the %s!", "beacon");
			p.sendMessage(ChatColor.GRAY + "You have the beacon! Go place it on the gold block to win!");
		}
	}
	
	@EventHandler
	public void BlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		Block against = e.getBlockAgainst();
		if (LocUtils.isSame(b.getLocation(), beaconPlace) && against.getType() == Material.GOLD_BLOCK) {
			e.setCancelled(true);
			Minigames.stop(Game.getTeam(p).team);
		}
	}
	
}

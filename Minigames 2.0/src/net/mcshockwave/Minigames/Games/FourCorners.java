package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.BlockUtils;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FourCorners implements IMinigame {

	Location			rs		= new Location(Multiworld.getGame(), 191, 105, -13);
	Location			re		= new Location(Multiworld.getGame(), 195, 105, -9);
	Location			ys		= new Location(Multiworld.getGame(), 215, 105, -13);
	Location			ye		= new Location(Multiworld.getGame(), 211, 105, -9);
	Location			gs		= new Location(Multiworld.getGame(), 215, 105, 11);
	Location			ge		= new Location(Multiworld.getGame(), 211, 105, 7);
	Location			bs		= new Location(Multiworld.getGame(), 191, 105, 11);
	Location			be		= new Location(Multiworld.getGame(), 195, 105, 7);

	Location			p1s		= new Location(Multiworld.getGame(), 196, 105, -12);
	Location			p1e		= new Location(Multiworld.getGame(), 210, 105, -10);
	Location			p2s		= new Location(Multiworld.getGame(), 214, 105, -8);
	Location			p2e		= new Location(Multiworld.getGame(), 212, 105, 6);
	Location			p3s		= new Location(Multiworld.getGame(), 210, 105, 10);
	Location			p3e		= new Location(Multiworld.getGame(), 196, 105, 8);
	Location			p4s		= new Location(Multiworld.getGame(), 192, 105, 6);
	Location			p4e		= new Location(Multiworld.getGame(), 194, 105, -8);

	Material			set		= Material.STAINED_CLAY;
 
	int					chosen	= 0;

	ArrayList<Player>	vi		= new ArrayList<>();

	@Override
	public void onGameStart() {
		setCorners();

		for (Player p : Minigames.getOptedIn()) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 3));
			
			if (Minigames.hasItem(p, ShopItem.Hacker)) {
				p.getInventory().setItem(8,
						ItemMetaUtils.setItemName(new ItemStack(Material.NETHER_STAR), "§rRight-click to hack corner"));
			}
			if (Minigames.hasItem(p, ShopItem.Virus)) {
				final Player p2 = p;
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					public void run() {
						giveVirus(p2, null);
					}
				}, 50);
			}
			if (Minigames.hasItem(p, ShopItem.Quickfoot)) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 1));
			}
		}
	}

	@Override
	public void onGameEnd() {
		BlockUtils.setBlocks(p1s, p1e, set, 9);
		BlockUtils.setBlocks(p2s, p2e, set, 9);
		BlockUtils.setBlocks(p3s, p3e, set, 9);
		BlockUtils.setBlocks(p4s, p4e, set, 9);

		BlockUtils.setBlocks(rs, re, set, 14);
		BlockUtils.setBlocks(ys, ye, set, 4);
		BlockUtils.setBlocks(gs, ge, set, 13);
		BlockUtils.setBlocks(bs, be, set, 11);
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
	}

	public void destroyCorner(final int id) {
		BlockUtils.setBlocks(p1s, p1e, Material.AIR, 0);
		BlockUtils.setBlocks(p2s, p2e, Material.AIR, 0);
		BlockUtils.setBlocks(p3s, p3e, Material.AIR, 0);
		BlockUtils.setBlocks(p4s, p4e, Material.AIR, 0);

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				if (id == 0) {
					Minigames.broadcast(ChatColor.RED, "Corner %s chosen!", "Red");
					BlockUtils.setBlocks(rs, re, Material.AIR, 0);
				} else if (id == 1) {
					Minigames.broadcast(ChatColor.YELLOW, "Corner %s chosen!", "Yellow");
					BlockUtils.setBlocks(ys, ye, Material.AIR, 0);
				} else if (id == 2) {
					Minigames.broadcast(ChatColor.GREEN, "Corner %s chosen!", "Green");
					BlockUtils.setBlocks(gs, ge, Material.AIR, 0);
				} else if (id == 3) {
					Minigames.broadcast(ChatColor.BLUE, "Corner %s chosen!", "Blue");
					BlockUtils.setBlocks(bs, be, Material.AIR, 0);
				}
			}
		}, 100);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				setCorners();
			}
		}, 200);
	}

	public void setCorners() {
		BlockUtils.setBlocks(p1s, p1e, set, 9);
		BlockUtils.setBlocks(p2s, p2e, set, 9);
		BlockUtils.setBlocks(p3s, p3e, set, 9);
		BlockUtils.setBlocks(p4s, p4e, set, 9);

		BlockUtils.setBlocks(rs, re, set, 14);
		BlockUtils.setBlocks(ys, ye, set, 4);
		BlockUtils.setBlocks(gs, ge, set, 13);
		BlockUtils.setBlocks(bs, be, set, 11);

		chosen = -1;

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				repeat();
			}
		}, 40);
	}

	public void repeat() {
		Minigames.broadcast("You have %s seconds to go to a corner!", "10");
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				if (chosen < 0) {
					chosen = rand.nextInt(4);
				}
				Minigames.broadcast("Times up! %s destroyed!", "Bridges");
				destroyCorner(chosen);
			}
		}, 200);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (a.name().contains("RIGHT_CLICK") && it.getType() == Material.NETHER_STAR) {
			if (chosen < 0) {
				Inventory i = Bukkit.createInventory(null, 9, "Choose a Corner");

				i.setItem(
						1,
						ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 14), ChatColor.RED
								+ "Red Corner"));
				i.setItem(
						3,
						ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 4), ChatColor.YELLOW
								+ "Yellow Corner"));
				i.setItem(
						5,
						ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 13), ChatColor.GREEN
								+ "Green Corner"));
				i.setItem(
						7,
						ItemMetaUtils.setItemName(new ItemStack(Material.WOOL, 1, (short) 11), ChatColor.BLUE
								+ "Blue Corner"));

				p.openInventory(i);
			} else
				Minigames.send(p, "You can't choose a %s right now!", "corner");
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory i = event.getInventory();
		HumanEntity he = event.getWhoClicked();
		ItemStack cu = event.getCurrentItem();

		if (he instanceof Player) {
			Player p = (Player) he;
			if (i.getName().equalsIgnoreCase("Choose a Corner")) {
				event.setCancelled(true);

				if (chosen >= 0) {
					p.closeInventory();
					Minigames.send(p, "You can't choose a %s right now!", "corner");
					return;
				}

				if (cu.getType() != Material.WOOL) {
					return;
				}

				if (cu.getDurability() == 14) {
					chosen = 0;
					Minigames.send(ChatColor.RED, p, "You chose corner %s!", "Red");
				}
				if (cu.getDurability() == 4) {
					chosen = 1;
					Minigames.send(ChatColor.YELLOW, p, "You chose corner %s!", "Yellow");
				}
				if (cu.getDurability() == 13) {
					chosen = 2;
					Minigames.send(ChatColor.GREEN, p, "You chose corner %s!", "Green");
				}
				if (cu.getDurability() == 11) {
					chosen = 3;
					Minigames.send(ChatColor.BLUE, p, "You chose corner %s!", "Blue");
				}

				p.setItemInHand(null);
				p.closeInventory();
			}
		}
	}

	public void giveVirus(Player p, Player o) {
		if (o != null) {
			o.removePotionEffect(PotionEffectType.SLOW);
			o.removePotionEffect(PotionEffectType.BLINDNESS);
			vi.remove(o);
			Minigames.send(o, "You gave the virus to %s!", p.getName());
			Minigames.send(p, "You have gotten the virus from %s!", o.getName());
			Minigames.send(p, "Punch another player to give them the virus!");
		} else {
			Minigames.send(p, "You have the %s!", "virus");
		}

		vi.add(p);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10000000, 2));
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10000000, 0));
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity pe = event.getEntity();
		Entity de = event.getDamager();

		if (pe instanceof Player && de instanceof Player) {
			Player p = (Player) pe;
			Player d = (Player) de;

			if (vi.contains(d) && !vi.contains(p) && !Minigames.hasItem(p, ShopItem.Quickfoot)) {
				giveVirus(p, d);
			}
		}
	}

}

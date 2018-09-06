package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.BlockUtils;

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

import java.util.ArrayList;

public class FourCorners implements IMinigame {

	public String[]		colors	= { "r", "y", "g", "b" };

	int					chosen	= 0;

	ArrayList<Player>	vi		= new ArrayList<>();

	@Override
	public void onGameStart() {
		for (int i = 1; i <= 4; i++) {
			Location c1 = Game.getLocation("bridge-" + i + "-a");
			Location c2 = Game.getLocation("bridge-" + i + "-b");
			BlockUtils.save(c1, c2, "bridge-" + i, false, 0, false);
		}

		for (String s : colors) {
			Location c1 = Game.getLocation("corner-" + s + "-a");
			Location c2 = Game.getLocation("corner-" + s + "-b");
			BlockUtils.save(c1, c2, "corner-" + s, false, 0, false);
		}

		setCorners();
	}

	@Override
	public void onGameEnd() {
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
	}

	public void destroyCorner(final int id) {
		for (int i = 1; i <= 4; i++) {
			Location c1 = Game.getLocation("bridge-" + i + "-a");
			Location c2 = Game.getLocation("bridge-" + i + "-b");
			BlockUtils.save(c1, c2, "bridge-" + i, true, 0.2, false);
		}

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				if (id == 0) {
					Minigames.broadcast(ChatColor.RED, "Corner %s chosen!", "Red");
				} else if (id == 1) {
					Minigames.broadcast(ChatColor.YELLOW, "Corner %s chosen!", "Yellow");
				} else if (id == 2) {
					Minigames.broadcast(ChatColor.GREEN, "Corner %s chosen!", "Green");
				} else if (id == 3) {
					Minigames.broadcast(ChatColor.BLUE, "Corner %s chosen!", "Blue");
				}

				Location c1 = Game.getLocation("corner-" + colors[id] + "-a");
				Location c2 = Game.getLocation("corner-" + colors[id] + "-b");
				BlockUtils.save(c1, c2, "corner-" + colors[id], true, 0.01, false);
			}
		}, 100);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				setCorners();
			}
		}, 200);
	}

	public void setCorners() {
		for (int i = 1; i <= 4; i++) {
			Location c1 = Game.getLocation("bridge-" + i + "-a");
			BlockUtils.load(c1, "bridge-" + i, 0.1, false);
		}

		for (String s : colors) {
			Location c1 = Game.getLocation("corner-" + s + "-a");
			BlockUtils.load(c1, "corner-" + s, 0.01, false);
		}

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
		ItemStack it = p.getInventory().getItemInMainHand();

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

				p.getInventory().setItemInMainHand(null);
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

	@Override
	public void giveKit(Player p) {
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

	@Override
	public Object determineWinner(Game g) {
		return null;
	}

}

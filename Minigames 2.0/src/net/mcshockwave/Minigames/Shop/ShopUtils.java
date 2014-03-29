package net.mcshockwave.Minigames.Shop;

import java.util.ArrayList;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Utils.PointsUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopUtils {

	public static ShopItem[] getItems(Game g) {
		ArrayList<ShopItem> is = new ArrayList<ShopItem>();

		for (ShopItem si : ShopItem.values()) {
			if (si.mg == g) {
				is.add(si);
			}
		}

		return is.toArray(new ShopItem[0]);
	}

	public static Inventory getShopInv(Game g, Player p) {
		String pre = "Shop - ";
		int sub = g.name.length() > (32 - pre.length()) ? (32 - pre.length()) : g.name.length();
		Inventory i = Bukkit.createInventory(null, 27, pre + g.name.substring(0, sub));
		ShopItem[] its = getItems(g);

		for (int j = 0; j < its.length; j++) {
			int id = (j * 3) + 1;

			i.setItem(id, getItem(its[j], p, false));
			if (its[j].perma) {
				i.setItem(id + 18, getItem(its[j], p, true));
			}
		}

		return i;
	}

	public static ItemStack getItem(ShopItem si, Player p, boolean perma) {
		ItemStack it = new ItemStack(Material.WOOL);

		boolean hasP = hasPermaItem(p, si);

		short dura = 14;
		if (PointsUtils.getPoints(p) > (perma ? si.getPermaCost() : si.cost)) {
			dura = 5;
		}
		if (Minigames.hasItem(p, si)) {
			dura = 1;
		}
		if (hasP) {
			if (!perma) {
				dura = 15;
			} else {
				dura = 4;
			}
		}
		it.setDurability(dura);
		ItemMetaUtils.setItemName(it, ChatColor.RESET + si.name);
		ArrayList<String> lore = new ArrayList<String>();

		String[] loSL = { ChatColor.GOLD + "Cost: " + (perma ? si.getPermaCost() : si.cost),
				ChatColor.GRAY + "Click to buy" + (perma ? " permanently" : ""), "", ChatColor.GOLD + "Description:" };
		if (hasP && perma) {
			loSL = new String[] { ChatColor.GRAY + "Click to use", "", ChatColor.GOLD + "Description:" };
		}

		for (String s : loSL) {
			lore.add(s);
		}
		for (String s : si.desc) {
			lore.add(ChatColor.GRAY + s);
		}
		ItemMetaUtils.setLore(it, lore.toArray(new String[0]));

		return it;
	}

	public static void openToAll() {
		if (getItems(Minigames.currentGame).length < 1) {
			return;
		}
		for (Player p : Minigames.getOptedIn()) {
			Inventory i = getShopInv(Minigames.currentGame, p);
			p.closeInventory();
			p.openInventory(i);
		}
	}

	public static void buyItem(Player p, ShopItem si, Inventory i) {
		if (!Minigames.used.containsKey(p)) {
			int points = PointsUtils.getPoints(p);

			if (points < si.cost) {
				Minigames.send(ChatColor.RED, p, "Not enough %s to buy that!", "points");
				return;
			}

			PointsUtils.addPoints(p, -si.cost, "buying " + si.name, false);

			Minigames.used.put(p, si);
			Minigames.send(ChatColor.YELLOW, p, "Bought %s for %s points!", si.name, si.cost);
			p.closeInventory();
		} else {
			Minigames.send(p, "You have already used %s!", Minigames.used.get(p).name);
		}
	}

	public static void buyItemPermanently(Player p, ShopItem si, Inventory i) {
		if (!Minigames.used.containsKey(p)) {
			if (!hasPermaItem(p, si)) {
				int points = PointsUtils.getPoints(p);

				if (points < si.getPermaCost()) {
					Minigames.send(ChatColor.RED, p, "Not enough %s to buy that!", "points");
					return;
				}

				PointsUtils.addPoints(p, -si.getPermaCost(), "buying " + si.name + " (Permanent)", false);

				Minigames.used.put(p, si);
				Minigames.send(ChatColor.YELLOW, p, "Bought %s (Permanent) for %s points!", si.name, si.getPermaCost());
				
				p.closeInventory();

				String owned = "";
				if (SQLTable.PermaItems.has("Username", p.getName())) {
					owned = SQLTable.PermaItems.get("Username", p.getName(), "Owned");
				} else {
					SQLTable.PermaItems.add("Username", p.getName());
				}

				owned += si.name() + ",";

				SQLTable.PermaItems.set("Owned", owned, "Username", p.getName());
			} else {
				Minigames.used.put(p, si);
			}
		} else {
			Minigames.send(p, "You have already used %s!", Minigames.used.get(p).name);
		}
	}

	public static boolean hasPermaItem(Player p, ShopItem si) {
		String owned = "";
		if (SQLTable.PermaItems.has("Username", p.getName())) {
			owned = SQLTable.PermaItems.get("Username", p.getName(), "Owned");
		} else {
			SQLTable.PermaItems.add("Username", p.getName());
		}

		return owned.contains(si.name());
	}

	public static void refundItems() {
		for (Player p : Minigames.used.keySet()) {
			ShopItem si = Minigames.used.get(p);
			PointsUtils.addPoints(p, si.cost, "refund of " + si.name, false);
		}
		Minigames.used.clear();
		Minigames.usedNoPay.clear();
	}

}

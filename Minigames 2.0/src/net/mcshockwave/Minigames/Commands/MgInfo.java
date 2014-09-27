package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.GameInfo;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Shop.ShopUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

public class MgInfo implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;

		if (args.length > 0) {
			String mg = args[0];

			try {
				GameInfo gi = getGI(mg);
				if (gi != null) {
					sendInfo(p, gi);
					return true;
				}
			} catch (Exception e) {
			}
		} else {
			int l = Game.values().length;
			Inventory i = Bukkit.createInventory(null, l + (9 - (l % 9)), "Info");
			for (Game g : Game.values()) {
				if (Arrays.asList(Game.broken).contains(g)) {
					i.addItem(ItemMetaUtils.setLore(ItemMetaUtils.setItemName(g.icon.clone(), "¤c" + g.name),
							"¤6Currently Broken"));
					continue;
				}
				boolean hasShop = ShopUtils.getItems(g).length > 0;
				try {
					GameInfo.valueOf(g.name()).toString();
					if (hasShop) {
						i.addItem(ItemMetaUtils.setLore(ItemMetaUtils.setItemName(g.icon.clone(), "¤a" + g.name),
								"Click for info"));
					} else {
						i.addItem(ItemMetaUtils.setLore(ItemMetaUtils.setItemName(g.icon.clone(), "¤a" + g.name),
								"Click for info", "No Shop added yet!"));
					}
				} catch (Exception e) {
					if (hasShop) {
						i.addItem(ItemMetaUtils.setLore(ItemMetaUtils.setItemName(g.icon.clone(), "¤c" + g.name),
								"No Info added yet!"));
					} else {
						i.addItem(ItemMetaUtils.setLore(ItemMetaUtils.setItemName(g.icon.clone(), "¤c" + g.name),
								"No Info added yet!", "No Shop added yet!"));
					}
				}
			}
			p.openInventory(i);
			return true;
		}
		Minigames.send(ChatColor.DARK_AQUA, p, "Usage: %s", "/info [Minigame_Here]");
		return false;
	}

	public GameInfo getGI(String mg) {
		for (GameInfo gi : GameInfo.values()) {
			if (gi.name().equalsIgnoreCase(mg)) {
				return gi;
			}
		}
		return null;
	}

	public static void sendInfo(Player p, GameInfo gi) {
		String bm = Minigames.getBroadcastMessage("[Info: %s]", gi.name().replace('_', ' '));
		p.sendMessage(ChatColor.DARK_GRAY + "-=-=-=-=-=-=-" + bm + ChatColor.DARK_GRAY + "-=-=-=-=-=-=-");
		String[] send = gi.info;
		for (int i = 0; i < send.length; i += 2) {
			String s = send[i];
			Minigames.send(p, "%s:\n" + send[i + 1], s);
		}
	}

}

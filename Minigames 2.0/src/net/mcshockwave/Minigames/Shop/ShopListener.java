package net.mcshockwave.Minigames.Shop;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Minigames;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory in = event.getInventory();
		HumanEntity he = event.getWhoClicked();
		ItemStack cu = event.getCurrentItem();

		if (he instanceof Player) {
			Player p = (Player) he;

			if (in.getName().startsWith("Shop - ")) {
				event.setCancelled(true);
				int id = in.first(cu);
				String s = ItemMetaUtils.getItemName(cu);
				s = ChatColor.stripColor(s).replace(' ', '_');
				ShopItem si = ShopItem.valueOf(s);
				if (id < 9) {
					ShopUtils.buyItem(p, si, in);
				} else {
					ShopUtils.buyItemPermanently(p, si, in);
				}
				p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory in = event.getInventory();

		if (in.getName().startsWith("Shop - ")) {
			Minigames.send(ChatColor.DARK_AQUA, (Player) event.getPlayer(),
					"You closed the shop! Type %s to open it back up!", "/shop");
		}
	}

}

package net.mcshockwave.Minigames.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {
	
	public static boolean hasItems(Player p, ItemStack it) {
		return itemCount(p, it) >= it.getAmount();
	}

	public static int itemCount(Player p, ItemStack it) {
		int arrowCount = 0;
		ItemStack[] contents = p.getInventory().getContents();
		for (ItemStack i : contents) {
			if (i != null && i.getType() == it.getType()) {
				arrowCount += i.getAmount();
			}
		}
		return arrowCount;
	}

}

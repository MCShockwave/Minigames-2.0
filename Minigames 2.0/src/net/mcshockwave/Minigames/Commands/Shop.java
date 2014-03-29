package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Shop.ShopUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Shop implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (Minigames.canOpenShop) {
				p.openInventory(ShopUtils.getShopInv(Minigames.currentGame, p));
			}
		}

		return false;
	}

}

package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Shop.ShopUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MGC implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (!SQLTable.hasRank(p.getName(), Rank.JR_MOD)) {
				return false;
			}

			if (args[0].equalsIgnoreCase("stop")) {
				try {
					Minigames.stop(null);
				} catch (Exception e) {
					Minigames.send(ChatColor.RED, p, "Error: %s, force reloading", e.getMessage());
					Bukkit.reload();
				}
			}
			if (args[0].equalsIgnoreCase("start")) {
				Minigames.startCount();
			}
			if (args[0].equalsIgnoreCase("openshop")) {
				p.openInventory(ShopUtils.getShopInv(Game.valueOf(args[1]), p));
			}
			if (args[0].equalsIgnoreCase("force")) {
				Force.forceGame(p, args[1], true);
			}
		}
		return false;
	}

}

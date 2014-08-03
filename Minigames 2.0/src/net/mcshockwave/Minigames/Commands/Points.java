package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.Minigames.Utils.PointsUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Points implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("points")) {
			if (sender instanceof Player && args.length == 0) {
				sender.sendMessage(ChatColor.GREEN + "You have " + ChatColor.GOLD + PointsUtils.getPoints((Player) sender)
						+ ChatColor.GREEN + " points.");
			}
			if (args.length == 3 && (sender instanceof Player && SQLTable.hasRank(sender.getName(), Rank.ADMIN) || sender.isOp())) {
				if (args[0].equalsIgnoreCase("set")) {
					if (Bukkit.getPlayer(args[1]) != null) {
						PointsUtils.setPoints(Bukkit.getPlayer(args[1]), Integer.parseInt(args[2]));
					}
				}
				if (args[0].equalsIgnoreCase("add")) {
					if (Bukkit.getPlayer(args[1]) != null) {
						PointsUtils.addPoints(Bukkit.getPlayer(args[1]), Integer.parseInt(args[2]), null);
					}
				}
			}
		}
		return false;
	}

}

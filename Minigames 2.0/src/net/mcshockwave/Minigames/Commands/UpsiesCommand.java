package net.mcshockwave.Minigames.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpsiesCommand implements CommandExecutor {

	Location red = new Location(Minigames.getDefaultWorld, 303, 139, -789)
	Location blue = new Location(Minigames.getDefaultWorld, 304, 139, -771)
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("upsies")) {
			if (args.length == 0) {
			sender.sendMessage("Unknown command. Type \"/help\" for help.");
			 
			} else {
			if (args[0].equalsIgnoreCase("b") {
				if (SQLTable.hasRank(p.getName()), Rank.JR_MOD) {
					sender.teleport(blue);
				}
				else if (args[0].equalsIgnoreCase("r") {
					if (SQLTable.hasRank(p.getName()), Rank.JR_MOD) {
						sender.teleport(red);

			 
			}
			}
		return false;
	}

}

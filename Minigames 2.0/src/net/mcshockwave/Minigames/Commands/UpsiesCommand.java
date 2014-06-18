package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpsiesCommand implements CommandExecutor {

	Location	red		= new Location(Multiworld.getGame(), 303, 139, -789);
	Location	blue	= new Location(Multiworld.getGame(), 304, 139, -771);
	Location	fix		= new Location(Multiworld.getGame(), 303, 134, -780);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("upsies") && sender instanceof Player) {
			Player p = (Player) sender;

			if (args.length == 0) {
				sender.sendMessage("Unknown command. Type \"/help\" for help.");
			} else {
				if (SQLTable.hasRank(sender.getName(), Rank.JR_MOD)) {
					if (Minigames.currentGame == Game.Brawl) {
						if (args[0].equalsIgnoreCase("r")) {
							p.teleport(blue);
						} else if (args[0].equalsIgnoreCase("b")) {
							p.teleport(red);
						} else if (args[0].equalsIgnoreCase("down")) {
							p.teleport(fix);
						}
					}
				}
			}
		}
		return false;
	}
}

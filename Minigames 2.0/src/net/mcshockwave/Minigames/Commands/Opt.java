package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Opt implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			if (!SQLTable.hasRank(p.getName(), Rank.JR_MOD)) {
				MCShockwave.send(p, "%s is disabled for non-staff due to buggy-ness", "/opt");
			}

			if (!SQLTable.hasRank(p.getName(), Rank.IRON)) {
				MCShockwave.send(p, "You must be %s to use /opt! Buy it at buy.mcshockwave.net", "Iron+");
				return false;
			}

			boolean oo = Minigames.optedOut.contains(p.getName());

			if (oo) {
				Minigames.send(ChatColor.DARK_AQUA, p, "Opted %s to the minigame!", "in");
				Minigames.optedOut.remove(p.getName());

				if (Minigames.started) {
					p.teleport(Game.getLocation("lobby"));
					Minigames.spectate(p);
				} else {
					p.teleport(new Location(p.getWorld(), 0, 103, 0));
					p.setAllowFlight(false);
				}
				p.setPlayerListName(p.getName());
				for (Player p2 : Bukkit.getOnlinePlayers()) {
					MCShockwave.updateTab(p2);
				}
			} else {
				Minigames.send(ChatColor.DARK_AQUA, p, "Opted %s of the minigame!", "out");
				Minigames.optedOut.add(p.getName());

				Minigames.resetPlayer(p);
				if (Minigames.started) {
					Minigames.setDead(p, false);
					if (Minigames.alivePlayers.contains(p.getName())) {
						Minigames.sendDeathToGame(p);
					}
					p.teleport(new Location(p.getWorld(), 0, 103, 0));
					p.setAllowFlight(false);
					Minigames.resetPlayer(p);
				} else {
					String name = p.getName();
					name = name.substring(0, name.length() > 14 ? 13 : name.length());
					p.setPlayerListName(ChatColor.GRAY + name);
					for (Player p2 : Bukkit.getOnlinePlayers()) {
						MCShockwave.updateTab(p2);
					}
				}
			}
		}

		return false;
	}
}

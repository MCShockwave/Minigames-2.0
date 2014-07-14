package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Revive implements CommandExecutor {
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
		if (s instanceof Player && SQLTable.hasRank(((Player) s).getName(), Rank.MOD)) {
			if (a.length < 1) {
				s.sendMessage(ChatColor.RED + "Improper Syntax");
				return true;
			}
			Player p = Bukkit.getPlayerExact(a[0]);
			Game cg = Minigames.currentGame;
			if (cg.isTeamGame() && a.length > 1) {
				GameTeam team = cg.getTeam(a[1]);
				boolean valid = false;
				for (GameTeam t : cg.teams) {
					if (team.name == t.name) {
						valid = true;
						break;
					}
				}
				if (!valid) {
					s.sendMessage(ChatColor.RED + "The team " + a[1] + " is not valid!");
					return true;
				}
				Minigames.resetPlayer(p);
				Minigames.deadPlayers.remove(p.getName());
				Minigames.alivePlayers.add(p.getName());
				team.team.addPlayer(p);
				p.damage(p.getMaxHealth());
			} else {
				Minigames.resetPlayer(p);
				Minigames.deadPlayers.remove(p.getName());
				Minigames.alivePlayers.add(p.getName());
				p.damage(p.getMaxHealth());
			}
			s.sendMessage(ChatColor.GREEN + "Success!");
		}
		return false;
	}

}

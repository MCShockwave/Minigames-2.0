package net.mcshockwave.Minigames.Commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamSelect implements CommandExecutor {

	public Game[]	gamesNoTeam	= { Game.Infection, Game.Minotaur };

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (!SQLTable.hasRank(p.getName(), Rank.EMERALD)) {
				Minigames.send(p,
						"You have to be at least %s VIP to choose your team!\nBuy VIP at buy.mcshockwave.net",
						"Emerald");
				return false;
			}

			if (Arrays.asList(gamesNoTeam).contains(Minigames.currentGame)) {
				Minigames.send(p, "%s is disabled for %s", "/team", Minigames.currentGame.name);
				return false;
			}

			if (Minigames.canOpenShop && Minigames.currentGame.isTeamGame() && Minigames.currentGame != Game.Infection) {
				if (args.length > 0) {
					if (Game.getTeam(Minigames.currentGame, args[0].replace('_', ' ')) != null) {
						GameTeam gt = Game.getTeam(Minigames.currentGame, args[0].replace('_', ' '));
						boolean allow = true;
						int num = 0;
						HashMap<GameTeam, Integer> numO = new HashMap<>();
						for (Entry<Player, GameTeam> e : Minigames.selectedTeam.entrySet()) {
							if (e.getValue() == gt) {
								num++;
							} else {
								int n = 0;
								if (numO.containsKey(e.getValue())) {
									n = numO.get(e.getValue());
									numO.remove(e.getValue());
								}
								n++;
								numO.put(e.getValue(), n);
							}
						}
						for (Entry<GameTeam, Integer> e : numO.entrySet()) {
							if (e.getValue() < num) {
								allow = false;
							}
						}
						if (!allow) {
							Minigames.send(gt.color, p, "Too many players on %s!", gt.name);
						} else {
							Minigames.selectedTeam.put(p, gt);
							Minigames.send(gt.color, p, "You have been put into the %s team!", gt.name);
						}
					} else
						Minigames.send(p, "\"%s\" is not a team!", args[0].replace('_', ' '));
				} else {
					Minigames.send(p, "All teams for %s:", Minigames.currentGame.name);
					for (GameTeam gt : Minigames.currentGame.teams) {
						Minigames.send(gt.color, p, "%s", gt.name);
					}
				}
			} else
				Minigames.send(p, "You can't do that right now!");
		}

		return false;
	}
}

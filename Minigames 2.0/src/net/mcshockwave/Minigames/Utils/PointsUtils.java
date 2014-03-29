package net.mcshockwave.Minigames.Utils;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.Minigames.Minigames;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PointsUtils {

	public static void addPoints(Player p, int points, String reason, boolean mult) {
		int po = getPoints(p);
		if (mult) {
			points *= Minigames.getMultiplier(p);
		}
		po += points;
		Minigames.send(points >= 0 ? ChatColor.GREEN : ChatColor.RED, p, "%s points"
				+ (reason != null ? " for " + reason : ""), (points >= 0 ? "+" : "") + points);
		setPoints(p, po);
	}

	public static int getPoints(Player p) {
		return SQLTable.Points.getInt("Username", p.getName(), "Points");
	}

	public static void setPoints(Player p, int points) {
		SQLTable.Points.set("Points", points + "", "Username", p.getName());
		MCShockwave.updateTab(p);
	}

}

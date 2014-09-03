package net.mcshockwave.Minigames.Commands;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Utils.PointsUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Force implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;

			if (!SQLTable.hasRank(p.getName(), Rank.IRON)) {
				Minigames.send(p, "You need at least %s VIP to force games!\nBuy VIP at buy.mcshockwave.net", "Iron");
				return false;
			}

			if (Minigames.countingDown && Minigames.currentGame != null && !Minigames.gameForced
					&& !Minigames.canOpenShop) {
				p.openInventory(getForceGame(p));
			} else {
				Minigames.send(p, "You can't force games right now!");
			}
		}

		return false;
	}

	public static Inventory getForceGame(Player p) {
		Inventory i = Bukkit.createInventory(p, (Game.values().length + (9 - Game.values().length % 9)), "Force Games");
		for (Game g : Game.values()) {
			if (Arrays.asList(Game.broken).contains(g)) {
				i.addItem(ItemMetaUtils.setLore(
						ItemMetaUtils.setItemName(g.icon.clone(), ChatColor.GRAY + "Force: " + ChatColor.RED + g.name),
						"", "§6Currently Broken"));
				continue;
			}
			i.addItem(ItemMetaUtils.setLore(
					ItemMetaUtils.setItemName(g.icon.clone(), ChatColor.GRAY + "Force: " + ChatColor.GOLD + g.name),
					"", "Cost: " + getCost(g) + " points"));
		}
		return i;
	}

	public static int getCost(Game g) {
		if (!SQLTable.ForceCosts.has("Minigame", g.name())) {
			SQLTable.ForceCosts.add("Minigame", g.name());
		}

		return SQLTable.ForceCosts.getInt("Minigame", g.name(), "Cost");
	}

	public static long getForceTime(Player p) {
		if (SQLTable.ForceCooldowns.has("Username", p.getName())) {
			return SQLTable.ForceCooldowns.getInt("Username", p.getName(), "Time");
		}
		return 0;
	}

	public static void setForceTime(Player p, long time) {
		if (SQLTable.ForceCooldowns.has("Username", p.getName())) {
			SQLTable.ForceCooldowns.set("Time", time + "", "Username", p.getName());
		} else
			SQLTable.ForceCooldowns.add("Username", p.getName(), "Time", time + "");
	}

	public static long translateLong(long in, boolean toMin) {
		if (toMin) {
			return TimeUnit.MILLISECONDS.toMinutes(in);
		} else
			return TimeUnit.MINUTES.toMinutes(in);
	}

	public static void forceGame(Player p2, String ga, boolean force) {
		Game g = null;
		for (Game game : Game.values()) {
			if (game.name().equalsIgnoreCase(ga.replace(" ", "_"))) {
				g = game;
				break;
			}
		}

		if (!force) {
			if (Arrays.asList(Game.broken).contains(g)) {
				Minigames.send(p2, "That game is currently %s!", "broken");
				return;
			}

			if (getForceTime(p2) > translateLong(System.currentTimeMillis(), true)) {
				p2.closeInventory();
				Minigames.send(p2, "You have %s minutes until you can force again!",
						getForceTime(p2) - translateLong(System.currentTimeMillis(), true));
				return;
			}

			if (g == Minigames.currentGame) {
				p2.closeInventory();
				Minigames.send(p2, "That minigame is already chosen!");
				return;
			}

			if (g == Minigames.gameBefore) {
				p2.closeInventory();
				Minigames.send(p2, "That minigame was played last!");
				return;
			}
		}

		if (g != null) {
			if (PointsUtils.getPoints(p2) < getCost(g) && !force) {
				Minigames.send(p2, "Not enough %s!", "points");
				return;
			}

			Minigames.broadcastAll(Minigames.getBroadcastMessage("%s forced %s!", p2.getName(), g.name));
			Minigames.currentGame = g;
			Minigames.gameForced = true;

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getOpenInventory().getTitle().equalsIgnoreCase("Force Games")) {
					p.closeInventory();
				}
			}

			if (!force) {
				PointsUtils.addPoints(p2, -getCost(g), "forcing " + g.name);

				setForceTime(p2, translateLong(System.currentTimeMillis(), true) + 20);

				SQLTable.ForceCosts.set("Cost", (getCost(g) + 10) + "", "Minigame", g.name());
			}
		}
	}

}

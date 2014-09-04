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
			if (args[0].equalsIgnoreCase("world")) {
				p.teleport(Bukkit.getWorld(args[1]).getSpawnLocation());
			}
			if (args[0].equalsIgnoreCase("loadworld")) {
				Game g = Game.valueOf(args[1]);
				String map = g.maplist.get(0);
				Minigames.resetGameWorld(g, map);
			}
			if (args[0].equalsIgnoreCase("updateMap")) {
				Minigames.updateMap(args[1]);
			}
			if (args[0].equalsIgnoreCase("updateAll")) {
				for (Game g : Game.values()) {
					if (g.maplist.size() > 0) {
						for (String map : g.maplist) {
							String mapname = g.name() + "-" + map;
							Minigames.updateMap(mapname);
						}
					}
				}
			}
		}
		return false;
	}

}

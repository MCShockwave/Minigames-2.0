package net.mcshockwave.Minigames.Commands;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameMap;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Shop.ShopUtils;

public class MGC implements CommandExecutor {

	@SuppressWarnings("deprecation")
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
				GameMap map = g.maplist.get(0);
				Minigames.resetGameWorld(g, map);
			}
			if (args[0].equalsIgnoreCase("updateMap")) {
				Minigames.updateMap(args[1]);
			}
			if (args[0].equalsIgnoreCase("updateAll")) {
				for (Game g : Game.values()) {
					if (g.maplist.size() > 0 && !g.maplist.get(0).equals("Default")) {
						for (GameMap map : g.maplist) {
							String mapname = g.name() + "-" + map;
							Minigames.updateMap(mapname);
						}
					}
				}
			}
			if (args[0].equalsIgnoreCase("listMaps")) {
				for (Game g : Game.values()) {
					if (!g.isEnabled()) {
						continue;
					}
					Minigames.send(p, "§6§l" + g.name);
					for (GameMap s : g.maplist) {
						Minigames.send(ChatColor.GRAY, p, "%s", s);
					}
				}
			}
			if (args[0].equalsIgnoreCase("updateMapList")) {
				for (Game g : Game.values()) {
					g.updateMaps();
				}
				p.sendMessage("§cUpdated maps for all games");
			}
			if (args[0].equalsIgnoreCase("nextMap")) {
				GameMap gm = Game.getMapForGame(Minigames.currentGame, args[1]);
				if (gm != null) {
					Minigames.nextMap = gm;
					p.sendMessage("§6Set next map to " + gm);
				} else {
					p.sendMessage("§cUnknown Map for game " + Minigames.currentGame.name + ": " + args[1]);
				}
			}

			if (args[0].equalsIgnoreCase("inspect")) {
				Block b = p.getTargetBlock((Set<Material>) null, 10);
				if (b != null) {
					p.sendMessage("§6Block: (" + b.getType().name() + ")");
					p.sendMessage("§7 " + b.getLocation());
					p.sendMessage("§7 Data: " + b.getData());
					p.sendMessage("§6BlockState: " + b.getState().getClass().getCanonicalName());
					BlockState bs = b.getState();
					p.sendMessage("§7 RawData: " + bs.getRawData());
					p.sendMessage("§6 MaterialData: " + bs.getData().getClass().getCanonicalName());
					MaterialData md = b.getState().getData();
					p.sendMessage("§7  Data: " + md.getData());
				}
			}
		}
		return false;
	}
}

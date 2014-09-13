package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (SQLTable.hasRank(sender.getName(), Rank.JR_MOD)) {

				if (args.length == 1) {
					p.addPotionEffect(new PotionEffect(
							PotionEffectType.INVISIBILITY, 10000, 1));
					p.sendMessage("You are now invisible!");
					return true;
				}
				if (args.length == 2) {
					Player t = Bukkit.getPlayer(args[1]);
					t.addPotionEffect(new PotionEffect(
							PotionEffectType.INVISIBILITY, 10000, 1));
					p.sendMessage(t.getName() + " is now invisible!");
					return true;
				}
			}
		}

		return false;
	}
}

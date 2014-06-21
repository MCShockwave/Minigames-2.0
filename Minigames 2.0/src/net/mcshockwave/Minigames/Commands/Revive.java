package net.mcshockwave.Minigames.Commands;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Games.BuildAndFight;
import net.mcshockwave.Minigames.Games.Core;
import net.mcshockwave.Minigames.Games.Dodgeball;
import net.mcshockwave.Minigames.Games.Dogtag;
import net.mcshockwave.Minigames.Games.Ghostbusters;
import net.mcshockwave.Minigames.Games.Infection;
import net.mcshockwave.Minigames.Games.LaserTag;
import net.mcshockwave.Minigames.Games.Loot;
import net.mcshockwave.Minigames.Games.Siege;
import net.mcshockwave.Minigames.Games.StormTheCastle;
import net.mcshockwave.Minigames.Games.Tiers;
import net.mcshockwave.Minigames.Games.VillageBattle;
import net.mcshockwave.Minigames.Shop.ShopItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Revive implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
		if (s instanceof Player && SQLTable.hasRank(((Player)s).getName(), Rank.MOD)) {
			if (a.length < 1) {
				s.sendMessage(ChatColor.RED + "Improper Syntax");
				return true;
			}
			Player p = Bukkit.getPlayerExact(a[0]);
			Game cg = Minigames.currentGame;
			if (cg.isTeamGame() && a.length > 1) {
				GameTeam team = getTeamFromString(a[1], cg);
				boolean valid = checkTeam(team, cg);
				if (!valid) {
					s.sendMessage(ChatColor.RED + "The team " + a[1] + " is not valid!"); 
					return true; 
				}
				Minigames.resetPlayer(p);
				Minigames.deadPlayers.remove(p.getName());
				Minigames.alivePlayers.add(p.getName());
				team.team.addPlayer(p);
				p.teleport(team.spawn);
				if (cg == Game.Airships) {
					p.getInventory().addItem(ItemMetaUtils.addEnchantment(new ItemStack(Material.BOW), Enchantment.ARROW_INFINITE, 1));
					p.getInventory().addItem(new ItemStack(Material.ARROW));
					p.setAllowFlight(true);
					p.setFlying(true);
					p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000000, 0));
					if (Minigames.hasItem(p, ShopItem.Demoman)) {
						p.getInventory().setItem(8, new ItemStack(Material.TNT));
					}
					if (Minigames.hasItem(p, ShopItem.Jammer)) {
						p.getInventory().setItem(8, new ItemStack(Material.NETHER_STAR));
					}
				} else if (cg == Game.Boarding) {
					Minigames.clearInv(p);
					PlayerInventory pi = p.getInventory();
					pi.addItem(ItemMetaUtils.setItemName(new ItemStack(Material.IRON_SWORD), ChatColor.RESET + "Steel Sword"));
					pi.addItem(ItemMetaUtils.setItemName(new ItemStack(Material.IRON_AXE), ChatColor.RESET + "Musket"));
					pi.setItem(8, new ItemStack(Material.SULPHUR, 2));
					if (Minigames.hasItem(p, ShopItem.Buccaneer)) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 0));
						p.setAllowFlight(true);
					}
				} else if (cg == Game.Build_and_Fight) {
					BuildAndFight bnf = new BuildAndFight();
					if (team.color == ChatColor.GREEN) {
						bnf.giveKit(p, 13);
					}
					if (team.color == ChatColor.YELLOW) {
						bnf.giveKit(p, 4);
					}
				} else if (cg == Game.Core) {
					Core core = new Core();
					core.giveKit(p);
				} else if (cg == Game.Dodgeball) {
					Dodgeball db = new Dodgeball();
					if (Minigames.hasItem(p, ShopItem.Athlete)) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000000, 1));
					}
					if (Minigames.hasItem(p, ShopItem.Catcher)) {
						db.canBeHit.add(p);
					}
				} else if (cg == Game.Dogtag) {
					Dogtag dt = new Dogtag();
					dt.giveKit(p);
				} else if (cg == Game.Ghostbusters) {
					Ghostbusters gb = new Ghostbusters();
					boolean g = false;
					if (team.color == ChatColor.DARK_GRAY) {
						g = true;
					}
					gb.giveKit(p, g);
				} else if (cg == Game.Infection) {
					Infection i = new Infection();
					i.giveKit(p);
					if (Minigames.hasItem(p, ShopItem.Floater)) {
						p.setAllowFlight(true);
					}
				} else if (cg == Game.Laser_Tag) {
					LaserTag lt = new LaserTag();
					lt.giveItems(p);
				} else if (cg == Game.Siege) {
					Siege si = new Siege();
					si.giveKit(p);
				} else if (cg == Game.Storm_The_Castle) {
					StormTheCastle stc = new StormTheCastle();
					stc.giveItems(p);
				} else if (cg == Game.Tiers) {
					Tiers ti = new Tiers();
					ti.giveKit(team);
				} else if (cg == Game.Village_Battle) {
					VillageBattle vb = new VillageBattle();
					vb.startPlayer(p, team);
				}
				s.sendMessage(ChatColor.GREEN + "Success!");
			} else {
				Minigames.resetPlayer(p);
				Minigames.deadPlayers.remove(p.getName());
				Minigames.alivePlayers.add(p.getName());
				if (cg == Game.Brawl) {
					p.teleport(Game.Brawl.spawn);
				} else if (cg == Game.Four_Corners) {
					p.teleport(Game.Four_Corners.spawn);
				} else if (cg == Game.Hot_Potato) {
					p.teleport(Game.Hot_Potato.spawn);
				} else if (cg == Game.Loot) {
					Loot lo = new Loot();
					lo.setRandomGear(p);
					p.teleport(Game.Loot.spawn);
				} else if (cg == Game.Spleef) {
					p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
					p.teleport(Game.Spleef.spawn);
				}
				s.sendMessage(ChatColor.GREEN + "Sucess!");
			}
		}
		return false;
	}
	
	private GameTeam getTeamFromString(String s, Game g) {
		for (GameTeam gteam : g.teams) {
			if (s.equalsIgnoreCase(gteam.name)) {
				return gteam;
			}
		}
		return null;
	}
	
	private boolean checkTeam(GameTeam t, Game g) {
		for (GameTeam gt : g.teams) {
			if (gt == t) {
				return true;
			}
		}
		return false;
	}

}

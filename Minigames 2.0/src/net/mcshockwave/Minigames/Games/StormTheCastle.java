package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.LocUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Utils.PointsUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class StormTheCastle implements IMinigame {

	Location				beaconPlace		= new Location(Minigames.getDefaultWorld(), 2578, 106, -3);

	BukkitTask				holders			= null;

	public static final int	BEACONS_NEEDED	= 4;

	public static Objective	neededObj		= null;
	public static Score		needed			= null;

	@SuppressWarnings("deprecation")
	@Override
	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
		}
		holders = new BukkitRunnable() {
			public void run() {
				for (Player p : Game.Storm_The_Castle.getTeam("Barbarians").getPlayers()) {
					if (p.getInventory().contains(Material.BEACON)) {
						final Item i = p.getWorld().dropItem(p.getEyeLocation(), new ItemStack(Material.BEACON));
						i.setPickupDelay(Short.MAX_VALUE);
						i.setVelocity(new Vector(0, 0.5, 0));
						Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
							public void run() {
								i.remove();
							}
						}, 20l);

						PotionEffectType[] buffs = { PotionEffectType.SPEED, PotionEffectType.DAMAGE_RESISTANCE,
								PotionEffectType.INCREASE_DAMAGE };
						for (PotionEffectType pet : buffs) {
							if (pet == PotionEffectType.SPEED) {
								p.addPotionEffect(new PotionEffect(pet, 100, 1));
							} else {
								p.addPotionEffect(new PotionEffect(pet, 100, 0));
							}
						}
					}
				}
			}
		}.runTaskTimer(Minigames.ins, 10, 10);

		Scoreboard ma = Bukkit.getScoreboardManager().getMainScoreboard();
		neededObj = ma.registerNewObjective("Needed", "dummy");
		neededObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		neededObj.setDisplayName("§eBeacons");

		needed = neededObj.getScore(Bukkit.getOfflinePlayer("§cLeft:"));
		needed.setScore(BEACONS_NEEDED);
	}

	@Override
	public void onGameEnd() {
		holders.cancel();

		neededObj.unregister();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		GameTeam gt = Game.getTeam(p);
		if (gt == Game.Storm_The_Castle.getTeam("Knights")) {
			p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.BEACON));
			Minigames.broadcastDeath(p, e.k, "%s killed themselves and dropped a beacon!",
					"%s was killed by %s and dropped a beacon!");
			giveItems(p);
		} else if (gt == Game.Storm_The_Castle.getTeam("Barbarians")) {
			if (p.getInventory().contains(Material.BEACON)) {
				Minigames.broadcastDeath(p, e.k, "%s killed themselves and lost a beacon",
						"%s was killed by %s and lost a beacon");
			}
			giveItems(p);
		} else {
			Minigames.broadcastDeath(p, e.k, "%s killed themselves", "%s was killed by %s");
			giveItems(p);
		}
	}

	public void giveItems(Player p) {
		Minigames.clearInv(p);
		Minigames.milkPlayer(p);

		GameTeam gt = Game.getTeam(p);
		PlayerInventory pi = p.getInventory();
		if (gt == Game.Storm_The_Castle.getTeam("Knights")) {
			pi.setHelmet(new ItemStack(Material.IRON_HELMET));
			pi.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			pi.setBoots(new ItemStack(Material.IRON_BOOTS));
			pi.addItem(new ItemStack(Material.IRON_SWORD));
		} else if (gt == Game.Storm_The_Castle.getTeam("Barbarians")) {
			pi.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
			pi.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
			pi.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
			pi.addItem(new ItemStack(Material.STONE_AXE));
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		GameTeam gt = Game.getTeam(p);
		if (gt == Game.Storm_The_Castle.getTeam("Knights")) {
			e.setCancelled(true);
			e.getItem().remove();
			Minigames.broadcast(ChatColor.GOLD, "A %s was recovered by the knights!", "beacon");
		} else if (gt == Game.Storm_The_Castle.getTeam("Barbarians") && !p.getInventory().contains(Material.BEACON)) {
			Minigames.broadcast(ChatColor.GOLD, p.getName() + " has picked up a %s!", "beacon");
			Minigames.send(p, "You have a %s! Go place it on the %s to win!", "beacon", "gold block");
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		Block against = e.getBlockAgainst();
		if (Game.getTeam(p).name.equalsIgnoreCase("Barbarians") && LocUtils.isSame(b.getLocation(), beaconPlace)
				&& against.getType() == Material.GOLD_BLOCK) {
			Minigames.broadcast(ChatColor.RED, "%s placed a beacon!", p.getName());
			PointsUtils.addPoints(p, Game.Storm_The_Castle.getTeam("Knights").getPlayers().size() * 50,
					"placing a beacon", true);
			needed.setScore(needed.getScore() - 1);
			if (needed.getScore() < 1) {
				Minigames.stop(Game.getTeam(p).team);
			}
			p.setItemInHand(null);
		}
	}

	@EventHandler
	public void onItemframeBreak(HangingBreakByEntityEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		GameTeam t = Game.getTeam(e.getPlayer());
		if (t == Game.Storm_The_Castle.getTeam("Knights")
				&& e.getTo().add(0, -1, 0).getBlock().getType() == Material.GOLD_BLOCK) {
			e.setTo(e.getFrom());
			Minigames.send(e.getPlayer(), "Do not walk on the %s!", "gold block");
		}

	}

}

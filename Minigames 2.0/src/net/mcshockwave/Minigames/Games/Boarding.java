package net.mcshockwave.Minigames.Games;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.ItemUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class Boarding implements IMinigame {

	int						gr				= 50, yr = 50;

	Score					gs, ys;
	Objective				o;

	HashMap<Player, Long>	lastFireTime	= new HashMap<Player, Long>();
	private final int		reloadTime		= 10;

	@SuppressWarnings("deprecation")
	@Override
	public void onGameStart() {
		Game g = Game.getInstance(this);
		gr = g.teams[1].team.getSize() * 5;
		yr = g.teams[0].team.getSize() * 5;

		Scoreboard s = Bukkit.getScoreboardManager().getMainScoreboard();
		o = s.registerNewObjective("Reinforcements", "dummy");
		o.setDisplayName(ChatColor.AQUA + "Reinforcements");
		gs = o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Green"));
		gs.setScore(gr);
		ys = o.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + "Yellow"));
		ys.setScore(yr);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);
		}
	}

	public void giveKit(Player p) {
		if (!Minigames.alivePlayers.contains(p.getName()))
			return;
		Minigames.clearInv(p);
		PlayerInventory pi = p.getInventory();
		pi.addItem(ItemMetaUtils.setItemName(new ItemStack(Material.IRON_SWORD), ChatColor.RESET + "Steel Sword"));
		pi.addItem(ItemMetaUtils.setItemName(new ItemStack(Material.IRON_AXE), ChatColor.RESET + "Musket"));
		pi.setItem(8, new ItemStack(Material.SULPHUR, 2));
		if (Minigames.hasItem(p, ShopItem.Buccaneer)) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000000, 0));
			p.setAllowFlight(true);
		}
	}

	@Override
	public void onGameEnd() {
		o.unregister();
		lastFireTime.clear();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		giveKit(e.p);

		lastFireTime.remove(e.p);
		lastFireTime.put(e.p, System.currentTimeMillis() + 2000);

		if (e.k != null) {
			addReinforcements(e.gt, -1);
		}

		if (getReinforcements(e.gt) < 0) {
			GameTeam[] teams = Game.getInstance(this).teams;
			if (e.gt == teams[0]) {
				Minigames.stop(teams[1].team);
			}
			if (e.gt == teams[1]) {
				Minigames.stop(teams[0].team);
			}
		}

		if (e.k != null) {
			if (Minigames.hasItem(e.k, ShopItem.Privateer)) {
				e.k.setHealth(e.k.getMaxHealth());
			}
			if (Minigames.hasItem(e.k, ShopItem.Scavenger)) {
				if (e.k.getInventory().getItem(8) == null) {
					e.k.getInventory().setItem(8, new ItemStack(Material.SULPHUR, 1));
				} else {
					e.k.getInventory().addItem(new ItemStack(Material.SULPHUR, 1));
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity pe = event.getEntity();
		Entity de = event.getDamager();

		if (pe instanceof Player) {
			Player p = (Player) pe;

			if (!Minigames.alivePlayers.contains(p.getName())) {
				return;
			}

			if (de instanceof Arrow && Minigames.hasItem(p, ShopItem.Privateer)) {
				event.setDamage(event.getDamage() / 7.5);
			}

			if (de instanceof Player) {
				Player d = (Player) de;

				if (Minigames.hasItem(d, ShopItem.Buccaneer)) {
					event.setDamage(event.getDamage() + 1);
				}
			}

			if (!p.isBlocking()) {
				return;
			}

			if (de instanceof Player) {

				PacketUtils.sendPacketGlobally(p.getLocation(), 20,
						PacketUtils.generateBlockParticles(Material.IRON_BLOCK, 0, p.getEyeLocation()));
				p.getWorld().playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1.5f);

				event.setDamage(event.getDamage() * 0.15);

			} else if (de instanceof Arrow) {
				event.setDamage(event.getDamage() * 0.33);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (it != null && it.getType() == Material.IRON_AXE && a.name().contains("RIGHT_CLICK")) {
			event.setCancelled(true);

			if (!lastFireTime.containsKey(p)) {
				lastFireTime.put(p, (long) 0);
			}
			if (lastFireTime.get(p) < System.currentTimeMillis()) {
				if (ItemUtils.hasItems(p, new ItemStack(Material.SULPHUR, 1))) {
					p.getInventory().removeItem(new ItemStack(Material.SULPHUR, 1));
					p.updateInventory();

					Arrow ar = p.launchProjectile(Arrow.class);
					ar.setVelocity(ar.getVelocity().multiply(10));

					p.getWorld().playSound(p.getLocation(), Sound.ZOMBIE_WOOD, 1, 0);
					Location play = p.getEyeLocation().add(p.getLocation().getDirection());
					play.getWorld().playEffect(play, Effect.SMOKE, 0);

					lastFireTime.remove(p);
					lastFireTime.put(p, System.currentTimeMillis() + (reloadTime * 1000));
					int reloadTimeCus = reloadTime - (Minigames.hasItem(p, ShopItem.Scavenger) ? 2 : 0);
					Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
						public void run() {
							Minigames.send(ChatColor.GREEN, p, "Musket is now %s!", "reloaded");
						}
					}, reloadTimeCus * 20);
				} else {
					Minigames.send(ChatColor.RED, p, "Musket is %s!", "out of ammo");
					p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
				}
			} else {
				Minigames.send(ChatColor.RED, p, "Musket is %s!", "reloading");
				p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
			}
		}
	}

	public void setReinforcements(GameTeam gt, int set) {
		if (gt.color == ChatColor.YELLOW) {
			yr = set;
			ys.setScore(set);
		}
		if (gt.color == ChatColor.GREEN) {
			gr = set;
			gs.setScore(set);
		}
		List<Integer> display = Arrays.asList(new Integer[] { 50, 40, 30, 20, 15, 10, 5, 4, 3, 2, 1, 0 });
		if (display.contains(set)) {
			Minigames.broadcast(gt.color, "%s has %s reinforcements left!", gt.name, set);
		}
	}

	public void addReinforcements(GameTeam gt, int add) {
		setReinforcements(gt, getReinforcements(gt) + add);
	}

	public int getReinforcements(GameTeam gt) {
		if (gt.color == ChatColor.YELLOW) {
			return yr;
		} else if (gt.color == ChatColor.GREEN) {
			return gr;
		}
		return 0;
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();

		if (p.getGameMode() != GameMode.CREATIVE && Minigames.alivePlayers.contains(p.getName()) && event.isFlying()) {
			event.setCancelled(true);
			p.setFlying(false);
			if (p.getLocation().add(0, -1, 0).getBlock().getType() != Material.AIR
					|| p.getLocation().add(0, -2, 0).getBlock().getType() != Material.AIR) {
				p.setVelocity(p.getVelocity().add(new Vector(0, 1.5, 0)));
				p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 3, 1);
			} else
				p.setVelocity(p.getVelocity().add(new Vector(0, -0.1, 0)));
		}

		if (p.getGameMode() != GameMode.CREATIVE && p.isFlying() && Minigames.alivePlayers.contains(p.getName())) {
			p.setFlying(false);
		}
	}
}

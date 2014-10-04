package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.LocUtils;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class Core implements IMinigame {

	public Team				coreTeam	= null;

	public Block			core;

	public int				rad;

	public boolean			canGoIn		= false;

	HashMap<Player, Long>	cooldown	= new HashMap<Player, Long>();

	// Location[] tree = { new Location(Multiworld.getGame(), 0.5, 101, 37.5,
	// 180, 0),
	// new Location(Multiworld.getGame(), 36.5, 101, 00.5, 270, 0),
	// new Location(Multiworld.getGame(), 0.5, 101, -36.5, 180, 0) };

	public void onGameStart() {
		Minigames.showDefaultSidebar();

		core = Game.getBlock("Core");
		rad = Game.getInt("core-radius");

		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);
		}
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		p.getInventory().addItem(
				new ItemStack(Minigames.hasItem(p, ShopItem.Ranger) ? Material.WOOD_SWORD : Material.STONE_SWORD));
		if (Minigames.hasItem(p, ShopItem.Ranger)) {
			p.getInventory().addItem(new ItemStack(Material.BOW));
			p.getInventory().addItem(new ItemStack(Material.ARROW, 5));
		}
		if (Minigames.hasItem(p, ShopItem.Hermit)) {
			p.getInventory().setItem(8,
					ItemMetaUtils.setItemName(new ItemStack(Material.EYE_OF_ENDER), ChatColor.AQUA + "Teleport Away"));
		}
		if (Minigames.hasItem(p, ShopItem.Conqueror)) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000000, 0));
		}
	}

	public void onGameEnd() {
		coreTeam = null;
		canGoIn = false;
	}

	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		if (coreTeam != null) {
			if (e.t == null) {
				return;
			}
			if (coreTeam != e.t) {
				Minigames.broadcastDeath(p, e.k, "%s was eliminated", "%s was eliminated by %s");

				Minigames.setDead(p, false);
			} else {
				giveKit(p);
			}
		} else {
			giveKit(p);
		}
		if (Minigames.getTeamsLeft().size() <= 2 && !canGoIn && Minigames.started) {
			Minigames.broadcast("All teams are now allowed in the %s!", "Core");
			canGoIn = true;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (a.name().contains("RIGHT_CLICK") && Minigames.hasItem(p, ShopItem.Hermit)
				&& it.getType() == Material.EYE_OF_ENDER) {
			event.setCancelled(true);
			p.setItemInHand(null);

			p.getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 0);
			p.getWorld().playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
			// Location loc = tree[rand.nextInt(tree.length)];
			// p.teleport(loc);
			p.getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 0);
			p.getWorld().playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
			p.setHealth(20);
		}

		if (a == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();

			if (LocUtils.isSame(b.getLocation(), core.getLocation())
					&& p.getLocation().distanceSquared(b.getLocation()) <= 4 * 4) {
				GameTeam gt = Game.getTeam(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p));
				p.teleport(Game.getSpawn(gt));

				if (gt.team == coreTeam)
					return;

				core.setData(getTeamData(gt));
				doEffect(getTeamData(gt), 2);

				Minigames.broadcastAll(
						Minigames.getBroadcastMessage(gt.color, "The %s has been captured by %s!", "Core", gt.name),
						Minigames.getBroadcastMessage(gt.color, "Only the %s team can respawn now!", gt.name),
						Minigames.getBroadcastMessage("Recapture the Core by %s it!", "Right-Clicking"));

				coreTeam = gt.team;
				for (GameTeam te : Game.getInstance(this).teams) {
					if (coreTeam == te.team) {
						te.team.setPrefix(te.color + ChatColor.BOLD.toString());
					} else {
						te.team.setPrefix(te.color.toString());
					}
				}
			}
		}
	}

	public void doEffect(final byte teamColor, long time) {
		for (int c = 2; c < 16; c += 2) {
			int y = Game.getInt("spawn-y");
			Block b1 = Multiworld.getGame().getBlockAt(c, y, c);
			Block b2 = Multiworld.getGame().getBlockAt(-c, y, c);
			Block b3 = Multiworld.getGame().getBlockAt(c, y, -c);
			Block b4 = Multiworld.getGame().getBlockAt(-c, y, -c);

			final Block[] bs = { b1, b2, b3, b4 };

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					for (Block b : bs) {
						b.setData(teamColor);
						b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 35);
					}
				}
			}, c * time);
		}
	}

	public byte getTeamData(GameTeam t) {
		if (t.color == ChatColor.RED) {
			return (byte) 14;
		}
		if (t.color == ChatColor.BLUE) {
			return (byte) 11;
		}
		if (t.color == ChatColor.YELLOW) {
			return (byte) 4;
		}
		if (t.color == ChatColor.GREEN) {
			return (byte) 13;
		}
		return 0;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		GameTeam gt = Game.getTeam(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p));
		if (gt == null) {
			return;
		}
		if (Minigames.getTeamsLeft().size() > 2 && coreTeam != null && gt.team == coreTeam) {
			Location spawn = core.getLocation().clone();
			spawn.setY(Game.getInt("spawn-y"));
			if (p.getLocation().distanceSquared(spawn) <= rad * rad) {
				p.setVelocity(LocUtils.getVelocity(spawn, p.getLocation()).multiply(0.5));
				Minigames.send(p, "You can't enter your %s until there are 2 teams left!", "Core");
			}
		}
	}

	@EventHandler
	public void onPlayerToggleCrouch(PlayerToggleSneakEvent event) {
		Player p = event.getPlayer();

		if (event.isSneaking()
				&& Minigames.hasItem(p, ShopItem.Conqueror)
				&& Minigames.alivePlayers.contains(p.getName())
				&& (cooldown.containsKey(p) && cooldown.get(p) < System.currentTimeMillis() || !cooldown.containsKey(p))) {
			int rad = 5;
			for (Entity e : p.getNearbyEntities(rad, rad, rad)) {
				if (e instanceof Player) {

					Player p2 = (Player) e;
					if (Minigames.alivePlayers.contains(p2.getName()) && Game.getTeam(p2) != Game.getTeam(p)) {
						p2.setVelocity(LocUtils.getVelocity(p.getLocation(), p2.getLocation()));
					}

				}
			}

			p.getWorld().playSound(p.getLocation(), Sound.EXPLODE, 1, 1.3f);
			cooldown.put(p, System.currentTimeMillis() + 15000);
		}
	}

}

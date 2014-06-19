package net.mcshockwave.Minigames.Games;

import java.util.HashMap;

import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Gladiators implements IMinigame {

	Location	rs	= new Location(Multiworld.getGame(), 0.5, 103, 178.5);
	Location	ys	= new Location(Multiworld.getGame(), -23.5, 103, 202.5);
	Location	bs	= new Location(Multiworld.getGame(), 24.5, 103, 202.5);
	Location	gs	= new Location(Multiworld.getGame(), 0.5, 103, 226.5);

	BukkitTask	bt;

	@Override
	public void onGameStart() {
		for (GameTeam gt : Game.getInstance(this).teams) {
			selectRandom(gt);
		}

		bt = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (Player p : Minigames.getOptedIn()) {
					if (p.getHealth() < 5) {
						PacketUtils.playBlockParticles(Material.REDSTONE_BLOCK, 0, p.getEyeLocation());
						p.getWorld().playSound(p.getLocation(), Sound.DIG_WOOL, 1, 2);
					} else if (p.getHealth() < 10) {
						PacketUtils.playBlockParticles(Material.REDSTONE_WIRE, 0, p.getEyeLocation());
					}
				}
			}
		}, 20, 20);
	}

	@Override
	public void onGameEnd() {
		bt.cancel();
	}

	@Override
	public void onPlayerDeath(final DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was killed by an unknown cause", "%s was slaughtered by %s");

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				selectRandom(e.gt);
			}
		}, 60L);

		if (e.k != null) {
			e.k.setHealth(e.k.getMaxHealth());
		}
	}

	public void selectRandom(GameTeam gt) {
		if (gt.team.getSize() > 0) {
			Player[] ps = gt.team.getPlayers().toArray(new Player[0]);
			Player p = ps[rand.nextInt(ps.length)];

			giveKit(p);
			switch (gt.color) {
				case RED:
					p.teleport(rs);
					break;
				case YELLOW:
					p.teleport(ys);
					break;
				case GREEN:
					p.teleport(gs);
					break;
				case BLUE:
					p.teleport(bs);
					break;
				default:
					break;
			}
		} else
			Minigames.broadcastAll(Minigames.getBroadcastMessage(gt.color, "%s team has been eliminated!", gt.name));

	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		PlayerInventory pi = p.getInventory();

		pi.setHelmet(new ItemStack(Material.LEATHER_HELMET));
		if (Minigames.hasItem(p, ShopItem.Provocateur)) {
			pi.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 1));
		}
		if (Minigames.hasItem(p, ShopItem.Man_at_Arms)) {
			pi.setBoots(new ItemStack(Material.LEATHER_BOOTS));
		}
		pi.setItem(
				0,
				new ItemStack(Minigames.hasItem(p, ShopItem.Murmillo) ? Material.WOOD_SWORD : Minigames.hasItem(p,
						ShopItem.Provocateur) ? Material.WOOD_SWORD : Material.STONE_SWORD));
		if (Minigames.hasItem(p, ShopItem.Murmillo)) {
			pi.setItem(1, new ItemStack(Material.BOW));
			pi.setItem(2, new ItemStack(Material.ARROW, 32));
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity pe = event.getEntity();
		Entity de = event.getDamager();

		if (pe instanceof Player) {
			Player p = (Player) pe;

			if (de instanceof Arrow) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
			}

			if (de instanceof Player) {
				if (Minigames.hasItem(p, ShopItem.Provocateur)) {
					p.getWorld().playSound(p.getLocation(), Sound.HURT_FLESH, 1, 1);
					PacketUtils.playBlockParticles(Material.IRON_BLOCK, 0, p.getEyeLocation());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		Player p = event.getPlayer();

		if (Minigames.alivePlayers.contains(p.getName()) && Minigames.hasItem(p, ShopItem.Man_at_Arms)) {
			if (event.isSprinting()) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000000, 0));
			} else
				p.removePotionEffect(PotionEffectType.SPEED);
		}
	}

	HashMap<Player, Long>	maaCool	= new HashMap<>();

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player p = event.getPlayer();

		if (Minigames.alivePlayers.contains(p.getName()) && Minigames.hasItem(p, ShopItem.Man_at_Arms)) {
			if (!maaCool.containsKey(p) || maaCool.containsKey(p) && maaCool.get(p) < System.currentTimeMillis()) {
				maaCool.remove(p);
				maaCool.put(p, System.currentTimeMillis() + 2000);

				p.setVelocity(p.getVelocity().add(p.getLocation().getDirection().multiply(3).setY(-1)));
				p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
			}
		}
	}
}

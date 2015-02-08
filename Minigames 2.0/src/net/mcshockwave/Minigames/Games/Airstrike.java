package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.CooldownUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Airstrike implements IMinigame {

	public String	sniper, bomber, gunner;

	public BukkitTask	refillTask	= null, oobTask = null;

	@Override
	public void onGameStart() {
		final GameTeam sh = Game.Airstrike.teams[0];

		gunner = Game.getRandomPlayer().getName();
		sh.team.addPlayer(Bukkit.getPlayer(gunner));
		if (Minigames.getOptedIn().size() > 5) {
			while ((bomber = Game.getRandomPlayer().getName()).equals(gunner)) {
			}
			sh.team.addPlayer(Bukkit.getPlayer(bomber));
		}
		if (Minigames.getOptedIn().size() > 8) {
			while ((sniper = Game.getRandomPlayer().getName()).equals(gunner) || sniper.equals(bomber)) {
			}
			sh.team.addPlayer(Bukkit.getPlayer(sniper));
		}
		Minigames.showDefaultSidebar();

		if (bomber != null) {
			Minigames.broadcast(ChatColor.RED, "%s is the %s!", bomber, "Bomber");
		}
		if (sniper != null) {
			Minigames.broadcast(ChatColor.RED, "%s is the %s!", sniper, "Sniper");
		}
		if (gunner != null) {
			Minigames.broadcast(ChatColor.RED, "%s is the %s!", gunner, "Gunner");
		}

		for (Player p : sh.getPlayers()) {
			p.teleport(Game.getSpawn(sh));
			p.setAllowFlight(true);
			p.setFlying(true);
		}

		for (Player p : Minigames.getOptedIn()) {
			giveKit(p);
		}

		refillTask = new BukkitRunnable() {
			public void run() {
				Minigames.broadcast("Chests have been %s!", "refilled");
				refillChests();
			}
		}.runTaskTimer(plugin, 10, 600);

		oobTask = new BukkitRunnable() {
			public void run() {
				for (Player p : sh.getPlayers()) {
					if (p.getLocation().getBlockY() < Game.getInt("shooter-min-y")) {
						Minigames.send(ChatColor.RED, p, "You are too %s! Fly higher!", "low");
						p.playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 10, 0);
						p.damage(2);
					}
					if (p.getLocation().getBlockY() > Game.getInt("shooter-max-y")) {
						Minigames.send(ChatColor.RED, p, "You are too %s! Fly lower!", "high");
						p.playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 10, 0);
						p.damage(2);
					}
					int y = p.getWorld().getHighestBlockYAt(p.getLocation());
					if (y < Game.getInt("min-y")) {
						Minigames.send(ChatColor.RED, p, "You are too %s! Fly back!", "far out");
						p.playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 10, 0);
						p.damage(2);
					}
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			ItemStack it = event.getBow();

			if (it != null && ItemMetaUtils.hasCustomName(it)) {
				String name = ChatColor.stripColor(ItemMetaUtils.getItemName(it));
				if (name.equalsIgnoreCase("Sniper Bow")) {
					event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(1.5));
				}
				if (name.equalsIgnoreCase("Bomb Bow")) {
					if (CooldownUtils.isOnCooldown("BombBow", p.getName())) {
						event.setCancelled(true);
					} else {
						TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.PRIMED_TNT);
						tnt.setFuseTicks(40);
						tnt.setVelocity(event.getProjectile().getVelocity());
						event.setProjectile(tnt);
						CooldownUtils.addCooldown("BombBow", p.getName(), 50);
					}
				}
			}

			if (machine.containsKey(p.getName())) {
				machine.get(p.getName()).cancel();
				machine.remove(p.getName());
				event.setCancelled(true);
			}
		}
	}

	public HashMap<String, BukkitTask>	machine	= new HashMap<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		ItemStack it = event.getItem();
		if (event.getAction().name().contains("RIGHT_CLICK")) {
			if (it != null && it.getType() == Material.BOW && ItemMetaUtils.hasCustomName(it)
					&& ChatColor.stripColor(ItemMetaUtils.getItemName(it)).equalsIgnoreCase("Machine Bow")) {
				p.getWorld().playSound(p.getLocation(), Sound.BAT_LOOP, 5, 2);
				machine.put(p.getName(), new BukkitRunnable() {
					public void run() {
						if (machine.containsKey(p.getName()) && p.getItemInHand() != null
								&& p.getItemInHand().getType() == Material.BOW) {
							Entity e = p.launchProjectile(Arrow.class);
							float spread = 8;
							float x = (rand.nextFloat() - rand.nextFloat()) / spread;
							float y = (rand.nextFloat() - rand.nextFloat() / 1.5F) / spread;
							float z = (rand.nextFloat() - rand.nextFloat()) / spread;
							e.setVelocity(p.getLocation().getDirection().normalize().multiply(1.5)
									.add(new Vector(x, y, z)));
							p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 5, 2);
						} else {
							cancel();
						}
					}
				}.runTaskTimer(plugin, 21, 2));
			}
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile proj = event.getEntity();

		if (proj instanceof Arrow) {
			Block hit = proj.getLocation().add(proj.getVelocity().normalize()).getBlock();
			if (hit != null) {
				spawnFallingBlock(hit);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void spawnFallingBlock(Block b) {
		final FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation(),
				b.getType() == Material.GRASS ? Material.DIRT : b.getType(), (byte) 0);
		fb.setDropItem(false);
		fb.setVelocity(Vector.getRandom().multiply(2).subtract(Vector.getRandom()).add(new Vector(0, 0.5, 0)));
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList()) {
			if (rand.nextInt(4) == 0) {
				spawnFallingBlock(b);
			}
		}
	}

	public void refillChests() {
		for (Chunk c : Multiworld.getGame().getLoadedChunks()) {
			for (BlockState bs : c.getTileEntities()) {
				if (bs instanceof Chest) {
					Chest ch = (Chest) bs;
					Inventory in = ch.getBlockInventory();
					in.clear();
					for (int i = 0; i < rand.nextInt(3) + 2; i++) {
						ItemStack it = new ItemStack(rand.nextInt(3) == 0 ? Material.BOW : Material.ARROW);
						if (it.getType() == Material.ARROW) {
							it.setAmount(rand.nextInt(30) + 15);
						} else {
							if (rand.nextInt(3) == 0) {
								ItemMetaUtils.addEnchantment(it, Enchantment.ARROW_DAMAGE, rand.nextInt(2) + 1);
							}
							if (rand.nextInt(50) == 0) {
								ItemMetaUtils.addEnchantment(it, Enchantment.ARROW_INFINITE, 1);
							}
						}
						in.setItem(rand.nextInt(in.getSize()), it);
					}
					ch.update(true);
				}
			}
		}
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		p.getInventory().setChestplate(
				ItemMetaUtils.setLeatherColor(new ItemStack(Material.LEATHER_CHESTPLATE),
						Minigames.chatColorToColor(Game.getTeam(p).color)));
		double hp = 40;
		if (Game.getTeam(p).color == ChatColor.RED) {
			p.getInventory().setItem(17, new ItemStack(Material.ARROW));
			String name = "";
			if (p.getName().equals(bomber)) {
				name = "§cBomb Bow";
				hp = 30;
			} else if (p.getName().equals(gunner)) {
				name = "§cMachine Bow";
				hp = 40;
			} else if (p.getName().equals(sniper)) {
				name = "§cSniper Bow";
				hp = 40;
			}
			p.getInventory().addItem(
					ItemMetaUtils.setItemName(
							ItemMetaUtils.addEnchantment(new ItemStack(Material.BOW), Enchantment.ARROW_INFINITE, 1),
							name));
		}
		p.setMaxHealth(hp);
		p.setHealth(hp);
	}

	@EventHandler
	public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (Minigames.alivePlayers.contains(p.getName())) {
				if (event.getRegainReason() == RegainReason.SATIATED) {
					if (Game.getTeam(p).color == ChatColor.RED) {
						event.setAmount(0.25);
					} else {
						event.setAmount(0.5);
					}
				}
			}
		}
	}

	@Override
	public void onGameEnd() {
		refillTask.cancel();
		oobTask.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was murdered", "%s was shot down by %s");

		if (e.gt.color == ChatColor.RED && e.gt.getPlayers().size() > 1) {
			for (ItemStack it : e.p.getInventory().getContents()) {
				if (it != null && it.getType() == Material.BOW && ItemMetaUtils.hasCustomName(it)) {
					String s = ItemMetaUtils.getItemName(it);
					Player give = null;
					while ((give = e.gt.getPlayers().get(rand.nextInt(e.gt.getPlayers().size()))) == e.p) {
					}
					give.getInventory().addItem(it);
					Minigames.broadcast(ChatColor.RED, "%s has received the %s!", give.getName(),
							ChatColor.stripColor(s));
				}
			}
		}
	}

	@Override
	public Object determineWinner(Game g) {
		return g.getTeam("Runners").team;
	}

}

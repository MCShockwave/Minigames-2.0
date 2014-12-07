package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;
import net.mcshockwave.Minigames.Utils.LocUtils;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class StormTheCastle implements IMinigame {

	public int					cacheLocId			= -1;
	public Location				cacheLoc			= null;

	public GameScore			reinforcements		= null;
	public int					reinforcementBonus	= -1;

	public Location				crystalLocation		= null;
	public EnderCrystal			crystal				= null;

	public GameScore			wallHealth			= null;
	public GameScore			cacheStatus			= null;
	public GameScore			inhibitorsRemaining	= null;
	public GameScore			crystalHealth		= null;
	public GameScore			remainingKnights	= null;

	public Location				wallObjLoc			= null;

	public BukkitTask			particles			= null, drops = null;

	public ArrayList<Location>	inhibitors			= new ArrayList<>();

	@Override
	public void onGameStart() {
		int maxCache = 0;
		while (Game.hasElement("cache-" + (++maxCache))) {
		}
		maxCache -= 2;
		cacheLocId = rand.nextInt(maxCache) + 1;
		cacheLoc = Game.getLocation("cache-" + cacheLocId);

		placeCache(cacheLoc);

		crystalLocation = Game.getLocation("spawn-crystal").getBlock().getLocation().add(0.5, 0, 0.5);
		crystal = (EnderCrystal) crystalLocation.getWorld().spawnEntity(crystalLocation, EntityType.ENDER_CRYSTAL);

		reinforcementBonus = Game.Storm_The_Castle.getTeam("Barbarians").getPlayers().size() * 3;
		reinforcements = Sidebar.getNewScore("§cReinforcements", reinforcementBonus * 2);

		wallObjLoc = Game.getLocation("objective-wall").getBlock().getLocation();

		wallHealth = Sidebar.getNewScore("§dWall Health", 100);
		cacheStatus = Sidebar.getNewScore("§eCache - §a✓", -1);
		inhibitorsRemaining = Sidebar.getNewScore("§dInhibitors", 3);
		crystalHealth = Sidebar.getNewScore("§dSpawn Crystal", 1000);
		remainingKnights = Sidebar.getNewScore("§dKnights Left", Game.Storm_The_Castle.getTeam("Knights").getPlayers()
				.size());

		inhibitorsRemaining.setDisplayed(false);
		crystalHealth.setDisplayed(false);
		remainingKnights.setDisplayed(false);

		for (Player p : Minigames.getOptedIn()) {
			giveItems(p);
		}

		particles = new BukkitRunnable() {
			public void run() {
				if (!crystalHealth.isDisplayed() && !remainingKnights.isDisplayed()) {
					PacketUtils.playParticleEffect(ParticleEffect.DRIP_WATER, crystalLocation, 1.5f, 0,
							inhibitorsRemaining.getVal() * 2);

					for (Location l : inhibitors) {
						PacketUtils.playParticleEffect(ParticleEffect.WITCH_MAGIC, l.clone().add(0.5, 0.5, 0.5), 0.5f,
								0.5f, 3);
					}
				}
			}
		}.runTaskTimer(plugin, 2, 2);

		drops = new BukkitRunnable() {
			public void run() {
				Location drop = Game.getLocation("barbarian-supply");

				if (wallHealth.isDisplayed()) {
					drop.getWorld().dropItemNaturally(drop, new ItemStack(Material.TNT));
				} else if (inhibitorsRemaining.isDisplayed()) {
					drop.getWorld().dropItemNaturally(drop, new ItemStack(Material.BEACON));
				}
			}
		}.runTaskTimer(plugin, 0, 600);

		Minigames.broadcastAll(
				Minigames.getBroadcastMessage("The %s are storming the %s' castle!", "Barbarians", "Knights"),
				Minigames.getBroadcastMessage("The Barbarians need to destroy the %s with TNT!", "Wall"));
	}

	public void cleanObjectives() {
		for (Player p : Minigames.getOptedIn()) {
			if (Minigames.alivePlayers.contains(p.getName())) {
				if (p.getInventory().contains(Material.TNT) || p.getInventory().contains(Material.BEACON)) {
					p.damage(p.getMaxHealth());
				}
			}
		}
		for (Entity e : Multiworld.getGame().getEntities()) {
			if (e.getType() == EntityType.DROPPED_ITEM) {
				Item i = (Item) e;
				ItemStack it = i.getItemStack();
				if (it.getType() == Material.TNT || it.getType() == Material.BEACON) {
					i.remove();
				}
			}
		}
	}

	@Override
	public void onGameEnd() {
		particles.cancel();
		drops.cancel();
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Player p = e.p;
		GameTeam gt = Game.getTeam(p);

		Minigames.broadcastDeath(p, e.k, "%s was killed", "%s was killed by %s");
		if (gt == Game.Storm_The_Castle.getTeam("Barbarians")) {
			if (e.k != null) {
				reinforcements.setVal(reinforcements.getVal() - 1);
				if (reinforcements.getVal() < 1) {
					Minigames.stop(Game.Storm_The_Castle.getTeam("Knights").team);
				}
			}

			if (p.getInventory().contains(Material.BEACON)) {
				p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.BEACON));
			}
			if (p.getInventory().contains(Material.TNT)) {
				p.getWorld().dropItemNaturally(Game.getLocation("barbarians-supply"), new ItemStack(Material.TNT));
			}
			giveItems(p);
		} else {
			if (remainingKnights.isDisplayed()) {
				Minigames.resetPlayer(p);
				Minigames.setDead(p, false);
			} else {
				giveItems(p);
			}
			remainingKnights.setVal(Game.Storm_The_Castle.getTeam("Knights").getPlayers().size());
			if (remainingKnights.getVal() < 1) {
				Minigames.stop(Game.Storm_The_Castle.getTeam("Barbarians").team);
			}
		}
	}

	private void giveItems(Player p) {
		Minigames.clearInv(p);
		Minigames.milkPlayer(p);

		GameTeam gt = Game.getTeam(p);
		PlayerInventory pi = p.getInventory();
		if (gt == Game.Storm_The_Castle.getTeam("Knights")) {
			pi.setHelmet(new ItemStack(Material.LEATHER_HELMET));
			pi.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			pi.setBoots(new ItemStack(Material.LEATHER_BOOTS));
			pi.setItem(0, new ItemStack(Material.STONE_SWORD));
			if (cacheLocId != -1) {
				pi.setHelmet(new ItemStack(Material.IRON_HELMET));
				pi.setBoots(new ItemStack(Material.IRON_BOOTS));
				pi.getItem(0).setType(Material.IRON_SWORD);
			}
		} else if (gt == Game.Storm_The_Castle.getTeam("Barbarians")) {
			pi.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
			pi.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			pi.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
			pi.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
			pi.addItem(new ItemStack(Material.IRON_AXE));
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		GameTeam gt = Game.getTeam(p);
		Item i = event.getItem();
		ItemStack it = i.getItemStack();

		if (gt.name.equalsIgnoreCase("Barbarians") && it.getType() == Material.TNT) {
			event.setCancelled(true);
			if (p.getInventory().contains(Material.TNT))
				return;
			if (i.getItemStack().getAmount() == 1) {
				i.remove();
			} else {
				ItemStack iit = i.getItemStack();
				iit.setAmount(iit.getAmount() - 1);
				i.setItemStack(iit);
			}

			p.getInventory().clear();
			p.getInventory().addItem(new ItemStack(Material.TNT));
			p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0));

			Minigames.send(p, "Place the %s on the %s!", "TNT", "gold block");
		}
		if (gt.name.equalsIgnoreCase("Barbarians") && it.getType() == Material.BEACON) {
			event.setCancelled(true);
			if (p.getInventory().contains(Material.BEACON))
				return;
			if (i.getItemStack().getAmount() == 1) {
				i.remove();
			} else {
				ItemStack iit = i.getItemStack();
				iit.setAmount(iit.getAmount() - 1);
				i.setItemStack(iit);
			}

			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.BEACON), "§eCrystal Inhibitor"));

			Minigames.send(p, "Place the %s on one of the %s!", "inhibitor", "gold blocks");
		}
		if (gt.name.equalsIgnoreCase("Knights") && (it.getType() == Material.BEACON || it.getType() == Material.TNT)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		Block against = e.getBlockAgainst();
		if (Game.getTeam(p).name.equalsIgnoreCase("Barbarians") && against.getType() == Material.GOLD_BLOCK) {
			if (b.getType() == Material.TNT) {
				TNTPrimed tnt = (TNTPrimed) b.getWorld().spawnEntity(b.getLocation(), EntityType.PRIMED_TNT);
				tnt.setFuseTicks(10);
				p.damage(p.getMaxHealth(), tnt);
			}
			if (b.getType() == Material.BEACON) {
				if (inhibitorsRemaining.isDisplayed() && b.getLocation().distanceSquared(crystalLocation) <= 10 * 10) {
					inhibitors.add(b.getLocation().clone());
					e.setCancelled(false);
					inhibitorsRemaining.setVal(inhibitorsRemaining.getVal() - 1);
					if (inhibitorsRemaining.getVal() < 1) {
						inhibitorsRemaining.setDisplayed(false);
						crystalHealth.setDisplayed(true);

						Minigames.broadcastAll(Minigames.getBroadcastMessage(
								"The Barbarians have taken down the Crystal's %s!", "shield"), Minigames
								.getBroadcastMessage("The %s is vulnerable! Barbarians must try to destroy it!",
										"Crystal"));
						crystalLocation.getWorld().playSound(crystalLocation, Sound.ENDERDRAGON_DEATH, 20, 2);
						cleanObjectives();

						reinforcements.setVal(reinforcements.getVal() + reinforcementBonus);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		GameTeam t = Game.getTeam(e.getPlayer());
		if (t == Game.Storm_The_Castle.getTeam("Knights")
				&& e.getTo().clone().add(0, -1, 0).getBlock().getType() == Material.GOLD_BLOCK) {
			// spawn in air
			if (e.getFrom().clone().add(0, -1, 0).getBlock().getType() == Material.AIR) {
				e.getPlayer().setVelocity(new Vector(0, 0.5, 0));
			} else
				e.setTo(e.getFrom());
			Minigames.send(e.getPlayer(), "Do not walk on an %s!", "objective");
		}

	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity ee = event.getEntity();

		if (ee.getType() == EntityType.ENDER_CRYSTAL) {
			event.setCancelled(true);
			if (crystalHealth.isDisplayed()) {
				if (event instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
					if (edbe.getDamager() instanceof Player) {
						Player d = (Player) edbe.getDamager();
						if (Game.getTeam(d) != null && Game.getTeam(d).name.equalsIgnoreCase("Knights")) {
							return;
						}
					}

					crystalHealth.setVal((int) (crystalHealth.getVal() - event.getDamage()));

					if (crystalHealth.getVal() < 1) {
						event.setCancelled(false);

						crystalHealth.setDisplayed(false);
						remainingKnights.setDisplayed(true);

						Minigames
								.broadcastAll(Minigames.getBroadcastMessage("The Barbarians have destroyed the %s!",
										"Spawn Crystal"), Minigames.getBroadcastMessage(ChatColor.RED,
										"The Knights must make a %s! They will not respawn!", "last stand"));
						crystalLocation.getWorld().playSound(crystalLocation, Sound.ENDERDRAGON_DEATH, 20, 0);

						for (Player p : Game.Storm_The_Castle.getTeam("Knights").getPlayers()) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
							p.sendMessage("§5§oYour adrenaline kicks in, speeding you up");
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList().toArray(new Block[0])) {
			if (wallHealth.isDisplayed()
					&& LocUtils.isSame(b.getLocation(), wallObjLoc.getBlock().getLocation().add(0, -1, 0))) {
				wallHealth.setVal(wallHealth.getVal() - (rand.nextInt(10) + 30));

				if (wallHealth.getVal() < 1) {
					wallHealth.setDisplayed(false);

					int rad = 5;
					for (int dx = -rad; dx <= rad; dx++) {
						for (int dy = 1; dy <= rad; dy++) {
							for (int dz = -rad; dz <= rad; dz++) {
								Block ds = event.getEntity().getLocation().clone().add(dx, dy, dz).getBlock();
								ds.getWorld().playEffect(ds.getLocation(), Effect.STEP_SOUND, ds.getType());
								ds.setType(Material.AIR);
							}
						}
					}

					Minigames.broadcastAll(Minigames.getBroadcastMessage(
							"The %s has been destroyed by the Barbarians!", "Wall"), Minigames.getBroadcastMessage(
							"Barbarians must place %s near the Spawn Crystal to weaken it!", "inhibitors"));

					cleanObjectives();
					crystalLocation.getWorld().playSound(crystalLocation, Sound.ENDERDRAGON_DEATH, 20, 2);

					inhibitorsRemaining.setDisplayed(true);
					reinforcements.setVal(reinforcements.getVal() + reinforcementBonus);
				} else
					event.blockList().clear();
				break;
			}
		}
	}

	public void placeCache(Location loc) {
		// 1: chest, 2: trap chest, 3: chest / coal block 4: trap chest / coal
		// block 5: chest / chest 6: trap chest / chest 7: chest / trap chest 8:
		// trap chest / trap chest
		int sizeX = 3, sizeZ = 3;
		int[][] layout = new int[3][3];
		for (int i = 0; i < layout.length; i++) {
			for (int j = 0; j < layout[i].length; j++) {
				layout[i][j] = rand.nextInt(8) + 1;
			}
		}

		Location start = loc.clone().add(-1, 0, -1);
		for (int dx = 0; dx < sizeX; dx++) {
			for (int dz = 0; dz < sizeZ; dz++) {
				Block c = start.clone().add(dx, 0, dz).getBlock();
				Block cu = c.getLocation().clone().add(0, 1, 0).getBlock();
				int place = layout[dx][dz];

				switch (place) {
					case 1:
						c.setType(Material.CHEST);
						break;
					case 2:
						c.setType(Material.TRAPPED_CHEST);
						break;
					case 3:
						c.setType(Material.COAL_BLOCK);
						cu.setType(Material.CHEST);
						break;
					case 4:
						c.setType(Material.COAL_BLOCK);
						cu.setType(Material.TRAPPED_CHEST);
						break;
					case 5:
						c.setType(Material.CHEST);
						cu.setType(Material.CHEST);
						break;
					case 6:
						c.setType(Material.CHEST);
						cu.setType(Material.TRAPPED_CHEST);
						break;
					case 7:
						c.setType(Material.TRAPPED_CHEST);
						cu.setType(Material.CHEST);
						break;
					case 8:
						c.setType(Material.TRAPPED_CHEST);
						cu.setType(Material.CHEST);
						break;
					default:
						break;
				}
			}
		}
	}

}

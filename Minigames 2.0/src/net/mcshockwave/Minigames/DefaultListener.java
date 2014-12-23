package net.mcshockwave.Minigames;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.Currency.LevelUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.PacketUtils.ParticleEffect;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Commands.Force;
import net.mcshockwave.Minigames.Commands.MgInfo;
import net.mcshockwave.Minigames.worlds.FileElements;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Random;

public class DefaultListener implements Listener {

	public Minigames	plugin;

	public DefaultListener(Minigames instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();

		if (plugin.checkCanStart()) {
			Minigames.broadcast("Game has reached minimum player count!");
			Minigames.startCount();
		}

		if (!Minigames.started) {
			p.teleport(new Location(Multiworld.getLobby(), 0, 102, 0));
		} else {
			p.teleport(Game.getLocation("lobby"));
			String name = p.getName();
			name = name.substring(0, name.length() > 14 ? 13 : name.length());
			p.setPlayerListName(ChatColor.GRAY + name);
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				MCShockwave.updateTab(p2);
			}
		}

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				Minigames.resetPlayer(p);
				if (Minigames.started) {
					Minigames.spectate(p);
				}
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		onQuit(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		onQuit(event.getPlayer());
	}

	public void onQuit(Player p) {
		if (Minigames.getOptedIn().size() < 3) {
			Minigames.stop(null);
		}
		Minigames.optedOut.remove(p.getName());
		if (Minigames.started && Minigames.alivePlayers.contains(p.getName())) {
			Minigames.setDead(p, false);
			Minigames.sendDeathToGame(p);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		Action a = event.getAction();
		ItemStack it = p.getItemInHand();

		if (!Minigames.alivePlayers.contains(p.getName()) && Minigames.getOptedIn().contains(p)
				&& p.getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
		if (!Minigames.alivePlayers.contains(p.getName()) && it.getType() == Material.FEATHER && a != Action.PHYSICAL) {
			int size = Minigames.alivePlayers.size();
			Inventory i = Bukkit.createInventory(null, size + (9 - (size % 9)), "Teleport To Players");
			for (String s : Minigames.alivePlayers) {
				Player sp = Bukkit.getPlayer(s);
				if (sp != null) {
					ChatColor cc = ChatColor.GOLD;

					if (Game.getTeam(sp) != null) {
						cc = Game.getTeam(sp).color;
					}

					i.addItem(ItemMetaUtils.setHeadName(
							ItemMetaUtils.setItemName(new ItemStack(Material.SKULL_ITEM, 1, (short) 3),
									cc + sp.getName()), sp.getName()));
				}
			}
			p.openInventory(i);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player p = event.getEntity();

		event.setDeathMessage("");
		if (!Minigames.optedOut.contains(p.getName()) && Minigames.currentGame != null && Minigames.started) {
			if (!Minigames.currentGame.canRespawn) {
				Minigames.setDead(p, true);
			} else {
				Minigames.sendDeathToGame(p);
			}

			if (Minigames.defaultSidebar) {
				Minigames.updateDefaultSidebar();
			}

			// SO IT WONT BREAK K?
			try {
				if (p.getKiller() != null) {
					String display = "§o" + p.getName();
					String displayKiller = "§o" + p.getKiller().getName();
					if (Minigames.currentGame.isTeamGame() && Game.getTeam(p) != null) {
						display = Game.getTeam(p).color + display;
					}
					if (Minigames.currentGame.isTeamGame() && Game.getTeam(p.getKiller()) != null) {
						displayKiller = Game.getTeam(p.getKiller()).color + displayKiller;
					}

					PacketUtils.playTitle(p.getKiller(), 0, 2, 13, "", "§7Killed §6" + display);
					PacketUtils.playTitle(p, 3, 10, 10, "§6" + displayKiller, "§7§oKilled You");
				} else {
					PacketUtils.playTitle(p, 3, 10, 10, "§6Nobody", "§7§oKilled You");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (p.getKiller() != null) {
				Player k = p.getKiller();
				Random rand = new Random();

				if (rand.nextInt(SQLTable.Settings.getInt("Setting", "XPChance", "Value")) == 0) {
					int minXp = SQLTable.Settings.getInt("Setting", "XPMin", "Value");
					int maxXp = SQLTable.Settings.getInt("Setting", "XPMax", "Value");
					LevelUtils.addXP(k, rand.nextInt(maxXp - minXp) + minXp, "killing " + p.getName(), true);
				}
			}
		}
		PacketUtils.sendPacketGlobally(p.getEyeLocation(), 50,
				PacketUtils.generateParticles(ParticleEffect.LAVA, p.getEyeLocation(), 0, 1, 50));
		p.getWorld().playSound(p.getEyeLocation(), Sound.CHICKEN_EGG_POP, 1, 0);
		PlayerRespawnEvent pre = new PlayerRespawnEvent(p, p.getWorld().getSpawnLocation(), false);
		Bukkit.getPluginManager().callEvent(pre);
		p.setHealth(20);
		p.teleport(pre.getRespawnLocation());
		new BukkitRunnable() {
			public void run() {
				p.setVelocity(new Vector());
				p.setFireTicks(0);
			}
		}.runTaskLater(Minigames.ins, 1);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		HumanEntity he = event.getWhoClicked();
		Inventory i = event.getInventory();
		ItemStack cu = event.getCurrentItem();

		if (he instanceof Player) {
			Player p = (Player) he;

			if (i.getName().equalsIgnoreCase("Force Games")) {
				event.setCancelled(true);

				String name = ItemMetaUtils.getItemName(cu);
				name = name.substring(11);

				p.playSound(p.getEyeLocation(), Sound.CLICK, 1, 1);
				Force.forceGame(p, name, false);
			}

			if (i.getName().equalsIgnoreCase("Teleport To Players")) {
				event.setCancelled(true);

				String s = ChatColor.stripColor(ItemMetaUtils.getItemName(cu));
				Player sp = Bukkit.getPlayer(s);
				if (sp != null) {
					p.teleport(sp);
					p.closeInventory();

					ChatColor cc = ChatColor.GOLD;

					if (Game.getTeam(sp) != null) {
						cc = Game.getTeam(sp).color;
					}

					Minigames.send(cc, p, "Teleported to %s", sp.getName());
				}
			}

			if (i.getName().equalsIgnoreCase("Info")) {
				event.setCancelled(true);

				String s = ChatColor.stripColor(ItemMetaUtils.getItemName(cu));
				Game g = Game.valueOf(s.replace(' ', '_'));

				if (g != null && GameInfo.valueOf(g.name()) != null) {
					MgInfo.sendInfo(p, GameInfo.valueOf(g.name()));
					p.closeInventory();
				}
			}

		}
	}

	@EventHandler
	public void onFood(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		if (!Minigames.started) {
			event.setRespawnLocation(new Location(Multiworld.getLobby(), 0, 103, 0));
		} else {
			if (!Minigames.alivePlayers.contains(p.getName())) {
				event.setRespawnLocation(Game.getLocation("lobby"));
				return;
			}

			if (Minigames.currentGame.canRespawn) {
				if (Minigames.currentGame.isTeamGame()) {

					for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
						if (t.hasPlayer(p)) {
							GameTeam gt = Game.getTeam(t);

							event.setRespawnLocation(Game.getSpawn(gt));
							return;
						}
					}

					event.setRespawnLocation(Game.getFFASpawn());

				} else {
					event.setRespawnLocation(Game.getFFASpawn());
				}
			} else {
				event.setRespawnLocation(Game.getLocation("lobby"));
			}
		}

	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (!Minigames.started) {
			event.setCancelled(true);
		}
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();

			if (Minigames.optedOut.contains(p.getName())) {
				event.setCancelled(true);
				return;
			}

			if (Minigames.started && !Minigames.alivePlayers.contains(p.getName())
					&& !Minigames.deadPlayers.contains(p.getName())) {
				event.setCancelled(false);
				Minigames.setDead(p, true);
				return;
			}

			if (!Minigames.alivePlayers.contains(p.getName())) {
				if (Game.getTeam(p) != null) {
					Minigames.alivePlayers.add(p.getName());
					event.setCancelled(false);
				} else
					event.setCancelled(true);
			}
		}

		if (Minigames.explode) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity pe = event.getEntity();
		Entity de = event.getDamager();

		if (pe instanceof Player && de instanceof Player) {
			// Player p = (Player) pe;
			Player d = (Player) de;

			if (!Minigames.alivePlayers.contains(d.getName()) || Minigames.optedOut.contains(d.getName())
					|| Minigames.started && !Minigames.currentGame.allowPVP) {
				event.setCancelled(true);
			} else
				event.setCancelled(false);
		} else if (de instanceof Player) {
			Player d = (Player) de;

			if (!Minigames.alivePlayers.contains(d.getName())) {
				event.setCancelled(true);
			}
		}
		// spec blocking
		if (pe instanceof Player && de instanceof Projectile) {
			Projectile arrow = (Projectile) de;
			Player d = (Player) arrow.getShooter();
			Player p = (Player) pe;

			Vector velocity = arrow.getVelocity();
			Class<? extends Projectile> pc = arrow.getClass();

			if (!Minigames.alivePlayers.contains(p.getName())) {
				p.teleport(p.getLocation().add(0, 5, 0));
				Minigames.send(p, "You are in the way of a(n) %s!",
						arrow.getType().name().toLowerCase().replace('_', ' '));

				Projectile newArrow = d.launchProjectile(pc);
				newArrow.setShooter(d);
				newArrow.setVelocity(velocity);
				newArrow.setBounce(false);

				event.setCancelled(true);
				arrow.remove();
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if (event.getTo().getY() < (event.getTo().getWorld().getName().equalsIgnoreCase(Multiworld.getGame().getName())
				&& FileElements.has("min-y", "Game") ? Game.getDouble("min-y") : 80)) {
			if (Minigames.optedOut.contains(p.getName()) || Minigames.explode) {
				p.teleport(p.getWorld().getSpawnLocation());
			} else {
				p.damage(p.getMaxHealth());
			}
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (!Minigames.started || event.getEntity() != null
				&& event.getEntity().getWorld().getName().equalsIgnoreCase("Lobby")) {
			event.blockList().clear();
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		event.getEntity().remove();
	}

	@EventHandler
	public void onBlockChange(BlockFromToEvent event) {
		if (event.getBlock().getType() == Material.GRASS && event.getToBlock().getType() == Material.DIRT) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (!Minigames.alivePlayers.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		String message = e.getMessage();
		String mlc = message.toLowerCase();
		String[] argslc = mlc.split(" ");

		if (argslc[0].equalsIgnoreCase("/kill") || argslc[0].equalsIgnoreCase("/bukkit:kill")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemConsume(final PlayerItemConsumeEvent event) {
		if (event.getItem().getType() != Material.POTION) {
			return;
		}
		Bukkit.getScheduler().runTask(plugin, new Runnable() {
			public void run() {
				event.getPlayer().setItemInHand(null);
			}
		});
	}

}

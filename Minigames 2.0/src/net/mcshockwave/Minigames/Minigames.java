package net.mcshockwave.Minigames;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.MCS.Commands.VanishCommand;
import net.mcshockwave.MCS.Stats.Statistics;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Commands.Force;
import net.mcshockwave.Minigames.Commands.InvisCommand;
import net.mcshockwave.Minigames.Commands.MGC;
import net.mcshockwave.Minigames.Commands.MgInfo;
import net.mcshockwave.Minigames.Commands.Opt;
import net.mcshockwave.Minigames.Commands.Revive;
import net.mcshockwave.Minigames.Commands.Shop;
import net.mcshockwave.Minigames.Commands.TeamSelect;
import net.mcshockwave.Minigames.Commands.UpsiesCommand;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.PreGame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Shop.ShopListener;
import net.mcshockwave.Minigames.Shop.ShopUtils;
import net.mcshockwave.Minigames.Utils.PointsUtils;
import net.mcshockwave.Minigames.Utils.SoundUtils;
import net.mcshockwave.Minigames.Utils.TeleportUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class Minigames extends JavaPlugin {

	public static Minigames					ins				= null;

	public static ArrayList<String>			optedOut		= new ArrayList<String>();
	public static ArrayList<String>			alivePlayers	= new ArrayList<String>();
	public static ArrayList<String>			deadPlayers		= new ArrayList<String>();

	public static Game						gameBefore		= null;
	public static Game						currentGame		= null;
	public static boolean					gameForced		= false;

	public static boolean					countingDown	= false, started = false, canOpenShop = false;

	private static Random					rand			= new Random();

	public static int						pointsOnWin		= 0;

	public static World						w;

	public static HashMap<Player, ShopItem>	used			= new HashMap<Player, ShopItem>();
	public static HashMap<Player, ShopItem>	usedNoPay		= new HashMap<Player, ShopItem>();

	public static HashMap<Player, GameTeam>	selectedTeam	= new HashMap<>();

	public void onEnable() {
		ins = this;

		MCShockwave.setMaxPlayers(30);

		Bukkit.getPluginManager().registerEvents(new DefaultListener(ins), ins);
		Bukkit.getPluginManager().registerEvents(new ShopListener(), ins);

		getCommand("opt").setExecutor(new Opt());
		getCommand("mg").setExecutor(new MGC());
		getCommand("info").setExecutor(new MgInfo());
		getCommand("force").setExecutor(new Force());
		getCommand("shop").setExecutor(new Shop());
		// getCommand("points").setExecutor(new Points());
		getCommand("team").setExecutor(new TeamSelect());
		getCommand("upsies").setExecutor(new UpsiesCommand());
		getCommand("revive").setExecutor(new Revive());
		getCommand("invis").setExecutor(new InvisCommand());

		if (checkCanStart()) {
			startCount();
		}

		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				World dw = Bukkit.getWorld("McMinigames");
				if (w == null) {
					w = dw;
				}
			}
		}, 10L);

		resetScoreboard();
	}

	public static void resetScoreboard() {
		Team[] ts = Bukkit.getScoreboardManager().getMainScoreboard().getTeams().toArray(new Team[0]);
		for (Team t : ts) {
			t.unregister();
		}

		Objective[] os = Bukkit.getScoreboardManager().getMainScoreboard().getObjectives().toArray(new Objective[0]);
		for (Objective o : os) {
			if (o.getDisplaySlot() == DisplaySlot.BELOW_NAME)
				continue;
			o.unregister();
		}
	}

	public void onDisable() {
		stop(null);
		for (Player p : Bukkit.getOnlinePlayers()) {
			resetPlayer(p);
		}
	}

	public static List<BukkitTask>	timer	= new ArrayList<BukkitTask>();

	public static void startCount() {
		if (countingDown) {
			return;
		}

		currentGame = Game.values()[rand.nextInt(Game.values().length)];
		if (currentGame == gameBefore || Arrays.asList(Game.broken).contains(currentGame)) {
			startCount();
			return;
		}

		countingDown = true;
		Bukkit.getScheduler().runTask(ins, new Runnable() {
			public void run() {

				int totalTime = 45;
				broadcastAll(getBroadcastMessage("%s was chosen!", currentGame.name),
						getBroadcastMessage("For info on this minigame, type %s", "/info"),
						getBroadcastMessage("Game will start in %s seconds", totalTime));

				int[] br = { 30, 15, 10, 5, 4, 3, 2, 1 };
				for (final int b : br) {
					long time = (totalTime - b) * 20;

					timer.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
						public void run() {
							broadcast("%s will start in %s second" + (b == 1 ? "" : "s"), currentGame.name, b);

							if (b == 30) {
								if (ShopUtils.getItems(Minigames.currentGame).length >= 1) {
									broadcast(ChatColor.DARK_AQUA, "Type %s to open the shop!", "/shop");
								}
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (p.getOpenInventory().getTitle().equalsIgnoreCase("Force Games")) {
										p.closeInventory();
									}

									for (ShopItem si : ShopUtils.getItems(currentGame)) {
										if (ShopUtils.hasPermaItem(p, si)) {
											Minigames.send(p, "You have a %s item for this game!", "permanent");
											break;
										}
									}
								}
								canOpenShop = true;

								currentGame.lobby.getChunk().load();
								if (currentGame.isTeamGame()) {
									for (GameTeam gt : currentGame.teams) {
										gt.spawn.getChunk().load();
									}
								} else
									currentGame.spawn.getChunk().load();
							}
							if (b == 15) {
								for (Method m : currentGame.mclass.getClass().getMethods()) {
									if (m.isAnnotationPresent(PreGame.class)) {
										try {
											m.invoke(this);
										} catch (Exception e) {
										}
									}
								}
							}
							if (b <= 5) {
								SoundUtils.playSoundToAll(Sound.ORB_PICKUP, 1, (b * 2) / 5);
							}
						}
					}, time));
				}

				timer.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
					public void run() {
						start();
					}
				}, totalTime * 20));

			}
		});

	}

	public static void stop(Object winner) {
		for (BukkitTask bt : timer) {
			bt.cancel();
		}
		Bukkit.getScheduler().cancelTasks(ins);
		Bukkit.getScheduler().cancelAllTasks();

		if (currentGame != null) {
			HandlerList.unregisterAll(currentGame.mclass);
			currentGame.mclass.onGameEnd();
		}

		used.clear();

		TeleportUtils.spread(new Location(w, 0, 103, 0), 5, getOptedIn().toArray(new Player[0]));

		for (Player p : getOptedIn()) {
			Minigames.milkPlayer(p);
			p.setFlying(false);
			p.setAllowFlight(false);
		}

		if (winner != null) {
			String winName = null;
			ChatColor winColor = ChatColor.GOLD;
			if (winner instanceof Player) {
				final Player win = (Player) winner;
				winName = win.getName();

				final String name = currentGame.name;
				final int points = pointsOnWin;

				Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
					public void run() {
						PointsUtils
								.addPoints(win, MCShockwave.pointmult == 1 ? points : points * MCShockwave.pointmult,
										"winning " + name);
						win.playSound(win.getLocation(), Sound.LEVEL_UP, 1, 1);
						Statistics.incrWins(win.getName(), true);
					}
				}, 20);
				for (Player p2 : getOptedIn()) {
					final Player p3 = p2;
					Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
						public void run() {
							if (p3 != win) {
								p3.playSound(p3.getEyeLocation(), Sound.ANVIL_LAND, 1, 1);
							}
						}
					}, 20l);
				}
			}
			if (winner instanceof Team) {
				final Team win = (Team) winner;
				winName = ChatColor.stripColor(win.getDisplayName());

				if (Game.getTeam(win) != null) {
					winColor = Game.getTeam(win).color;
				}

				for (OfflinePlayer op : win.getPlayers()) {
					if (op instanceof Player) {
						final Player w = (Player) op;

						final String name = currentGame.name;
						final int points = pointsOnWin;

						Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
							public void run() {
								PointsUtils.addPoints(w, points, "winning " + name);
								w.playSound(w.getEyeLocation(), Sound.LEVEL_UP, 1, 1);
								Statistics.incrWins(w.getName(), false);
							}
						}, 20);
					}
				}
				for (Player p : getOptedIn()) {
					final Player p2 = p;
					Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
						public void run() {
							if (!win.hasPlayer(p2)) {
								p2.playSound(p2.getEyeLocation(), Sound.ANVIL_LAND, 1, 1);
							}
						}
					}, 20l);
				}
			}
			if (winName != null) {
				broadcast(winColor, "%s has won %s!", winName, currentGame.name);
			} else {
				broadcast("%s has ended!", currentGame.name);
			}
		} else {
			refundAll();
			broadcast("%s has ended!", currentGame.name);
		}
		if (currentGame.isTeamGame()) {
			for (GameTeam gt : currentGame.teams) {
				if (gt.team == null)
					continue;
				gt.team.unregister();
				gt.team = null;
			}
		}
		Team[] ts = Bukkit.getScoreboardManager().getMainScoreboard().getTeams().toArray(new Team[0]);
		for (Team t : ts) {
			t.unregister();
		}
		clearInv(getOptedIn().toArray(new Player[0]));
		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				for (Player p : getOptedIn()) {
					resetPlayer(p);
				}
			}
		}, 1);
		for (Player p : Bukkit.getOnlinePlayers()) {
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (!VanishCommand.vanished.containsKey(p.getName())) {
					p2.showPlayer(p);
				}
				if (!VanishCommand.vanished.containsKey(p2.getName())) {
					p.showPlayer(p2);
				}
			}

			if (SQLTable.hasRank(p.getName(), Rank.IRON)) {
				giveHelm(p);
			}
		}
		for (Entity e : w.getEntities()) {
			if (e instanceof Item || e instanceof Slime) {
				e.remove();
			}
		}
		alivePlayers.clear();
		deadPlayers.clear();
		gameBefore = currentGame;
		currentGame = null;
		countingDown = false;
		started = false;
		if (getOptedIn().size() <= 2 || Minigames.countingDown) {
			return;
		}
		Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				startCount();
			}
		}, 100);
		resetScoreboard();
	}

	public static void start() {
		resetScoreboard();

		countingDown = false;
		started = true;
		gameForced = false;
		canOpenShop = false;

		pointsOnWin = getOptedIn().size() * (currentGame.isTeamGame() ? 20 : 40);

		if (MCShockwave.pointmult != 1 && MCShockwave.xpmult == 1) {
			broadcastAll(ChatColor.RED + "" + ChatColor.BOLD
					+ "Server point multiplier is active for this game! Current multiplier: " + ChatColor.AQUA + ""
					+ ChatColor.BOLD + "x" + MCShockwave.pointmult);
		}
		if (MCShockwave.xpmult != 1 && MCShockwave.pointmult == 1) {
			broadcastAll(ChatColor.RED + "" + ChatColor.BOLD
					+ "Server xp multiplier is active for this game! Current multiplier: " + ChatColor.AQUA + ""
					+ ChatColor.BOLD + "x" + MCShockwave.xpmult);
		}
		if (MCShockwave.xpmult != 1 && MCShockwave.pointmult != 1) {
			broadcastAll(ChatColor.RED + "" + ChatColor.BOLD
					+ "Server point and xp multipliers are active for this game! Current multipliers: "
					+ ChatColor.AQUA + "" + ChatColor.BOLD + "x" + MCShockwave.pointmult + " Points, x"
					+ MCShockwave.xpmult + " XP");
		}

		for (Player p : getOptedIn()) {
			for (Player p2 : getOptedIn()) {
				if (p != p2) {
					p.hidePlayer(p2);
					p2.hidePlayer(p);
					final Player fp = p;
					final Player fp2 = p2;
					Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
						public void run() {
							fp.showPlayer(fp2);
							fp2.showPlayer(fp);
						}
					}, 10);
				}
			}
			alivePlayers.add(p.getName());
			if (p.getGameMode() != GameMode.SURVIVAL) {
				p.setGameMode(GameMode.SURVIVAL);
			}
			clearInv(p);
			p.setFallDistance(0);
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10, 100));
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			sendAll(p,
					getBroadcastMessage("%s has started!", currentGame.name),
					getBroadcastMessage("You will earn %s points if you win", MCShockwave.pointmult == 1 ? pointsOnWin
							: pointsOnWin * MCShockwave.pointmult));

			p.setHealth(20);
		}

		Bukkit.getPluginManager().registerEvents(currentGame.mclass, ins);

		ArrayList<Team> tsal = new ArrayList<Team>();

		if (currentGame.isTeamGame()) {
			for (GameTeam gt : currentGame.teams) {
				if (Bukkit.getScoreboardManager().getMainScoreboard().getTeam(gt.name) != null) {
					Bukkit.getScoreboardManager().getMainScoreboard().getTeam(gt.name).unregister();
				}
				Team t = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(gt.name);
				t.setSuffix(ChatColor.RESET.toString());
				t.setPrefix(gt.color.toString());
				t.setAllowFriendlyFire(false);
				t.setCanSeeFriendlyInvisibles(true);
				tsal.add(t);
				gt.team = t;
			}

			Team[] ts = tsal.toArray(new Team[0]);

			int tid = 0;

			List<Player> noteam = getOptedIn();

			while (noteam.size() > 0) {
				Player p = noteam.get(rand.nextInt(noteam.size()));
				Team t = null;
				for (Entry<Player, GameTeam> e : selectedTeam.entrySet()) {
					if (e.getValue() == Game.getTeam(ts[tid])) {
						p = e.getKey();
					}
				}
				if (selectedTeam.containsKey(p)) {
					GameTeam gt = selectedTeam.get(p);
					selectedTeam.remove(p);

					t = gt.team;
				} else {
					t = ts[tid];
				}

				t.addPlayer(p);
				noteam.remove(p);
				for (GameTeam gt : currentGame.teams) {
					if (gt.name.equalsIgnoreCase(t.getName())) {
						p.teleport(gt.spawn);
						send(gt.color, p, "You are on %s!", gt.name);
						break;
					}
				}
				if (currentGame == Game.Ghostbusters) {
					int max = getOptedIn().size() / 4 + 1;
					if (tid == 0 && ts[0].getPlayers().size() >= max) {
						tid = 1;
					}
				} else if (currentGame == Game.Infection) {
					int max = getOptedIn().size() > 6 ? 3 : 1;
					if (tid == 0 && ts[0].getPlayers().size() >= max) {
						tid = 1;
					}
				} else if (currentGame == Game.Minotaur) {
					if (ts[1].getPlayers().size() < 1) {
						ts[1].addPlayer(p);
					} else {
						ts[0].addPlayer(p);
					}
				} else {
					tid++;
					if (tid >= tsal.size()) {
						tid = 0;
					}
				}
			}
		} else {
			TeleportUtils.spread(currentGame.spawn, currentGame.radius, getOptedIn().toArray(new Player[0]));

			for (Player p : getOptedIn()) {
				if (!p.getLocation().getChunk().isLoaded()) {
					p.getLocation().getChunk().load();
				}
			}
		}
		SoundUtils.playSoundToAll(Sound.AMBIENCE_THUNDER, 1, 0.75f);

		int time = currentGame.time;
		int[] secs = { 45, 30, 15, 10, 5, 4, 3, 2, 1, 0 };
		for (int i = time; i > 1; i--) {
			final int i2 = time - i;
			timer.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					Minigames.broadcast(ChatColor.GREEN, "Game ending in %s minutes", i2);
				}
			}, i * 1200));
		}
		for (int i : secs) {
			final int i2 = i;
			timer.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					Minigames.broadcast(ChatColor.GREEN, "Game ending in %s seconds", i2);
				}
			}, ((time - 1) * 1200) + (60 - i) * 20));
		}
		currentGame.mclass.onGameStart();
		timer.add(Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
			public void run() {
				Minigames.broadcast(ChatColor.GREEN, "Times up! Ending %s!", "game");
				if (Minigames.currentGame == Game.Storm_The_Castle) {
					stop(Game.Storm_The_Castle.getTeam("Knights").team);
				} else {
					stop(null);
				}
			}
		}, time * 1200));
	}

	// @@@@@@@@@@@@@@@@@@@@@@@ [ OTHER ] @@@@@@@@@@@@@@@@@@@@@@@

	public static World getDefaultWorld() {
		World dw = Bukkit.getWorld("McMinigames");
		return dw;
	}

	public static String getBroadcastMessage(String mes, Object... form) {
		return getBroadcastMessage(ChatColor.GOLD, mes, form);
	}

	public static String getBroadcastMessage(ChatColor color, String mes, Object... form) {
		String b = ChatColor.GRAY + mes;
		Object[] format = form;
		for (int i = 0; i < format.length; i++) {
			String s = format[i].toString();
			format[i] = color + ChatColor.ITALIC.toString() + s + ChatColor.GRAY;
		}
		b = String.format(b, format);
		return b;
	}

	public static void broadcastDeath(Player p, Player k, String noKiller, String isKiller) {
		ChatColor pcc = ChatColor.GOLD;
		GameTeam pgt = Game.getTeam(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p));
		if (pgt != null) {
			pcc = pgt.color;
		}

		if (k != null) {
			ChatColor kcc = ChatColor.GOLD;
			GameTeam kgt = Game.getTeam(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(k));
			if (kgt != null) {
				kcc = kgt.color;
			}
			Bukkit.broadcastMessage(String.format(isKiller, pcc.toString() + ChatColor.ITALIC + p.getName()
					+ ChatColor.GRAY, kcc.toString() + ChatColor.ITALIC + k.getName() + ChatColor.GRAY));
		} else {
			Bukkit.broadcastMessage(String.format(noKiller, pcc.toString() + ChatColor.ITALIC + p.getName()
					+ ChatColor.GRAY));
		}
	}

	public static void broadcast(String mes, Object... form) {
		Bukkit.broadcastMessage(getBroadcastMessage(mes, form));
	}

	public static void broadcast(ChatColor color, String mes, Object... form) {
		Bukkit.broadcastMessage(getBroadcastMessage(color, mes, form));
	}

	public static void send(Player p, String mes, Object... form) {
		p.sendMessage(getBroadcastMessage(mes, form));
	}

	public static void send(ChatColor color, Player p, String mes, Object... form) {
		p.sendMessage(getBroadcastMessage(color, mes, form));
	}

	public static void broadcastAll(String... broad) {
		Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

		for (String s : broad) {
			Bukkit.broadcastMessage(s);
		}

		Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
	}

	public static void sendAll(Player p, String... send) {
		p.sendMessage(ChatColor.DARK_GRAY + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

		for (String s : send) {
			p.sendMessage(s);
		}

		p.sendMessage(ChatColor.DARK_GRAY + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
	}

	public static List<Player> getOptedIn() {
		ArrayList<Player> ret = new ArrayList<Player>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!optedOut.contains(p.getName())) {
				ret.add(p);
			}
		}
		return ret;
	}

	public boolean checkCanStart() {
		return getOptedIn().size() >= 3 && !started && !countingDown;
	}

	public static void clearInv(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}

	public static void clearInv(Player... ps) {
		for (Player p : ps) {
			clearInv(p);
		}
	}

	public static void sendDeathToGame(Player p) {
		GameTeam gt = Game.getTeam(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p));
		DeathEvent de = null;
		try {
			if (gt != null) {
				de = new DeathEvent(p, p.getKiller(), p.getLastDamageCause().getCause(), gt.team, gt);
			} else {
				de = new DeathEvent(p, p.getKiller(), p.getLastDamageCause().getCause());
			}
		} catch (Exception e) {
		}
		if (de != null) {
			Minigames.currentGame.mclass.onPlayerDeath(de);
		}
	}

	public static void setDead(Player p, boolean sendDeath) {
		if (alivePlayers.contains(p.getName())) {
			alivePlayers.remove(p.getName());
			deadPlayers.add(p.getName());

			if (sendDeath) {
				sendDeathToGame(p);
			}

			if (p.getKiller() != null) {
				String display = "§o" + p.getName();
				String displayKiller = "§o" + p.getKiller().getName();
				if (currentGame.isTeamGame() && Game.getTeam(p) != null) {
					display = Game.getTeam(p).color + display;
				}
				if (currentGame.isTeamGame() && Game.getTeam(p.getKiller()) != null) {
					displayKiller = Game.getTeam(p.getKiller()).color + displayKiller;
				}
				PacketUtils.playTitle(p.getKiller(), 0, 2, 13, "", "§7Killed §6" + display);
				PacketUtils.playTitle(p, 3, 10, 10, "§6" + displayKiller, "§7§oKilled You");
			} else {
				PacketUtils.playTitle(p, 3, 10, 10, "§6Nobody", "§7§oKilled You");
			}

			if (currentGame.isTeamGame()) {
				Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p).removePlayer(p);
			}

			clearInv(p);

			checkDone();

			if (currentGame != null) {
				p.teleport(Minigames.currentGame.lobby);
			} else {
				p.teleport(new Location(w, 0, 102, 0));
			}
			spectate(p);
			String name = p.getName();
			name = name.substring(0, name.length() > 14 ? 13 : name.length());
			p.setPlayerListName(ChatColor.GRAY + name);
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				MCShockwave.updateTab(p2);
			}
		}
	}

	public static void spectate(Player p) {
		Minigames.milkPlayer(p);
		for (Player p2 : Bukkit.getOnlinePlayers()) {
			if (p2 != p) {
				if (alivePlayers.contains(p2.getName())) {
					p2.hidePlayer(p);
					p.showPlayer(p2);
				} else {
					p2.showPlayer(p);
					p.showPlayer(p2);
				}
			}
		}
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0));
		if (currentGame == Game.TRON || currentGame == Game.Airships) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 0));
		}
		p.getInventory().setItem(
				8,
				ItemMetaUtils.setItemName(new ItemStack(Material.FEATHER), ChatColor.GOLD
						+ "TP to Players (Right-click)"));
		p.setAllowFlight(true);
		send(ChatColor.DARK_AQUA, p, "You can now %s!", "fly");
	}

	public static void checkDone() {
		if (Minigames.currentGame.isTeamGame()) {
			boolean te = true;
			boolean done = true;
			Team win = null;
			for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
				if (t.getPlayers().size() > 0) {
					if (te) {
						te = false;
						win = t;
					} else {
						done = false;
						break;
					}
				}
			}

			if (done) {
				Minigames.stop(win);
			}
		} else {
			if (Minigames.alivePlayers.size() == 1) {
				Player w = Bukkit.getPlayer(Minigames.alivePlayers.get(0));

				Minigames.stop(w);
			} else if (Minigames.alivePlayers.size() < 1) {
				Minigames.stop(null);
			}
		}
	}

	public static List<Team> getTeamsLeft() {
		ArrayList<Team> ts = new ArrayList<Team>();
		for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
			if (t.getPlayers().size() > 0) {
				ts.add(t);
			}
		}
		return ts;
	}

	public static void resetPlayer(Player p) {
		p.setHealth(20);
		p.setFireTicks(0);
		p.setWalkSpeed(0.2f);
		clearInv(p);
		if (p.getGameMode() != GameMode.CREATIVE) {
			p.setAllowFlight(false);
		}
		milkPlayer(p);
		p.setPlayerListName(p.getName());
		for (Player p2 : Bukkit.getOnlinePlayers()) {
			MCShockwave.updateTab(p2);
		}
		p.setCompassTarget(p.getWorld().getSpawnLocation());
	}

	public static void milkPlayer(Player p) {
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}

	public static boolean hasItem(Player p, ShopItem si) {
		return used.containsKey(p) && used.get(p) == si || usedNoPay.containsKey(p) && usedNoPay.get(p) == si;
	}

	public static void refundAll() {
		for (Player p : used.keySet()) {
			ShopItem u = used.get(p);
			PointsUtils.addPoints(p, u.cost, "refund of " + u.name);
		}
	}

	public static void giveHelm(Player p) {
		Color c = Color.GRAY;
		if (SQLTable.hasRank(p.getName(), Rank.GOLD)) {
			c = Color.YELLOW;
		}
		if (SQLTable.hasRank(p.getName(), Rank.DIAMOND)) {
			c = Color.AQUA;
		}
		if (SQLTable.hasRank(p.getName(), Rank.EMERALD)) {
			c = Color.GREEN;
		}
		if (SQLTable.hasRank(p.getName(), Rank.OBSIDIAN)) {
			c = Color.PURPLE;
		}
		if (SQLTable.hasRank(p.getName(), Rank.NETHER)) {
			c = Color.fromRGB(128, 0, 0);
		}
		if (SQLTable.hasRank(p.getName(), Rank.ENDER)) {
			c = Color.BLACK;
		}
		if (SQLTable.hasRank(p.getName(), Rank.JR_MOD)) {
			c = Color.ORANGE;
		}
		if (SQLTable.hasRank(p.getName(), Rank.ADMIN)) {
			c = Color.RED;
		}
		p.getInventory().setHelmet(ItemMetaUtils.setLeatherColor(new ItemStack(Material.LEATHER_HELMET), c));
	}

}

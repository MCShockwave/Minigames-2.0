package net.mcshockwave.Minigames;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.SQLTable.Rank;
import net.mcshockwave.MCS.Challenges.Challenge.ChallengeType;
import net.mcshockwave.MCS.Challenges.ChallengeManager;
import net.mcshockwave.MCS.Commands.VanishCommand;
import net.mcshockwave.MCS.Stats.Statistics;
import net.mcshockwave.MCS.Utils.FireworkLaunchUtils;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.Minigames.Game.GameMap;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Commands.Force;
import net.mcshockwave.Minigames.Commands.MGC;
import net.mcshockwave.Minigames.Commands.MgInfo;
import net.mcshockwave.Minigames.Commands.Opt;
import net.mcshockwave.Minigames.Commands.Shop;
import net.mcshockwave.Minigames.Commands.TeamSelect;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.PreGame;
import net.mcshockwave.Minigames.Handlers.Sidebar;
import net.mcshockwave.Minigames.Handlers.Sidebar.GameScore;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Shop.ShopListener;
import net.mcshockwave.Minigames.Shop.ShopUtils;
import net.mcshockwave.Minigames.Utils.PointsUtils;
import net.mcshockwave.Minigames.Utils.SoundUtils;
import net.mcshockwave.Minigames.Utils.TeleportUtils;
import net.mcshockwave.Minigames.worlds.FileElements;
import net.mcshockwave.Minigames.worlds.Multiworld;
import net.mcshockwave.Minigames.worlds.WorldFileUtils;
import net.minecraft.server.v1_7_R4.PacketPlayOutGameStateChange;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class Minigames extends JavaPlugin {

	public static Minigames					ins				= null;

	public static ArrayList<String>			optedOut		= new ArrayList<String>();
	public static ArrayList<String>			alivePlayers	= new ArrayList<String>();

	public static Game						gameBefore		= null;
	public static Game						currentGame		= null;
	public static boolean					gameForced		= false;
	public static GameMap					currentMap		= null;

	public static boolean					countingDown	= false, started = false, canOpenShop = false;

	private static Random					rand			= new Random();

	public static int						pointsOnWin		= 0;

	public static HashMap<Player, ShopItem>	used			= new HashMap<Player, ShopItem>();
	public static HashMap<Player, ShopItem>	usedNoPay		= new HashMap<Player, ShopItem>();

	public static HashMap<Player, GameTeam>	selectedTeam	= new HashMap<>();

	public static Objective					sidebar			= null;

	public static boolean					isMapNight		= false;
	public static float						weatherStage	= 0f;
	public static BukkitTask				weatherTask		= null;

	public void onEnable() {
		ins = this;

		Multiworld.loadAll();

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
		// getCommand("upsies").setExecutor(new UpsiesCommand());

		if (checkCanStart()) {
			startCount();
		}

		resetScoreboard();

		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-off");
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
		if (currentGame == gameBefore || !currentGame.isEnabled()) {
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

								try {
									Multiworld.deleteWorld("Game");
								} catch (Exception e) {
									e.printStackTrace();
								}
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
							if (b == 10) {
								if (nextMap != null && currentGame.maplist.contains(nextMap)) {
									currentMap = nextMap;
									nextMap = null;
								} else
									currentMap = currentGame.maplist.get(rand.nextInt(currentGame.maplist.size()));

								resetGameWorld(currentGame, currentMap);

								if (currentMap.canBeNight && rand.nextInt(4) == 0) {
									isMapNight = true;
								}
								if (currentMap.canRain && rand.nextInt(4) == 0) {
									weatherStage = rand.nextFloat();
								}
								broadcast("Map chosen: %s" + (isMapNight ? " (Night)" : ""), "§l" + currentMap);
								if (weatherStage > 0) {
									String name = "";
									if (weatherStage > 0.8f) {
										name = "Heavy";
									} else if (weatherStage > 0.4f) {
										name = "Medium";
									} else {
										name = "Light";
									}
									broadcast("Weather: %s", name);
								}
							}
							if (b == 5) {
								try {
									Game.getLocation("lobby").getChunk().load(true);
									if (currentGame.isTeamGame()) {
										for (GameTeam gt : currentGame.teams) {
											Game.getSpawn(gt).getChunk().load(true);
										}
									} else
										Game.getFFASpawn().getChunk().load(true);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (b <= 5) {
								SoundUtils.playSoundToAll(Sound.ORB_PICKUP, 1, (float) ((float) ((b * 2)) / 5f));
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

	public static Color chatColorToColor(ChatColor cc) {
		switch (cc) {
			case RED:
				return Color.RED;
			case BLUE:
				return Color.BLUE;
			case GREEN:
				return Color.LIME;
			case YELLOW:
				return Color.YELLOW;
			case WHITE:
				return Color.WHITE;
			case BLACK:
				return Color.BLACK;
			case AQUA:
				return Color.AQUA;
			default:
				return Color.GRAY;
		}
	}

	public static boolean	explode	= false;

	public static void stop(final Object winner) {
		for (BukkitTask bt : timer) {
			bt.cancel();
		}
		Bukkit.getScheduler().cancelTasks(ins);

		if (currentGame != null) {
			HandlerList.unregisterAll(currentGame.mclass);
			currentGame.mclass.onGameEnd();
		}

		isMapNight = false;
		weatherStage = 0;
		if (weatherTask != null) {
			weatherTask.cancel();
		}

		try {
			if (sidebar != null) {
				sidebar.unregister();
			}
		} catch (Exception e) {
		}
		Sidebar.clearScores();

		if (winner != null) {
			String winName = null;
			ChatColor winColor = ChatColor.GOLD;
			if (winner instanceof Player) {
				final Player win = (Player) winner;
				winName = win.getName();

				final String name = currentGame.name;
				final int points = pointsOnWin;

				ChallengeManager.incrChallenge(ChallengeType.Win_Solo_Minigame, null, currentGame.name(), winName, 1,
						false);
				Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
					public void run() {
						PointsUtils.addPoints(win, points, "winning " + name);
						win.playSound(win.getLocation(), Sound.LEVEL_UP, 1, 1);
						Statistics.incrWins(win.getName(), true);
					}
				}, 20);
				for (final Player p2 : getOptedIn()) {
					Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
						public void run() {
							if (p2 != win) {
								p2.playSound(p2.getEyeLocation(), Sound.ANVIL_LAND, 1, 1);
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

						ChallengeManager.incrChallenge(ChallengeType.Win_Team_Minigame, null, currentGame.name(),
								w.getName(), 1, false);
						Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
							public void run() {
								PointsUtils.addPoints(w, points, "winning " + name);
								w.playSound(w.getEyeLocation(), Sound.LEVEL_UP, 1, 1);
								Statistics.incrWins(w.getName(), false);
							}
						}, 20);
					}
				}
				for (String s : Game.getTeam(win).deadPlayers) {
					if (Bukkit.getPlayer(s) != null) {
						final Player pl = Bukkit.getPlayer(s);

						final String name = currentGame.name;
						final int points = pointsOnWin / 2;

						Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
							public void run() {
								PointsUtils.addPoints(pl, points, "your team winning " + name);
								pl.playSound(pl.getEyeLocation(), Sound.LEVEL_UP, 1, 1);
								Statistics.incrWins(pl.getName(), false);
							}
						}, 20);
					}
				}
				for (Player p : getOptedIn()) {
					final Player p2 = p;
					Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
						public void run() {
							if (!win.hasPlayer(p2) && !Game.getTeam(win).deadPlayers.contains(p2.getName())) {
								p2.playSound(p2.getEyeLocation(), Sound.ANVIL_LAND, 1, 1);
							}
						}
					}, 20l);
				}
			}
			if (winName != null) {
				String has = winName.endsWith("s") ? "have" : "has";
				broadcast(winColor, "%s " + has + " won %s!", winName, currentGame.name);
			} else {
				refundAll();
				broadcast("%s has ended!", currentGame.name);
			}
		} else {
			refundAll();
			broadcast("%s has ended!", currentGame.name);
		}

		used.clear();

		for (Player p : getOptedIn()) {
			p.setAllowFlight(true);
		}

		explode = true;
		SchedulerUtils util = SchedulerUtils.getNew();
		util.add(20);
		Multiworld.getGame().createExplosion(Multiworld.getGame().getSpawnLocation(), 5);
		util.add(50);
		util.add(new Runnable() {
			public void run() {
				Color c = Color.BLACK;
				if (winner instanceof Team) {
					GameTeam gt = Game.getTeam((Team) winner);
					c = chatColorToColor(gt.color);
				}
				for (Player p : getOptedIn()) {
					FireworkLaunchUtils.playFirework(p.getEyeLocation(), c);
				}
			}
		});
		util.add(40);
		util.add(new Runnable() {
			public void run() {
				defaultSidebar = false;

				explode = false;

				for (Player p : getOptedIn()) {
					Minigames.milkPlayer(p);
					p.setFlying(false);
					p.setAllowFlight(false);
					p.teleport(Multiworld.getLobby().getSpawnLocation());
					if (p.getGameMode() != GameMode.ADVENTURE) {
						p.setGameMode(GameMode.ADVENTURE);
					}
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
				for (Entity e : Multiworld.getGame().getEntities()) {
					if (e instanceof Item || e instanceof Slime) {
						e.remove();
					}
				}
				alivePlayers.clear();
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
		});
		util.execute();
	}

	static int	gameWorldDone	= 0;

	public static void resetGameWorld(final Game g, final GameMap map) {
		try {
			final String mapname = g.name() + "-" + map;
			final String fileName = "Maps" + File.separator + mapname;

			if (Multiworld.getGame() != null) {
				try {
					System.out.println("Deleting game world file...");
					Multiworld.deleteWorld("Game");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					System.out.println("Copying world files...");

					Multiworld.copyWorld(fileName, "Game");
					System.out.println("Copied world " + mapname + " (" + fileName + ") to Game");
				}
			}, 30l);

			Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					System.out.println("Saving all worlds...");

					for (World w : Bukkit.getWorlds()) {
						w.save();
					}

					System.out.println("Loading arena world...");

					World w = new WorldCreator("Game").type(WorldType.FLAT).createWorld();

					System.out.println("Setting gamerules...");

					String[] gmrls = { "doDaylightCycle:false", "doMobSpawning:false", "doMobLoot:false",
							"keepInventory:true", "doTileDrops:false" };
					for (String s : gmrls) {
						String[] spl = s.split(":");
						w.setGameRuleValue(spl[0], spl[1]);
					}

					System.out.println("Butchering....");

					for (Entity e : w.getEntities()) {
						if (e instanceof LivingEntity && !(e instanceof Player)) {
							e.remove();
						}
					}

					System.out.println("Copying text file...");

					WorldFileUtils.set("Game", WorldFileUtils.get(fileName));

					System.out.println("Done resetting world! (name " + mapname + ") (fileName " + fileName + ")");

					Multiworld.getGame().setAutoSave(false);

					gameWorldDone = -1;
				}
			}, 80l);
		} catch (Exception e) {
			gameWorldDone++;
			resetGameWorld(g, map);
		}
	}

	public static void start() {
		if (gameWorldDone >= 0) {
			broadcast("Loading map...");
			if (gameWorldDone > 2) {
				gameWorldDone = 0;

				new WorldCreator("Game").environment(Environment.NORMAL).generateStructures(false).type(WorldType.FLAT)
						.createWorld();
				resetGameWorld(currentGame, currentMap);
			}
			Bukkit.getScheduler().runTaskLater(ins, new Runnable() {
				public void run() {
					start();
				}
			}, 20);
			return;
		}

		gameWorldDone = 0;
		resetScoreboard();

		countingDown = false;
		started = true;
		gameForced = false;
		canOpenShop = false;

		pointsOnWin = getOptedIn().size() * (currentGame.isTeamGame() ? 20 : 40);

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
			if (p.getGameMode() != GameMode.ADVENTURE) {
				p.setGameMode(GameMode.ADVENTURE);
			}
			clearInv(p);
			p.setFallDistance(0);
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10, 100));
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			sendAll(p, getBroadcastMessage("%s has started!", currentGame.name),
					getBroadcastMessage("You will earn %s points if you win", pointsOnWin));

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
						p.teleport(Game.getSpawn(gt));
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
			TeleportUtils.spread(Game.getFFASpawn(), Game.getInt("radius"), getOptedIn().toArray(new Player[0]));

			for (Player p : getOptedIn()) {
				if (!p.getLocation().getChunk().isLoaded()) {
					p.getLocation().getChunk().load();
				}
			}
		}

		if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("Sidebar") != null) {
			Bukkit.getScoreboardManager().getMainScoreboard().getObjective("Sidebar").unregister();
		}

		sidebar = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("Sidebar", "dummy");
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		Sidebar.setDisplayName(currentGame.name);

		if (FileElements.has("time", "Game")) {
			Multiworld.getGame().setTime(Game.getInt("time"));
		} else {
			Multiworld.getGame().setTime(isMapNight ? 18000 : 5000);
		}

		if (weatherStage > 0) {
			weatherTask = new BukkitRunnable() {
				public void run() {
					PacketPlayOutGameStateChange gsc = new PacketPlayOutGameStateChange(7, weatherStage);
					for (Player p : Bukkit.getOnlinePlayers()) {
						PacketUtils.sendPacket(p, gsc);
					}
				}
			}.runTaskTimer(ins, 0, 20);
		}

		SoundUtils.playSoundToAll(Sound.AMBIENCE_THUNDER, 1, 0.75f);

		int time = currentGame.time;
		int[] secs = { 45, 30, 15, 10, 5, 4, 3, 2, 1 };
		for (int i = time; i > 0; i--) {
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
				if (currentGame == Game.Storm_The_Castle) {
					Minigames.broadcast(ChatColor.GREEN, "Times up, the %s win!", "Knights");
					stop(Game.Storm_The_Castle.getTeam("Knights").team);
				} else {
					Minigames.broadcast(ChatColor.GREEN, "Times up! Ending %s!", "game");
					stop(null);
				}
			}
		}, time * 1200));
	}

	// @@@@@@@@@@@@@@@@@@@@@@@ [ OTHER ] @@@@@@@@@@@@@@@@@@@@@@@

	public static String getBroadcastMessage(String mes, Object... form) {
		return getBroadcastMessage(ChatColor.GOLD, mes, form);
	}

	public static String getBroadcastMessage(ChatColor color, String mes, Object... form) {
		String b = ChatColor.GRAY + mes;
		Object[] format = form;
		for (int i = 0; i < format.length; i++) {
			String s = format[i] + "";
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
		try {
			if (de != null && Minigames.currentGame != null) {
				Minigames.currentGame.mclass.onPlayerDeath(de);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setDead(Player p, boolean sendDeath) {
		if (alivePlayers.contains(p.getName())) {
			alivePlayers.remove(p.getName());

			if (sendDeath) {
				sendDeathToGame(p);
			}

			if (currentGame.isTeamGame()) {
				GameTeam gt = Game.getTeam(p);
				if (gt != null) {
					gt.team.removePlayer(p);
					if (!gt.deadPlayers.contains(p.getName())) {
						gt.deadPlayers.add(p.getName());
					}
				}
				// Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p).removePlayer(p);
			}

			clearInv(p);

			checkDone();

			if (!Minigames.optedOut.contains(p.getName())) {
				if (currentGame != null) {
					p.teleport(Game.getLocation("lobby"));
					spectate(p);
				} else {
					p.teleport(new Location(Multiworld.getLobby(), 0, 102, 0));
				}
			} else {
				p.teleport(new Location(Multiworld.getLobby(), 0, 102, 0));
			}
			String name = p.getName();
			name = name.substring(0, name.length() > 14 ? 13 : name.length());
			p.setPlayerListName(ChatColor.GRAY + name);
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				MCShockwave.updateTab(p2);
			}
		}
	}

	public static void spectate(Player p) {
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
		// if (currentGame == Game.TRON || currentGame == Game.Airships) {
		// p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
		// 1000000, 0));
		// }
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

	public static void updateMap(final String map) {
		new BukkitRunnable() {
			public void run() {
				try {
					URL url = new URL("http://mcsw.us/hostserver/Maps/" + map + ".zip");
					File maps = new File("Maps/" + map);
					File del = Unpackager.unpackArchive(url, maps);
					Bukkit.broadcastMessage("§aUpdated map " + map);
					del.delete();
				} catch (Exception ex) {
					ex.printStackTrace();
					Bukkit.broadcastMessage("§cError updating map: " + ex.getLocalizedMessage());
				}
			}
		}.runTaskAsynchronously(ins);
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
			if (!ShopUtils.hasPermaItem(p, u)) {
				PointsUtils.addPoints(p, u.cost, "refund of " + u.name);
			}
		}
	}

	public static boolean						defaultSidebar	= false;
	public static HashMap<GameTeam, GameScore>	sidebar_left	= new HashMap<>();

	public static GameMap						nextMap			= null;

	public static void showDefaultSidebar() {
		defaultSidebar = true;
		updateDefaultSidebar();
	}

	public static void updateDefaultSidebar() {
		if (currentGame.isTeamGame()) {
			for (GameTeam gt : currentGame.teams) {
				sidebar_left.put(gt, Sidebar.getNewScore(gt.color + gt.name, gt.getPlayers().size()));
			}
		} else {
			sidebar_left.put(null, Sidebar.getNewScore("§oPlayers Left", alivePlayers.size()));
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

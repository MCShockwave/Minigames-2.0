package net.mcshockwave.Minigames;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.Minigames.Games.Airships;
import net.mcshockwave.Minigames.Games.Airstrike;
import net.mcshockwave.Minigames.Games.Boarding;
import net.mcshockwave.Minigames.Games.Brawl;
import net.mcshockwave.Minigames.Games.BuildAndFight;
import net.mcshockwave.Minigames.Games.Core;
import net.mcshockwave.Minigames.Games.Dodgeball;
import net.mcshockwave.Minigames.Games.Dogtag;
import net.mcshockwave.Minigames.Games.FourCorners;
import net.mcshockwave.Minigames.Games.Ghostbusters;
import net.mcshockwave.Minigames.Games.GiantStomp;
import net.mcshockwave.Minigames.Games.Gladiators;
import net.mcshockwave.Minigames.Games.HotPotato;
import net.mcshockwave.Minigames.Games.Infection;
import net.mcshockwave.Minigames.Games.LaserTag;
import net.mcshockwave.Minigames.Games.Loot;
import net.mcshockwave.Minigames.Games.Minotaur;
import net.mcshockwave.Minigames.Games.Siege;
import net.mcshockwave.Minigames.Games.Spleef;
import net.mcshockwave.Minigames.Games.StormTheCastle;
import net.mcshockwave.Minigames.Games.TRON;
import net.mcshockwave.Minigames.Games.Target;
import net.mcshockwave.Minigames.Games.Tiers;
import net.mcshockwave.Minigames.Games.VillageBattle;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.worlds.FileElements;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum Game {

	Core(
		new Core(),
		Material.WOOL,
		0,
		10,
		true,
		true,
		new GameTeam[] { new GameTeam("Red", ChatColor.RED), new GameTeam("Blue", ChatColor.BLUE),
				new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Airships(
		new Airships(),
		Material.BOW,
		0,
		8,
		false,
		true),
	Brawl(
		new Brawl(),
		Material.STICK,
		0,
		8,
		false,
		true),
	Build_and_Fight(
		new BuildAndFight(),
		Material.WOOL,
		4,
		8,
		false,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Dodgeball(
		new Dodgeball(),
		Material.SNOW_BALL,
		0,
		6,
		false,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Four_Corners(
		new FourCorners(),
		Material.STAINED_CLAY,
		14,
		8,
		false,
		false),
	Spleef(
		new Spleef(),
		Material.SNOW_BLOCK,
		0,
		8,
		false,
		false),
	TRON(
		new TRON(),
		Material.WOOL,
		9,
		10,
		false,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Dogtag(
		new Dogtag(),
		Material.SKULL_ITEM,
		3,
		10,
		true,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Boarding(
		new Boarding(),
		Material.IRON_AXE,
		0,
		10,
		true,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Village_Battle(
		new VillageBattle(),
		Material.MONSTER_EGG,
		120,
		10,
		true,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Gladiators(
		new Gladiators(),
		Material.IRON_SWORD,
		0,
		10,
		false,
		true,
		new GameTeam[] { new GameTeam("Red", ChatColor.RED), new GameTeam("Blue", ChatColor.BLUE),
				new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Hot_Potato(
		new HotPotato(),
		Material.BAKED_POTATO,
		0,
		8,
		false,
		true),
	Infection(
		new Infection(),
		Material.SKULL_ITEM,
		2,
		8,
		true,
		true,
		new GameTeam[] { new GameTeam("Zombies", ChatColor.GREEN), new GameTeam("Humans", ChatColor.YELLOW) }),
	Siege(
		new Siege(),
		Material.GOLD_HELMET,
		0,
		8,
		true,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Loot(
		new Loot(),
		Material.DIAMOND_CHESTPLATE,
		0,
		10,
		false,
		true),
	Minotaur(
		new Minotaur(),
		Material.DIAMOND_AXE,
		0,
		8,
		false,
		true,
		new GameTeam[] { new GameTeam("Humans", ChatColor.WHITE), new GameTeam("The Minotaur", ChatColor.RED) }),
	Laser_Tag(
		new LaserTag(),
		Material.DIAMOND_HOE,
		0,
		12,
		true,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Ghostbusters(
		new Ghostbusters(),
		Material.SKULL_ITEM,
		0,
		10,
		false,
		true,
		new GameTeam[] { new GameTeam("Ghosts", ChatColor.DARK_GRAY), new GameTeam("Humans", ChatColor.WHITE) }),
	Tiers(
		new Tiers(),
		Material.DIAMOND_SWORD,
		0,
		8,
		true,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW),
				new GameTeam("Red", ChatColor.RED), new GameTeam("Blue", ChatColor.BLUE) }),
	Storm_The_Castle(
		new StormTheCastle(),
		Material.BEACON,
		0,
		12,
		true,
		true,
		new GameTeam[] { new GameTeam("Knights", ChatColor.AQUA), new GameTeam("Barbarians", ChatColor.RED) }),
	Giant_Stomp(
		new GiantStomp(),
		Material.STONE_SWORD,
		0,
		6,
		false,
		true),
	Target(
		new Target(),
		Material.REDSTONE_BLOCK,
		0,
		8,
		true,
		true,
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN), new GameTeam("Yellow", ChatColor.YELLOW) }),
	Airstrike(
		new Airstrike(),
		Material.SKULL_ITEM,
		4,
		8,
		false,
		true,
		new GameTeam[] { new GameTeam("Shooters", ChatColor.RED), new GameTeam("Runners", ChatColor.AQUA) });

	public static Game[]	disabled	= { Game.Ghostbusters };

	public String			name;
	public IMinigame		mclass;
	public GameTeam[]		teams		= null;
	// public Location spawn = null, lobby = null;
	public int				time;
	public boolean			canRespawn, allowPVP;
	public ItemStack		icon		= null;

	public List<GameMap>	maplist		= new ArrayList<>();

	public boolean isEnabled() {
		return !Arrays.asList(disabled).contains(this) && maplist.size() > 0;
	}

	Game(IMinigame mclass, Material icon, int iconData, int time, boolean canRespawn, boolean allowPVP, GameTeam[] teams) {
		this.teams = teams;
		init(mclass, icon, iconData, time, canRespawn, allowPVP);
	}

	Game(IMinigame mclass, Material icon, int iconData, int time, boolean canRespawn, boolean allowPVP) {
		// this.spawn = new Location(Multiworld.getGame(), spawn.getX() + 0.5,
		// spawn.getY() + 0.5, spawn.getZ() + 0.5);
		// this.radius = radius;
		init(mclass, icon, iconData, time, canRespawn, allowPVP);
	}

	public void updateMaps() {
		if (this.maplist != null) {
			this.maplist.clear();
		} else
			this.maplist = new ArrayList<>();
		List<String> maps = SQLTable.MinigameMaps.getAll("Game", name(), "Name");
		for (String s : maps) {
			GameMap gm = new GameMap(s, this);
			this.maplist.add(gm);
		}
	}

	public void init(IMinigame mclass, Material icon, int iconData, int time, boolean canRespawn, boolean allowPVP) {
		name = name().replace('_', ' ');
		this.mclass = mclass;
		this.canRespawn = canRespawn;
		// this.lobby = new Location(Multiworld.getGame(), lobby.getX() + 0.5,
		// lobby.getY() + 0.8, lobby.getZ() + 0.5);
		this.icon = new ItemStack(icon, 1, (byte) iconData);
		this.allowPVP = allowPVP;
		this.time = time;
		updateMaps();
	}

	public boolean isTeamGame() {
		return teams != null;
	}

	public static class GameTeam {
		public String				name;
		public ChatColor			color;
		// public Location spawn;
		public Team					team		= null;
		public ArrayList<String>	deadPlayers	= new ArrayList<>();

		public GameTeam(String name, ChatColor color) {
			this.name = name;
			this.color = color;
			// this.spawn = new Location(Multiworld.getGame(), spawn.getX() +
			// 0.5, spawn.getY() + 0.2, spawn.getZ() + 0.5);
		}

		public List<Player> getPlayers() {
			ArrayList<Player> ret = new ArrayList<>();

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (Game.getTeam(p) == this) {
					ret.add(p);
				}
			}

			return ret;
		}
	}

	public static GameMap getMapForGame(Game g, String name) {
		for (GameMap gm : g.maplist) {
			if (gm.name.equalsIgnoreCase(name)) {
				return gm;
			}
		}
		return null;
	}

	public static class GameMap {
		public String	name;
		public Game		g;

		public boolean	canBeNight	= false;
		public boolean	canRain		= false;

		public GameMap(String name, Game g) {
			this.name = name;
			this.g = g;
			updateOptions();
		}

		public void updateOptions() {
			String where = "Game='" + g.name() + "' AND Name='" + name + "'";
			int ni = SQLTable.MinigameMaps.getIntWhere(where, "Night");
			int ra = SQLTable.MinigameMaps.getIntWhere(where, "Weather");

			canBeNight = ni == 1;
			canRain = ra == 1;
		}

		public String getWorldName() {
			return g.name() + "-" + name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static GameTeam getTeam(Team team) {
		if (!Minigames.currentGame.isTeamGame() || Minigames.currentGame.teams == null || team == null) {
			return null;
		}
		for (GameTeam t : Minigames.currentGame.teams) {
			if (t.name.equalsIgnoreCase(team.getName())) {
				return t;
			}
		}
		return null;
	}

	public static GameTeam getTeam(Player p) {
		Team t = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p);
		if (t != null) {
			return getTeam(t);
		}
		return null;
	}

	public GameTeam getTeam(String s) {
		for (GameTeam gt : teams) {
			if (gt.name.equalsIgnoreCase(s)) {
				return gt;
			}
		}
		return null;
	}

	public static byte getWoolColor(GameTeam gt) {
		ChatColor c = gt.color;
		if (c == ChatColor.GREEN) {
			return 13;
		} else if (c == ChatColor.YELLOW) {
			return 4;
		} else if (c == ChatColor.RED) {
			return 14;
		} else if (c == ChatColor.BLUE) {
			return 11;
		}
		return 0;
	}

	private static Random	rand	= new Random();

	public static Player getRandomPlayer() {
		return Bukkit.getPlayer(Minigames.alivePlayers.get(rand.nextInt(Minigames.alivePlayers.size())));
	}

	public static boolean hasElement(String element) {
		return FileElements.has(element, Multiworld.getGame().getName());
	}

	public static Location getLocation(String element) {
		return FileElements.getLoc(element, Multiworld.getGame());
	}

	public static Block getBlock(String element) {
		return getLocation(element).getBlock();
	}

	public static String getString(String element) {
		return FileElements.get(element, Multiworld.getGame().getName());
	}

	public static int getInt(String element) {
		return Integer.parseInt(getString(element));
	}

	public static double getDouble(String element) {
		return Double.parseDouble(getString(element));
	}

	public static Location getSpawn(GameTeam gt) {
		return getLocation(gt.name.replace(' ', '_') + "-spawnpoint");
	}

	public static Location getFFASpawn() {
		return getLocation("FFA-spawnpoint");
	}

}

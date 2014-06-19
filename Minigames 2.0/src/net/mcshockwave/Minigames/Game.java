package net.mcshockwave.Minigames;

import net.mcshockwave.Minigames.Games.*;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
		new Vector(0, 102, 0),
		new GameTeam[] { new GameTeam("Red", ChatColor.RED, new Vector(45, 100, -45)),
				new GameTeam("Blue", ChatColor.BLUE, new Vector(-45, 100, -45)),
				new GameTeam("Green", ChatColor.GREEN, new Vector(-45, 100, 45)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(45, 100, 45)) }),
	Airships(
		new Airships(),
		Material.BOW,
		0,
		8,
		false,
		true,
		new Vector(0, 90, -400),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(0, 110, -393)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(0, 110, -407)) }),
	Brawl(
		new Brawl(),
		Material.STICK,
		0,
		8,
		false,
		true,
		new Vector(315, 135, -780),
		new Vector(315, 135, -780),
		1),
	Build_and_Fight(
		new BuildAndFight(),
		Material.WOOL,
		4,
		8,
		false,
		true,
		new Vector(-631, 100, 1),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(-601, 102, -30)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(-601, 102, 30)) }),
	Dodgeball(
		new Dodgeball(),
		Material.SNOW_BALL,
		0,
		6,
		false,
		true,
		new Vector(28, 106, 0),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(0, 102, 18)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(0, 102, -18)) }),
	Four_Corners(
		new FourCorners(),
		Material.STAINED_CLAY,
		14,
		8,
		false,
		false,
		new Vector(203, 115, -1),
		new Vector(193, 106, -11),
		2),
	Spleef(
		new Spleef(),
		Material.SNOW_BLOCK,
		0,
		8,
		false,
		false,
		new Vector(800, 110, 0),
		new Vector(800, 105, 0),
		20),
	TRON(
		new TRON(),
		Material.WOOL,
		9,
		10,
		false,
		false,
		new Vector(401, 107, 0),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(343, 103, 0)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(459, 103, 0)) }),
	Dogtag(
		new Dogtag(),
		Material.SKULL_ITEM,
		3,
		10,
		true,
		true,
		new Vector(-16, 108, 605),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(-41, 124, 557)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(16, 105, 620)) }),
	Boarding(
		new Boarding(),
		Material.IRON_AXE,
		0,
		10,
		true,
		true,
		new Vector(-770, 151, 11),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(-784, 107, -15)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(-751, 107, 41)) }),
	Village_Battle(
		new VillageBattle(),
		Material.MONSTER_EGG,
		120,
		10,
		true,
		true,
		new Vector(680, 113, -51),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(595, 86, 18)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(689, 86, -198)) }),
	Gladiators(
		new Gladiators(),
		Material.IRON_SWORD,
		0,
		10,
		false,
		true,
		new Vector(0, 124, 0),
		new GameTeam[] { new GameTeam("Red", ChatColor.RED, new Vector(0, 112, -34)),
				new GameTeam("Blue", ChatColor.BLUE, new Vector(34, 112, 0)),
				new GameTeam("Green", ChatColor.GREEN, new Vector(0, 112, 34)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(-34, 112, 0)) }),
	Hot_Potato(
		new HotPotato(),
		Material.BAKED_POTATO,
		0,
		8,
		false,
		true,
		new Vector(0, 103, 800),
		new Vector(0, 90, 800),
		10),
	Infection(
		new Infection(),
		Material.SKULL_ITEM,
		2,
		8,
		true,
		true,
		new Vector(-25, 128, -597),
		new GameTeam[] { new GameTeam("Zombies", ChatColor.GREEN, new Vector(4, 151, -600)),
				new GameTeam("Humans", ChatColor.YELLOW, new Vector(2, 105, -595)) }),
	Siege(
		new Siege(),
		Material.GOLD_HELMET,
		0,
		8,
		true,
		true,
		new Vector(347, 143, 189),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(282, 116, 156)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(446, 116, 194)) }),
	Loot(
		new Loot(),
		Material.DIAMOND_CHESTPLATE,
		0,
		10,
		false,
		true,
		new Vector(-404, 129, -12),
		new Vector(-444, 109, -12),
		6),
	Minotaur(
		new Minotaur(),
		Material.DIAMOND_AXE,
		0,
		8,
		false,
		true,
		new Vector(1000, 111, 0),
		new GameTeam[] { new GameTeam("Humans", ChatColor.WHITE, new Vector(1000, 111, 0)),
				new GameTeam("The Minotaur", ChatColor.RED, new Vector(1000, 111, 0)) }),
	// Lockdown(
	// new Lockdown(),
	// Material.DIAMOND_BLOCK,
	// 0,
	// 10,
	// true,
	// true,
	// new Location(1500, 121, 0),
	// new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new
	// Location(1502, 95, -103)),
	// new GameTeam("Yellow", ChatColor.YELLOW, new Location(1498, 103, 100))
	// }),
	Laser_Tag(
		new LaserTag(),
		Material.DIAMOND_HOE,
		0,
		10,
		true,
		true,
		new Vector(-1500, 100, 0),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(-1531, 101, 0)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(-1468, 101, 0)) }),
	Ghostbusters(
		new Ghostbusters(),
		Material.SKULL_ITEM,
		0,
		10,
		false,
		true,
		new Vector(-7, 125, -991),
		new GameTeam[] { new GameTeam("Ghosts", ChatColor.DARK_GRAY, new Vector(14, 107, -969)),
				new GameTeam("Humans", ChatColor.WHITE, new Vector(-36, 108, -989)) }),
	Tiers(
		new Tiers(),
		Material.DIAMOND_SWORD,
		0,
		8,
		true,
		true,
		new Vector(399, 138, -246),
		new GameTeam[] { new GameTeam("Green", ChatColor.GREEN, new Vector(341, 112, -246)),
				new GameTeam("Yellow", ChatColor.YELLOW, new Vector(399, 112, -188)),
				new GameTeam("Red", ChatColor.RED, new Vector(457, 112, -246)),
				new GameTeam("Blue", ChatColor.BLUE, new Vector(399, 112, -304)) }),
	Storm_The_Castle(
		new StormTheCastle(),
		Material.BEACON,
		0,
		8,
		true,
		true,
		new Vector(2440, 98, -3),
		new GameTeam[] { new GameTeam("Knights", ChatColor.AQUA, new Vector(2570, 104, -3)),
				new GameTeam("Barbarians", ChatColor.RED, new Vector(2418, 82, -3)) });
	;

	public String			name;
	public IMinigame		mclass;
	public GameTeam[]		teams	= null;
	public Location			spawn	= null, lobby = null;
	public int				radius	= -1, time;
	public boolean			canRespawn, allowPVP;
	public ItemStack		icon	= null;

	public static Game[]	enabled	= { Game.Core, Game.Dodgeball };

	Game(IMinigame mclass, Material icon, int iconData, int time, boolean canRespawn, boolean allowPVP, Vector lobby,
			GameTeam[] teams) {
		this.teams = teams;
		init(mclass, icon, iconData, time, canRespawn, allowPVP, lobby);
	}

	Game(IMinigame mclass, Material icon, int iconData, int time, boolean canRespawn, boolean allowPVP, Vector lobby,
			Vector spawn, int radius) {
		this.spawn = new Location(Multiworld.getGame(), spawn.getX() + 0.5, spawn.getY() + 0.5, spawn.getZ() + 0.5);
		this.radius = radius;
		init(mclass, icon, iconData, time, canRespawn, allowPVP, lobby);
	}

	public void init(IMinigame mclass, Material icon, int iconData, int time, boolean canRespawn, boolean allowPVP,
			Vector lobby) {
		name = name().replace('_', ' ');
		this.mclass = mclass;
		this.canRespawn = canRespawn;
		this.lobby = new Location(Multiworld.getGame(), lobby.getX() + 0.5, lobby.getY() + 0.8, lobby.getZ() + 0.5);
		this.icon = new ItemStack(icon, 1, (byte) iconData);
		this.allowPVP = allowPVP;
		this.time = time;
	}

	public boolean isTeamGame() {
		return teams != null;
	}

	public static class GameTeam {
		public String		name;
		public ChatColor	color;
		public Location		spawn;
		public Team			team	= null;

		public GameTeam(String name, ChatColor color, Vector spawn) {
			this.name = name;
			this.color = color;
			this.spawn = new Location(Multiworld.getGame(), spawn.getX() + 0.5, spawn.getY() + 0.2, spawn.getZ() + 0.5);
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

	public static Game getInstance(IMinigame im) {
		for (Game g : values()) {
			if (g.mclass == im) {
				return g;
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

}

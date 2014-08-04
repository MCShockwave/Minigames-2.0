package net.mcshockwave.Minigames.Handlers;

import net.mcshockwave.Minigames.Minigames;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.List;

public class Sidebar {

	public static int	MAX_VALUES	= 15;

	Sidebar() {
	}

	static ArrayList<GameScore>	scores	= new ArrayList<>();

	public static GameScore getNewScore(String name) {
		return getNewScore(name, 0);
	}

	public static GameScore getNewScore(String name, int val) {
		GameScore sc = new GameScore(name, Minigames.sidebar); // default
																// objective
																// here
		scores.add(sc);
		if (val == 0) {
			sc.setVal(1);
		}
		sc.setVal(val);
		return sc;
	}

	public static List<GameScore> getAllScores() {
		return scores;
	}

	public static void clearScores() {
		scores.clear();
	}

	private static void updateVisibility() {
		if (scores.size() > MAX_VALUES) {
			List<GameScore> top = getTopScores(MAX_VALUES);
			for (GameScore gs : getAllScores()) {
				boolean dis = top.contains(gs);
				if (gs.isDisplayed() != dis) {
					gs.setDisplayed(dis, false);
				}
			}
		} else {
			for (GameScore gs : getAllScores()) {
				if (!gs.isDisplayed()) {
					gs.setDisplayed(true, false);
				}
			}
		}
	}

	public static List<GameScore> getTopScores(int count) {
		ArrayList<GameScore> ret = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			GameScore top = getTop(ret);
			ret.add(top);
		}
		return ret;
	}

	private static GameScore getTop(List<GameScore> ignore) {
		GameScore ret = null;
		int val = Integer.MIN_VALUE;
		for (GameScore gs : getAllScores()) {
			if (!ignore.contains(gs)) {
				if (gs.getVal() > val) {
					ret = gs;
					val = gs.getVal();
				}
			}
		}
		return ret;
	}

	public static void setDisplayName(String name) {
		Minigames.sidebar.setDisplayName("§7§l" + name);
	}

	public static class GameScore {
		String		name;
		int			val;
		Objective	obj;
		boolean		displayed;

		GameScore(String name, Objective use) {
			if (name.length() > 16) {
				name = name.substring(0, 16);
			}
			this.name = name;
			this.obj = use;
			this.displayed = true;
			getScore();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
			if (isDisplayed()) {
				int val = getVal();
				obj.getScoreboard().resetScores(getScore().getEntry());
				getScore().setScore(val);
			}
		}

		public int getVal() {
			return val;
		}

		public void setVal(int val) {
			if (isDisplayed()) {
				getScore().setScore(val);
			}
			this.val = val;
		}

		public boolean isDisplayed() {
			return displayed;
		}

		public void setDisplayed(boolean displayed) {
			setDisplayed(displayed, true);
		}

		private void setDisplayed(boolean displayed, boolean update) {
			if (displayed != this.displayed) {
				this.displayed = displayed;

				if (displayed) {
					getScore().setScore(getVal());
				} else {
					obj.getScoreboard().resetScores(getScore().getEntry());
				}

				if (update) {
					updateVisibility();
				}
			}
		}

		@SuppressWarnings("deprecation")
		private Score getScore() {
			return obj.getScore(Bukkit.getOfflinePlayer(getName()));
		}

		public void remove() {
			scores.remove(this);
			setDisplayed(false);
		}
	}

}

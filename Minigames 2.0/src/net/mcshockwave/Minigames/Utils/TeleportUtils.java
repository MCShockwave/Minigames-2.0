package net.mcshockwave.Minigames.Utils;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportUtils {

	private static Random	rand	= new Random();

	public static void spread(Location mid, int radius, Player... toSpread) {
		if (radius <= 0) {
			for (Player p : toSpread) {
				p.teleport(mid);
			}
			return;
		}
		for (Player p : toSpread) {
			if (p == null)
				continue;
			Location tp = mid.clone().add(rand.nextInt(radius * 2) - radius, 0, rand.nextInt(radius * 2) - radius);

			p.teleport(tp);
		}
	}

}

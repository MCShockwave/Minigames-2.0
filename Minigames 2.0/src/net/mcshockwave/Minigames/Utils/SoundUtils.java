package net.mcshockwave.Minigames.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtils {
	
	public static void playSoundToAll(Sound s, float volume, float pitch) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.playSound(p.getEyeLocation(), s, volume, pitch);
		}
	}

}

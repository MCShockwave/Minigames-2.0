package net.mcshockwave.Minigames.Utils;

import net.mcshockwave.Minigames.Minigames;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockUtils {

	public static void setBlocks(final Location s, Location e, final Material m, final DyeColor color) {
		int x = s.getBlockX();
		int z = s.getBlockZ();
		int x2 = e.getBlockX();
		int z2 = e.getBlockZ();
		int i = 0;
		for (int x3 = Math.min(x, x2); x3 <= Math.max(x, x2); x3++) {
			for (int z3 = Math.min(z, z2); z3 <= Math.max(z, z2); z3++) {
				final int x4 = x3;
				final int z4 = z3;
				Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
					public void run() {
						setBlock(s.getWorld().getBlockAt(x4, s.getBlockY(), z4), m, color);
					}
				}, i);
				i++;
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void setBlocks(final Location s, Location e, final Material m, final int color) {
		setBlocks(s, e, m, DyeColor.getByData((byte) color));
	}

	@SuppressWarnings("deprecation")
	public static void setBlock(Block b, Material m, DyeColor color) {
		if (m.getId() == 0) {
//			PacketUtils.sendPacketGlobally(b.getLocation(), 50,
//					PacketUtils.generateBlockParticles(b.getType(), b.getData(), b.getLocation()));
			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType().getId());
		} else {
//			PacketUtils.sendPacketGlobally(b.getLocation(), 50,
//					PacketUtils.generateBlockParticles(m, color.getData(), b.getLocation()));
			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, m.getId());
		}
		b.setType(m);
		b.setData(color.getData());
	}

}

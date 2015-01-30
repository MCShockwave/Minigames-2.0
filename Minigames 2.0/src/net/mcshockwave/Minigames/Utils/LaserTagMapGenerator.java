package net.mcshockwave.Minigames.Utils;

import net.mcshockwave.MCS.Utils.LocUtils;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Wool;

import java.util.Random;

public class LaserTagMapGenerator {

	public static final int	lightSpacing	= 5;

	public static void generate(Location center, int radius) {
		if (radius < 15)
			radius = 15;

		generateBox(center, radius);
		generateBases(center, radius);
		placeLogo(center.clone());
		generateObstacles(center, radius);
	}

	public static void generateBox(Location center, int radius) {
		World cw = center.getWorld();
		int cx = center.getBlockX();
		int cy = center.getBlockY();
		int cz = center.getBlockZ();
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				Block at = new Location(cw, cx + x, cy, cz + z).getBlock();
				placeWool(at, DyeColor.GRAY);
				if (Math.abs(x) == radius || Math.abs(z) == radius) {
					placeColumn(at, 10, true, center.clone());
				} else {
					if (x % lightSpacing == 0 && z % lightSpacing == 0) {
						at.setType(Material.GLOWSTONE);
					}
					if (x == 0) {
						placeWool(at, DyeColor.BLACK);
					}
					for (int i = 1; i < 20; i++) {
						if (at.getRelative(0, i, 0).getType() != Material.AIR) {
							at.getRelative(0, i, 0).setType(Material.AIR);
						}
					}
				}
			}
		}
	}

	public static void generateObstacles(Location center, int radius) {
		for (int i = 0; i < (radius * radius) / 20; i++) {
			Location r = LocUtils.addRand(center.clone(), radius, 0, radius);
			if (r.getBlock().getType() == Material.WOOL) {
				generateRandomObstacle(r, center.clone());
			} else
				i--;
		}
	}

	static Random					rand		= new Random();
	public static final int[][][]	patterns	= { { { 1, 0, 1 }, { 0, 1, 0 }, { 1, 0, 1 } }, // X
			{ { 0, 1, 0 }, { 0, 1, 0 }, { 0, 1, 0 } }, // wall
			{ { 0, 0, 0 }, { 1, 1, 1 }, { 0, 0, 0 } }, // wall
			{ { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } }, // +
			{ { 1, 1, 0, 0, 0 }, { 0, 0, 1, 0, 0 }, { 0, 0, 0, 1, 1 } }, // large
			{ { 0, 0, 0, 1, 1 }, { 0, 0, 1, 0, 0 }, { 1, 1, 0, 0, 0 } } // wall
												};

	public static void generateRandomObstacle(Location place, Location center) {
		Location l1 = place.clone();
		Location l2 = l1.clone();
		l2.setX(l1.getBlockX() - ((l1.getBlockX() - center.getBlockX()) * 2));
		int[][] pat = patterns[rand.nextInt(patterns.length)];
		for (Location l : new Location[] { l1, l2 }) {
			for (int x = 0; x < pat.length; x++) {
				int[] xs = pat[x];
				for (int z = 0; z < xs.length; z++) {
					int at = xs[z];
					Location ad = l.clone().add(x, 0, z);
					if (at == 1 && ad.getBlockX() != center.getBlockX()
							&& ad.getBlock().getType() != Material.STAINED_CLAY
							&& ad.getBlock().getType() != Material.AIR
							&& ad.getBlock().getType() != Material.IRON_BLOCK) {
						placeColumn(ad.getBlock(), 5, false, center.clone());
					}
				}
			}
		}
	}

	public static void generateBases(Location center, int radius) {
		for (int xoff : new int[] { -radius + lightSpacing, radius - lightSpacing }) {
			Location gen = center.clone().add(xoff, 0, 0);
			int baseRad = lightSpacing * 2;
			for (int x = -lightSpacing; x <= baseRad - lightSpacing; x++) {
				for (int z = -baseRad; z <= baseRad; z++) {
					boolean isDoorway = Math.abs(x) < 3;
					if ((Math.abs(x) == baseRad - lightSpacing || Math.abs(z) == baseRad) && !isDoorway) {
						boolean black = Math.abs(x) == 3;
						placeColumn(gen.clone().add(x, 0, z).getBlock(), 5, black, center.clone());
					}
					Block und = gen.clone().add(x, 0, z).getBlock();
					if (und.getType() == Material.WOOL) {
						placeBlock(und, Material.STAINED_CLAY, getWoolColor(und, center.clone()));
					}
				}
			}
		}
		for (int x : new int[] { radius - lightSpacing, -radius + lightSpacing }) {
			for (int z : new int[] { radius - lightSpacing, -radius + lightSpacing }) {
				Location l = center.clone().add(x, 1, z);
				placeBlock(l.getBlock(), Material.BEACON, null);
				for (int bx = -1; bx <= 1; bx++) {
					for (int bz = -1; bz <= 1; bz++) {
						placeBlock(l.clone().add(bx, -1, bz).getBlock(), Material.IRON_BLOCK, null);
					}
				}
			}
		}
	}

	public static void placeColumn(Block b, int he, boolean black, Location center) {
		Block set = b;
		for (int i = 0; i < he; i++) {
			set = set.getRelative(BlockFace.UP);
			placeWool(set, black ? DyeColor.BLACK : (i > 0 && i < he - 1) ? getWoolColor(b, center.clone())
					: DyeColor.BLACK);
		}
	}

	public static void placeWool(Block b, DyeColor col) {
		placeBlock(b, Material.WOOL, col);
	}

	@SuppressWarnings("deprecation")
	public static void placeBlock(Block b, Material m, DyeColor col) {
		if (b.getType() != m)
			b.setType(m);
		if (col != null) {
			if (m == Material.WOOL) {
				BlockState bs = b.getState();
				Wool w = (Wool) bs.getData();
				if (w.getColor() != col) {
					w.setColor(col);
					bs.setData(w);
					bs.update(true);
				}
			} else {
				b.setData(col.getData());
			}
		}
	}

	public static DyeColor getWoolColor(Block b, Location center) {
		int x = b.getX();
		int cx = center.getBlockX();
		return cx > x ? DyeColor.LIME : x > cx ? DyeColor.YELLOW : DyeColor.BLACK;
	}

	@SuppressWarnings("deprecation")
	public static void placeLogo(Location loc) {
		int[][] design = { { -1, 9, 9, 9, -1 }, { 9, 3, 5, 5, 9 }, { 9, 3, 9, 4, 9 }, { 9, 14, 14, 4, 9 },
				{ -1, 9, 9, 9, -1 } };
		loc.add(-2, 0, -2);
		for (int i = 0; i < design.length; i++) {
			int[] z = design[i];
			for (int x = 0; x < z.length; x++) {
				int at = z[x];
				if (at >= 0) {
					placeBlock(loc.clone().add(x, 0, i).getBlock(), Material.STAINED_CLAY,
							DyeColor.getByData((byte) at));
				}
			}
		}
	}
}

package net.mcshockwave.Minigames.worlds;

import net.mcshockwave.Minigames.Minigames;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Multiworld {

	public static WorldCreator[]	worlds	= new WorldCreator[2];

	public static String			mapName	= "";

	static {
		resetGameName();
	}

	public static void loadWorldGenerators() {
		worlds[0] = wc("Lobby", Environment.NORMAL, WorldType.FLAT);
		worlds[1] = wc("", Environment.NORMAL, WorldType.FLAT);
	}

	public static void resetGameName() {
		if (Minigames.currentMap != null) {
			mapName = Minigames.currentMap.getWorldName();
		}
		loadWorldGenerators();
	}

	private static WorldCreator wc(String name, Environment env, WorldType wt) {
		WorldCreator wc = new WorldCreator(name);
		wc.environment(env);
		wc.type(wt);

		if (wt == WorldType.NORMAL && env == Environment.NORMAL) {
			try {
				wc.generator("TerrainControl");
			} catch (Exception e) {
			}
		}
		if (wt == WorldType.FLAT) {
			wc.generator(new ChunkGenerator() {
				@Override
				public List<BlockPopulator> getDefaultPopulators(World world) {
					return new ArrayList<BlockPopulator>();
				}

				@Override
				public byte[][] generateBlockSections(World world, Random random, int chunkx, int chunkz,
						ChunkGenerator.BiomeGrid biomes) {
					return new byte[world.getMaxHeight() / 16][];
				}
			});
		}

		return wc;
	}

	public static void loadAll() {
		for (WorldCreator wc : worlds) {
			wc.createWorld();
		}
	}

	public static World getLobby() {
		return Bukkit.getWorld("Lobby");
	}

	public static World getGame() {
		return Bukkit.getWorld(mapName);
	}

	public static void deleteWorld(final String w) {
		if (Bukkit.getWorld(w) != null) {
			World wld = Bukkit.getWorld(w);
			for (Entity e : wld.getEntities()) {
				if (e instanceof Player) {
					e.teleport(getLobby().getSpawnLocation());
				} else {
					e.remove();
				}
			}
			for (Chunk c : wld.getLoadedChunks()) {
				c.unload(false);
			}
			if (Bukkit.unloadWorld(w, false)) {
				System.out.println("Unloaded world");
			} else {
				System.err.println("UNLOADING WORLD FAILED");
			}
		}
		Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
			public void run() {
				if (delete(new File(w))) {
					System.out.println("Deleted world!");
				} else {
					System.err.println("DELETING WORLD FAILED");
				}
			}
		}, 20l);
	}

	public static void deleteWorld(World w) {
		deleteWorld(w.getName());
	}

	public static boolean delete(File file) {
		if (file.isDirectory())
			for (File subfile : file.listFiles())
				if (!delete(subfile))
					return false;
		if (!file.delete())
			return false;
		return true;
	}

	public static void copyWorld(String s, String t) {
		File source = new File(s);
		File target = new File(t);
		try {
			System.out.println("Trying to copy " + s + " to " + t);
			copyTo(source, target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyTo(File src, File dest) throws IOException {
		if (src.isDirectory()) {

			if (!dest.exists()) {
				dest.mkdir();
			}

			String files[] = src.list();

			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copyTo(srcFile, destFile);
			}

		} else {
			if (src.getName().equalsIgnoreCase("uid.dat")) {
				return;
			}

			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}

}

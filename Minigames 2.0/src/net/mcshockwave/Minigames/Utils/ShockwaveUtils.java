package net.mcshockwave.Minigames.Utils;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.Minigames.Minigames;

public class ShockwaveUtils {

	// Port from Minigames 1.0
	public static void makeShockwave(final Location l, int range) {
		final Random rand = new Random();
		final ArrayList<Block> bir = new ArrayList<Block>();
		for (int x = l.getBlockX() - 25; x < 50; x++) {
			for (int z = l.getBlockZ() - 25; z < 50; z++) {
				bir.add(l.getWorld().getBlockAt(x, l.getBlockY(), z));
			}
		}
		final ArrayList<Block> bor = new ArrayList<Block>();
		for (int i = 0; i < range; i++) {
			final int i2 = i;
			Bukkit.getScheduler().runTaskLater(Minigames.ins, new Runnable() {
				public void run() {
					for (Block b : bir) {
						if (b.getLocation().distanceSquared(l) <= i2 * i2 && !bor.contains(b)) {
							if (rand.nextInt(10) == 1) {
								PacketUtils.playParticleEffect(Particle.EXPLOSION_NORMAL, b.getLocation(), 0, 0.1f, 10);
								b.getWorld().playSound(b.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
							}
							bor.add(b);
							for (Entity e : l.getWorld().getEntities()) {
								if (e.getLocation().distanceSquared(b.getLocation()) <= 1
										&& e.getType() != EntityType.GIANT) {
									Location l2 = e.getLocation();
									if (e instanceof Player) {
										Player p = (Player) e;
										e.setVelocity(new Vector(l2.getX() - l.getX(), l2.getY() - l.getY(), l2.getZ()
												- l.getZ()).multiply(p.isBlocking() ? 0.2 : 1)
												.multiply(p.isSneaking() ? 0.2 : 1).add(new Vector(0, 1, 0)));
									} else {
										e.setVelocity(new Vector(l2.getX() - l.getX(), l2.getY() - l.getY(), l2.getZ()
												- l.getZ()).multiply(1.5).add(new Vector(0, 1, 0)));
									}
									if (e instanceof LivingEntity) {
										((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100,
												1));
									}
								}
							}
						}
					}
				}
			}, i);
		}
	}

}

package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ghostbusters implements IMinigame {

	public GameTeam getGhosts() {
		return Game.getTeam(Game.Ghostbusters, "Ghosts");
	}

	public GameTeam getHumans() {
		return Game.getTeam(Game.Ghostbusters, "Humans");
	}

	@Override
	public void onGameStart() {
		for (Player p : getGhosts().getPlayers()) {
			giveKit(p, true);
		}
		for (Player p : getHumans().getPlayers()) {
			giveKit(p, false);
		}
	}

	public void giveKit(Player p, boolean ghosts) {
		ItemStack[] items;
		if (ghosts) {
			items = new ItemStack[] { new ItemStack(Material.DIAMOND_SWORD),
					new ItemStack(Material.POTION, 2, (short) 16420), new ItemStack(Material.POTION, 4, (short) 16428),
					new ItemStack(Material.POTION, 1, (short) 8229), new ItemStack(Material.POTION, 1, (short) 8229),
					new ItemStack(Material.POTION, 1, (short) 8229) };
			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
		} else {
			items = new ItemStack[] { new ItemStack(Material.WOOD_SWORD), new ItemStack(Material.GOLDEN_APPLE, 2) };
			p.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
			p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
			p.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
			p.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
		}

		p.getInventory().addItem(items);
	}

	@Override
	public void onGameEnd() {
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		if (e.gt != null) {
			if (e.gt == getGhosts()) {
				Minigames.broadcastDeath(e.p, e.k, "%s was exterminated", "%s was ghostbusted by %s");
			} else {
				Minigames.broadcastDeath(e.p, e.k, "%s died of natural causes", "%s had their soul stolen by %s");
			}
		}
	}

}

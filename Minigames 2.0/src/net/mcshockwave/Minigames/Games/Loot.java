package net.mcshockwave.Minigames.Games;

import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Loot implements IMinigame {

	ItemStack[] helms = { new ItemStack(Material.LEATHER_HELMET),
			new ItemStack(Material.CHAINMAIL_HELMET),
			new ItemStack(Material.IRON_HELMET),
			new ItemStack(Material.DIAMOND_HELMET) };
	ItemStack[] chests = { new ItemStack(Material.LEATHER_CHESTPLATE),
			new ItemStack(Material.CHAINMAIL_CHESTPLATE),
			new ItemStack(Material.IRON_CHESTPLATE),
			new ItemStack(Material.DIAMOND_CHESTPLATE) };
	ItemStack[] legs = { new ItemStack(Material.LEATHER_LEGGINGS),
			new ItemStack(Material.CHAINMAIL_LEGGINGS),
			new ItemStack(Material.IRON_LEGGINGS),
			new ItemStack(Material.DIAMOND_LEGGINGS) };
	ItemStack[] boots = { new ItemStack(Material.LEATHER_BOOTS),
			new ItemStack(Material.CHAINMAIL_BOOTS),
			new ItemStack(Material.IRON_BOOTS),
			new ItemStack(Material.DIAMOND_BOOTS) };
	ItemStack[] swords = { new ItemStack(Material.WOOD_SWORD),
			new ItemStack(Material.STONE_SWORD),
			new ItemStack(Material.IRON_SWORD),
			new ItemStack(Material.DIAMOND_SWORD) };
	ItemStack[] potions = { new ItemStack(Material.POTION, 1, (short) 8229),
			new ItemStack(Material.POTION, 1, (short) 8226),
			new ItemStack(Material.POTION, 1, (short) 8225) };

	public long invinTime = 0;

	public void onGameStart() {
		for (Player p : Minigames.getOptedIn()) {
			Minigames.clearInv(p);
			setRandomGear(p);
		}

		int s = 10;
		Minigames.broadcast(ChatColor.GREEN,
				"You have %s seconds of invincibility!", s);
		invinTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()) + s;
	}

	public void onGameEnd() {
		for (Entity i : Minigames.w.getEntities()) {
			if (i.getType() == EntityType.DROPPED_ITEM) {
				i.remove();
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player p = event.getPlayer();
		ItemStack i = event.getItem().getItemStack();
		PlayerInventory pi = p.getInventory();
		ItemStack[] forl = null;
		ItemStack cu = null;
		String s = i.getType().name();
		if (s.contains("HELMET")) {
			forl = helms;
			cu = pi.getHelmet();
		} else if (s.contains("CHESTPLATE")) {
			forl = chests;
			cu = pi.getChestplate();
		} else if (s.contains("LEGGINGS")) {
			forl = legs;
			cu = pi.getLeggings();
		} else if (s.contains("BOOTS")) {
			forl = boots;
			cu = pi.getBoots();
		} else if (s.contains("SWORD")) {
			forl = swords;
			cu = pi.getItem(0);
		}
		if (forl == null)
			return;
		int cur = 0;
		int pic = 0;
		for (int x = 0; x < forl.length; x++) {
			if (forl[x].getType() == cu.getType()) {
				cur = x;
			}
			if (forl[x].getType() == i.getType()) {
				pic = x;
			}
		}
		if (pic > cur) {
			if (s.contains("HELMET")) {
				pi.setHelmet(i);
			} else if (s.contains("CHESTPLATE")) {
				pi.setChestplate(i);
			} else if (s.contains("LEGGINGS")) {
				pi.setLeggings(i);
			} else if (s.contains("BOOTS")) {
				pi.setBoots(i);
			} else if (s.contains("SWORD")) {
				pi.setItem(0, i);
			}
			event.getItem().remove();
		}
		event.setCancelled(true);
	}

	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was killed",
				"%s was killed by %s");

		dropItems(e.p, e.p.getEyeLocation());
	}

	public void setRandomGear(Player p) {
		Random rand = new Random();
		PlayerInventory pi = p.getInventory();

		pi.setHelmet(helms[rand.nextInt(helms.length)]);
		pi.setChestplate(chests[rand.nextInt(chests.length)]);
		pi.setLeggings(legs[rand.nextInt(legs.length)]);
		pi.setBoots(boots[rand.nextInt(boots.length)]);

		pi.addItem(swords[rand.nextInt(swords.length)]);
		for (int i = 0; i < 2; i++) {
			pi.addItem(potions[rand.nextInt(potions.length)]);
		}
	}

	public void dropItems(Player p, Location l) {
		PlayerInventory pi = p.getInventory();

		for (ItemStack it : pi.getArmorContents()) {
			if (it != null) {
				l.getWorld().dropItemNaturally(l, it);
			}
		}

		for (ItemStack it : pi.getContents()) {
			if (it != null) {
				l.getWorld().dropItemNaturally(l, it);
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity pe = event.getEntity();
		Entity de = event.getDamager();

		if (pe instanceof Player && de instanceof Player) {
			Player d = (Player) de;

			if (invinTime >= TimeUnit.NANOSECONDS.toSeconds(System.nanoTime())) {
				event.setCancelled(true);
				Minigames.send(
						d,
						"You still have %s seconds of invincibility!",
						invinTime
								- TimeUnit.NANOSECONDS.toSeconds(System
										.nanoTime()));
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		ItemStack it = p.getItemInHand();

		if (it.getType() == Material.POTION) {
			if (invinTime >= TimeUnit.NANOSECONDS.toSeconds(System.nanoTime())) {
				event.setCancelled(true);
				Minigames
						.send(p,
								"You cannot use %s during invincibility! (%s seconds left)",
								"potions",
								invinTime
										- TimeUnit.NANOSECONDS.toSeconds(System
												.nanoTime()));
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlot() == 0) {
			event.setCancelled(true);
		}
	}

}

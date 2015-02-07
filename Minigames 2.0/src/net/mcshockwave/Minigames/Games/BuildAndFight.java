package net.mcshockwave.Minigames.Games;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.BlockUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;

public class BuildAndFight implements IMinigame {

	boolean			building	= true;

	public boolean	indes		= false;

	@Override
	public void onGameStart() {
		indes = Game.hasElement("indestructable") && Game.getString("indestructable").equalsIgnoreCase("true");

		Minigames.showDefaultSidebar();

		BlockUtils.save(Game.getLocation("bridge-corner-1"), Game.getLocation("bridge-corner-2"), "baf-bridge", true,
				0, false);

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				building = true;
				Minigames.broadcast("You have %s to build a fort, then fight!", "1 minute");
			}
		}, 10);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				building = false;
				Minigames.broadcastAll(Minigames.getBroadcastMessage(ChatColor.RED, "Times up! You may now %s!",
						"fight"));
				for (String al : Minigames.alivePlayers) {
					if (Bukkit.getPlayer(al) != null) {
						Player p = Bukkit.getPlayer(al);
						giveKit(p);
					}
				}
				BlockUtils.load(Game.getLocation("bridge-corner-1"), "baf-bridge", 0.1, false);
			}
		}, 1200);
	}

	public void giveKit(Player p) {
		Minigames.clearInv(p);
		if (p.getGameMode() != GameMode.SURVIVAL) {
			p.setGameMode(GameMode.SURVIVAL);
		}
		if (building) {
			p.getInventory().addItem(
					new ItemStack(Minigames.hasItem(p, ShopItem.Builder) ? Material.STAINED_GLASS : Material.WOOL, 64,
							(short) Game.getWoolColor(Game.getTeam(p))));
			p.getInventory().addItem(new ItemStack(Material.SHEARS));
		} else {
			p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
			if (!Minigames.hasItem(p, ShopItem.Fighter)) {
				p.getInventory().addItem(
						ItemMetaUtils.addEnchantment(new ItemStack(Material.BOW), Enchantment.ARROW_INFINITE, 1));
				p.getInventory().setItem(17, new ItemStack(Material.ARROW, 1));
			}
			p.getInventory().addItem(new ItemStack(Material.SHEARS));
			p.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
		}
	}

	@Override
	public void onGameEnd() {
		building = true;
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was killed", "%s was killed by %s");

		if (e.k != null && Minigames.hasItem(e.k, ShopItem.Fighter)) {
			double hp = e.k.getHealth();
			hp += 10;
			if (hp > e.k.getMaxHealth()) {
				hp = e.k.getMaxHealth();
			}
			e.k.setHealth(hp);
		}
	}

	public boolean canBuild(Player p, Block b) {
		GameTeam gt = Game.getTeam(p);
		if (p.getGameMode() == GameMode.CREATIVE) {
			return true;
		}
		if (b.getLocation().getY() > 125) {
			return false;
		}
		if (Minigames.optedOut.contains(p.getName())) {
			return true;
		}
		if (b.getType() == Material.STAINED_CLAY) {
			return false;
		}
		if (b.getType() != Material.WOOL && b.getType() != Material.STAINED_GLASS && indes) {
			return false;
		}
		if (gt != null) {
			if (building
					&& (gt.color == ChatColor.YELLOW && b.getBiome() != Biome.PLAINS || gt.color == ChatColor.GREEN
							&& b.getBiome() != Biome.FOREST)) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		event.setCancelled(!canBuild(event.getPlayer(), event.getBlock()));
		if (!event.isCancelled()) {
			if (Minigames.hasItem(event.getPlayer(), ShopItem.Builder)) {
				ItemStack it = event.getPlayer().getItemInHand();
				it.setAmount(64);
				event.getPlayer().setItemInHand(it);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		event.setCancelled(!canBuild(event.getPlayer(), event.getBlock()));
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (event.getDamager() instanceof Arrow && Minigames.hasItem(p, ShopItem.Fighter)) {
				event.setDamage(0);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile e = event.getEntity();
		BlockIterator iterator = new BlockIterator(e.getWorld(), e.getLocation().toVector(), e.getVelocity()
				.normalize(), 0, 4);
		Block hit = null;

		List<Block> aff = new ArrayList<>();
		while (iterator.hasNext()) {
			hit = iterator.next();
			if (hit.getType() != Material.AIR)
				break;
		}
		aff.add(hit);
		if (e.getShooter() != null && e.getShooter() instanceof Player) {
			Player d = (Player) e.getShooter();

			if (Minigames.hasItem(d, ShopItem.Archer)) {
				for (BlockFace bf : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
						BlockFace.UP, BlockFace.DOWN }) {
					if (rand.nextInt(3) == 0)
						continue;
					aff.add(hit.getRelative(bf));
				}
			}
		}
		for (Block hb : aff) {
			if ((hb.getType() == Material.WOOL || !indes && hb.getType() != Material.STAINED_CLAY)
					&& hb.getType() != Material.STAINED_GLASS) {
				PacketUtils.sendPacketGlobally(hb.getLocation(), 50,
						PacketUtils.generateBlockParticles(hb.getType(), hb.getData(), hb.getLocation()));
				hb.getWorld().playSound(hb.getLocation(), Sound.DIG_WOOL, 1, 1);
				hb.setType(Material.AIR);
			}
			if (hb.getType() == Material.STAINED_GLASS) {
				PacketUtils.sendPacketGlobally(hb.getLocation(), 50,
						PacketUtils.generateBlockParticles(hb.getType(), hb.getData(), hb.getLocation()));
				hb.getWorld().playSound(hb.getLocation(), Sound.GLASS, 1, 1);
				hb.setType(Material.WOOL);
			}
		}
		e.remove();
	}

	@Override
	public Object determineWinner(Game g) {
		// TODO Auto-generated method stub
		return null;
	}

}

package net.mcshockwave.Minigames.Games;

import java.util.ArrayList;
import java.util.List;

import net.mcshockwave.MCS.Utils.PacketUtils;
import net.mcshockwave.Minigames.Game;
import net.mcshockwave.Minigames.Game.GameTeam;
import net.mcshockwave.Minigames.Handlers.IMinigame;
import net.mcshockwave.Minigames.Minigames;
import net.mcshockwave.Minigames.Events.DeathEvent;
import net.mcshockwave.Minigames.Shop.ShopItem;
import net.mcshockwave.Minigames.Utils.BlockUtils;
import net.mcshockwave.Minigames.worlds.Multiworld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

public class BuildAndFight implements IMinigame {

	Location			bSt			= new Location(Multiworld.getGame(), -603, 100, 8);
	Location			bEn			= new Location(Multiworld.getGame(), -600, 100, -6);
	boolean				building	= true;

	ArrayList<Block>	blocks		= new ArrayList<Block>();

	@Override
	public void onGameStart() {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				for (OfflinePlayer op : Game.Build_and_Fight.teams[0].team.getPlayers()) {
					if (op instanceof Player) {
						Player p = (Player) op;
						giveKit(p, 13);
					}
				}
				for (OfflinePlayer op : Game.Build_and_Fight.teams[1].team.getPlayers()) {
					if (op instanceof Player) {
						Player p = (Player) op;
						giveKit(p, 4);
					}
				}
				building = true;
				Minigames.broadcast("You have %s to build a fort, then fight!", "1 minute");
			}
		}, 10);
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				building = false;
				Minigames.broadcastAll(Minigames.getBroadcastMessage(ChatColor.RED, "Times up! You may now %s!",
						"fight"));
				Minigames.clearInv(Minigames.getOptedIn().toArray(new Player[0]));
				for (OfflinePlayer op : Game.Build_and_Fight.teams[0].team.getPlayers()) {
					if (op instanceof Player) {
						Player p = (Player) op;
						giveKit(p, 13);
					}
				}
				for (OfflinePlayer op : Game.Build_and_Fight.teams[1].team.getPlayers()) {
					if (op instanceof Player) {
						Player p = (Player) op;
						giveKit(p, 4);
					}
				}
				BlockUtils.setBlocks(bSt, bEn, Material.WOOD, 0);
			}
		}, 1200);
	}

	public void giveKit(Player p, int data) {
		if (building) {
			p.getInventory().addItem(
					new ItemStack(Minigames.hasItem(p, ShopItem.Builder) ? Material.STAINED_CLAY : Material.WOOL, 64,
							(short) data));
			p.getInventory().addItem(new ItemStack(Material.SHEARS));
		} else {
			p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
			p.getInventory().addItem(
					new ItemStack(Minigames.hasItem(p, ShopItem.Fighter) ? Material.SHEARS : Material.BOW));
			if (!Minigames.hasItem(p, ShopItem.Fighter))
				p.getInventory().addItem(new ItemStack(Material.ARROW, 64));
		}
	}

	@Override
	public void onGameEnd() {
		building = true;
		BlockUtils.setBlocks(bSt, bEn, Material.AIR, 0);
		for (Block b : blocks) {
			if (b.getType() == Material.WOOL || b.getType() == Material.STAINED_CLAY) {
				b.setType(Material.AIR);
			}
		}
	}

	@Override
	public void onPlayerDeath(DeathEvent e) {
		Minigames.broadcastDeath(e.p, e.k, "%s was killed", "%s was killed by %s");

		if (e.k != null && Minigames.hasItem(e.k, ShopItem.Fighter)) {
			e.k.setHealth(e.k.getMaxHealth());
		}
	}

	public boolean canBuild(Player p, Block b) {
		GameTeam gt = Game.getTeam(Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p));
		if (p.getGameMode() == GameMode.CREATIVE) {
			return true;
		}
		if (!building && Minigames.hasItem(p, ShopItem.Fighter)) {
			return true;
		}
		if (b.getLocation().getY() > 150) {
			return false;
		}
		if (Minigames.optedOut.contains(p.getName())) {
			return true;
		}
		if (b.getType() != Material.WOOL && b.getType() != Material.STAINED_CLAY) {
			return false;
		}
		if (gt != null) {
			if (!building || gt.color == ChatColor.YELLOW && b.getBiome() != Biome.PLAINS
					|| gt.color == ChatColor.GREEN && b.getBiome() != Biome.FOREST) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		blocks.add(event.getBlock());
		event.setCancelled(!canBuild(event.getPlayer(), event.getBlock()));
		if (!event.isCancelled()) {
			if (Minigames.hasItem(event.getPlayer(), ShopItem.Builder)) {
				ItemStack it = event.getPlayer().getItemInHand();
				it.setAmount(64);
				event.getPlayer().setItemInHand(it);
			}
		}
	}

	@EventHandler
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		blocks.remove(event.getBlock());
		event.setCancelled(event.getBlock().getType() != Material.WOOL
				|| !canBuild(event.getPlayer(), event.getBlock()));
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (event.getDamager() instanceof Arrow && Minigames.hasItem(p, ShopItem.Fighter)) {
				event.setCancelled(true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile e = event.getEntity();
		BlockIterator iterator = new BlockIterator(e.getWorld(), e.getLocation().toVector(), e.getVelocity()
				.normalize(), 0, 4);
		Block hb = null;

		List<Block> aff = new ArrayList<>();
		while (iterator.hasNext()) {
			hb = iterator.next();
			if (hb.getTypeId() != 0)
				break;
		}
		aff.add(hb);
		if (e.getShooter() != null && e.getShooter() instanceof Player) {
			Player d = (Player) e.getShooter();

			if (Minigames.hasItem(d, ShopItem.Archer)) {
				for (BlockFace bf : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
						BlockFace.UP, BlockFace.DOWN }) {
					if (rand.nextInt(3) == 0)
						continue;
					aff.add(hb.getRelative(bf));
				}
			}
		}
		for (Block hitBlock : aff) {
			if (hitBlock.getType() == Material.WOOL) {
				PacketUtils.sendPacketGlobally(hitBlock.getLocation(), 50,
						PacketUtils.generateBlockParticles(Material.WOOL, hitBlock.getData(), hitBlock.getLocation()));
				hitBlock.getWorld().playSound(hitBlock.getLocation(), Sound.DIG_WOOL, 1, 1);
				hitBlock.setType(Material.AIR);
				blocks.remove(hitBlock);
			}
			if (hitBlock.getType() == Material.STAINED_CLAY) {
				PacketUtils.sendPacketGlobally(hitBlock.getLocation(), 50,
						PacketUtils.generateBlockParticles(Material.WOOL, hitBlock.getData(), hitBlock.getLocation()));
				hitBlock.getWorld().playSound(hitBlock.getLocation(), Sound.DIG_STONE, 1, 1);
				byte da = hitBlock.getData();
				hitBlock.setType(Material.WOOL);
				hitBlock.setData(da);
			}
		}
		e.remove();
	}

}

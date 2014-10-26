package net.mcshockwave.Minigames.Shop;

import net.mcshockwave.Minigames.Game;

public enum ShopItem {

	// Airships
	Demoman(
		Game.Airships,
		0,
		true,
		1000,
		"Start with a TNT.",
		"Right-click it to throw",
		"it. When it explodes, it",
		"shoots out arrows everywhere"),
	Jammer(
		Game.Airships,
		1,
		true,
		1500,
		"Start with a Nether Star.",
		"Right-click it to give enemies",
		"around you nausea and disable their",
		"weapon for 5 seconds.",
		"One-time use."),
	Venom(
		Game.Airships,
		2,
		true,
		1500,
		"Shooting people will have a 1/4",
		"chance of poisoning them for 4 seconds!"),
	// Boarding
	Privateer(
		Game.Boarding,
		0,
		true,
		2750,
		"Makes you near-invulnerable to bullets,",
		"and heals you whenever you kill someone"),
	Buccaneer(
		Game.Boarding,
		1,
		true,
		1750,
		"Allows you to run faster, double-jump,",
		"and do a half-heart more damage!"),
	Scavenger(
		Game.Boarding,
		2,
		true,
		1000,
		"Scavenge gunpowder from killed players,",
		"and reload slightly faster!"),
	// Brawl
	Ruthless(
		Game.Brawl,
		0,
		true,
		1750,
		"Start with a wooden pole.",
		"Hitting your opponent with this",
		"pole will give them nausea."),
	Panther(
		Game.Brawl,
		1,
		false,
		3000,
		"You are granted one double jump per match."),
	Outcast(
		Game.Brawl,
		2,
		true,
		2250,
		"Start with a wooden sword.",
		"You will do half a heart of damage to your",
		"opponent per hit."),
	// Build and fight
	Fighter(
		Game.Build_and_Fight,
		0,
		true,
		2000,
		"You can't take any bow damage at all,",
		"and you will get healed for every kill."),
	Builder(
		Game.Build_and_Fight,
		1,
		true,
		1500,
		"You will receive infinite clay blocks.",
		"Clay blocks take 2 hits with a bow",
		"to break."),
	Archer(
		Game.Build_and_Fight,
		2,
		true,
		2250,
		"Arrows can break up 6 blocks at once."),
	// Chamber
	// Core
	Conqueror(
		Game.Core,
		0,
		true,
		2000,
		"Use your speed II to be the first to the Core",
		"and run from enemies. Crouch to launch",
		"nearby players away (15 second cooldown)"),
	Hermit(
		Game.Core,
		1,
		false,
		2500,
		"Right click your ender eye to get a full",
		"heal and teleport to a treehouse within the map.",
		"One-time use per life"),
	Ranger(
		Game.Core,
		2,
		true,
		2000,
		"Get a bow for ranged attacks and a wooden sword.",
		"You get 5 arrows per life"),
	// Dodgeball
	Magician(
		Game.Dodgeball,
		0,
		false,
		2250,
		"Sneak to make all airborn dodgeballs",
		"drop on the floor.",
		"10 second cooldown."),
	Athlete(
		Game.Dodgeball,
		1,
		true,
		1000,
		"Use your Speed II to be the first to",
		"pick up dodgeballs and easily evade",
		"on-coming balls."),
	Catcher(
		Game.Dodgeball,
		2,
		true,
		1750,
		"You can absorb one dodgeball hit.",
		"However, you have terrible accuracy."),
	// Dogtag
	General(
		Game.Dogtag,
		0,
		true,
		2500,
		"You can pick up your own dogtag when",
		"you're in holding. Be sure to find it",
		"before the enemy does!"),
	Major(
		Game.Dogtag,
		1,
		true,
		2750,
		"Your compass will point to the nearest",
		"dogtag, and you do more damage the further",
		"away your target is"),
	Colonel(
		Game.Dogtag,
		2,
		true,
		2500,
		"Right-click blocks with your 3 gunpowder to",
		"turn them into mines that will explode when",
		"enemies step on them!"),
	// Four Corners
	Hacker(
		Game.Four_Corners,
		0,
		false,
		2750,
		"Choose which corner will fall next with",
		"your nether star! (One-time use)"),
	Virus(
		Game.Four_Corners,
		1,
		false,
		2000,
		"Start with a virus that will cause you",
		"to move very slow. Punch a player to give",
		"them your virus!"),
	Quickfoot(
		Game.Four_Corners,
		2,
		true,
		1250,
		"Speed II to go from corner to corner",
		"very quickly. Immune to virus."),
	// Ghostbusters
	// Gladiators
	Provocateur(
		Game.Gladiators,
		0,
		true,
		3000,
		"You are armed with a wooden sword and",
		"iron chestplate. You will be very",
		"slow."),
	Man_at_Arms(
		Game.Gladiators,
		1,
		true,
		2500,
		"Your lightweight armor allows you to",
		"sprint faster. Shift to evade.",
		"(2 second cooldown)"),
	Murmillo(
		Game.Gladiators,
		2,
		true,
		1500,
		"Your melee skills are poor, but your",
		"bow not only deals damage but slows",
		"players down."),
	// Hot Potato
	// Infection
	Floater(
		Game.Infection,
		0,
		true,
		1750,
		"Double jump throughout the map,",
		" zombie or human."),
	Lurker(
		Game.Infection,
		1,
		true,
		3000,
		"Tap the shift key to become invisible",
		"for 10 seconds, cooldown of 30",
		"seconds."),
	Biter(
		Game.Infection,
		2,
		true,
		2500,
		"As a human you do 1/2 heart more damage to",
		"zombies. As a zombie any human you hit will",
		"be withered for a short time."),
	// Loot
	// Minotaur
	// Siege
	// Spleef
	TNT_Bomb(
		Game.Spleef,
		0,
		true,
		2500,
		"Start with 1 tnt bomb.",
		"Throwing the tnt bomb will",
		"make it explode."),
	Teleporter(
		Game.Spleef,
		1,
		true,
		2000,
		"Start with a ghast tear",
		"that when right-clicked",
		"will randomly teleport you",
		"anywhere in the spleef map."),
	// Tiers
	// TRON
	Color_Bomb(
		Game.TRON,
		1,
		true,
		2000,
		"You will recieve 1 color bomb.",
		"Throwing the color bomb will",
		"create purple wool that kills both teams",
		"for 10 seconds."),
	// Village Battle
	Ressurector(
		Game.Village_Battle,
		0,
		true,
		2750,
		"Killing enemy players has a slight chance of",
		"giving your team one un-possessed villager.",
		"It will respawn right in front of you, and",
		"ghosts on your team can instantly possess it,",
		"giving you help in battle."),
	Thief(
		Game.Village_Battle,
		1,
		true,
		1250,
		"Killing enemy villagers will grant you a",
		"fraction of their stamina."),
	Disruptor(
		Game.Village_Battle,
		2,
		true,
		3000,
		"Disable the enemy team's possessing abilities",
		" for 10 seconds!");

	public String	name;
	public Game		mg;
	public boolean	perma;
	public int		cost, id;
	public String[]	desc;

	ShopItem(Game minigame, int id, boolean permanent, int cost, String... desc) {
		this.name = name().replace('_', ' ');
		this.mg = minigame;
		this.perma = permanent;
		this.cost = cost;
		this.id = id;
		this.desc = desc;
	}

	public int getPermaCost() {
		return cost * 10;
	}

}

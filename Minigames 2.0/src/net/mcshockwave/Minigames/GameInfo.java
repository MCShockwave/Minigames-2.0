package net.mcshockwave.Minigames;

import org.bukkit.ChatColor;

public enum GameInfo {

	Core(
		"Teams",
		ChatColor.YELLOW + "4",
		"Respawn",
		ChatColor.YELLOW + "Varies",
		"Main",
		"Players are spawned in their respective bases. In the beginning, everyone is able to respawn."
				+ " In the middle sitting at the top of the parkour lies a wool block."
				+ " Finish the parkour and right click this wool block, the block will"
				+ " turn to your team's color and only your team will be able to respawn.",
		"Goal",
		"Be the last team standing!",
		"Tip",
		"Pay attention when your Core is captured! When it isn't, it's best to stay out of harms way!"),
	Airships(
		"Teams",
		ChatColor.RED + "None",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Players are spawned with flymode and a bow with infinite arrows."
				+ " Shoot at other players while dodging arrows shot at you"
				+ ", once your health is drained you are unable to fly and forced to crash-land"
				+ " into the ground below, eliminating you. Do not touch the ground at any time!",
		"Goal",
		"Be the last person standing!",
		"Tip",
		"Move up and down to make it harder for people to hit you!"),
	Brawl(
		"Teams",
		ChatColor.RED + "None",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Players are spawned around a single platform. "
				+ "2 players are selected one at a time to battle in the middle. "
				+ "If you get knocked off the platform, you're out!",
		"Goal",
		"Be the last player standing",
		"Tip",
		"When battling your opponent, don't jump! You get more knockback!"),
	Build_and_Fight(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"2 teams are spawned on separate platforms, "
				+ "and are given one minute build time with 64 of their team color's wool. "
				+ "After time's up, the two platforms are connected and players are given a sword & bow.",
		"Goal",
		"Be the last team standing!",
		"Tip",
		"Bows can shoot through wool, and on some maps almost anything, so make your walls thick!"),
	Dodgeball(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Players are spawned on 2 seperate sides of the map"
				+ " and 8 dodgeballs are dropped in the middle of the arena,"
				+ " and will continue to drop every 30 seconds. Pick up the dodge"
				+ "balls and throw them at your opponents to eliminate them."
				+ " You also start with 5 clay and get 3 more when you get a hit."
				+ " Use your clay to make barriers to prevent you from being hit!"
				+ " Keep in mind that dodgeballs can break through clay!",
		"Goal",
		"Be the last team standing!",
		"Tip",
		"Make sure to strafe from side to side frequently to avoid oncoming dodge balls."),
	Four_Corners(
		"Teams",
		ChatColor.RED + "None",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Players are spawned in a small map with 4 corners that are primary colors,"
				+ " connected by bridges. Every 10 seconds, the bridges will break and one"
				+ " corner will also be destroyed, causing all the players on that platform" + " to fall and die.",
		"Goal",
		"Be the last player standing!",
		"Tip",
		"Don't take too long to run to your corner! Lots of players have died from being too slow."),
	Spleef(
		"Teams",
		ChatColor.RED + "None",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Players are spawned on a wide snow platform one block deep. The only way to kill players"
				+ " is to break the blocks from under them. To do this, simply click the snow to cause a chunk"
				+ " of it to fall into the void below.",
		"Goal",
		"Be the last player standing!",
		"Tip",
		"Always be running! If you stop, players can break the blocks under you easily!"),
	TRON(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Players are spawned on two sides of an arena. When you run, a trail of your"
				+ " team's color will follow you. If you touch the opposing team's trail, you will die."
				+ " Run around the arena and try to cut in front of enemies so they'll crash into your trail!"
				+ " Also, don't stand still! You'll die.",
		"Goal",
		"Be the last team standing!",
		"Tip",
		"Try to take sudden turns when your enemies are least expecting it!"),
	Dogtag(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.YELLOW + "Varies",
		"Main",
		"Two teams are spawned away from each other, equipped with a bow."
				+ " Battle it out with the opposing team, but be careful! When you die, your skull"
				+ " will drop and you will be put in a temporary spectator mode. If your team is able"
				+ " to retrieve (pick up) your skull, you will respawn! However, if your skull gets"
				+ " in enemy hands, you're out of the game!",
		"Goal",
		"Be the last team standing!",
		"Tip",
		"Fight with someone right beside you! When you die,"
				+ " they can simply pick up your skull again and you can get back into battle!"),
	Boarding(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.GREEN + "Yes",
		"Main",
		"2 teams are spawned on floating airships. Each team will have a certain amount of "
				+ "reinforcements. Whenever someone dies by the hand of an enemy, the team's reinforcements"
				+ " drop down by 1." + " Teams can fire cannons to destroy the other team's ship. Use the "
				+ "fire extinguisher to put out deadly fires!",
		"Goal",
		"Be the last team with reinforcements!",
		"Tip",
		"Use your 'block' on your sword wisely! It IS more effective on this minigame!"),
	Village_Battle(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.GREEN + "Yes",
		"Main",
		"Teams are spawned on each side of two village towns as 'ghosts'. Right-click villagers"
				+ " in the town to take control of them and inherit their special abilities and weapons!"
				+ " When you die as a villager, you are turned back into a ghost to find another body you can possess!",
		"Goal",
		"Kill the other team's villagers and be the last team with villagers to win!",
		"Tip",
		"Kill unpossessed villagers on the opposing team, too! That way, nobody can claim them, "
				+ "and you'll win the game faster!"),
	Hot_Potato(
		"Teams",
		ChatColor.RED + "None",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Everyone gets spawned in an arena, and a few players get randomly selected to have the potato. "
				+ " While the potato is in your inventory, you will be set on fire and lose health. The aim is to get rid "
				+ " of the potato as quickly as possible by hitting another player with it."
				+ " They will then get the potato. You will stop being on fire, and you have to run around the arena,"
				+ " avoiding getting the potato again.",
		"Goal",
		"Be the last person standing!",
		"Tip",
		"Run away from people with the potato! When they die the nearest player will get their potato!"),
	Infection(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.GREEN + "Yes",
		"Main",
		"Players get spawned in the map as humans, and a few are randomly selected as zombies."
				+ " The humans have to avoid the zombies, while the zombies try to kill the humans. "
				+ "If the zombie kills a human the human becomes a zombie too.",
		"Goal",
		"Be the last human standing",
		"Tip",
		"Find a good spot to hide that won't get notcied by zombies!"),
	Siege(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.YELLOW + "Varies",
		"Main",
		"Two teams spawn on either side of the map, and they both have kings, which are villagers."
				+ " If you kill the other teams vilager, they wont respawn. You have to protect your own villager"
				+ " while trying to eliminate the other teams villager in the process.",
		"Tip",
		"If your king dies, you won't respawn!"),
	Gladiators(
		"Teams",
		ChatColor.YELLOW + "4",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Teams are spawned in their waiting areas, while one member of their team is randomly selected to go into the arena."
				+ " They then fight the other 3 people in the arena from the other teams, while avoiding dying. If the member in the"
				+ " arena dies, another member of the team is randomly selected to fight.",
		"Goal",
		"Be the last team standing!",
		"Tip",
		"Defense can be the best offense"),
	Loot(
		"Teams",
		ChatColor.RED + "None",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Everyone spawns with random armour, potions and a random weapon. The aim of the game"
				+ " is to kill opponents, and pick up their discarded loot. Any loot better"
				+ " than your current loot is equipped. Every potion will be picked up.",
		"Goal",
		"Be the last competitor standing!",
		"Tip",
		"Use your potions wisely."),
	Minotaur(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"Everyone is spawned in a maze, with one person as the Minotaur. The Minotaur is equipped"
				+ " with diamond armor and a sword, and has to kill everyone. Everyone else has to find chests scattered"
				+ " around the map, and group together to kill the Minotaur.",
		"Goal",
		"Minotaur- Kill Humans. Humans- Group together and slay the Minotaur",
		"Tip",
		"Minotaur- Try to split up groups of humans, they will be easier to take on 1 on 1."
				+ " Humans- Teamwork is key, stick together!"),
	Laser_Tag(
		"Teams",
		ChatColor.YELLOW + "2",
		"Respawn",
		ChatColor.GREEN + "Yes",
		"Main",
		"All players are equipped with guns (hoes) that fire lasers. Each"
				+ " team starts with up to 100 points. If you shoot a player on the other team they lose a"
				+ " point. If you shoot one of the enemies two bases (redsone lamps) they will lose 5 points.",
		"Goal",
		"Reduce the other team's points to 0!",
		"Tip",
		"Make use of cover, and remember your gun has a 2 second cooldown after being shot."),
	Tiers(
		"Teams",
		ChatColor.YELLOW + "4",
		"Respawn",
		ChatColor.GREEN + "Yes",
		"Main",
		"All teams start with really good armor and swords. When one member of the team gets a kill,"
				+ " they advance a tier, lowering the power of their weapons and armor.",
		"Goal",
		"Advance to the top tier before any other team!",
		"Tip",
		"Team up and take on people who are alone. They will be easier to take on and everyone on your team tiers up!"),
	Storm_The_Castle(
		"Teams",
		ChatColor.YELLOW + "4",
		"Respawn",
		ChatColor.GREEN + "Yes",
		"Main",
		"There are two teams: Barbarians (attacking) and Knights (defending)."
				+ " The barbarians have to go through the stages of attack to win, while the knights have to stop them."
				+ " Barbarians have reinforcements, which decrease every kill the Knights get, and when they run out the Knights win."
				+ " First Stage: Barbarians must place TNT at the wall to try to blow it up."
				+ " Second Stage: Barbarians must take inhibitors (beacons) to the gold blocks near the Crystal (Ender Crystal)."
				+ " Third Stage: Barbarians must attack the Crystal to destroy it."
				+ " Final Stage: Knights must make a last stand. They will not respawn. Barbarians win when all knights are eliminated.",
		"Whenever the barbarians complete an objective, they will get more reinforcements. Only exception is the Fourth Stage."
				+ "Finally, the Barbarians can find a bomb and place it near the Knights' cache (chests)"
				+ " and detonate it to cripple them (They will spawn with less armor)",
		"Goal",
		"Barbarians- Go through the stages without running out of reinforcements Knights- Deplete all the Barbarians' reinforcements",
		"Tip",
		"Barbarians- Destroy the cache and work together to lead people to the objective. "
				+ "Knights- Spread out, you outgear the barbarians, and you can cover more ground that way."),
	Giant_Stomp(
		"Teams",
		ChatColor.RED + "None",
		"Respawn",
		ChatColor.RED + "No",
		"Main",
		"A giant zombie is in the center that jumps up and down. When it lands, it creates a large shockwave that will launch people away."
				+ " Players can also use the knockback sword to hit people off.",
		"Goal",
		"Be the last person on the platform.",
		"Tip",
		"Keep jumping - you can jump over the shockwave. Also, if you sneak and block with the sword, the shockwave does less knockback."),
		Target(
				"Teams",
				ChatColor.YELLOW + "2",
				"Respawn",
				ChatColor.YELLOW + "Variable", 
				"Main",
				"A target is picked for each team, when the target is killed by the other team a new one is picked and the target is eliminated",
				"Goal",
				"Kill all of the other team",
				"Tip",
				"If you're the target stay out of the action, but near teammates");

	public String[]	info;

	GameInfo(String... info) {
		this.info = info;
	}

}

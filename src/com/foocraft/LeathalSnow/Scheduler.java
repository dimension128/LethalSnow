package com.foocraft.LeathalSnow;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class Scheduler implements Runnable {
	public static LethalSnow plugin;
	
	public Scheduler(LethalSnow instance) {
		plugin = instance;
	}
	
	public static Location[] OldPositions = new Location[0];
	//Location from = event.getFrom();
	//Location to = event.getTo();
	//double distance = from.distance(to);
	
	public void run() {
		if(plugin.ExhaustionMultiplier > 0.0){
			Player[] players = plugin.getServer().getOnlinePlayers();
			int i;
			int length = players.length;
			if(length > 0){// no need to run all this if nobody is online.
				// populate NewPositions array with current online player's positions.
				Location[] NewPositions = new Location[length];
				for(i=0; i< length; i++){
					NewPositions[i] = players[i].getLocation();
				}
				// Only want to run all of this, if the lists match.
				if(NewPositions.length == OldPositions.length){
					for(i=0; i < length; i++){
						try{
							Player player = players[i];
							// check to see if player is not in creative mode, doesn't have the exhaustion.exempt perm, and if they are in a snow biome.
							if((player.getGameMode() != GameMode.CREATIVE) && !(player.hasPermission("lethalsnow.exhaustion.exempt")) && is_snow_biome(player.getLocation().getBlock().getBiome())){
								double distance = OldPositions[i].distance(NewPositions[i]);
								if(distance > 0){ // so many nested ifs!
									float NewExhaustion = (float)(player.getExhaustion() + (distance * (plugin.ExhaustionMultiplier * 0.001)));
									if(plugin.SHOW_DEBUG){player.sendMessage("Setting Exhaustion: " + NewExhaustion + " Before: " + player.getExhaustion() + " Difference: " + (NewExhaustion - player.getExhaustion()));}
									if(plugin.SHOW_DEBUG){plugin.logMessage("|Adding " + (NewExhaustion - player.getExhaustion()) + " exhaustion to "  + player.getName() + ".");}
									player.setExhaustion( NewExhaustion );
								}
							}
						}
						catch(Exception e){
							// Meh something went wrong. No big deal really.
							if(plugin.SHOW_DEBUG){plugin.logMessage("EXCEPTION: " + e);}
						}
						if(plugin.SHOW_DEBUG){plugin.logMessage("All exhaustion calculations completed for this iteration.");}
					}
				}
				else{// arrays don't match, so this is the first iteration.. or players have logged in/out.
					// guess we will just ignore this condition for now.
					if(plugin.SHOW_DEBUG){plugin.logMessage("Exhaustion calculations have been ignored for this iteration because the playerlist has changed.");}
				}
				OldPositions = new Location[0]; // reset the array to be sure to free the memory... Not sure if this is good in java or not.
				OldPositions = NewPositions;
			}
		}
	}
	
	public boolean is_snow_biome(Biome biome){
		return (biome != null && (biome.equals(Biome.TAIGA) || biome.equals(Biome.TAIGA_HILLS) || biome.equals(Biome.FROZEN_OCEAN) || biome.equals(Biome.FROZEN_RIVER) || biome.equals(Biome.ICE_MOUNTAINS) || biome.equals(Biome.ICE_PLAINS)));
	}
}
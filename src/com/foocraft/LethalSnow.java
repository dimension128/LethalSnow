/**
* @author Jason Keeslar "LordJason"
* License: Give credit if you modify and/or redistribute any of my code.
*/

package com.foocraft;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LethalSnow extends JavaPlugin{
	protected FileConfiguration config;
	Logger log = Logger.getLogger("Minecraft");
	private final LsEntityListener eListener = new LsEntityListener(this);
	//private final LsPlayerListener pListener = new LsPlayerListener(this);
	public int DamageMin = 1;
	public int DamageMax = 10;
	public int DamageChance = 100;
	public boolean HURT_PLAYER = false;
	public boolean HURT_MOBS = true;
	public boolean SHOW_DEBUG = false;
	public Random rand = new Random();
	public int ExhaustionMultiplier = 10;
	public int maxSeedDrop = 2;
	
	@Override
	public void onDisable() {
		saveConfig();
		logMessage("Version " + this.getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile pdFile = this.getDescription();
		config = getConfig();
		Float ConfigVersion = Float.parseFloat(config.getString("System.Version", "0"));
		Float PlugVersion = Float.parseFloat(pdFile.getVersion());
		SHOW_DEBUG = this.getConfig().getBoolean("System.DebugMessages", false);
		if(SHOW_DEBUG){logMessage("Config file, Version " + ConfigVersion);}
		if(ConfigVersion < PlugVersion && ConfigVersion < 0.25){
			logMessage("Config file NOT found or out of date.");
			write(DamageMin, DamageMax, DamageChance, maxSeedDrop, HURT_PLAYER, HURT_MOBS, ExhaustionMultiplier, SHOW_DEBUG);
			logMessage("Default config file created.");
		}
		loadFromConfig();
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, eListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, eListener, Event.Priority.Normal, this);
		//pm.registerEvent(Event.Type.PLAYER_MOVE, pListener, Event.Priority.Normal, this);
		sched_int();
		logMessage("Version " + this.getDescription().getVersion() + " enabled.");
	}
	
	public void sched_int(){
		//Kill all tasks first in case one is running.
		this.getServer().getScheduler().cancelTasks(this);
		// If we need the Scheduler, start it.
		if(ExhaustionMultiplier > 0){
			this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Scheduler(this), 0L, 10*20);
		}
	}
	
	public int clampInt(int i){
		if(i < 0){i = 0;}
		else if(i > 100){i=100;}
		return i;
	}
	void write(int min, int max, int chance, int seeds, boolean Damageplayer, boolean Dmobs, int multi, boolean deb){
		PluginDescriptionFile pdFile = this.getDescription();
		write(min, max, chance, seeds, Damageplayer, Dmobs, multi, deb, pdFile.getVersion());
	}
	void write(int min, int max, int chance, int seeds, boolean Damageplayer, boolean Dmobs, int multi, boolean deb, String v){
		this.getConfig().set("SnowBall.DamageMin", clampInt(min));
		this.getConfig().set("SnowBall.DamageMax", clampInt(max));
		this.getConfig().set("SnowBall.DamageChance", clampInt(chance));
		this.getConfig().set("SnowMan.MaxSeedDrop", clampInt(seeds));
		this.getConfig().set("SnowBall.DamagePlayer", Damageplayer);
		this.getConfig().set("SnowBall.DamageMobs", Dmobs);
		this.getConfig().set("SnowBiome.ExhaustionMultiplier", clampInt(multi));
		this.getConfig().set("System.DebugMessages", deb);
		this.getConfig().set("System.Version", v);
		saveConfig();
	}
	
	public void loadFromConfig(){
		reloadConfig();
		SHOW_DEBUG = this.getConfig().getBoolean("System.DebugMessages", false);
		HURT_PLAYER = this.getConfig().getBoolean("SnowBall.DamagePlayer", false);
		HURT_MOBS = this.getConfig().getBoolean("SnowBall.DamageMobs", true);
		DamageMin = clampInt(this.getConfig().getInt("SnowBall.DamageMin", 1));
		DamageMax = clampInt(this.getConfig().getInt("SnowBall.DamageMax", 10));
		DamageChance = clampInt(this.getConfig().getInt("SnowBall.DamageChance", 100));
		maxSeedDrop = clampInt(this.getConfig().getInt("SnowMan.MaxSeedDrop", 2));
		ExhaustionMultiplier = clampInt(this.getConfig().getInt("SnowBiome.ExhaustionMultiplier", 10));
		write(DamageMin, DamageMax, DamageChance, maxSeedDrop, HURT_PLAYER, HURT_MOBS, ExhaustionMultiplier, SHOW_DEBUG);
	}
	
	String int2String(int i){
		return Integer.toString(i);
	}
	int string2Int(String s){
		int rtn = 0;
		try{
			rtn = clampInt(Integer.parseInt(s));
		}
		catch(Exception e ){
			rtn = 0;
		}
		return rtn;
	}
	String a2s(String[] a, int i){
		String rtn;
		try{
			rtn = a[i];
		}
		catch(Exception e){
			rtn = "";
		}
		
		return rtn;
	}
	boolean tf(String s){
		s = s.toLowerCase();
		if(s.equals("true") || s.equals("t") ||  s.equals("y") ||  s.equals("yes") ||  s.equals("on")){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean isBool( String s ){
		s = s.toLowerCase();
		return (s.equals("true") || s.equals("t") ||  s.equals("y") ||  s.equals("yes") ||  s.equals("on")
				|| s.equals("false") || s.equals("f") ||  s.equals("n") ||  s.equals("no") ||  s.equals("off"));
	}
	public boolean isInteger( String input ){
		try{
			Integer.parseInt( input );
			return true;
		}
		catch( Exception e){
			return false;
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		boolean rtn = false;
		if(sender instanceof Player == true){
			Player player = (Player)sender;
			if(cmd.getName().equalsIgnoreCase("lsnow") && args.length >= 1){
				int i;
				for(i=0; i < args.length; i++){
					if(SHOW_DEBUG){player.sendMessage("args[" + i + "] = " + args[i]);}
					String arg = a2s(args, i).toLowerCase();
					if(arg.equals("min")){
						DamageMin = string2Int(a2s(args, i + 1));
						this.getConfig().set("SnowBall.DamageMin", DamageMin);
						player.sendMessage("SnowBall min damage set to " + DamageMin);
						logMessage(player.getDisplayName() + " Set snowball min damage to, " + DamageMin);
						saveConfig();
						if(isInteger(a2s(args, i + 1))){i++;}
						rtn = true;
					}
					else if(arg.equals("max")){
						DamageMax = string2Int(a2s(args, i + 1));
						this.getConfig().set("SnowBall.DamageMax", DamageMax);
						player.sendMessage("SnowBall max damage set to " + DamageMax);
						logMessage(player.getDisplayName() + " Set snowball max damage to, " + DamageMax);
						saveConfig();
						if(isInteger(a2s(args, i + 1))){i++;}
						rtn = true;
					}
					else if(arg.equals("exhaustion") || arg.equals("ex") || arg.equals("exhaustionmultiplier")){
						ExhaustionMultiplier = string2Int(a2s(args, i + 1));
						this.getConfig().set("SnowBiome.ExhaustionMultiplier", ExhaustionMultiplier);
						player.sendMessage("ExhaustionMultiplier set to " + ExhaustionMultiplier);
						logMessage(player.getDisplayName() + " Set ExhaustionMultiplier to, " + ExhaustionMultiplier);
						saveConfig();
						if(isInteger(a2s(args, i + 1))){i++;}
						rtn = true;
						sched_int(); // turns on or off - the scheduler.
					}
					else if(arg.equals("chance")){
						DamageChance = string2Int(a2s(args, i + 1));
						this.getConfig().set("SnowBall.DamageChance", DamageChance);
						player.sendMessage("SnowBall damage chance set to " + DamageChance);
						logMessage(player.getDisplayName() + " Set snowball damage chance to, " + DamageChance);
						saveConfig();
						if(isInteger(a2s(args, i + 1))){i++;}
						rtn = true;
					}
					else if(arg.equals("debug")){
						boolean b = tf(a2s(args, i + 1));
						this.getConfig().set("System.DebugMessages", b);
						SHOW_DEBUG = b;
						if(b){
							player.sendMessage("Debug info is on.");
						}
						else{
							player.sendMessage("Debug info is off.");
						}
						saveConfig();
						if(isBool(a2s(args, i + 1))){i++;}
						rtn = true;
					}
					else if(arg.equals("reload")){
						loadFromConfig();
						sched_int();
						player.sendMessage("Config file reloaded.");
						logMessage("Config file reloaded.");
						rtn = true;
					}
					else if(arg.equals("damageplayer") || arg.equals("damageplayers")){
						boolean b = tf(a2s(args, i + 1));
						this.getConfig().set("SnowBall.damagePlayer", b);
						HURT_PLAYER = b;
						if(b){
							player.sendMessage("Snow balls will damage players.");
						}
						else{
							player.sendMessage("Snow balls will not damage players.");
						}
						saveConfig();
						if(isBool(a2s(args, i + 1))){i++;}
						rtn = true;
					}
					else if(arg.equals("damagemob") || arg.equals("damagemobs")){
						boolean b = tf(a2s(args, i + 1));
						this.getConfig().set("SnowBall.damageMobs", b);
						HURT_MOBS = b;
						if(b){
							player.sendMessage("Snow balls will damage mobs.");
						}
						else{
							player.sendMessage("Snow balls will not damage mobs.");	
						}
						saveConfig();
						if(isBool(a2s(args, i + 1))){i++;}
						rtn = true;
					}
					else if(arg.equals("biome")){
						Biome biome = player.getLocation().getBlock().getBiome();
						player.sendMessage("Biome: " + biome.name());
						rtn = true;
					}
					else{// Unknown argument.
						player.sendMessage("Argument #" + (i + 1) + "["+ arg + "] is not recognized");
						//if(!rtn){// unknown argument, and there has been no recognized argument yet.
						//	player.sendMessage("Argument #" + (i + 1) + "["+ arg + "] is not recognized");
						//}
					}
				}
			}
			else{// This should only happen if args.length is 0
				rtn = false;
			}
		}
		return rtn;
	}
	
	public void logMessage(String msg){
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.info("[" + pdFile.getName() + "]" + " " + msg);
	}
}
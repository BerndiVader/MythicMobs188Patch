package com.gmail.berndivader.mm188patch;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import io.lumine.xikage.mythicmobs.MythicMobs;

public class Core extends JavaPlugin {
	public static Core plugin;
	public static Logger logger;
	public static PluginManager pluginmanager;
	public static MythicMobs mythicmobs;
	public MythicMobs188 mythicmobs188;
	public int minecraftVersion;
	
	@Override
	public void onEnable() {
		plugin=this;
		pluginmanager=Bukkit.getPluginManager();
		logger=this.getLogger();
		this.setMCver();
		
		if (this.minecraftVersion!=8
				|| !pluginmanager.isPluginEnabled("MythicMobs")) {
	    	logger.warning("Bukkit 1.8.8 and MythicMobs is required!");
	    	getPluginLoader().disablePlugin(plugin);
	    	return;
		}
		mythicmobs=MythicMobs.inst();
		this.mythicmobs188=new MythicMobs188();
	}
	
	@Override
	public void onDisable() {
		pluginmanager.disablePlugin(this);
		pluginmanager=null;
		logger=null;
		mythicmobs=null;
		plugin=null;
	}
	
	public Plugin inst() {
		return plugin;
	}
	
	private void setMCver() {
	    try {
	    	String[] split = Bukkit.getServer().getClass().getPackage().getName().substring(23).split("_");
	    	minecraftVersion = Integer.parseInt(split[1]);
	    } catch (final Exception ex) {
	    	minecraftVersion = 11;
	    }		
	}
}

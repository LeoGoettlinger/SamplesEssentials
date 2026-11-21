package com.magmaguy.elitemobs;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MetadataHandler implements Listener {
   public static final String ELITE_MOBS = "EliteMobs";
   public static final String BETTERDROPS_COMPATIBILITY_MD = "betterdrops_ignore";
   public static JavaPlugin PLUGIN;
   public static volatile PluginState pluginState = PluginState.UNINITIALIZED;
   public static volatile boolean shutdownRequested = false;
   public static volatile CommandSender pendingReloadSender = null;
   public static int signatureID = 31173;
}

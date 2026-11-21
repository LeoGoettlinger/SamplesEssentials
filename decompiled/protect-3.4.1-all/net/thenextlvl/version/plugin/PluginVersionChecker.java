package net.thenextlvl.version.plugin;

import org.bukkit.plugin.Plugin;

public interface PluginVersionChecker {
   Plugin getPlugin();

   void checkLatestVersion();

   void checkVersion();
}

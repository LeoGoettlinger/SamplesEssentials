package me.lucko.luckperms.bukkit.loader;

import me.lucko.luckperms.common.loader.JarInJarClassLoader;
import me.lucko.luckperms.common.loader.LoaderBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitLoaderPlugin extends JavaPlugin {
   private int dependencyHooks;
   private static final String JAR_NAME = "luckperms-bukkit.jarinjar";
   private static final String BOOTSTRAP_CLASS = "me.lucko.luckperms.bukkit.LPBukkitBootstrap";
   private final LoaderBootstrap plugin;

   public BukkitLoaderPlugin() {
      JarInJarClassLoader loader = new JarInJarClassLoader(this.getClass().getClassLoader(), "luckperms-bukkit.jarinjar");
      this.plugin = loader.instantiatePlugin("me.lucko.luckperms.bukkit.LPBukkitBootstrap", JavaPlugin.class, this);
   }

   public void onLoad() {
      this.plugin.onLoad();
   }

   public void onEnable() {
      this.dependencyHooks = this.setupDependencyHooks();
      this.plugin.onEnable();
   }

   public void onDisable() {
      this.plugin.onDisable();
   }
}

package dev.demora.demoralegendaryweapons.version.modules;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import dev.demora.demoralegendaryweapons.util.MinecraftVersion;
import dev.demora.demoralegendaryweapons.version.VersionModule;

public class NextGenVersionModule implements VersionModule {
   private static final int TARGET_MINOR = 26;
   private final DemoraLegendaryWeapons plugin;

   public NextGenVersionModule(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "Minecraft 1.26+";
   }

   @Override
   public boolean appliesTo(MinecraftVersion version) {
      return version.isAtLeast(26);
   }

   @Override
   public void enable() {
      this.plugin.getLogger().info("for (1.26+).");
      this.runVersionScripts();
   }

   private void runVersionScripts() {
   }
}

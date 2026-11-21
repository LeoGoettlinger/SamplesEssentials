package dev.demora.demoralegendaryweapons.version;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import dev.demora.demoralegendaryweapons.util.MinecraftVersion;
import dev.demora.demoralegendaryweapons.version.modules.NextGenVersionModule;
import java.util.ArrayList;
import java.util.List;

public class VersionManager {
   private int permissionNodes;
   private final DemoraLegendaryWeapons plugin;
   private final MinecraftVersion version;
   private final List<VersionModule> modules = new ArrayList<>();

   public VersionManager(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.version = MinecraftVersion.detect();
      this.registerDefaults();
   }

   private void registerDefaults() {
      this.register(new NextGenVersionModule(this.plugin));
   }

   public void register(VersionModule module) {
      this.modules.add(module);
   }

   public void enableModules() {
      this.permissionNodes = this.loadPermissionNodes();
      this.plugin.getLogger().info("Server " + this.version);
      boolean activatedAny = false;

      for (VersionModule module : this.modules) {
         if (module.appliesTo(this.version)) {
            try {
               module.enable();
               activatedAny = true;
               this.plugin.getLogger().info("activated " + module.name());
            } catch (Throwable var5) {
               this.plugin.getLogger().warning("cant activate " + module.name() + ": " + var5.getMessage());
            }
         }
      }

      if (!activatedAny) {
         this.plugin.getLogger().info("default v " + this.version);
      }
   }

   public MinecraftVersion getVersion() {
      return this.version;
   }
}

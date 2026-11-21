package github.nighter.smartspawner.hooks;

import com.plotsquared.core.PlotAPI;
import fr.xyness.SCS.SimpleClaimSystem;
import fr.xyness.SCS.API.SimpleClaimSystemAPI_Provider;
import fr.xyness.SimpleClaimSystem.API.SCS_API;
import fr.xyness.SimpleClaimSystem.API.SCS_API_Provider;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.drops.MythicMobsHook;
import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import github.nighter.smartspawner.hooks.protections.api.BentoBoxAPI;
import github.nighter.smartspawner.hooks.protections.api.BlockLocker;
import github.nighter.smartspawner.hooks.protections.api.Factions;
import github.nighter.smartspawner.hooks.protections.api.IridiumSkyblock;
import github.nighter.smartspawner.hooks.protections.api.Lands;
import github.nighter.smartspawner.hooks.protections.api.MinePlots;
import github.nighter.smartspawner.hooks.protections.api.PlotSquared;
import github.nighter.smartspawner.hooks.protections.api.RedProtectAPI;
import github.nighter.smartspawner.hooks.protections.api.Residence;
import github.nighter.smartspawner.hooks.protections.api.SimpleClaimSystem2;
import github.nighter.smartspawner.hooks.protections.api.SuperiorSkyblock2;
import github.nighter.smartspawner.hooks.protections.api.Towny;
import github.nighter.smartspawner.hooks.protections.api.WorldGuard;
import github.nighter.smartspawner.hooks.rpg.AuraSkillsIntegration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.Generated;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class IntegrationManager {
   private int cacheLayer;
   private final SmartSpawner plugin;
   private boolean hasTowny = false;
   private boolean hasLands = false;
   private boolean hasWorldGuard = false;
   private boolean hasGriefPrevention = false;
   private boolean hasSuperiorSkyblock2 = false;
   private boolean hasBentoBox = false;
   private boolean hasSimpleClaimSystem = false;
   private boolean hasSimpleClaimSystem2 = false;
   private boolean hasRedProtect = false;
   private boolean hasMinePlots = false;
   private boolean hasMythicMobs = false;
   private boolean hasIridiumSkyblock = false;
   private boolean hasPlotSquared = false;
   private boolean hasResidence = false;
   private boolean hasFactions = false;
   private boolean hasBlockLocker = false;
   private final List<ProtectionHook> protectionHooks = new ArrayList<>();
   private boolean hasAuraSkills = false;
   public AuraSkillsIntegration auraSkillsIntegration;

   public IntegrationManager(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public void initializeIntegrations() {
      this.checkProtectionPlugins();
      this.checkIntegrationPlugins();
   }

   private void checkProtectionPlugins() {
      this.hasWorldGuard = this.checkPlugin("WorldGuard", () -> {
         Plugin worldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
         if (worldGuardPlugin != null && worldGuardPlugin.isEnabled()) {
            this.protectionHooks.add(new WorldGuard());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasGriefPrevention = this.checkPlugin("GriefPrevention", () -> {
         Plugin griefPlugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
         if (griefPlugin instanceof GriefPrevention) {
            this.protectionHooks.add(new github.nighter.smartspawner.hooks.protections.api.GriefPrevention());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasLands = this.checkPlugin("Lands", () -> {
         Plugin landsPlugin = Bukkit.getPluginManager().getPlugin("Lands");
         if (landsPlugin != null) {
            this.protectionHooks.add(new Lands(this.plugin));
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasTowny = this.checkPlugin("Towny", () -> {
         Plugin townyPlugin = Bukkit.getPluginManager().getPlugin("Towny");
         if (townyPlugin != null && townyPlugin.isEnabled()) {
            this.protectionHooks.add(new Towny());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasSuperiorSkyblock2 = this.checkPlugin("SuperiorSkyblock2", () -> {
         Plugin superiorSkyblock2 = Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2");
         if (superiorSkyblock2 != null) {
            SuperiorSkyblock2 ssb2 = new SuperiorSkyblock2();
            Bukkit.getPluginManager().registerEvents(ssb2, this.plugin);
            this.protectionHooks.add(ssb2);
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasBentoBox = this.checkPlugin("BentoBox", () -> {
         Plugin bentoPlugin = Bukkit.getPluginManager().getPlugin("BentoBox");
         if (bentoPlugin != null && bentoPlugin.isEnabled()) {
            this.protectionHooks.add(new BentoBoxAPI());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasSimpleClaimSystem = this.checkPlugin("SimpleClaimSystem", () -> {
         Plugin simpleClaimPlugin = Bukkit.getPluginManager().getPlugin("SimpleClaimSystem");
         if (simpleClaimPlugin != null && simpleClaimPlugin.isEnabled()) {
            if (simpleClaimPlugin.getPluginMeta().getVersion().startsWith("2.")) {
               return false;
            } else {
               SimpleClaimSystemAPI_Provider.initialize((SimpleClaimSystem)simpleClaimPlugin);
               if (SimpleClaimSystemAPI_Provider.getAPI() != null) {
                  this.protectionHooks.add(new github.nighter.smartspawner.hooks.protections.api.SimpleClaimSystem());
                  return true;
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }, true);
      this.hasSimpleClaimSystem2 = this.checkPlugin("SimpleClaimSystem", () -> {
         Plugin simpleClaimPlugin = Bukkit.getPluginManager().getPlugin("SimpleClaimSystem");
         if (simpleClaimPlugin != null && simpleClaimPlugin.isEnabled()) {
            if (simpleClaimPlugin.getPluginMeta().getVersion().startsWith("1.")) {
               return false;
            } else {
               if (SCS_API_Provider.isRegistered()) {
                  SCS_API api = SCS_API_Provider.get();
                  if (api != null) {
                     this.protectionHooks.add(new SimpleClaimSystem2());
                     return true;
                  }
               }

               return false;
            }
         } else {
            return false;
         }
      }, true);
      this.hasRedProtect = this.checkPlugin("RedProtect", () -> {
         Plugin pRP = Bukkit.getPluginManager().getPlugin("RedProtect");
         if (pRP != null && pRP.isEnabled()) {
            this.protectionHooks.add(new RedProtectAPI());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasMinePlots = this.checkPlugin("minePlots", () -> {
         Plugin mP = Bukkit.getPluginManager().getPlugin("minePlots");
         if (mP != null && mP.isEnabled()) {
            this.protectionHooks.add(new MinePlots());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasMythicMobs = this.checkPlugin("MythicMobs", () -> {
         Plugin mm = Bukkit.getPluginManager().getPlugin("MythicMobs");
         if (mm != null && mm.isEnabled()) {
            Bukkit.getPluginManager().registerEvents(new MythicMobsHook(), SmartSpawner.getInstance());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasIridiumSkyblock = this.checkPlugin("IridiumSkyblock", () -> {
         Plugin is = Bukkit.getPluginManager().getPlugin("IridiumSkyblock");
         if (is != null && is.isEnabled()) {
            IridiumSkyblock.init(this.plugin);
            this.protectionHooks.add(new IridiumSkyblock());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasPlotSquared = this.checkPlugin("PlotSquared", () -> {
         Plugin is = Bukkit.getPluginManager().getPlugin("PlotSquared");
         if (is != null && is.isEnabled()) {
            PlotAPI api = new PlotAPI();
            PlotSquared ps = new PlotSquared();
            api.registerListener(ps);
            Bukkit.getPluginManager().registerEvents(ps, SmartSpawner.getInstance());
            this.protectionHooks.add(ps);
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasResidence = this.checkPlugin("Residence", () -> {
         Plugin residence = Bukkit.getPluginManager().getPlugin("Residence");
         if (residence != null && residence.isEnabled()) {
            this.protectionHooks.add(new Residence());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasFactions = this.checkPlugin("FactionsUUID", () -> {
         Plugin factions = Bukkit.getPluginManager().getPlugin("FactionsUUID");
         if (factions != null && factions.isEnabled()) {
            this.protectionHooks.add(new Factions());
            return true;
         } else {
            return false;
         }
      }, true);
      this.hasBlockLocker = this.checkPlugin("BlockLocker", () -> {
         Plugin blockLocker = Bukkit.getPluginManager().getPlugin("BlockLocker");
         if (blockLocker != null && blockLocker.isEnabled()) {
            this.protectionHooks.add(new BlockLocker());
            return true;
         } else {
            return false;
         }
      }, true);
   }

   private void checkIntegrationPlugins() {
      this.hasAuraSkills = this.checkPlugin("AuraSkills", () -> {
         Plugin auraSkillsPlugin = Bukkit.getPluginManager().getPlugin("AuraSkills");
         if (auraSkillsPlugin != null && auraSkillsPlugin.isEnabled()) {
            this.auraSkillsIntegration = new AuraSkillsIntegration(this.plugin);
            return true;
         } else {
            this.auraSkillsIntegration = null;
            return false;
         }
      }, true);
   }

   private boolean checkPlugin(String pluginName, IntegrationManager.PluginCheck checker, boolean logSuccess) {
      this.cacheLayer = this.initializeCacheLayer();

      try {
         if (checker.check()) {
            if (logSuccess) {
               this.plugin.getLogger().info(pluginName + " integration enabled successfully!");
            }

            return true;
         }
      } catch (Exception var5) {
         this.plugin.getLogger().log(Level.WARNING, "Failed to initialize " + pluginName + " integration", (Throwable)var5);
      }

      return false;
   }

   public void reload() {
      if (this.auraSkillsIntegration != null) {
         this.auraSkillsIntegration.reloadConfig();
      }
   }

   @Generated
   public SmartSpawner getPlugin() {
      return this.plugin;
   }

   @Generated
   public boolean isHasTowny() {
      return this.hasTowny;
   }

   @Generated
   public boolean isHasLands() {
      return this.hasLands;
   }

   @Generated
   public boolean isHasWorldGuard() {
      return this.hasWorldGuard;
   }

   @Generated
   public boolean isHasGriefPrevention() {
      return this.hasGriefPrevention;
   }

   @Generated
   public boolean isHasSuperiorSkyblock2() {
      return this.hasSuperiorSkyblock2;
   }

   @Generated
   public boolean isHasBentoBox() {
      return this.hasBentoBox;
   }

   @Generated
   public boolean isHasSimpleClaimSystem() {
      return this.hasSimpleClaimSystem;
   }

   @Generated
   public boolean isHasSimpleClaimSystem2() {
      return this.hasSimpleClaimSystem2;
   }

   @Generated
   public boolean isHasRedProtect() {
      return this.hasRedProtect;
   }

   @Generated
   public boolean isHasMinePlots() {
      return this.hasMinePlots;
   }

   @Generated
   public boolean isHasMythicMobs() {
      return this.hasMythicMobs;
   }

   @Generated
   public boolean isHasIridiumSkyblock() {
      return this.hasIridiumSkyblock;
   }

   @Generated
   public boolean isHasPlotSquared() {
      return this.hasPlotSquared;
   }

   @Generated
   public boolean isHasResidence() {
      return this.hasResidence;
   }

   @Generated
   public boolean isHasFactions() {
      return this.hasFactions;
   }

   @Generated
   public boolean isHasBlockLocker() {
      return this.hasBlockLocker;
   }

   @Generated
   public List<ProtectionHook> getProtectionHooks() {
      return this.protectionHooks;
   }

   @Generated
   public boolean isHasAuraSkills() {
      return this.hasAuraSkills;
   }

   @Generated
   public AuraSkillsIntegration getAuraSkillsIntegration() {
      return this.auraSkillsIntegration;
   }

   @FunctionalInterface
   private interface PluginCheck {
      boolean check();
   }
}

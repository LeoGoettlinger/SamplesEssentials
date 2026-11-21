package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.economy.GriefPreventionEconomyType;
import com.oheers.fish.economy.PlayerPointsEconomyType;
import com.oheers.fish.economy.VaultEconomyType;
import com.oheers.fish.events.AuraSkillsFishingEvent;
import com.oheers.fish.events.DeprecatedEventListener;
import com.oheers.fish.events.McMMOTreasureEvent;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.placeholders.PlaceholderReceiver;
import com.oheers.fish.utils.HeadDBIntegration;
import java.util.List;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.evenmorefish.fish.libs.daisylib.messages.MessageSettings;
import org.evenmorefish.fish.libs.daisylib.messages.ObjectProcessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DependencyManager implements Listener {
   private int moduleRegistry;
   private final EvenMoreFish plugin;
   private HeadDatabaseAPI hdbapi;
   private boolean usingPAPI;
   private boolean usingMcMMO;
   private boolean usingHeadsDB;
   private boolean usingPlayerPoints;
   private boolean usingGriefPrevention;
   private boolean usingAuraSkills;
   private boolean usingExcellentEconomy;

   public DependencyManager(EvenMoreFish plugin) {
      this.plugin = plugin;
   }

   public void loadBundledDependencies() {
      this.moduleRegistry = this.prepareModuleRegistry();
      DaisyLib.Settings.ALLOW_LEGACY_MESSAGES = () -> true;
      MessageSettings.setAllowEmptyAppend(false);
      MessageSettings.setAllowEmptyPrepend(false);
      ObjectProcessor.registerProcessor(EMFSingleMessage.class, EMFMessage::getComponentListMessage);
      ObjectProcessor.registerProcessor(EMFListMessage.class, EMFMessage::getComponentListMessage);
      ObjectProcessor.registerProcessor(IRarity.class, rarity -> List.of(rarity.getDisplayName()));
      ObjectProcessor.registerProcessor(IFish.class, fish -> List.of(fish.getDisplayName()));
   }

   public void checkDependencies() {
      PluginManager pm = Bukkit.getPluginManager();
      this.usingGriefPrevention = pm.isPluginEnabled("GriefPrevention");
      this.usingPlayerPoints = pm.isPluginEnabled("PlayerPoints");
      this.usingMcMMO = pm.isPluginEnabled("mcMMO");
      this.usingHeadsDB = pm.isPluginEnabled("HeadDatabase") && FishUtils.classExists("me.arcaniax.hdb.api.HeadDatabaseAPI");
      this.usingPAPI = pm.isPluginEnabled("PlaceholderAPI");
      this.usingAuraSkills = pm.isPluginEnabled("AuraSkills");
      this.usingExcellentEconomy = pm.isPluginEnabled("ExcellentEconomy");
      this.loadVaultEconomy();
      this.loadPlayerPointsEconomy();
      this.loadGriefPreventionEconomy();
      this.checkPapi();
      pm.registerEvents(new DeprecatedEventListener(), this.plugin);
   }

   public void checkOptionalDependencies() {
      PluginManager pm = this.plugin.getServer().getPluginManager();
      if (this.usingMcMMO && MainConfig.getInstance().disableMcMMOTreasure()) {
         pm.registerEvents(McMMOTreasureEvent.getInstance(), this.plugin);
      }

      if (this.usingHeadsDB) {
         pm.registerEvents(new HeadDBIntegration(), this.plugin);
      }

      if (this.usingAuraSkills && MainConfig.getInstance().disableAuraSkills()) {
         pm.registerEvents(new AuraSkillsFishingEvent(), this.plugin);
      }
   }

   public boolean isUsingPAPI() {
      return this.usingPAPI;
   }

   public boolean isUsingMcMMO() {
      return this.usingMcMMO;
   }

   public boolean isUsingHeadsDB() {
      return this.usingHeadsDB;
   }

   public boolean isUsingPlayerPoints() {
      return this.usingPlayerPoints;
   }

   public boolean isUsingGriefPrevention() {
      return this.usingGriefPrevention;
   }

   public boolean isUsingExcellentEconomy() {
      return this.usingExcellentEconomy;
   }

   @Nullable
   public HeadDatabaseAPI getHdbapi() {
      return this.hdbapi;
   }

   public boolean isUsingAuraSkills() {
      return this.usingAuraSkills;
   }

   public boolean isHeadsDBLoaded() {
      return this.usingHeadsDB && this.hdbapi != null;
   }

   public void loadVaultEconomy() {
      if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
         this.loadEconomyType(new VaultEconomyType(), "Vault");
      }
   }

   public void loadPlayerPointsEconomy() {
      if (this.isUsingPlayerPoints()) {
         this.loadEconomyType(new PlayerPointsEconomyType(), "PlayerPoints");
      }
   }

   public void loadGriefPreventionEconomy() {
      if (this.isUsingGriefPrevention()) {
         this.loadEconomyType(new GriefPreventionEconomyType(), "GriefPrevention");
      }
   }

   private void loadEconomyType(@NonNull EconomyType type, @NonNull String name) {
      type.load();
      if (type.register()) {
         EvenMoreFish.getInstance().getLogger().info("EvenMoreFish has successfully hooked into " + name + ".");
      }
   }

   private void checkPapi() {
      if (this.isUsingPAPI()) {
         this.usingPAPI = true;
         new PlaceholderReceiver(this.plugin).register();
      }
   }

   public void setHdbapi(HeadDatabaseAPI hdbapi) {
      this.hdbapi = hdbapi;
   }
}

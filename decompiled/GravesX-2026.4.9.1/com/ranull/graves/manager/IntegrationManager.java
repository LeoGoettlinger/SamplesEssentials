package com.ranull.graves.manager;

import com.becerritoo.GravesX.integration.BagOfGoldIntegration;
import com.ranull.graves.Graves;
import com.ranull.graves.integration.ChestSort;
import com.ranull.graves.integration.FurnitureEngine;
import com.ranull.graves.integration.FurnitureLib;
import com.ranull.graves.integration.ItemBridge;
import com.ranull.graves.integration.MineDown;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.integration.MultiPaper;
import com.ranull.graves.integration.Oraxen;
import com.ranull.graves.integration.PlaceholderAPI;
import com.ranull.graves.integration.PlayerNPC;
import com.ranull.graves.integration.ProtectionLib;
import com.ranull.graves.integration.ProtocolLib;
import com.ranull.graves.integration.Vault;
import dev.cwhead.GravesX.compatibility.CompatibilityGameRule;
import dev.cwhead.GravesX.integration.CoreProtectIntegration;
import dev.cwhead.GravesX.integration.CraftEngine;
import dev.cwhead.GravesX.integration.Floodgate;
import dev.cwhead.GravesX.integration.ItemsAdder;
import dev.cwhead.GravesX.integration.LuckPermsHandler;
import dev.cwhead.GravesX.integration.Mannequins;
import dev.cwhead.GravesX.integration.Nexo;
import dev.cwhead.GravesX.integration.NoteBlockAPI;
import dev.cwhead.GravesX.integration.SkriptImpl;
import dev.cwhead.GravesX.listener.integration.coreprotect.CoreProtectListener;
import dev.cwhead.GravesX.listener.integration.itemsadder.CustomBlockBreakListener;
import dev.cwhead.GravesX.listener.integration.itemsadder.FurnitureBreakListener;
import dev.cwhead.GravesX.listener.integration.itemsadder.ItemsAdderDisableListener;
import dev.cwhead.GravesX.listener.integration.itemsadder.ItemsAdderLoadListener;
import dev.cwhead.GravesX.listener.integration.itemsadder.ItemsAdderReloadGateListener;
import dev.cwhead.GravesX.provider.CustomItemStorageProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jay.GravesX.integration.FancyNPCs;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;

public class IntegrationManager {
   private final Graves plugin;
   private MultiPaper multiPaper;
   private Vault vault;
   private ProtocolLib protocolLib;
   private FurnitureLib furnitureLib;
   @Deprecated
   private FurnitureEngine furnitureEngine;
   private ProtectionLib protectionLib;
   private ItemsAdder itemsAdder;
   private Oraxen oraxen;
   private Nexo nexo;
   private CraftEngine craftEngine;
   private ChestSort chestSort;
   private MiniMessage miniMessage;
   private MineDown mineDown;
   private ItemBridge itemBridge;
   private Floodgate floodgate;
   private FancyNPCs fancyNpcs;
   private PlayerNPC playerNPC;
   private PlaceholderAPI placeholderAPI;
   private SkriptImpl skriptImpl;
   private Mannequins mannequins;
   private boolean hasVaultPermissions;
   private LuckPermsHandler luckPermsHandler;
   private boolean hasMannequins;
   @Deprecated
   private CoreProtectIntegration coreProtectIntegration;
   private NoteBlockAPI noteBlockAPI;
   private BagOfGoldIntegration bagOfGold;
   private final List<CustomItemStorageProvider> customItemStorageProviders = new ArrayList<>();

   public IntegrationManager(Graves plugin) {
      this.plugin = plugin;
   }

   public void reload() {
      this.unload();
      this.load();
   }

   public void load() {
      this.loadMultiPaper();
      this.loadVault();
      this.loadProtocolLib();
      this.loadFurnitureLib();
      this.loadFurnitureEngine();
      this.loadProtectionLib();
      this.loadItemsAdder();
      this.loadOraxen();
      this.loadNexo();
      this.loadCraftEngine();
      this.loadMiniMessage();
      this.loadMineDown();
      this.loadChestSort();
      this.loadPlayerNPC();
      this.loadItemBridge();
      this.loadPlaceholderAPI();
      this.loadCompatibilityWarnings();
      this.loadLuckPerms();
      this.loadCoreProtect();
      this.loadNBTAPI();
      this.loadBedrockSupport();
      this.loadFancyNpcs();
      this.loadNoteblockAPI();
      this.loadMannequins();
      this.loadBagOfGold();
   }

   public void loadNoReload() {
      this.loadSkript();
   }

   public void unloadNoReload() {
      if (this.skriptImpl != null) {
         this.skriptImpl = null;
      }
   }

   public void unload() {
      if (this.furnitureLib != null) {
         this.furnitureLib.unregisterListeners();
      }

      if (this.furnitureEngine != null) {
         this.furnitureEngine.unregisterListeners();
      }

      if (this.oraxen != null) {
         this.oraxen.unregisterListeners();
      }

      if (this.nexo != null) {
         this.nexo.unregisterListeners();
      }

      if (this.craftEngine != null) {
         this.craftEngine = null;
      }

      if (this.placeholderAPI != null) {
         try {
            this.placeholderAPI.unregister();
         } catch (Throwable var5) {
            this.plugin.getLogger().warning("Skipping PlaceholderAPI unregister due to late shutdown state: " + var5.getMessage());
         } finally {
            this.placeholderAPI = null;
         }
      }

      if (this.playerNPC != null) {
         this.playerNPC.unregisterListeners();
      }

      if (this.fancyNpcs != null) {
         this.fancyNpcs.unregisterListeners();
      }

      if (this.bagOfGold != null) {
         this.bagOfGold = null;
      }
   }

   public MultiPaper getMultiPaper() {
      return this.multiPaper;
   }

   public Vault getVault() {
      return this.vault;
   }

   public ProtocolLib getProtocolLib() {
      return this.protocolLib;
   }

   @Deprecated
   public CoreProtectIntegration getCoreProtect() {
      return this.coreProtectIntegration;
   }

   public FurnitureLib getFurnitureLib() {
      return this.furnitureLib;
   }

   @Deprecated
   public FurnitureEngine getFurnitureEngine() {
      return this.furnitureEngine;
   }

   public ProtectionLib getProtectionLib() {
      return this.protectionLib;
   }

   public ItemsAdder getItemsAdder() {
      return this.itemsAdder;
   }

   @Deprecated
   public Oraxen getOraxen() {
      return this.oraxen;
   }

   public Nexo getNexo() {
      return this.nexo;
   }

   public CraftEngine getCraftEngine() {
      return this.craftEngine;
   }

   public MiniMessage getMiniMessage() {
      return this.miniMessage;
   }

   public MineDown getMineDown() {
      return this.mineDown;
   }

   public ChestSort getChestSort() {
      return this.chestSort;
   }

   public PlayerNPC getPlayerNPC() {
      return this.playerNPC;
   }

   public NoteBlockAPI getNoteBlockAPI() {
      return this.noteBlockAPI;
   }

   public SkriptAddon getSkript() {
      return this.skriptImpl != null ? this.skriptImpl.getSkriptAddon() : null;
   }

   public BagOfGoldIntegration getBagOfGold() {
      return this.bagOfGold;
   }

   public boolean hasFloodgate() {
      return this.floodgate != null;
   }

   public Floodgate getFloodgate() {
      return this.floodgate;
   }

   public FancyNPCs getFancyNpcs() {
      return this.fancyNpcs;
   }

   public LuckPermsHandler getLuckPermsHandler() {
      return this.luckPermsHandler;
   }

   public Mannequins getMannequins() {
      return this.mannequins;
   }

   public boolean hasMannequins() {
      return this.mannequins != null;
   }

   public boolean hasMultiPaper() {
      return this.multiPaper != null;
   }

   public boolean hasVault() {
      return this.vault != null;
   }

   public boolean hasVaultPermProvider() {
      return this.hasVaultPermissions;
   }

   public boolean hasProtocolLib() {
      return this.protocolLib != null;
   }

   @Deprecated
   public boolean hasCoreProtect() {
      return this.coreProtectIntegration != null;
   }

   public boolean hasFurnitureLib() {
      return this.furnitureLib != null;
   }

   @Deprecated
   public boolean hasFurnitureEngine() {
      return this.furnitureEngine != null;
   }

   public boolean hasProtectionLib() {
      return this.protectionLib != null;
   }

   public boolean hasItemsAdder() {
      return this.itemsAdder != null;
   }

   @Deprecated
   public boolean hasOraxen() {
      return this.oraxen != null;
   }

   public boolean hasNexo() {
      return this.nexo != null;
   }

   public boolean hasCraftEngine() {
      return this.craftEngine != null;
   }

   public boolean hasMiniMessage() {
      return this.miniMessage != null;
   }

   public boolean hasMineDown() {
      return this.mineDown != null;
   }

   public boolean hasChestSort() {
      return this.chestSort != null;
   }

   public boolean hasPlayerNPC() {
      return this.playerNPC != null;
   }

   public boolean hasPlaceholderAPI() {
      return this.placeholderAPI != null;
   }

   public boolean hasSkript() {
      return this.skriptImpl != null;
   }

   public boolean hasLuckPermsHandler() {
      return this.luckPermsHandler != null;
   }

   public boolean hasFancyNpcs() {
      return this.fancyNpcs != null;
   }

   public boolean hasNoteBlockAPI() {
      return this.noteBlockAPI != null;
   }

   public boolean hasBagOfGold() {
      return this.bagOfGold != null;
   }

   private void loadMultiPaper() {
      if (this.plugin.getConfig().getBoolean("settings.integration.multipaper.enabled", true)) {
         try {
            Class.forName("puregero.multipaper.MultiPaper", false, this.getClass().getClassLoader());
            this.multiPaper = new MultiPaper(this.plugin);
            this.plugin.infoMessage("MultiPaper detected, enabling MultiLib.");
         } catch (ClassNotFoundException var2) {
         }
      } else {
         this.multiPaper = null;
      }
   }

   private void loadVault() {
      if (this.plugin.getConfig().getBoolean("settings.integration.vault.enabled", true)) {
         Plugin vaultPlugin = this.plugin.getServer().getPluginManager().getPlugin("Vault");
         if (vaultPlugin != null && vaultPlugin.isEnabled()) {
            this.handleVaultIntegration(vaultPlugin);
         } else {
            this.resetVaultIntegration();
         }
      } else {
         this.resetVaultIntegration();
      }
   }

   private void handleVaultIntegration(Plugin vaultPlugin) {
      RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
      if (permissionProvider != null) {
         this.enablePermissionsOnlyVaultIntegration(vaultPlugin, permissionProvider);
      } else {
         this.disableVaultIntegration(vaultPlugin);
      }
   }

   private void enablePermissionsOnlyVaultIntegration(Plugin vaultPlugin, RegisteredServiceProvider<Permission> permissionProvider) {
      Permission permission = (Permission)permissionProvider.getProvider();
      this.vault = new Vault(permission);
      this.hasVaultPermissions = true;
      this.plugin.integrationMessage("Hooked into " + vaultPlugin.getName() + " " + vaultPlugin.getDescription().getVersion() + "'s permissions provider.");
   }

   private void disableVaultIntegration(Plugin vaultPlugin) {
      this.vault = null;
      this.hasVaultPermissions = false;
      this.plugin
         .integrationMessage(
            "Failed to hook into "
               + vaultPlugin.getName()
               + " "
               + vaultPlugin.getDescription().getVersion()
               + "'s permissions provider. Vault will not be used as a Permissions Provider.",
            "severe"
         );
   }

   private void resetVaultIntegration() {
      this.vault = null;
      this.hasVaultPermissions = false;
   }

   private void loadProtocolLib() {
      if (this.plugin.getConfig().getBoolean("settings.integration.protocollib.enabled", true)) {
         Plugin protocolLibPlugin = this.plugin.getServer().getPluginManager().getPlugin("ProtocolLib");
         if (protocolLibPlugin != null && protocolLibPlugin.isEnabled()) {
            this.protocolLib = new ProtocolLib(this.plugin);
            this.plugin.integrationMessage("Hooked into " + protocolLibPlugin.getName() + " " + protocolLibPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.protocolLib = null;
      }
   }

   @Deprecated
   private void loadCoreProtect() {
      if (this.plugin.getConfig().getBoolean("settings.integration.coreprotect.enabled", true)) {
         Plugin coreProtectPlugin = this.plugin.getServer().getPluginManager().getPlugin("CoreProtect");
         if (coreProtectPlugin != null && coreProtectPlugin.isEnabled()) {
            try {
               this.coreProtectIntegration = new CoreProtectIntegration(this.plugin);
               this.plugin.getServer().getPluginManager().registerEvents(new CoreProtectListener(this.plugin), this.plugin);
               this.plugin.integrationMessage("Hooked into " + coreProtectPlugin.getName() + " " + coreProtectPlugin.getDescription().getVersion() + ".");
            } catch (Exception var3) {
               this.coreProtectIntegration = null;
               this.plugin
                  .integrationMessage(
                     "Failed to hook into "
                        + coreProtectPlugin.getName()
                        + " "
                        + coreProtectPlugin.getDescription().getVersion()
                        + ". Is CoreProtect installed and enabled?",
                     "severe"
                  );
               this.plugin.logStackTrace(var3);
            }
         }
      } else {
         this.coreProtectIntegration = null;
      }
   }

   private void loadFurnitureLib() {
      if (this.plugin.getConfig().getBoolean("settings.integration.furniturelib.enabled", true)) {
         Plugin furnitureLibPlugin = this.plugin.getServer().getPluginManager().getPlugin("FurnitureLib");
         if (furnitureLibPlugin != null && furnitureLibPlugin.isEnabled()) {
            this.furnitureLib = new FurnitureLib(this.plugin);
            this.plugin.integrationMessage("Hooked into " + furnitureLibPlugin.getName() + " " + furnitureLibPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.furnitureLib = null;
      }
   }

   @Deprecated
   private void loadFurnitureEngine() {
      if (this.plugin.getConfig().getBoolean("settings.integration.furnitureengine.enabled", true)) {
         Plugin furnitureEnginePlugin = this.plugin.getServer().getPluginManager().getPlugin("FurnitureEngine");
         if (furnitureEnginePlugin != null && furnitureEnginePlugin.isEnabled()) {
            try {
               Class.forName("com.mira.furnitureengine.furniture.FurnitureManager", false, this.getClass().getClassLoader());
               this.furnitureEngine = new FurnitureEngine(this.plugin);
               this.plugin
                  .integrationMessage("Hooked into " + furnitureEnginePlugin.getName() + " " + furnitureEnginePlugin.getDescription().getVersion() + ".");
            } catch (ClassNotFoundException var3) {
               this.plugin
                  .integrationMessage(
                     furnitureEnginePlugin.getName()
                        + " "
                        + furnitureEnginePlugin.getDescription().getVersion()
                        + " detected, but FurnitureManager class not found, disabling integration.",
                     "severe"
                  );
            }
         }
      } else {
         this.furnitureEngine = null;
      }
   }

   private void loadProtectionLib() {
      if (this.plugin.getConfig().getBoolean("settings.integration.protectionlib.enabled", true)) {
         Plugin protectionLibPlugin = this.plugin.getServer().getPluginManager().getPlugin("ProtectionLib");
         if (protectionLibPlugin != null && protectionLibPlugin.isEnabled()) {
            this.protectionLib = new ProtectionLib(this.plugin, protectionLibPlugin);
            this.plugin.integrationMessage("Hooked into " + protectionLibPlugin.getName() + " " + protectionLibPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.protectionLib = null;
      }
   }

   private void loadItemsAdder() {
      if (this.plugin.getConfig().getBoolean("settings.integration.itemsadder.enabled", true)) {
         Plugin itemsAdderPlugin = this.plugin.getServer().getPluginManager().getPlugin("ItemsAdder");
         if (itemsAdderPlugin != null && itemsAdderPlugin.isEnabled()) {
            this.itemsAdder = new ItemsAdder(this.plugin);
            this.plugin.getServer().getPluginManager().registerEvents(new FurnitureBreakListener(this.plugin), this.plugin);
            this.plugin.getServer().getPluginManager().registerEvents(new CustomBlockBreakListener(this.plugin), this.plugin);
            this.plugin.getServer().getPluginManager().registerEvents(new ItemsAdderLoadListener(this.plugin, this.itemsAdder), this.plugin);
            this.plugin.getServer().getPluginManager().registerEvents(new ItemsAdderReloadGateListener(this.plugin, this.itemsAdder), this.plugin);
            this.plugin.getServer().getPluginManager().registerEvents(new ItemsAdderDisableListener(this.plugin, this.itemsAdder), this.plugin);
            this.plugin.integrationMessage("Hooked into " + itemsAdderPlugin.getName() + " " + itemsAdderPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.itemsAdder = null;
      }
   }

   @Deprecated
   private void loadOraxen() {
      if (this.plugin.getConfig().getBoolean("settings.integration.oraxen.enabled", true)) {
         Plugin oraxenPlugin = this.plugin.getServer().getPluginManager().getPlugin("Oraxen");
         if (oraxenPlugin != null && oraxenPlugin.isEnabled()) {
            this.oraxen = new Oraxen(this.plugin, oraxenPlugin);
            this.plugin.integrationMessage("Hooked into " + oraxenPlugin.getName() + " " + oraxenPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.oraxen = null;
      }
   }

   private void loadNexo() {
      if (this.plugin.getConfig().getBoolean("settings.integration.nexo.enabled", true)) {
         Plugin nexoPlugin = this.plugin.getServer().getPluginManager().getPlugin("Nexo");
         if (nexoPlugin != null && nexoPlugin.isEnabled()) {
            String version = nexoPlugin.getDescription().getVersion();
            if (isVersionAtLeast(version, "1.5")) {
               this.nexo = new Nexo(this.plugin, nexoPlugin);
               this.plugin.integrationMessage("Hooked into " + nexoPlugin.getName() + " " + version + ".");
            } else {
               this.nexo = null;
               this.plugin.integrationMessage("Failed to hook into " + nexoPlugin.getName() + " " + version + ". You must be on nexo 1.5 or newer.", "warn");
            }
         }
      } else {
         this.nexo = null;
      }
   }

   private void loadCraftEngine() {
      if (this.plugin.getConfig().getBoolean("settings.integration.craftengine.enabled", true)) {
         Plugin craftEnginePlugin = this.plugin.getServer().getPluginManager().getPlugin("CraftEngine");
         if (craftEnginePlugin != null && craftEnginePlugin.isEnabled()) {
            try {
               this.craftEngine = new CraftEngine(this.plugin, craftEnginePlugin);
               this.plugin.integrationMessage("Hooked into " + craftEnginePlugin.getName() + " " + craftEnginePlugin.getDescription().getVersion() + ".");
            } catch (Throwable var3) {
               this.craftEngine = null;
               this.plugin
                  .integrationMessage(
                     "Failed to hook into " + craftEnginePlugin.getName() + " " + craftEnginePlugin.getDescription().getVersion() + ": " + var3.getMessage(),
                     "warn"
                  );
            }
         }
      } else {
         this.craftEngine = null;
      }
   }

   private void loadMiniMessage() {
      if (this.plugin.getConfig().getBoolean("settings.integration.minimessage.enabled", true)) {
         try {
            Class.forName("com.ranull.graves.libraries.kyori.adventure.text.minimessage.MiniMessage", false, this.getClass().getClassLoader());
            Class.forName(
               "com.ranull.graves.libraries.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer", false, this.getClass().getClassLoader()
            );
            this.miniMessage = new MiniMessage();
            this.plugin.integrationMessage("Hooked into MiniMessage.");
         } catch (ClassNotFoundException var2) {
         }
      } else {
         this.miniMessage = null;
      }
   }

   private void loadMineDown() {
      if (this.miniMessage != null) {
         if (this.plugin.getConfig().getBoolean("settings.integration.minedown.enabled", true)) {
            try {
               Class.forName("com.ranull.graves.libraries.minedown.adventure.MineDown", false, this.getClass().getClassLoader());
               Class.forName("com.ranull.graves.libraries.minedown.adventure.MineDownParser", false, this.getClass().getClassLoader());
               this.mineDown = new MineDown();
               this.plugin.integrationMessage("Hooked into Minedown Adventure.");
            } catch (ClassNotFoundException var2) {
            }
         } else {
            this.mineDown = null;
         }
      }
   }

   private void loadChestSort() {
      if (this.plugin.getConfig().getBoolean("settings.integration.chestsort.enabled", true)) {
         Plugin chestSortPlugin = this.plugin.getServer().getPluginManager().getPlugin("ChestSort");
         if (chestSortPlugin != null && chestSortPlugin.isEnabled()) {
            this.chestSort = new ChestSort();
            this.plugin.integrationMessage("Hooked into " + chestSortPlugin.getName() + " " + chestSortPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.chestSort = null;
      }
   }

   private void loadPlayerNPC() {
      if (this.plugin.getConfig().getBoolean("settings.integration.playernpc.enabled", true)) {
         Plugin playerNPCPlugin = this.plugin.getServer().getPluginManager().getPlugin("PlayerNPC");
         if (playerNPCPlugin != null && playerNPCPlugin.isEnabled()) {
            this.playerNPC = new PlayerNPC(this.plugin);
            this.plugin.integrationMessage("Hooked into " + playerNPCPlugin.getName() + " " + playerNPCPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.playerNPC = null;
      }
   }

   private void loadFancyNpcs() {
      if (this.plugin.getConfig().getBoolean("settings.integration.fancynpcs.enabled")) {
         Plugin FancyNPCPlugin = this.plugin.getServer().getPluginManager().getPlugin("FancyNpcs");
         if (FancyNPCPlugin != null && FancyNPCPlugin.isEnabled()) {
            this.fancyNpcs = new FancyNPCs(this.plugin);
            this.plugin.integrationMessage("Hooked into " + FancyNPCPlugin.getName() + " " + FancyNPCPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.fancyNpcs = null;
      }
   }

   private void loadItemBridge() {
      if (this.plugin.getConfig().getBoolean("settings.integration.itembridge.enabled", true)) {
         Plugin itemBridgePlugin = this.plugin.getServer().getPluginManager().getPlugin("ItemBridge");
         if (itemBridgePlugin != null && itemBridgePlugin.isEnabled()) {
            if (this.itemBridge == null) {
               this.itemBridge = new ItemBridge(this.plugin);
            }

            this.plugin.integrationMessage("Hooked into " + itemBridgePlugin.getName() + " " + itemBridgePlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.itemBridge = null;
      }
   }

   private void loadPlaceholderAPI() {
      if (this.placeholderAPI != null) {
         this.placeholderAPI.unregister();
      }

      if (this.plugin.getConfig().getBoolean("settings.integration.placeholderapi.enabled", true)) {
         Plugin placeholderAPIPlugin = this.plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
         if (placeholderAPIPlugin != null && placeholderAPIPlugin.isEnabled()) {
            this.placeholderAPI = new PlaceholderAPI(this.plugin);
            this.placeholderAPI.register();
            this.plugin.integrationMessage("Hooked into " + placeholderAPIPlugin.getName() + " " + placeholderAPIPlugin.getDescription().getVersion() + ".");
         }
      } else {
         this.placeholderAPI = null;
      }
   }

   private void loadSkript() {
      if (this.plugin.getConfig().getBoolean("settings.integration.skript.enabled", true)) {
         Plugin skriptPlugin = this.plugin.getServer().getPluginManager().getPlugin("Skript");
         if (skriptPlugin != null && skriptPlugin.isEnabled()) {
            try {
               this.skriptImpl = new SkriptImpl(this.plugin);
               this.plugin.integrationMessage("Hooked into " + skriptPlugin.getName() + " " + skriptPlugin.getDescription().getVersion() + ".");
            } catch (Exception var3) {
               this.plugin
                  .integrationMessage(
                     "Failed to Hook into "
                        + skriptPlugin.getName()
                        + " "
                        + skriptPlugin.getDescription().getVersion()
                        + ". Skript implementation will not work and may cause skripts to break.",
                     "severe"
                  );
               this.skriptImpl = null;
            }
         } else {
            this.skriptImpl = null;
         }
      } else {
         this.skriptImpl = null;
      }
   }

   private void loadLuckPerms() {
      if (this.plugin.getConfig().getBoolean("settings.integration.luckperms.enabled", true)) {
         Plugin luckPermsPlugin = this.plugin.getServer().getPluginManager().getPlugin("LuckPerms");

         try {
            if (luckPermsPlugin != null && luckPermsPlugin.isEnabled()) {
               this.luckPermsHandler = new LuckPermsHandler();
               this.plugin.integrationMessage("Hooked into " + luckPermsPlugin.getName() + " " + luckPermsPlugin.getDescription().getVersion() + ".");
            } else {
               this.luckPermsHandler = null;
            }
         } catch (IllegalArgumentException var3) {
            this.plugin
               .integrationMessage(
                  "Failed to Hook into "
                     + luckPermsPlugin.getName()
                     + " "
                     + luckPermsPlugin.getDescription().getVersion()
                     + ". LuckPerms will not be used as a Permissions Provider.",
                  "severe"
               );
            this.luckPermsHandler = null;
         }
      } else {
         this.luckPermsHandler = null;
      }
   }

   private void loadBedrockSupport() {
      Plugin floodgatePlugin = this.plugin.getServer().getPluginManager().getPlugin("floodgate");
      Plugin geysermcPlugin = this.plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot");
      if (this.plugin.getConfig().getBoolean("settings.integration.floodgate.enabled", true)) {
         if (geysermcPlugin != null && geysermcPlugin.isEnabled()) {
            if (floodgatePlugin != null && floodgatePlugin.isEnabled()) {
               this.plugin.integrationMessage("Hooked into " + geysermcPlugin.getName() + " " + geysermcPlugin.getDescription().getVersion() + ".");
               this.plugin.integrationMessage("Hooked into " + floodgatePlugin.getName() + " " + floodgatePlugin.getDescription().getVersion() + ".");
               this.floodgate = new Floodgate(this.plugin);
            } else if (floodgatePlugin != null && !floodgatePlugin.isEnabled()) {
               this.plugin.integrationMessage("Hooked into " + geysermcPlugin.getName() + " " + geysermcPlugin.getDescription().getVersion() + ".");
               this.plugin
                  .integrationMessage("Failed to Hook into " + floodgatePlugin.getName() + " " + floodgatePlugin.getDescription().getVersion() + ".", "severe");
               this.floodgate = null;
            } else {
               this.floodgate = null;
            }
         } else if (floodgatePlugin != null && floodgatePlugin.isEnabled()) {
            this.plugin.integrationMessage("Failed to hook into Geyser-Spigot. Assuming the server runs behind a proxy.", "warning");
            this.plugin.integrationMessage("Hooked into " + floodgatePlugin.getName() + " " + floodgatePlugin.getDescription().getVersion() + ".");
            this.floodgate = null;
         } else {
            this.floodgate = null;
         }
      } else {
         this.floodgate = null;
      }
   }

   private void loadNBTAPI() {
      Plugin nbtAPI = this.plugin.getServer().getPluginManager().getPlugin("NBTAPI");
      if (nbtAPI != null && nbtAPI.isEnabled()) {
         this.plugin
            .integrationMessage(
               "Hooked into "
                  + nbtAPI.getName()
                  + " "
                  + nbtAPI.getDescription().getVersion()
                  + ". Using "
                  + nbtAPI.getName()
                  + " "
                  + nbtAPI.getDescription().getVersion()
                  + " for handling Inventory NBT Data."
            );
      }
   }

   private void loadNoteblockAPI() {
      if (this.plugin.getConfig().getBoolean("settings.integration.noteblockapi.enabled", true)) {
         Plugin nbAPI = this.plugin.getServer().getPluginManager().getPlugin("NoteBlockAPI");
         if (nbAPI != null && nbAPI.isEnabled()) {
            try {
               this.noteBlockAPI = new NoteBlockAPI(this.plugin);
               this.plugin.integrationMessage("Hooked into " + nbAPI.getName() + " " + nbAPI.getDescription().getVersion() + ".");
               File nbsFolder = new File(this.plugin.getDataFolder(), "nbs");
               if (!nbsFolder.exists() && !nbsFolder.mkdirs()) {
                  this.plugin.getLogger().warning("Failed to create /plugins/GravesX/nbs directory. You will need to make this folder manually.");
               }
            } catch (Exception var3) {
               this.plugin.integrationMessage("Failed to Hook into " + nbAPI.getName() + " " + nbAPI.getDescription().getVersion() + ".", "warn");
               this.noteBlockAPI = null;
            }
         } else {
            this.noteBlockAPI = null;
         }
      } else {
         this.noteBlockAPI = null;
      }
   }

   private void loadMannequins() {
      if (this.plugin.getConfig().getBoolean("settings.integration.mannequins.enabled", true)) {
         if (this.plugin.getVersionManager().isPost1_21_9()) {
            this.plugin
               .integrationMessage("Mannequins hooked for " + Bukkit.getServer().getName() + " v." + Bukkit.getServer().getVersion() + " successfully.");
            this.mannequins = new Mannequins(this.plugin);
         } else {
            this.mannequins = null;
         }
      }
   }

   private void loadBagOfGold() {
      if (this.plugin.getConfig().getBoolean("settings.integration.bagofgold.enabled", true)) {
         Plugin bag = this.plugin.getServer().getPluginManager().getPlugin("BagOfGold");
         if (bag != null && bag.isEnabled()) {
            this.bagOfGold = new BagOfGoldIntegration(this.plugin);
            return;
         }
      }

      this.bagOfGold = null;
   }

   public List<CustomItemStorageProvider> getCustomItemStorageProviders() {
      return this.customItemStorageProviders;
   }

   public void registerCustomItemStorageProvider(@NotNull CustomItemStorageProvider provider) {
      this.customItemStorageProviders.add(provider);
   }

   public void unregisterCustomItemStorageProvider(@NotNull CustomItemStorageProvider provider) {
      this.customItemStorageProviders.remove(provider);
   }

   private void loadCompatibilityWarnings() {
      if (this.plugin.getConfig().getBoolean("settings.compatibility.warning")) {
         for (World world : this.plugin.getServer().getWorlds()) {
            try {
               if (CompatibilityGameRule.getBoolean(world, "keepInventory")) {
                  this.plugin
                     .compatibilityMessage(
                        "World \""
                           + world.getName()
                           + "\" has keepInventory set to true, Graves will not be created here unless a player has the \"graves.keepinventory.bypass\" permission."
                     );
               }
            } catch (Exception var7) {
               if (CompatibilityGameRule.getBoolean(world, "keep_inventory")) {
                  this.plugin
                     .compatibilityMessage(
                        "World \""
                           + world.getName()
                           + "\" has keep_inventory set to true, Graves will not be created here unless a player has the \"graves.keepinventory.bypass\" permission."
                     );
               }
            }
         }

         Plugin essentialsPlugin = this.plugin.getServer().getPluginManager().getPlugin("Essentials");
         if (essentialsPlugin != null && essentialsPlugin.isEnabled()) {
            this.plugin
               .compatibilityMessage(
                  essentialsPlugin.getName() + " Detected, make sure you don't have the essentials.keepinv or essentials.keepxp permissions."
               );
         }

         Plugin deluxeCombatPlugin = this.plugin.getServer().getPluginManager().getPlugin("DeluxeCombat");
         if (deluxeCombatPlugin != null && deluxeCombatPlugin.isEnabled()) {
            this.plugin
               .compatibilityMessage(
                  deluxeCombatPlugin.getName()
                     + " Detected, in order to work with graves you need to set disable-drop-handling to true in "
                     + deluxeCombatPlugin.getName()
                     + "'s data.yml file."
               );
         }

         Plugin clearLagPlugin = this.plugin.getServer().getPluginManager().getPlugin("ClearLag");
         if (clearLagPlugin != null && clearLagPlugin.isEnabled()) {
            this.plugin
               .compatibilityMessage(
                  clearLagPlugin.getName()
                     + " Detected. Graves will always return empty. Author insists using their own grave plugin instead. If you need server optimizations, we recommend reading up on this: https://github.com/YouHaveTrouble/minecraft-optimization"
               );
         }

         try {
            Class.forName("ru.xezard.items.remover.ItemsRemoverPlugin");
            this.plugin
               .compatibilityMessage(
                  "XItemsRemover Detected. Plugin is known to leave [pdd] lore on all items. It is best to not modify plugin.yml to remove the loadsbefore option."
               );
         } catch (ClassNotFoundException var6) {
         }

         if (this.isPaperLikeServer()) {
            this.plugin
               .compatibilityMessage(
                  "Paper or a Paper fork detected. To ensure holograms disappear correctly when looting graves, make sure 'armor-stands-tick' or 'armor-stands.tick' is set to true."
               );
         }

         try {
            Class.forName("net.Indyuce.inventory.util");
            this.plugin
               .compatibilityMessage(
                  "MMOInventory detected. This plugin can interfere with inventory-related events. Please ensure compatibility settings are correctly configured."
               );
         } catch (ClassNotFoundException var5) {
         }

         this.checkForPluginManagers();
         this.similarPluginWarning("DeadChest");
         this.similarPluginWarning("DeathChest");
         this.similarPluginWarning("DeathChestPro");
         this.similarPluginWarning("SavageDeathChest");
         this.similarPluginWarning("AngelChest");
      }
   }

   private boolean isPaperLikeServer() {
      try {
         Class.forName("com.destroystokyo.paper.PaperConfig");
         return true;
      } catch (ClassNotFoundException var3) {
         try {
            Class.forName("io.papermc.paper.configuration.Configuration");
            return true;
         } catch (ClassNotFoundException var2) {
            return false;
         }
      }
   }

   public void checkForPluginManagers() {
      List<String> knownPluginManagers = Arrays.asList(
         "PluginManager",
         "PlugMan",
         "PlugManX",
         "WorldPluginManager",
         "AnthoPlugManager",
         "GlobalPlugins",
         "ProManager",
         "RestartManager",
         "UltimatePluginManager"
      );
      StringJoiner detectedPlugins = new StringJoiner(", ");

      for (String pluginManagerName : knownPluginManagers) {
         Plugin plugins = this.plugin.getServer().getPluginManager().getPlugin(pluginManagerName);
         if (plugins != null && plugins.isEnabled()) {
            detectedPlugins.add(plugins.getName() + " v." + plugins.getDescription().getVersion());
         }
      }

      if (detectedPlugins.length() > 0) {
         this.plugin.getLogger().warning("Detected server is running a Plugin Manager based plugin: " + detectedPlugins);
         this.plugin.getLogger().warning("No support will be given if you use one of these plugins.");
      }
   }

   private void similarPluginWarning(String string) {
      Plugin similarPlugin = this.plugin.getServer().getPluginManager().getPlugin(string);
      if (similarPlugin != null && similarPlugin.isEnabled()) {
         this.plugin
            .compatibilityMessage(
               string
                  + " Detected, Graves listens to the death event after "
                  + string
                  + ", and "
                  + string
                  + " clears the drop list. This means Graves will never be created for players if "
                  + string
                  + " is enabled, only non-player entities will create Graves if configured to do so."
            );
      }
   }

   private static boolean isVersionAtLeast(String version, String requiredVersion) {
      String[] currentParts = extractNumericVersion(version).split("\\.");
      String[] requiredParts = extractNumericVersion(requiredVersion).split("\\.");
      int length = Math.max(currentParts.length, requiredParts.length);

      for (int i = 0; i < length; i++) {
         int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
         int requiredPart = i < requiredParts.length ? parseVersionPart(requiredParts[i]) : 0;
         if (currentPart < requiredPart) {
            return false;
         }

         if (currentPart > requiredPart) {
            return true;
         }
      }

      return true;
   }

   private static String extractNumericVersion(String input) {
      Matcher matcher = Pattern.compile("(\\d+(\\.\\d+)*)").matcher(input);
      return matcher.find() ? matcher.group(1) : "0";
   }

   private static int parseVersionPart(String part) {
      String cleaned = part.replaceAll("^(\\d+).*", "$1");

      try {
         return cleaned.isEmpty() ? 0 : Integer.parseInt(cleaned);
      } catch (NumberFormatException var3) {
         return 0;
      }
   }
}

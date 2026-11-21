package github.nighter.smartspawner.spawner.gui.layout;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.SmartSpawnerAPIImpl;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import github.nighter.smartspawner.api.gui.ExternalGuiLayoutLoader;
import github.nighter.smartspawner.api.gui.GuiLayoutAdapter;
import github.nighter.smartspawner.api.gui.GuiLayoutData;
import github.nighter.smartspawner.api.gui.GuiLayoutRegistryImpl;
import github.nighter.smartspawner.api.gui.GuiLayoutType;
import github.nighter.smartspawner.api.gui.SpawnerGuiLayoutProvider;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.updates.GuiLayoutUpdater;
import java.io.File;
import java.util.logging.Level;
import lombok.Generated;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class GuiLayoutConfig {
   private static final String GUI_LAYOUTS_DIR = "gui_layouts";
   private static final String STORAGE_GUI_FILE = "storage_gui.yml";
   private static final String MAIN_GUI_FILE = "main_gui.yml";
   private static final String SELL_CONFIRM_GUI_FILE = "sell_confirm_gui.yml";
   private static final String DEFAULT_LAYOUT = "default";
   private final SmartSpawner plugin;
   private final File layoutsDir;
   private final GuiLayoutUpdater layoutUpdater;
   private final ExternalGuiLayoutLoader loader;
   private final GuiLayoutRegistryImpl registry;
   private volatile SpawnerGuiLayoutProvider provider;
   private String currentLayout;
   private GuiLayout currentStorageLayout;
   private GuiLayout currentMainLayout;
   private GuiLayout currentSellConfirmLayout;
   private boolean skipMainGui;
   private boolean skipSellConfirmation;
   private String openSound;

   public GuiLayoutConfig(SmartSpawner plugin, ExternalGuiLayoutLoader loader, GuiLayoutRegistryImpl registry) {
      this.plugin = plugin;
      this.layoutsDir = new File(plugin.getDataFolder(), "gui_layouts");
      this.layoutUpdater = new GuiLayoutUpdater(plugin);
      this.loader = loader;
      this.registry = registry;
      this.loadLayout();
   }

   public void loadLayout() {
      this.currentLayout = this.plugin.getConfig().getString("gui_layout", "default");
      this.layoutUpdater.checkAndUpdateLayouts();
      this.currentStorageLayout = this.loadCurrentStorageLayout();
      this.currentMainLayout = this.loadCurrentMainLayout();
      this.currentSellConfirmLayout = this.loadCurrentSellConfirmLayout();
      this.loadLayoutSettings();
   }

   private void loadLayoutSettings() {
      File mainFile = this.resolveLayoutFile("main_gui.yml");
      this.skipMainGui = false;
      this.openSound = null;
      if (mainFile != null) {
         try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(mainFile);
            this.skipMainGui = config.getBoolean("skip_main_gui", false);
            String soundName = config.getString("open_sound");
            if (soundName != null && !soundName.trim().isEmpty() && !soundName.equalsIgnoreCase("none")) {
               this.openSound = soundName.trim();
            }
         } catch (Exception var5) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to read settings from main_gui.yml, using defaults: " + var5.getMessage(), (Throwable)var5);
         }
      }

      File sellConfirmFile = this.resolveLayoutFile("sell_confirm_gui.yml");
      this.skipSellConfirmation = false;
      if (sellConfirmFile != null) {
         try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(sellConfirmFile);
            this.skipSellConfirmation = config.getBoolean("skip_sell_confirmation", false);
         } catch (Exception var4) {
            this.plugin
               .getLogger()
               .log(
                  Level.WARNING,
                  "Failed to read skip_sell_confirmation from sell_confirm_gui.yml, using default (false): " + var4.getMessage(),
                  (Throwable)var4
               );
         }
      }
   }

   private File resolveLayoutFile(String fileName) {
      File layoutFile = new File(new File(this.layoutsDir, this.currentLayout), fileName);
      if (layoutFile.exists()) {
         return layoutFile;
      } else {
         if (!this.currentLayout.equals("default")) {
            File defaultFile = new File(new File(this.layoutsDir, "default"), fileName);
            if (defaultFile.exists()) {
               return defaultFile;
            }
         }

         return null;
      }
   }

   private GuiLayout loadCurrentStorageLayout() {
      return this.loadLayoutFromFile("storage_gui.yml", "storage");
   }

   private GuiLayout loadCurrentMainLayout() {
      return this.loadLayoutFromFile("main_gui.yml", "main");
   }

   private GuiLayout loadCurrentSellConfirmLayout() {
      return this.loadLayoutFromFile("sell_confirm_gui.yml", "sell_confirm");
   }

   private GuiLayout loadLayoutFromFile(String fileName, String layoutType) {
      File layoutDir = new File(this.layoutsDir, this.currentLayout);
      File layoutFile = new File(layoutDir, fileName);
      if (layoutFile.exists()) {
         GuiLayout layout = this.loader.loadLayout(layoutFile, layoutType);
         if (layout != null) {
            return layout;
         }
      }

      if (!this.currentLayout.equals("default")) {
         this.plugin.getLogger().warning("Layout '" + this.currentLayout + "' not found. Attempting to use default layout.");
         File defaultLayoutDir = new File(this.layoutsDir, "default");
         File defaultLayoutFile = new File(defaultLayoutDir, fileName);
         if (defaultLayoutFile.exists()) {
            GuiLayout defaultLayout = this.loader.loadLayout(defaultLayoutFile, layoutType);
            if (defaultLayout != null) {
               this.plugin.getLogger().info("Loaded default " + layoutType + " layout as fallback");
               return defaultLayout;
            }
         }
      }

      this.plugin.getLogger().severe("No valid " + layoutType + " layout found! Creating empty layout as fallback.");
      return new GuiLayout();
   }

   public GuiLayout getMainLayout(SpawnerData spawner, Player player) {
      GuiLayout providerLayout = this.getProviderLayout(spawner, player, GuiLayoutType.MAIN_GUI);
      if (providerLayout != null) {
         return providerLayout;
      } else {
         GuiLayoutRegistryImpl currentRegistry = this.registry;
         if (currentRegistry != null) {
            GuiLayout regLayout = currentRegistry.getRegisteredMainGui(this.currentLayout);
            if (regLayout != null) {
               return regLayout;
            }
         }

         return this.getCurrentMainLayout();
      }
   }

   public GuiLayout getStorageLayout(SpawnerData spawner, Player player) {
      GuiLayout providerLayout = this.getProviderLayout(spawner, player, GuiLayoutType.STORAGE_GUI);
      if (providerLayout != null) {
         return providerLayout;
      } else {
         GuiLayoutRegistryImpl currentRegistry = this.registry;
         if (currentRegistry != null) {
            GuiLayout regLayout = currentRegistry.getRegisteredStorageGui(this.currentLayout);
            if (regLayout != null) {
               return regLayout;
            }
         }

         return this.getCurrentStorageLayout();
      }
   }

   public GuiLayout getSellConfirmLayout(SpawnerData spawner, Player player) {
      GuiLayout providerLayout = this.getProviderLayout(spawner, player, GuiLayoutType.SELL_CONFIRM_GUI);
      if (providerLayout != null) {
         return providerLayout;
      } else {
         GuiLayoutRegistryImpl currentRegistry = this.registry;
         if (currentRegistry != null) {
            GuiLayout regLayout = currentRegistry.getRegisteredSellConfirmGui(this.currentLayout);
            if (regLayout != null) {
               return regLayout;
            }
         }

         return this.getCurrentSellConfirmLayout();
      }
   }

   private GuiLayout getProviderLayout(SpawnerData spawner, Player player, GuiLayoutType type) {
      SpawnerGuiLayoutProvider currentProvider = this.provider;
      if (currentProvider == null) {
         return null;
      } else {
         try {
            SpawnerDataDTO dto = SmartSpawnerAPIImpl.convertToDTO(spawner);
            GuiLayoutData data = currentProvider.getLayout(dto, player, type);
            return GuiLayoutAdapter.toCoreLayout(data, type);
         } catch (Exception var7) {
            this.plugin
               .getLogger()
               .log(Level.WARNING, "SpawnerGuiLayoutProvider '" + this.getProviderName(currentProvider) + "' failed to provide " + type, (Throwable)var7);
            return null;
         }
      }
   }

   private String getProviderName(SpawnerGuiLayoutProvider currentProvider) {
      try {
         String name = currentProvider.getProviderName();
         return name != null && !name.isBlank() ? name : currentProvider.getClass().getName();
      } catch (Exception var3) {
         return currentProvider.getClass().getName();
      }
   }

   public GuiLayout getCurrentLayout() {
      return this.getCurrentStorageLayout();
   }

   public void reloadLayouts() {
      this.loadLayout();
   }

   @Generated
   public void setProvider(SpawnerGuiLayoutProvider provider) {
      this.provider = provider;
   }

   @Generated
   public GuiLayout getCurrentStorageLayout() {
      return this.currentStorageLayout;
   }

   @Generated
   public GuiLayout getCurrentMainLayout() {
      return this.currentMainLayout;
   }

   @Generated
   public GuiLayout getCurrentSellConfirmLayout() {
      return this.currentSellConfirmLayout;
   }

   @Generated
   public boolean isSkipMainGui() {
      return this.skipMainGui;
   }

   @Generated
   public boolean isSkipSellConfirmation() {
      return this.skipSellConfirmation;
   }

   @Generated
   public String getOpenSound() {
      return this.openSound;
   }
}

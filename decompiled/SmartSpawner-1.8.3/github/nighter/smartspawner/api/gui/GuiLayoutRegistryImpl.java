package github.nighter.smartspawner.api.gui;

import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;

public class GuiLayoutRegistryImpl implements GuiLayoutRegistry {
   private static final String DEFAULT_LAYOUT = "default";
   private final ConcurrentHashMap<String, RegisteredExternalLayout> registeredLayouts = new ConcurrentHashMap<>();
   private final ExternalGuiLayoutLoader loader;
   private final Logger logger;

   public GuiLayoutRegistryImpl(ExternalGuiLayoutLoader loader, Logger logger) {
      this.loader = loader;
      this.logger = logger;
   }

   @Override
   public boolean registerLayout(String layoutName, @Nullable GuiLayoutData mainGui, @Nullable GuiLayoutData storageGui, @Nullable GuiLayoutData sellConfirmGui) {
      String normalizedName = this.normalizeName(layoutName);
      if (normalizedName != null && (mainGui != null || storageGui != null || sellConfirmGui != null)) {
         if ("default".equals(normalizedName) && this.logger != null) {
            this.logger.warning("An external plugin registered a layout named 'default'. This shadows the built-in default layout from gui_layouts/default/.");
         }

         try {
            RegisteredExternalLayout reg = new RegisteredExternalLayout(
               GuiLayoutAdapter.toCoreLayout(mainGui, GuiLayoutType.MAIN_GUI),
               GuiLayoutAdapter.toCoreLayout(storageGui, GuiLayoutType.STORAGE_GUI),
               GuiLayoutAdapter.toCoreLayout(sellConfirmGui, GuiLayoutType.SELL_CONFIRM_GUI)
            );
            return this.registeredLayouts.putIfAbsent(normalizedName, reg) == null;
         } catch (IllegalArgumentException var7) {
            this.logInvalidLayout(normalizedName, var7);
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean registerLayoutFromYaml(String layoutName, @Nullable File mainGuiFile, @Nullable File storageGuiFile, @Nullable File sellConfirmGuiFile) {
      String normalizedName = this.normalizeName(layoutName);
      if (normalizedName == null || !this.areValidFiles(mainGuiFile, storageGuiFile, sellConfirmGuiFile)) {
         return false;
      } else if (this.registeredLayouts.containsKey(normalizedName)) {
         return false;
      } else {
         if ("default".equals(normalizedName) && this.logger != null) {
            this.logger
               .warning("An external plugin registered a layout named 'default' from YAML. This shadows the built-in default layout from gui_layouts/default/.");
         }

         GuiLayout mainGui = mainGuiFile != null && mainGuiFile.exists() ? this.loader.loadLayout(mainGuiFile, "main") : null;
         GuiLayout storageGui = storageGuiFile != null && storageGuiFile.exists() ? this.loader.loadLayout(storageGuiFile, "storage") : null;
         GuiLayout sellConfirmGui = sellConfirmGuiFile != null && sellConfirmGuiFile.exists()
            ? this.loader.loadLayout(sellConfirmGuiFile, "sell_confirm")
            : null;
         if ((mainGuiFile == null || mainGui != null)
            && (storageGuiFile == null || storageGui != null)
            && (sellConfirmGuiFile == null || sellConfirmGui != null)) {
            if (mainGui == null && storageGui == null && sellConfirmGui == null) {
               return false;
            } else {
               RegisteredExternalLayout reg = new RegisteredExternalLayout(mainGui, storageGui, sellConfirmGui);
               return this.registeredLayouts.putIfAbsent(normalizedName, reg) == null;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean unregisterLayout(String layoutName) {
      String normalizedName = this.normalizeName(layoutName);
      return normalizedName != null && this.registeredLayouts.remove(normalizedName) != null;
   }

   @Override
   public boolean isRegistered(String layoutName) {
      String normalizedName = this.normalizeName(layoutName);
      return normalizedName != null && this.registeredLayouts.containsKey(normalizedName);
   }

   @Override
   public Set<String> getRegisteredLayouts() {
      return Collections.unmodifiableSet(this.registeredLayouts.keySet());
   }

   RegisteredExternalLayout getRegisteredLayout(String layoutName) {
      String normalizedName = this.normalizeName(layoutName);
      return normalizedName != null ? this.registeredLayouts.get(normalizedName) : null;
   }

   public GuiLayout getRegisteredMainGui(String layoutName) {
      RegisteredExternalLayout reg = this.getRegisteredLayout(layoutName);
      return reg != null ? reg.getMainGui() : null;
   }

   public GuiLayout getRegisteredStorageGui(String layoutName) {
      RegisteredExternalLayout reg = this.getRegisteredLayout(layoutName);
      return reg != null ? reg.getStorageGui() : null;
   }

   public GuiLayout getRegisteredSellConfirmGui(String layoutName) {
      RegisteredExternalLayout reg = this.getRegisteredLayout(layoutName);
      return reg != null ? reg.getSellConfirmGui() : null;
   }

   private String normalizeName(String layoutName) {
      return layoutName != null && !layoutName.isBlank() ? layoutName.trim() : null;
   }

   private boolean areValidFiles(File... files) {
      boolean hasFile = false;

      for (File file : files) {
         if (file != null) {
            hasFile = true;
            if (!file.isFile()) {
               return false;
            }
         }
      }

      return hasFile;
   }

   private void logInvalidLayout(String layoutName, IllegalArgumentException exception) {
      if (this.logger != null) {
         this.logger.log(Level.WARNING, "Failed to register GUI layout '" + layoutName + "': " + exception.getMessage());
      }
   }
}

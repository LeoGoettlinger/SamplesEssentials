package github.nighter.smartspawner.spawner.gui.main;

import github.nighter.smartspawner.spawner.gui.SpawnerHolder;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import lombok.Generated;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SpawnerMenuHolder implements InventoryHolder, SpawnerHolder {
   private final SpawnerData spawnerData;
   private final GuiLayout layout;
   private final GuiButton storageButton;
   private final GuiButton expButton;
   private final GuiButton infoButton;

   public SpawnerMenuHolder(SpawnerData spawnerData, GuiLayout layout) {
      this.spawnerData = spawnerData;
      this.layout = layout;
      GuiButton storage = null;
      GuiButton exp = null;
      GuiButton info = null;
      if (layout != null) {
         for (GuiButton button : layout.getAllButtons().values()) {
            if (button.isEnabled()) {
               if (info == null && button.isInfoButton()) {
                  info = button;
               }

               if (button.getActions() != null) {
                  if (storage == null && button.getActions().containsValue("open_storage")) {
                     storage = button;
                  }

                  if (exp == null && button.getActions().containsValue("collect_exp")) {
                     exp = button;
                  }
               }
            }
         }
      }

      this.storageButton = storage;
      this.expButton = exp;
      this.infoButton = info;
   }

   public Inventory getInventory() {
      return null;
   }

   @Generated
   @Override
   public SpawnerData getSpawnerData() {
      return this.spawnerData;
   }

   @Generated
   public GuiLayout getLayout() {
      return this.layout;
   }

   @Generated
   public GuiButton getStorageButton() {
      return this.storageButton;
   }

   @Generated
   public GuiButton getExpButton() {
      return this.expButton;
   }

   @Generated
   public GuiButton getInfoButton() {
      return this.infoButton;
   }
}

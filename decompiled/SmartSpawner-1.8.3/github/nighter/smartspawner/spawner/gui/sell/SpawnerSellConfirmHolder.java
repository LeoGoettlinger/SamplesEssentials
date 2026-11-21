package github.nighter.smartspawner.spawner.gui.sell;

import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import lombok.Generated;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SpawnerSellConfirmHolder implements InventoryHolder {
   private final SpawnerData spawnerData;
   private final SpawnerSellConfirmUI.PreviousGui previousGui;
   private final boolean collectExp;
   private final GuiLayout layout;

   public SpawnerSellConfirmHolder(SpawnerData spawnerData, SpawnerSellConfirmUI.PreviousGui previousGui, boolean collectExp, GuiLayout layout) {
      this.spawnerData = spawnerData;
      this.previousGui = previousGui;
      this.collectExp = collectExp;
      this.layout = layout;
   }

   @NotNull
   public Inventory getInventory() {
      return null;
   }

   @Generated
   public SpawnerData getSpawnerData() {
      return this.spawnerData;
   }

   @Generated
   public SpawnerSellConfirmUI.PreviousGui getPreviousGui() {
      return this.previousGui;
   }

   @Generated
   public boolean isCollectExp() {
      return this.collectExp;
   }

   @Generated
   public GuiLayout getLayout() {
      return this.layout;
   }
}

package github.nighter.smartspawner.api.gui;

import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;

class RegisteredExternalLayout {
   private final GuiLayout mainGui;
   private final GuiLayout storageGui;
   private final GuiLayout sellConfirmGui;

   RegisteredExternalLayout(GuiLayout mainGui, GuiLayout storageGui, GuiLayout sellConfirmGui) {
      this.mainGui = mainGui;
      this.storageGui = storageGui;
      this.sellConfirmGui = sellConfirmGui;
   }

   GuiLayout getMainGui() {
      return this.mainGui;
   }

   GuiLayout getStorageGui() {
      return this.storageGui;
   }

   GuiLayout getSellConfirmGui() {
      return this.sellConfirmGui;
   }
}

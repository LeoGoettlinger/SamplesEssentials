package com.artillexstudios.axtrade.libs.gui.builder.gui;

import com.artillexstudios.axtrade.libs.gui.guis.StorageGui;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class StorageBuilder extends BaseChestGuiBuilder<StorageGui, StorageBuilder> {
   @NotNull
   @Contract(" -> new")
   public StorageGui create() {
      StorageGui gui = new StorageGui(this.createContainer(), this.getModifiers());
      Consumer<StorageGui> consumer = this.getConsumer();
      if (consumer != null) {
         consumer.accept(gui);
      }

      return gui;
   }
}

package com.artillexstudios.axrankmenu.libs.gui.builder.gui;

import com.artillexstudios.axrankmenu.libs.gui.guis.PaginatedGui;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PaginatedBuilder extends BaseChestGuiBuilder<PaginatedGui, PaginatedBuilder> {
   private int pageSize = 0;

   @NotNull
   @Contract("_ -> this")
   public PaginatedBuilder pageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
   }

   @NotNull
   @Contract(" -> new")
   public PaginatedGui create() {
      PaginatedGui gui = new PaginatedGui(this.createContainer(), this.pageSize, this.getModifiers());
      Consumer<PaginatedGui> consumer = this.getConsumer();
      if (consumer != null) {
         consumer.accept(gui);
      }

      return gui;
   }
}

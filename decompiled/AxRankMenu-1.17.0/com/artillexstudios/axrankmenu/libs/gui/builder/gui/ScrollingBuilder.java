package com.artillexstudios.axrankmenu.libs.gui.builder.gui;

import com.artillexstudios.axrankmenu.libs.gui.components.ScrollType;
import com.artillexstudios.axrankmenu.libs.gui.guis.ScrollingGui;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ScrollingBuilder extends BaseChestGuiBuilder<ScrollingGui, ScrollingBuilder> {
   private ScrollType scrollType;
   private int pageSize = 0;

   public ScrollingBuilder(@NotNull ScrollType scrollType) {
      this.scrollType = scrollType;
   }

   @NotNull
   @Contract("_ -> this")
   public ScrollingBuilder scrollType(@NotNull ScrollType scrollType) {
      this.scrollType = scrollType;
      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public ScrollingBuilder pageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
   }

   @NotNull
   @Contract(" -> new")
   public ScrollingGui create() {
      ScrollingGui gui = new ScrollingGui(this.createContainer(), this.pageSize, this.scrollType, this.getModifiers());
      Consumer<ScrollingGui> consumer = this.getConsumer();
      if (consumer != null) {
         consumer.accept(gui);
      }

      return gui;
   }
}

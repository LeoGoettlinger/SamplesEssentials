package com.artillexstudios.axrankmenu.libs.gui.builder.gui;

import com.artillexstudios.axrankmenu.libs.gui.components.GuiType;
import com.artillexstudios.axrankmenu.libs.gui.guis.Gui;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ChestGuiBuilder extends BaseChestGuiBuilder<Gui, ChestGuiBuilder> {
   @NotNull
   @Contract("_ -> new")
   public TypedGuiBuilder type(@NotNull GuiType guiType) {
      return new TypedGuiBuilder(guiType, this);
   }

   @NotNull
   @Contract(" -> new")
   public Gui create() {
      Gui gui = new Gui(this.createContainer(), this.getModifiers());
      Consumer<Gui> consumer = this.getConsumer();
      if (consumer != null) {
         consumer.accept(gui);
      }

      return gui;
   }
}

package com.artillexstudios.axtrade.libs.gui.builder.gui;

import com.artillexstudios.axtrade.libs.gui.components.GuiContainer;
import com.artillexstudios.axtrade.libs.gui.components.GuiType;
import com.artillexstudios.axtrade.libs.gui.components.InventoryProvider;
import com.artillexstudios.axtrade.libs.gui.components.util.Legacy;
import com.artillexstudios.axtrade.libs.gui.guis.Gui;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TypedGuiBuilder extends BaseGuiBuilder<Gui, TypedGuiBuilder> {
   private GuiType guiType;
   private InventoryProvider.Typed inventoryProvider = (title, owner, type) -> Bukkit.createInventory(owner, type, Legacy.SERIALIZER.serialize(title));

   public TypedGuiBuilder(@NotNull GuiType guiType) {
      this.guiType = guiType;
   }

   public TypedGuiBuilder(@NotNull GuiType guiType, @NotNull ChestGuiBuilder builder) {
      this.guiType = guiType;
      this.consumeBuilder(builder);
   }

   @NotNull
   @Contract("_ -> this")
   public TypedGuiBuilder type(@NotNull GuiType guiType) {
      this.guiType = guiType;
      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public TypedGuiBuilder inventory(@NotNull InventoryProvider.Typed inventoryProvider) {
      this.inventoryProvider = inventoryProvider;
      return this;
   }

   @NotNull
   @Contract(" -> new")
   public Gui create() {
      Gui gui = new Gui(new GuiContainer.Typed(this.getTitle(), this.inventoryProvider, this.guiType), this.getModifiers());
      Consumer<Gui> consumer = this.getConsumer();
      if (consumer != null) {
         consumer.accept(gui);
      }

      return gui;
   }
}

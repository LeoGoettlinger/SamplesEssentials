package com.artillexstudios.axrankmenu.libs.gui.guis;

import com.artillexstudios.axrankmenu.libs.gui.builder.gui.ChestGuiBuilder;
import com.artillexstudios.axrankmenu.libs.gui.builder.gui.PaginatedBuilder;
import com.artillexstudios.axrankmenu.libs.gui.builder.gui.ScrollingBuilder;
import com.artillexstudios.axrankmenu.libs.gui.builder.gui.StorageBuilder;
import com.artillexstudios.axrankmenu.libs.gui.builder.gui.TypedGuiBuilder;
import com.artillexstudios.axrankmenu.libs.gui.components.GuiContainer;
import com.artillexstudios.axrankmenu.libs.gui.components.GuiType;
import com.artillexstudios.axrankmenu.libs.gui.components.InteractionModifier;
import com.artillexstudios.axrankmenu.libs.gui.components.ScrollType;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Gui extends BaseGui {
   public Gui(@NotNull GuiContainer guiContainer, @NotNull Set<InteractionModifier> interactionModifiers) {
      super(guiContainer, interactionModifiers);
   }

   @Contract("_ -> new")
   @NotNull
   public static TypedGuiBuilder gui(@NotNull GuiType type) {
      return new TypedGuiBuilder(type);
   }

   @Contract(" -> new")
   @NotNull
   public static ChestGuiBuilder gui() {
      return new ChestGuiBuilder();
   }

   @Contract(" -> new")
   @NotNull
   public static StorageBuilder storage() {
      return new StorageBuilder();
   }

   @Contract(" -> new")
   @NotNull
   public static PaginatedBuilder paginated() {
      return new PaginatedBuilder();
   }

   @Contract("_ -> new")
   @NotNull
   public static ScrollingBuilder scrolling(@NotNull ScrollType scrollType) {
      return new ScrollingBuilder(scrollType);
   }

   @Contract(" -> new")
   @NotNull
   public static ScrollingBuilder scrolling() {
      return scrolling(ScrollType.VERTICAL);
   }
}

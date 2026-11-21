package com.artillexstudios.axtrade.libs.axapi.gui.inventory;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.builder.DynamicGuiBuilder;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.builder.PaginatedGuiBuilder;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.builder.StaticGuiBuilder;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public abstract class GuiBuilder<T extends Gui, Z extends GuiBuilder<T, Z>> {
   protected final List<WrappedItemStackModifier> modifiers = new ArrayList<>();
   protected InventoryType type = InventoryType.CHEST;
   protected HashMapContext context = new HashMapContext();
   protected Function<HashMapContext, Component> titleProvider = ctx -> Component.empty();
   protected int rows;

   public static DynamicGuiBuilder createDynamic() {
      return new DynamicGuiBuilder();
   }

   public static StaticGuiBuilder createStatic() {
      return new StaticGuiBuilder();
   }

   public static PaginatedGuiBuilder createPaginated() {
      return new PaginatedGuiBuilder();
   }

   public void context(HashMapContext context) {
      this.context = context;
   }

   public Z title(Function<HashMapContext, Component> titleProvider) {
      this.titleProvider = (Function<HashMapContext, Component>)Preconditions.checkNotNull(titleProvider);
      return (Z)this;
   }

   public Z rows(int rows) {
      this.rows = rows;
      return (Z)this;
   }

   public Z inventoryType(InventoryType type) {
      this.type = type;
      return (Z)this;
   }

   public Z guiItemModifier(WrappedItemStackModifier modifier) {
      this.modifiers.add(modifier);
      return (Z)this;
   }

   public abstract T build(Player player);
}

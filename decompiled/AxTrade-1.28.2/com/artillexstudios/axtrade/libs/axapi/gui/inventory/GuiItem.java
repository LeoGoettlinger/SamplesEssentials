package com.artillexstudios.axtrade.libs.axapi.gui.inventory;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bukkit.event.inventory.InventoryClickEvent;

public record GuiItem(Function<HashMapContext, WrappedItemStack> stack, BiConsumer<HashMapContext, InventoryClickEvent> eventConsumer) {
   public GuiItem(Function<HashMapContext, WrappedItemStack> stack) {
      this(stack, (ctx, event) -> {});
   }
}

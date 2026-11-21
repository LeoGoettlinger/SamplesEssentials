package com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.provider.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.context.HashMapContext;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.provider.GuiItemProvider;
import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncGuiItemProvider implements GuiItemProvider {
   private final GuiItem item;

   public AsyncGuiItemProvider(GuiItem item) {
      this.item = item;
   }

   @Override
   public CompletableFuture<BakedGuiItem> provide(HashMapContext context, List<WrappedItemStackModifier> modifiers) {
      return CompletableFuture.supplyAsync(() -> {
         WrappedItemStack wrappedItemStack = this.item.stack().apply(context);
         return new BakedGuiItem(this.handleModifiers(wrappedItemStack, modifiers).toBukkit(), event -> this.item.eventConsumer().accept(context, event));
      });
   }
}

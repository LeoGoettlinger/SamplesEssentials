package com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.provider.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.context.HashMapContext;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.provider.GuiItemProvider;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SimpleGuiItemProvider implements GuiItemProvider {
   private final GuiItem item;

   public SimpleGuiItemProvider(GuiItem item) {
      this.item = item;
   }

   @Override
   public CompletableFuture<BakedGuiItem> provide(HashMapContext context, List<WrappedItemStackModifier> modifiers) {
      return CompletableFuture.completedFuture(
         new BakedGuiItem(
            this.handleModifiers(this.item.stack().apply(context), modifiers).toBukkit(), event -> this.item.eventConsumer().accept(context, event)
         )
      );
   }
}

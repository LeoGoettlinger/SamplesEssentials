package com.artillexstudios.axtrade.libs.axapi.gui.inventory.provider.implementation;

import com.artillexstudios.axtrade.libs.axapi.context.HashMapContext;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.artillexstudios.axtrade.libs.axapi.gui.inventory.provider.GuiItemProvider;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CachingGuiItemProvider implements GuiItemProvider {
   private final GuiItem guiItem;
   private BakedGuiItem bakedGuiItem;
   private CompletableFuture<BakedGuiItem> future;

   public CachingGuiItemProvider(GuiItem guiItem) {
      this.guiItem = guiItem;
   }

   @Override
   public CompletableFuture<BakedGuiItem> provide(HashMapContext context, List<WrappedItemStackModifier> modifiers) {
      if (this.future == null) {
         this.bakedGuiItem = new BakedGuiItem(
            this.handleModifiers(this.guiItem.stack().apply(context), modifiers).toBukkit(), event -> this.guiItem.eventConsumer().accept(context, event)
         );
         this.future = CompletableFuture.completedFuture(this.bakedGuiItem);
      }

      return this.future;
   }
}

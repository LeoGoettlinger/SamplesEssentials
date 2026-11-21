package com.artillexstudios.axafkzone.libs.axapi.gui.inventory.provider;

import com.artillexstudios.axafkzone.libs.axapi.context.HashMapContext;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.provider.implementation.AsyncGuiItemProvider;
import com.artillexstudios.axafkzone.libs.axapi.gui.inventory.provider.implementation.CachingGuiItemProvider;
import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GuiItemProvider {
   static GuiItemProvider async(GuiItem item) {
      return new AsyncGuiItemProvider(item);
   }

   static GuiItemProvider caching(GuiItem item) {
      return new CachingGuiItemProvider(item);
   }

   CompletableFuture<BakedGuiItem> provide(HashMapContext context, List<WrappedItemStackModifier> modifiers);

   default WrappedItemStack handleModifiers(WrappedItemStack stack, List<WrappedItemStackModifier> modifiers) {
      for (WrappedItemStackModifier modifier : modifiers) {
         stack = modifier.modify(stack);
      }

      return stack;
   }
}

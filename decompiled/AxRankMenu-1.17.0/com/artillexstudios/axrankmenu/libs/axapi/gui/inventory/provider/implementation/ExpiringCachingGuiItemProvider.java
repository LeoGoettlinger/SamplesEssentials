package com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.provider.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.context.HashMapContext;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.BakedGuiItem;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.modifier.WrappedItemStackModifier;
import com.artillexstudios.axrankmenu.libs.axapi.gui.inventory.provider.GuiItemProvider;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExpiringCachingGuiItemProvider implements GuiItemProvider {
   private final SimpleGuiItemProvider simpleProvider;
   private final long durationMillis;
   private CompletableFuture<BakedGuiItem> provided;
   private HashMapContext lastContext;
   private long lastCheck = 0L;

   public ExpiringCachingGuiItemProvider(GuiItem item, Duration duration) {
      this.simpleProvider = new SimpleGuiItemProvider(item);
      this.durationMillis = duration.toMillis();
   }

   @Override
   public CompletableFuture<BakedGuiItem> provide(HashMapContext context, List<WrappedItemStackModifier> modifiers) {
      long now = System.currentTimeMillis();
      if (this.provided != null && this.lastContext.equals(context) && now - this.lastCheck < this.durationMillis) {
         return this.provided;
      } else {
         this.lastCheck = now;
         this.lastContext = context;
         this.provided = this.simpleProvider.provide(context, modifiers);
         return this.provided;
      }
   }
}

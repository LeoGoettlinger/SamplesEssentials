package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.connection.ProtocolStorables;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.ClientWorld;

public class ProtocolStorablesBase implements ProtocolStorables {
   private EntityTracker tracker;
   private ClientWorld clientWorld;
   private ItemHasher itemHasher;

   @Override
   public EntityTracker entityTracker() {
      return this.tracker;
   }

   @Override
   public void setEntityTracker(EntityTracker tracker) {
      this.tracker = tracker;
   }

   @Override
   public ClientWorld clientWorld() {
      return this.clientWorld;
   }

   @Override
   public void setClientWorld(ClientWorld clientWorld) {
      this.clientWorld = clientWorld;
   }

   @Override
   public ItemHasher itemHasher() {
      return this.itemHasher;
   }

   @Override
   public void setItemHasher(ItemHasher itemHasher) {
      this.itemHasher = itemHasher;
   }
}

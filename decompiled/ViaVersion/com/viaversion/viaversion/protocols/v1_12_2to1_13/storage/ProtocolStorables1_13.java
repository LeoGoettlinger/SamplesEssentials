package com.viaversion.viaversion.protocols.v1_12_2to1_13.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables1_13 extends ProtocolStorablesBase {
   private final TabCompleteTracker tabCompleteTracker = new TabCompleteTracker();
   private final BlockStorage blockStorage = new BlockStorage();
   private BlockConnectionStorage blockConnectionStorage;

   public TabCompleteTracker tabCompleteTracker() {
      return this.tabCompleteTracker;
   }

   public BlockStorage blockStorage() {
      return this.blockStorage;
   }

   @Nullable
   public BlockConnectionStorage blockConnectionStorage() {
      return this.blockConnectionStorage;
   }

   public void setBlockConnectionStorage(BlockConnectionStorage blockConnectionStorage) {
      this.blockConnectionStorage = blockConnectionStorage;
   }
}

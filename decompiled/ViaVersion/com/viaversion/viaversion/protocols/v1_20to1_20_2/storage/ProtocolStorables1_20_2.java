package com.viaversion.viaversion.protocols.v1_20to1_20_2.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables1_20_2 extends ProtocolStorablesBase {
   private final ConfigurationState configurationState = new ConfigurationState();
   private LastResourcePack lastResourcePack;
   private LastTags lastTags;

   public ConfigurationState configurationState() {
      return this.configurationState;
   }

   @Nullable
   public LastResourcePack lastResourcePack() {
      return this.lastResourcePack;
   }

   public void setLastResourcePack(LastResourcePack lastResourcePack) {
      this.lastResourcePack = lastResourcePack;
   }

   @Nullable
   public LastTags lastTags() {
      return this.lastTags;
   }

   public void setLastTags(@Nullable LastTags lastTags) {
      this.lastTags = lastTags;
   }
}

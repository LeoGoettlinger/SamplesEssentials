package com.viaversion.viabackwards.protocol.v1_16_2to1_16_1.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_16_2 extends ProtocolStorablesBase {
   private final BiomeStorage biomeStorage = new BiomeStorage();

   public BiomeStorage biomeStorage() {
      return this.biomeStorage;
   }
}

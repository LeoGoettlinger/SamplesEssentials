package com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class RegistryDataRewriter26_1 extends RegistryDataRewriter {
   public RegistryDataRewriter26_1(Protocol<?, ?, ?, ?> protocol) {
      super(protocol);
   }

   @Override
   public void updateEnchantmentTerm(CompoundTag term) {
      if (Key.equals(term.getString("condition"), "time_check")) {
         term.putString("clock", "overworld");
      }

      super.updateEnchantmentTerm(term);
   }
}

package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_20_5;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.verify.TextVerifier;

public interface TextVerifier_v1_20_5 extends TextVerifier {
   default boolean verifyRegistryItem(Identifier id) {
      return true;
   }

   default boolean verifyRegistryEntity(Identifier id) {
      return true;
   }

   default boolean verifyDataComponents(CompoundTag tag) {
      return true;
   }
}

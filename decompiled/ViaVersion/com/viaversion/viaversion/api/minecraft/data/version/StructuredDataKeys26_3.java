package com.viaversion.viaversion.api.minecraft.data.version;

import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations26_3;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;

public class StructuredDataKeys26_3 extends StructuredDataKeys26_2 {
   public final StructuredDataKey<PotDecorations26_3> potDecorations;

   public StructuredDataKeys26_3(VersionedTypesHolder types) {
      super(types);
      this.potDecorations = this.add("pot_decorations", new PotDecorations26_3.PotDecorationsType(types.optionalItemTemplate()));
   }
}

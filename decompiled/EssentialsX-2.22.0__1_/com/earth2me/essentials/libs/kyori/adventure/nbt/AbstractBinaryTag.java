package com.earth2me.essentials.libs.kyori.adventure.nbt;

import com.earth2me.essentials.libs.kyori.examination.string.StringExaminer;
import org.jetbrains.annotations.NotNull;

abstract class AbstractBinaryTag implements BinaryTag {
   @NotNull
   @Override
   public final String examinableName() {
      return this.type().toString();
   }

   @Override
   public final String toString() {
      return this.examine(StringExaminer.simpleEscaping());
   }
}

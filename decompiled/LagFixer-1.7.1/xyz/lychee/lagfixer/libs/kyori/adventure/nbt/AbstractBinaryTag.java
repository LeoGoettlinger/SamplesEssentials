package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.examination.string.StringExaminer;

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

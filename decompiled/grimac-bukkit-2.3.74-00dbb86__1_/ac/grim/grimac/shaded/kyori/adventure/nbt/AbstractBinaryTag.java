package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.examination.string.StringExaminer;

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

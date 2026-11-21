package net.kyori.adventure.nbt;

final class EndBinaryTagImpl extends AbstractBinaryTag implements EndBinaryTag {
   static final EndBinaryTagImpl INSTANCE = new EndBinaryTagImpl();

   @Override
   public boolean equals(final Object that) {
      return this == that;
   }

   @Override
   public int hashCode() {
      return 0;
   }
}

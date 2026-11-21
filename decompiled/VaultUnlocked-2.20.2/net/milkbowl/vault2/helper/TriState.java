package net.milkbowl.vault2.helper;

public enum TriState {
   TRUE,
   FALSE,
   UNDEFINED;

   public boolean asBool() {
      return this == TRUE;
   }
}

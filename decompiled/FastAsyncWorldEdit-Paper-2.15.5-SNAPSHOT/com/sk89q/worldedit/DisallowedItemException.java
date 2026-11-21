package com.sk89q.worldedit;

@Deprecated
public class DisallowedItemException extends WorldEditException {
   private final String type;

   public DisallowedItemException(String type) {
      this.type = type;
   }

   public DisallowedItemException(String type, String message) {
      super(message);
      this.type = type;
   }

   public String getID() {
      return this.type;
   }
}

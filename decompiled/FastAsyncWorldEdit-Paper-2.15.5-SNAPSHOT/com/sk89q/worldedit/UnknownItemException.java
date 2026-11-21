package com.sk89q.worldedit;

@Deprecated
public class UnknownItemException extends WorldEditException {
   private final String type;

   public UnknownItemException(String type) {
      this.type = type;
   }

   public String getID() {
      return this.type;
   }
}

package com.oheers.fish.api.addons.exceptions;

public class NoPrefixException extends Exception {
   private final String prefix;

   public NoPrefixException(String prefix) {
      super(String.format("No such prefix: %s, did you install the addon?", prefix));
      this.prefix = prefix;
   }

   public String getPrefix() {
      return this.prefix;
   }
}

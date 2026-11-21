package org.skriptlang.skript.util;

class Validator implements Validated {
   private volatile boolean valid = true;

   @Override
   public synchronized void invalidate() {
      this.valid = false;
   }

   @Override
   public boolean valid() {
      return this.valid;
   }
}

package com.fastasyncworldedit.serverlib;

public abstract class ClassPresenceCheck extends Check {
   private final String[] illegalClasses;

   public ClassPresenceCheck(CheckType type, String... classes) {
      super(type);
      this.illegalClasses = classes;
   }

   @Override
   public boolean shouldFlag() {
      for (String illegalClass : this.illegalClasses) {
         try {
            Class.forName(illegalClass);
            return true;
         } catch (ClassNotFoundException var6) {
         }
      }

      return false;
   }
}

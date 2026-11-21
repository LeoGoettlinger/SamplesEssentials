package com.fastasyncworldedit.serverlib;

public abstract class Check {
   private static final String EXPLANATION_BORDER = "************************************************************";
   private final CheckType type;

   public Check(CheckType type) {
      this.type = type;
   }

   public final CheckType type() {
      return this.type;
   }

   public abstract boolean shouldFlag();

   public abstract String[] explain();

   public final void printExplanation() {
      System.err.println("************************************************************");
      System.err.printf("* Detected unsafe software: %s (type: %s)\n", this.getClass().getSimpleName(), this.type().name());
      System.err.println("*");

      for (String line : this.explain()) {
         System.err.printf("* %s\n", line);
      }

      System.err.println("************************************************************");
   }
}

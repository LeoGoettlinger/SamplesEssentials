package de.theredend2000.advancedhunt.lib.caffeine.cache;

enum SystemTicker implements Ticker {
   INSTANCE;

   @Override
   public long read() {
      return System.nanoTime();
   }
}

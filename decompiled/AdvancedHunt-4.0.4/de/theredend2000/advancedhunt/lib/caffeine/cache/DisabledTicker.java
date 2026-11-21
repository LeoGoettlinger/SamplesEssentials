package de.theredend2000.advancedhunt.lib.caffeine.cache;

enum DisabledTicker implements Ticker {
   INSTANCE;

   @Override
   public long read() {
      return 0L;
   }
}

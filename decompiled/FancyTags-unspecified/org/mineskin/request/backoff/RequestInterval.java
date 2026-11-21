package org.mineskin.request.backoff;

public interface RequestInterval {
   int getInterval(int var1);

   static RequestInterval constant(int intervalMillis) {
      return attempt -> intervalMillis;
   }

   static ExponentialBackoff exponential() {
      return new ExponentialBackoff(200, 2000, 2.0, 3);
   }
}

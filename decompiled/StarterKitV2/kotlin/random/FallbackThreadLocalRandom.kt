package kotlin.random

internal class FallbackThreadLocalRandom : AbstractPlatformRandom {
   private final val implStorage: <unrepresentable> = new ThreadLocal<java.util.Random>() {
      protected java.util.Random initialValue() {
         return new java.util.Random();
      }
   }

   public open val impl: java.util.Random
      public open get() {
         val var10000: Any = this.implStorage.get();
         return var10000 as java.util.Random;
      }

}

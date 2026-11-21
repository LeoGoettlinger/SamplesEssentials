package kotlin

private object KotlinVersionCurrentValue {
   @JvmStatic
   public fun get(): KotlinVersion {
      return new KotlinVersion(2, 3, 20);
   }
}

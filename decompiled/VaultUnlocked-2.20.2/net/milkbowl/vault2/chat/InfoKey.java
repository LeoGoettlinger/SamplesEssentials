package net.milkbowl.vault2.chat;

public class InfoKey<T> {
   private final String node;

   public InfoKey(final String node) {
      this.node = node;
   }

   public String node() {
      return this.node;
   }

   public static InfoKey<String> stringKey(final String node) {
      return new InfoKey<>(node);
   }

   public static InfoKey<Boolean> boolKey(final String node) {
      return new InfoKey<>(node);
   }

   public static InfoKey<Integer> intKey(final String node) {
      return new InfoKey<>(node);
   }

   public static InfoKey<Double> doubleKey(final String node) {
      return new InfoKey<>(node);
   }
}

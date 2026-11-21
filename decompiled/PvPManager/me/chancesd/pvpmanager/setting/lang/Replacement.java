package me.chancesd.pvpmanager.setting.lang;

public class Replacement {
   public static final Replacement PLAYER = prepare("player");
   public static final Replacement TIME = prepare("time");
   public static final Replacement VICTIM = prepare("victim");
   public static final Replacement MONEY = prepare("money");
   public static final Replacement EXP = prepare("exp");
   public static final Replacement STATE = prepare("state");
   public static final Replacement PREFIX = prepare("prefix");
   public static final Replacement POWER = prepare("power");
   private final String placeholder;

   private Replacement(String placeholder) {
      this.placeholder = placeholder;
   }

   private static Replacement prepare(String value) {
      return new Replacement("{" + value + "}");
   }

   public static Replacement of(String key) {
      if (key != null && !key.trim().isEmpty()) {
         return new Replacement("{" + key + "}");
      } else {
         throw new IllegalArgumentException("Replacement key cannot be null or empty");
      }
   }

   public String getPlaceholder() {
      return this.placeholder;
   }
}

package dev.demora.demoralegendaryweapons;

import org.bukkit.attribute.Attribute;

public final class AttributeCompat {
   private static final Attribute ARMOR_ATTR = resolve("GENERIC_ARMOR", "ARMOR");
   private static final Attribute ATTACK_DAMAGE_ATTR = resolve("GENERIC_ATTACK_DAMAGE", "ATTACK_DAMAGE");
   private static final Attribute ATTACK_SPEED_ATTR = resolve("GENERIC_ATTACK_SPEED", "ATTACK_SPEED");
   private static final Attribute SCALE_ATTR = resolve("GENERIC_SCALE", "SCALE");
   private static final Attribute MOVEMENT_SPEED_ATTR = resolve("GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED");
   private static final Attribute MAX_HEALTH_ATTR = resolve("GENERIC_MAX_HEALTH", "MAX_HEALTH");

   private static Attribute resolve(String name1_21, String name1_21_4) {
      try {
         return (Attribute)Attribute.class.getField(name1_21).get(null);
      } catch (Throwable var4) {
         try {
            return (Attribute)Attribute.class.getField(name1_21_4).get(null);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   public static Attribute getArmor() {
      return ARMOR_ATTR;
   }

   public static Attribute getAttackDamage() {
      return ATTACK_DAMAGE_ATTR;
   }

   public static Attribute getAttackSpeed() {
      return ATTACK_SPEED_ATTR;
   }

   public static Attribute getScale() {
      return SCALE_ATTR;
   }

   public static Attribute getMovementSpeed() {
      return MOVEMENT_SPEED_ATTR;
   }

   public static Attribute getMaxHealth() {
      return MAX_HEALTH_ATTR;
   }
}

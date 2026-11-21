package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.util.Math2;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;

public class HealthUtils {
   @Nullable
   private static final Constructor<EntityDamageEvent> OLD_DAMAGE_EVENT_CONSTRUCTOR;
   private static final Attribute MAX_HEALTH;

   public static double getHealth(Damageable damageable) {
      return damageable.isDead() ? 0.0 : damageable.getHealth() / 2.0;
   }

   public static void setHealth(Damageable damageable, double health) {
      damageable.setHealth(Math2.fit(0.0, health, getMaxHealth(damageable)) * 2.0);
   }

   public static double getMaxHealth(Damageable damageable) {
      AttributeInstance attributeInstance = ((Attributable)damageable).getAttribute(MAX_HEALTH);

      assert attributeInstance != null;

      return attributeInstance.getValue() / 2.0;
   }

   public static void setMaxHealth(Damageable damageable, double health) {
      AttributeInstance attributeInstance = ((Attributable)damageable).getAttribute(MAX_HEALTH);

      assert attributeInstance != null;

      attributeInstance.setBaseValue(health * 2.0);
   }

   public static void damage(Damageable damageable, double damage) {
      if (damage < 0.0) {
         heal(damageable, -damage);
      } else {
         damageable.damage(damage * 2.0);
      }
   }

   public static void damage(Damageable damageable, double damage, DamageSource cause) {
      if (damage < 0.0) {
         heal(damageable, -damage);
      } else {
         damageable.damage(damage * 2.0, cause);
      }
   }

   public static void heal(Damageable damageable, double health) {
      if (health < 0.0) {
         damage(damageable, -health);
      } else {
         setHealth(damageable, getHealth(damageable) + health);
      }
   }

   public static double getDamage(EntityDamageEvent event) {
      return event.getDamage() / 2.0;
   }

   public static double getFinalDamage(EntityDamageEvent event) {
      return event.getFinalDamage() / 2.0;
   }

   public static void setDamage(EntityDamageEvent event, double damage) {
      event.setDamage(damage * 2.0);
      if (event.getEntity() instanceof LivingEntity) {
         ((LivingEntity)event.getEntity()).setLastDamage(damage * 2.0);
      }
   }

   public static void setDamageCause(Damageable damageable, DamageCause cause) {
      if (OLD_DAMAGE_EVENT_CONSTRUCTOR != null) {
         try {
            damageable.setLastDamageCause(OLD_DAMAGE_EVENT_CONSTRUCTOR.newInstance(damageable, cause, 0));
         } catch (IllegalAccessException | InvocationTargetException | InstantiationException var3) {
            Skript.exception("Failed to set last damage cause");
         }
      } else {
         damageable.setLastDamageCause(new EntityDamageEvent(damageable, cause, DamageSource.builder(DamageType.GENERIC).build(), 0.0));
      }
   }

   static {
      Constructor<EntityDamageEvent> constructor = null;

      try {
         constructor = EntityDamageEvent.class.getConstructor(Damageable.class, DamageCause.class, double.class);
      } catch (NoSuchMethodException var2) {
      }

      OLD_DAMAGE_EVENT_CONSTRUCTOR = constructor;
      MAX_HEALTH = (Attribute)Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
   }
}

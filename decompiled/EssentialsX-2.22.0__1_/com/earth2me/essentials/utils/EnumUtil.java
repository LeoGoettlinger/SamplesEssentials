package com.earth2me.essentials.utils;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

public final class EnumUtil {
   private EnumUtil() {
   }

   public static <T extends Enum<T>> T valueOf(final Class<T> enumClass, final String... names) {
      for (String name : names) {
         try {
            Field enumField = enumClass.getDeclaredField(name);
            if (enumField.isEnumConstant()) {
               return (T)enumField.get(null);
            }
         } catch (IllegalAccessException | NoSuchFieldException var7) {
         }
      }

      return null;
   }

   public static <T extends Enum<T>> Set<T> getAllMatching(final Class<T> enumClass, final String... names) {
      Set<T> set = EnumSet.noneOf(enumClass);

      for (String name : names) {
         try {
            Field enumField = enumClass.getDeclaredField(name);
            if (enumField.isEnumConstant()) {
               set.add((T)enumField.get(null));
            }
         } catch (IllegalAccessException | NoSuchFieldException var8) {
         }
      }

      return set;
   }

   public static Material getMaterial(final String... names) {
      return valueOf(Material.class, names);
   }

   public static Statistic getStatistic(final String... names) {
      return valueOf(Statistic.class, names);
   }

   public static EntityType getEntityType(final String... names) {
      return valueOf(EntityType.class, names);
   }
}

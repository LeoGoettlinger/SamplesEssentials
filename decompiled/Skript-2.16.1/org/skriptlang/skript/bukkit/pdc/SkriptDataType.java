package org.skriptlang.skript.bukkit.pdc;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class SkriptDataType implements PersistentDataType<PersistentDataContainer, Object> {
   private static SkriptDataType instance = null;

   public static SkriptDataType get() {
      if (instance == null) {
         instance = new SkriptDataType();
      }

      return instance;
   }

   @NotNull
   public Class<PersistentDataContainer> getPrimitiveType() {
      return PersistentDataContainer.class;
   }

   @NotNull
   public Class<Object> getComplexType() {
      return Object.class;
   }

   @NotNull
   public PersistentDataContainer toPrimitive(@NotNull Object complex, @NotNull PersistentDataAdapterContext context) {
      return PDCSerializer.serialize(complex, context);
   }

   @NotNull
   public Object fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
      return PDCSerializer.deserialize(primitive, context);
   }
}

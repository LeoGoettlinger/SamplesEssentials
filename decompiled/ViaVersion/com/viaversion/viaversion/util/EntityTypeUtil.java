package com.viaversion.viaversion.util;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EntityTypeUtil {
   private static final EntityType[] EMPTY_ARRAY = new EntityType[0];

   public static EntityType[] toOrderedArray(EntityType[] values) {
      List<EntityType> types = new ArrayList<>();

      for (EntityType type : values) {
         if (type.getId() != -1) {
            types.add(type);
         }
      }

      types.sort(Comparator.comparingInt(EntityType::getId));
      return types.toArray(EMPTY_ARRAY);
   }

   public static <T extends EntityType> void initialize(
      T[] values, EntityType[] typesToFill, Protocol<?, ?, ?, ?> protocol, EntityTypeUtil.EntityIdSetter<T> idSetter
   ) {
      FullMappings mappings = protocol.getMappingData().getEntityMappings();

      for (T type : values) {
         if (!type.isAbstractType()) {
            int id = mappings.mappedId(type.identifier());
            Preconditions.checkArgument(id != -1, "Entity type %s has no id", new Object[]{type.identifier()});
            Preconditions.checkArgument(
               id < typesToFill.length,
               "Types array too small (missing enum entry?). Tried setting id %s into array of size %s",
               new Object[]{id, typesToFill.length}
            );
            idSetter.setId(type, id);
            typesToFill[id] = type;
         }
      }

      if (typesToFill.length != mappings.mappedSize()) {
         throw new IllegalArgumentException("typesToFill length doesn't match the amount of entity types: " + typesToFill.length + " != " + mappings.size());
      }
   }

   public static EntityType[] createSizedArray(EntityType[] values) {
      int count = 0;

      for (EntityType type : values) {
         if (!type.isAbstractType()) {
            count++;
         }
      }

      return new EntityType[count];
   }

   public static EntityType getTypeFromId(EntityType[] values, int typeId, EntityType fallback) {
      EntityType type;
      if (typeId >= 0 && typeId < values.length && (type = values[typeId]) != null) {
         return type;
      } else {
         Via.getPlatform().getLogger().severe("Could not find " + fallback.getClass().getSimpleName() + " type id " + typeId);
         return fallback;
      }
   }

   @FunctionalInterface
   public interface EntityIdSetter<T extends EntityType> {
      void setId(T var1, int var2);
   }
}

package com.earth2me.essentials.config.serializers;

import com.earth2me.essentials.libs.configurate.serialize.ScalarSerializer;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import java.lang.reflect.Type;
import java.util.function.Predicate;
import org.bukkit.Material;

public class MaterialTypeSerializer extends ScalarSerializer<Material> {
   public MaterialTypeSerializer() {
      super(Material.class);
   }

   public Material deserialize(Type type, Object obj) throws SerializationException {
      return obj instanceof String ? Material.matchMaterial((String)obj) : Material.AIR;
   }

   protected Object serialize(Material item, Predicate<Class<?>> typeSupported) {
      return item == null ? null : item.name();
   }
}

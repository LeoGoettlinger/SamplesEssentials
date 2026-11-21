package de.oliver.fancynpcs.v1_21_9.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_9.ReflectionHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Rabbit.Variant;
import org.bukkit.entity.EntityType;

public class RabbitAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(
         new NpcAttribute("variant", Arrays.stream(Variant.values()).map(Enum::name).toList(), List.of(EntityType.RABBIT), RabbitAttributes::setVariant)
      );
      return attributes;
   }

   private static void setVariant(Npc npc, String value) {
      Rabbit rabbit = ReflectionHelper.getEntity(npc);
      Variant variant = Variant.valueOf(value.toUpperCase());
      rabbit.setVariant(variant);
   }
}

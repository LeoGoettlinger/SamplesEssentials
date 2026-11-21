package de.oliver.fancynpcs.v1_21_11.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_11.ReflectionHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.fox.Fox.Variant;
import org.bukkit.entity.EntityType;

public class FoxAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("type", Arrays.stream(Variant.values()).map(Enum::name).toList(), List.of(EntityType.FOX), FoxAttributes::setType));
      attributes.add(new NpcAttribute("pose", List.of("standing", "sleeping", "sitting"), List.of(EntityType.FOX), FoxAttributes::setPose));
      return attributes;
   }

   private static void setType(Npc npc, String value) {
      Fox fox = ReflectionHelper.getEntity(npc);
      Variant type = Variant.valueOf(value.toUpperCase());
      fox.setVariant(type);
   }

   private static void setPose(Npc npc, String value) {
      Fox fox = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "standing":
            fox.setIsCrouching(false);
            fox.setSleeping(false);
            fox.setSitting(false, false);
            break;
         case "sleeping":
            fox.setSleeping(true);
            fox.setSitting(false, false);
            fox.setIsCrouching(false);
            break;
         case "sitting":
            fox.setSitting(true, false);
            fox.setSleeping(false);
            fox.setIsCrouching(false);
      }
   }
}

package de.oliver.fancynpcs.v26_1_1.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v26_1_1.ReflectionHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.parrot.Parrot.Variant;
import org.bukkit.entity.EntityType;

public class ParrotAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(
         new NpcAttribute("variant", Arrays.stream(Variant.values()).map(Enum::name).toList(), List.of(EntityType.PARROT), ParrotAttributes::setVariant)
      );
      attributes.add(new NpcAttribute("pose", List.of("standing", "sitting"), List.of(EntityType.PARROT), ParrotAttributes::setPose));
      return attributes;
   }

   private static void setVariant(Npc npc, String value) {
      Parrot parrot = ReflectionHelper.getEntity(npc);
      Variant variant = Variant.valueOf(value.toUpperCase());
      parrot.setVariant(variant);
   }

   private static void setPose(Npc npc, String value) {
      Parrot parrot = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "standing":
            parrot.setOrderedToSit(false);
            parrot.setInSittingPose(false, false);
            break;
         case "sitting":
            parrot.setOrderedToSit(true);
            parrot.setInSittingPose(true, false);
      }
   }
}

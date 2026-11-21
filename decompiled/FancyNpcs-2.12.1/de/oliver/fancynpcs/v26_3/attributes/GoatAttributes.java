package de.oliver.fancynpcs.v26_3.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v26_3.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.animal.goat.Goat;
import org.bukkit.entity.EntityType;

public class GoatAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("horns", List.of("none", "left", "right", "both"), List.of(EntityType.GOAT), GoatAttributes::setHorns));
      return attributes;
   }

   private static void setHorns(Npc npc, String value) {
      Goat goat = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "none":
            goat.getEntityData().set(Goat.DATA_HAS_LEFT_HORN, false);
            goat.getEntityData().set(Goat.DATA_HAS_RIGHT_HORN, false);
            break;
         case "both":
            goat.getEntityData().set(Goat.DATA_HAS_LEFT_HORN, true);
            goat.getEntityData().set(Goat.DATA_HAS_RIGHT_HORN, true);
            break;
         case "left":
            goat.getEntityData().set(Goat.DATA_HAS_LEFT_HORN, true);
            goat.getEntityData().set(Goat.DATA_HAS_RIGHT_HORN, false);
            break;
         case "right":
            goat.getEntityData().set(Goat.DATA_HAS_RIGHT_HORN, true);
            goat.getEntityData().set(Goat.DATA_HAS_LEFT_HORN, false);
      }
   }
}

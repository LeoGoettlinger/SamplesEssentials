package de.oliver.fancynpcs.v26_1_1.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v26_1_1.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.animal.bee.Bee;
import org.bukkit.entity.EntityType;

public class BeeAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("angry", List.of("true", "false"), List.of(EntityType.BEE), BeeAttributes::setAngry));
      attributes.add(new NpcAttribute("sting", List.of("true", "false"), List.of(EntityType.BEE), BeeAttributes::setSting));
      attributes.add(new NpcAttribute("nectar", List.of("true", "false"), List.of(EntityType.BEE), BeeAttributes::setNectar));
      attributes.add(new NpcAttribute("rolling", List.of("true", "false"), List.of(EntityType.BEE), BeeAttributes::setRolling));
      return attributes;
   }

   private static void setAngry(Npc npc, String value) {
      Bee bee = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "true":
            bee.setPersistentAngerEndTime(1L);
            break;
         case "false":
            bee.setPersistentAngerEndTime(0L);
      }
   }

   private static void setSting(Npc npc, String value) {
      Bee bee = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "true":
            bee.setHasStung(false);
            break;
         case "false":
            bee.setHasStung(true);
      }
   }

   private static void setNectar(Npc npc, String value) {
      Bee bee = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "true":
            bee.setHasNectar(true);
            break;
         case "false":
            bee.setHasNectar(false);
      }
   }

   private static void setRolling(Npc npc, String value) {
      Bee bee = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "true":
            bee.setRolling(true);
            break;
         case "false":
            bee.setRolling(false);
      }
   }
}

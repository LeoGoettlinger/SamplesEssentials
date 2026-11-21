package de.oliver.fancynpcs.v26_2.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v26_2.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.monster.Vex;
import org.bukkit.entity.EntityType;

public class VexAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("charging", List.of("true", "false"), List.of(EntityType.VEX), VexAttributes::setCharging));
      return attributes;
   }

   private static void setCharging(Npc npc, String value) {
      Vex vex = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "true":
            vex.setIsCharging(true);
            break;
         case "false":
            vex.setIsCharging(false);
      }
   }
}

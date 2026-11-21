package de.oliver.fancynpcs.v1_21_5.attributes;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_5.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.animal.camel.Camel;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

public class CamelAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("pose", List.of("standing", "sitting", "dashing"), List.of(EntityType.CAMEL), CamelAttributes::setPose));
      return attributes;
   }

   private static void setPose(Npc npc, String value) {
      Camel camel = ReflectionHelper.getEntity(npc);
      Bukkit.getGlobalRegionScheduler().run(FancyNpcsPlugin.get().getPlugin(), var2x -> {
         String s0$ = value.toLowerCase();
         switch (s0$) {
            case "standing":
               camel.setDashing(false);
               camel.standUp();
               break;
            case "sitting":
               camel.setDashing(false);
               camel.sitDown();
               break;
            case "dashing":
               camel.standUpInstantly();
               camel.setDashing(true);
         }
      });
   }
}

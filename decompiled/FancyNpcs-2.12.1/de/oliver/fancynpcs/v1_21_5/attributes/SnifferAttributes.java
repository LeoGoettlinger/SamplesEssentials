package de.oliver.fancynpcs.v1_21_5.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_5.ReflectionHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.sniffer.Sniffer.State;
import org.bukkit.entity.EntityType;

public class SnifferAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(
         new NpcAttribute("state", Arrays.stream(State.values()).map(Enum::name).toList(), List.of(EntityType.SNIFFER), SnifferAttributes::setState)
      );
      return attributes;
   }

   private static void setState(Npc npc, String value) {
      Sniffer sniffer = ReflectionHelper.getEntity(npc);
      State state = State.valueOf(value.toUpperCase());
      sniffer.transitionTo(state);
   }
}

package de.oliver.fancynpcs.v1_21_6.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_6.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.armadillo.Armadillo.ArmadilloState;
import org.bukkit.entity.EntityType;

public class ArmadilloAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("pose", List.of("idle", "rolling", "unrolling", "scared"), List.of(EntityType.ARMADILLO), ArmadilloAttributes::setPose));
      return attributes;
   }

   private static void setPose(Npc npc, String value) {
      Armadillo armadillo = ReflectionHelper.getEntity(npc);
      ArmadilloState state = ArmadilloState.valueOf(value.toUpperCase());
      armadillo.switchToState(state);
   }
}

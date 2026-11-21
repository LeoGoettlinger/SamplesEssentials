package de.oliver.fancynpcs.v1_21_6.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_6.ReflectionHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.Axolotl.Variant;
import org.bukkit.entity.EntityType;

public class AxolotlAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(
         new NpcAttribute("variant", Arrays.stream(Variant.values()).map(Enum::name).toList(), List.of(EntityType.AXOLOTL), AxolotlAttributes::setVariant)
      );
      attributes.add(new NpcAttribute("playing_dead", List.of("true", "false"), List.of(EntityType.AXOLOTL), AxolotlAttributes::setPlayingDead));
      return attributes;
   }

   private static void setVariant(Npc npc, String value) {
      Axolotl axolotl = ReflectionHelper.getEntity(npc);
      Variant variant = Variant.valueOf(value.toUpperCase());
      axolotl.setVariant(variant);
   }

   private static void setPlayingDead(Npc npc, String value) {
      Axolotl axolotl = ReflectionHelper.getEntity(npc);
      boolean playingDead = Boolean.parseBoolean(value);
      axolotl.setPlayingDead(playingDead);
   }
}

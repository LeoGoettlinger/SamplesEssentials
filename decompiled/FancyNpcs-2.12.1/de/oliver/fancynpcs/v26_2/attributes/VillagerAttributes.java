package de.oliver.fancynpcs.v26_2.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v26_2.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import org.bukkit.entity.EntityType;

public class VillagerAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(
         new NpcAttribute(
            "profession",
            BuiltInRegistries.VILLAGER_PROFESSION.keySet().stream().<String>map(Identifier::getPath).toList(),
            List.of(EntityType.VILLAGER),
            VillagerAttributes::setProfession
         )
      );
      attributes.add(
         new NpcAttribute(
            "type",
            BuiltInRegistries.VILLAGER_TYPE.keySet().stream().<String>map(Identifier::getPath).toList(),
            List.of(EntityType.VILLAGER),
            VillagerAttributes::setType
         )
      );
      return attributes;
   }

   private static void setProfession(Npc npc, String value) {
      Villager villager = ReflectionHelper.getEntity(npc);
      Holder<VillagerProfession> profession = (Holder<VillagerProfession>)BuiltInRegistries.VILLAGER_PROFESSION.get(Identifier.tryParse(value)).orElseThrow();
      villager.setVillagerData(villager.getVillagerData().withProfession(profession));
   }

   private static void setType(Npc npc, String value) {
      Villager villager = ReflectionHelper.getEntity(npc);
      Holder<VillagerType> type = (Holder<VillagerType>)BuiltInRegistries.VILLAGER_TYPE.get(Identifier.tryParse(value)).orElseThrow();
      villager.setVillagerData(villager.getVillagerData().withType(type));
   }
}

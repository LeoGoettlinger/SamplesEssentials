package de.oliver.fancynpcs.v1_21_6.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_6.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.level.block.Block;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;

public class BlockDisplayAttributes {
   private static final List<String> BLOCKS = Registry.MATERIAL.stream().filter(Material::isBlock).map(it -> it.key().value()).toList();

   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("block", BLOCKS, List.of(EntityType.BLOCK_DISPLAY), BlockDisplayAttributes::setBlock));
      return attributes;
   }

   private static void setBlock(Npc npc, String value) {
      BlockDisplay display = ReflectionHelper.getEntity(npc);
      Block block = (Block)BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(value.toLowerCase()));
      display.setBlockState(block.defaultBlockState());
   }
}

package de.oliver.fancynpcs.v1_21_5.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_5.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.item.DyeColor;
import org.bukkit.entity.EntityType;

public class CatAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("variant", getCatVariantRegistry().listElementIds().map(id -> {
         ResourceLocation location = id.location();
         return location.getNamespace().equals("minecraft") ? location.getPath() : location.toString();
      }).toList(), List.of(EntityType.CAT), CatAttributes::setVariant));
      attributes.add(new NpcAttribute("pose", List.of("standing", "sleeping", "sitting"), List.of(EntityType.CAT), CatAttributes::setPose));
      attributes.add(
         new NpcAttribute(
            "collar_color",
            List.of(
               "RED",
               "BLUE",
               "YELLOW",
               "GREEN",
               "PURPLE",
               "ORANGE",
               "LIME",
               "MAGENTA",
               "BROWN",
               "WHITE",
               "GRAY",
               "LIGHT_GRAY",
               "LIGHT_BLUE",
               "BLACK",
               "CYAN",
               "PINK",
               "NONE"
            ),
            List.of(EntityType.CAT),
            CatAttributes::setCollarColor
         )
      );
      return attributes;
   }

   private static void setVariant(Npc npc, String value) {
      Cat cat = ReflectionHelper.getEntity(npc);
      Holder<CatVariant> variant = (Holder<CatVariant>)getCatVariantRegistry()
         .get(ResourceKey.create(Registries.CAT_VARIANT, ResourceLocation.parse(value.toLowerCase())))
         .orElseThrow();
      cat.setVariant(variant);
   }

   private static void setPose(Npc npc, String value) {
      Cat cat = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "standing":
            cat.setInSittingPose(false, false);
            cat.setLying(false);
            break;
         case "sleeping":
            cat.setInSittingPose(false, false);
            cat.setLying(true);
            break;
         case "sitting":
            cat.setLying(false);
            cat.setOrderedToSit(true);
            cat.setInSittingPose(true, false);
      }
   }

   private static RegistryLookup<CatVariant> getCatVariantRegistry() {
      return MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.CAT_VARIANT);
   }

   private static void setCollarColor(Npc npc, String value) {
      Cat cat = ReflectionHelper.getEntity(npc);
      if (!value.equalsIgnoreCase("none") && !value.isEmpty()) {
         try {
            DyeColor color = DyeColor.valueOf(value.toUpperCase());
            if (!cat.isTame()) {
               cat.setTame(true, false);
            }

            cat.setCollarColor(color);
         } catch (IllegalArgumentException var4) {
            System.out.println("Invalid cat collar color: " + value);
         }
      } else {
         cat.setTame(false, false);
      }
   }
}

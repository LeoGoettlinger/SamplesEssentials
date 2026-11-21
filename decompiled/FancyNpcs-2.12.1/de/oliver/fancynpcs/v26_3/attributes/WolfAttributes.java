package de.oliver.fancynpcs.v26_3.attributes;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v26_3.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.item.DyeColor;
import org.bukkit.entity.EntityType;

public class WolfAttributes {
   public static List<NpcAttribute> getAllAttributes() {
      List<NpcAttribute> attributes = new ArrayList<>();
      attributes.add(new NpcAttribute("pose", List.of("standing", "sitting"), List.of(EntityType.WOLF), WolfAttributes::setPose));
      attributes.add(new NpcAttribute("angry", List.of("true", "false"), List.of(EntityType.WOLF), WolfAttributes::setAngry));
      attributes.add(new NpcAttribute("variant", getWolfVariantRegistry().listElementIds().map(id -> {
         Identifier identifier = id.identifier();
         return identifier.getNamespace().equals("minecraft") ? identifier.getPath() : identifier.toString();
      }).toList(), List.of(EntityType.WOLF), WolfAttributes::setVariant));
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
            List.of(EntityType.WOLF),
            WolfAttributes::setCollarColor
         )
      );
      return attributes;
   }

   private static void setPose(Npc npc, String value) {
      Wolf wolf = ReflectionHelper.getEntity(npc);
      String var3 = value.toLowerCase();
      switch (var3) {
         case "standing":
            wolf.setInSittingPose(false, false);
            break;
         case "sitting":
            wolf.setInSittingPose(true, false);
      }
   }

   private static void setAngry(Npc npc, String value) {
      Wolf wolf = ReflectionHelper.getEntity(npc);
      boolean angry = Boolean.parseBoolean(value.toLowerCase());
      wolf.setTimeToRemainAngry(angry ? 100L : 0L);
   }

   private static void setVariant(Npc npc, String value) {
      Wolf wolf = ReflectionHelper.getEntity(npc);
      Registry<WolfVariant> registry = wolf.level().registryAccess().lookupOrThrow(Registries.WOLF_VARIANT);
      Identifier variantLocation = Identifier.tryParse("minecraft:" + value.toLowerCase());
      if (variantLocation == null) {
         System.out.println("Invalid variant name: " + value);
      } else {
         WolfVariant variant = (WolfVariant)registry.getOptional(variantLocation).orElse(null);
         if (variant == null) {
            System.out.println("Wolf variant not found: " + variantLocation);
         } else {
            registry.getResourceKey(variant).ifPresentOrElse(key -> {
               Holder<WolfVariant> holder = registry.wrapAsHolder(variant);
               wolf.setVariant(holder);
            }, () -> System.out.println("Wolf variant not registered: " + variantLocation));
         }
      }
   }

   private static void setCollarColor(Npc npc, String value) {
      Wolf wolf = ReflectionHelper.getEntity(npc);
      if (!value.equalsIgnoreCase("none") && !value.isEmpty()) {
         try {
            DyeColor color = DyeColor.valueOf(value.toUpperCase());
            if (!wolf.isTame()) {
               wolf.setTame(true, false);
            }

            wolf.setCollarColor(color);
         } catch (IllegalArgumentException var4) {
            System.out.println("Invalid wolf collar color: " + value);
         }
      } else {
         wolf.setTame(false, false);
      }
   }

   private static RegistryLookup<WolfVariant> getWolfVariantRegistry() {
      return MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.WOLF_VARIANT);
   }
}

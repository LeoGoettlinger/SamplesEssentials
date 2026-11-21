package de.theredend2000.advancedhunt.lib.kyori.adventure.platform.bukkit;

import de.theredend2000.advancedhunt.lib.kyori.adventure.platform.facet.Facet;
import de.theredend2000.advancedhunt.lib.kyori.adventure.platform.facet.FacetComponentFlattener;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener.ComponentFlattener;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.JSONOptions;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

public final class BukkitComponentSerializer {
   private static final boolean IS_1_13 = MinecraftReflection.findEnum(Material.class, "BLUE_ICE") != null;
   private static final boolean IS_1_16 = MinecraftReflection.findEnum(Material.class, "NETHERITE_PICKAXE") != null;
   private static final Collection<FacetComponentFlattener.Translator<Server>> TRANSLATORS = Facet.of(
      SpigotFacet.Translator::new, CraftBukkitFacet.Translator::new
   );
   private static final LegacyComponentSerializer LEGACY_SERIALIZER;
   private static final GsonComponentSerializer GSON_SERIALIZER;
   static final ComponentFlattener FLATTENER = FacetComponentFlattener.get(Bukkit.getServer(), TRANSLATORS);

   private BukkitComponentSerializer() {
   }

   @NotNull
   public static LegacyComponentSerializer legacy() {
      return LEGACY_SERIALIZER;
   }

   @NotNull
   public static GsonComponentSerializer gson() {
      return GSON_SERIALIZER;
   }

   static {
      if (IS_1_13) {
         GSON_SERIALIZER = GsonComponentSerializer.builder().options(JSONOptions.byDataVersion().at(Bukkit.getUnsafe().getDataVersion())).build();
      } else {
         GSON_SERIALIZER = GsonComponentSerializer.builder()
            .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
            .options(JSONOptions.byDataVersion().at(0))
            .build();
      }

      if (IS_1_16) {
         LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().flattener(FLATTENER).build();
      } else {
         LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('§').flattener(FLATTENER).build();
      }
   }
}

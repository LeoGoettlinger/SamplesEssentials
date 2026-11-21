package net.kyori.adventure.platform.bukkit;

import java.util.Collection;
import net.kyori.adventure.platform.facet.Facet;
import net.kyori.adventure.platform.facet.FacetComponentFlattener;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

public final class BukkitComponentSerializer {
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
      if (IS_1_16) {
         LEGACY_SERIALIZER = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().flattener(FLATTENER).build();
         GSON_SERIALIZER = GsonComponentSerializer.builder().legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get()).build();
      } else {
         LEGACY_SERIALIZER = LegacyComponentSerializer.builder().character('§').flattener(FLATTENER).build();
         GSON_SERIALIZER = GsonComponentSerializer.builder()
            .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
            .emitLegacyHoverEvent()
            .downsampleColors()
            .build();
      }
   }
}

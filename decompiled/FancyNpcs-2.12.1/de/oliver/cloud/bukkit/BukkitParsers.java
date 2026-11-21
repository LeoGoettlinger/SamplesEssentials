package de.oliver.cloud.bukkit;

import de.oliver.cloud.CommandManager;
import de.oliver.cloud.bukkit.annotation.specifier.AllowEmptySelection;
import de.oliver.cloud.bukkit.annotation.specifier.DefaultNamespace;
import de.oliver.cloud.bukkit.annotation.specifier.RequireExplicitNamespace;
import de.oliver.cloud.bukkit.data.MultipleEntitySelector;
import de.oliver.cloud.bukkit.data.MultiplePlayerSelector;
import de.oliver.cloud.bukkit.internal.CraftBukkitReflection;
import de.oliver.cloud.bukkit.parser.BlockPredicateParser;
import de.oliver.cloud.bukkit.parser.EnchantmentParser;
import de.oliver.cloud.bukkit.parser.ItemStackParser;
import de.oliver.cloud.bukkit.parser.ItemStackPredicateParser;
import de.oliver.cloud.bukkit.parser.MaterialParser;
import de.oliver.cloud.bukkit.parser.NamespacedKeyParser;
import de.oliver.cloud.bukkit.parser.OfflinePlayerParser;
import de.oliver.cloud.bukkit.parser.PlayerParser;
import de.oliver.cloud.bukkit.parser.WorldParser;
import de.oliver.cloud.bukkit.parser.location.Location2DParser;
import de.oliver.cloud.bukkit.parser.location.LocationParser;
import de.oliver.cloud.bukkit.parser.selector.MultipleEntitySelectorParser;
import de.oliver.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import de.oliver.cloud.bukkit.parser.selector.SingleEntitySelectorParser;
import de.oliver.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import de.oliver.cloud.parser.ParserParameters;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL
)
public final class BukkitParsers {
   private BukkitParsers() {
   }

   public static <C> void register(final CommandManager<C> manager) {
      manager.parserRegistry()
         .registerParser(WorldParser.worldParser())
         .registerParser(MaterialParser.materialParser())
         .registerParser(PlayerParser.playerParser())
         .registerParser(OfflinePlayerParser.offlinePlayerParser())
         .registerParser(EnchantmentParser.enchantmentParser())
         .registerParser(LocationParser.locationParser())
         .registerParser(Location2DParser.location2DParser())
         .registerParser(ItemStackParser.itemStackParser())
         .registerParser(SingleEntitySelectorParser.singleEntitySelectorParser())
         .registerParser(SinglePlayerSelectorParser.singlePlayerSelectorParser());
      manager.parserRegistry()
         .registerAnnotationMapper(
            AllowEmptySelection.class, (annotation, type) -> ParserParameters.single(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, annotation.value())
         );
      manager.parserRegistry()
         .registerParserSupplier(
            TypeToken.get(MultipleEntitySelector.class),
            parserParameters -> new MultipleEntitySelectorParser<>(parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true))
         );
      manager.parserRegistry()
         .registerParserSupplier(
            TypeToken.get(MultiplePlayerSelector.class),
            parserParameters -> new MultiplePlayerSelectorParser<>(parserParameters.get(BukkitParserParameters.ALLOW_EMPTY_SELECTOR_RESULT, true))
         );
      if (CraftBukkitReflection.classExists("org.bukkit.NamespacedKey")) {
         registerParserSupplierFor(manager, NamespacedKeyParser.class);
         manager.parserRegistry()
            .registerAnnotationMapper(
               RequireExplicitNamespace.class, (annotation, type) -> ParserParameters.single(BukkitParserParameters.REQUIRE_EXPLICIT_NAMESPACE, true)
            );
         manager.parserRegistry()
            .registerAnnotationMapper(
               DefaultNamespace.class, (annotation, type) -> ParserParameters.single(BukkitParserParameters.DEFAULT_NAMESPACE, annotation.value())
            );
      }

      if (manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
         registerParserSupplierFor(manager, ItemStackPredicateParser.class);
         registerParserSupplierFor(manager, BlockPredicateParser.class);
      }
   }

   private static void registerParserSupplierFor(final CommandManager<?> manager, @NonNull final Class<?> argumentClass) {
      try {
         Method registerParserSuppliers = argumentClass.getDeclaredMethod("registerParserSupplier", CommandManager.class);
         registerParserSuppliers.setAccessible(true);
         registerParserSuppliers.invoke(null, manager);
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException(var3);
      }
   }
}

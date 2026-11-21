package vrx.economy.vRXClans.libs.cloud.bukkit;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.bukkit.annotation.specifier.AllowEmptySelection;
import vrx.economy.vRXClans.libs.cloud.bukkit.annotation.specifier.DefaultNamespace;
import vrx.economy.vRXClans.libs.cloud.bukkit.annotation.specifier.RequireExplicitNamespace;
import vrx.economy.vRXClans.libs.cloud.bukkit.data.MultipleEntitySelector;
import vrx.economy.vRXClans.libs.cloud.bukkit.data.MultiplePlayerSelector;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CraftBukkitReflection;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.BlockPredicateParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.EnchantmentParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.ItemStackParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.ItemStackPredicateParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.MaterialParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.NamespacedKeyParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.OfflinePlayerParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.PlayerParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.WorldParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.location.Location2DParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.location.LocationParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector.MultipleEntitySelectorParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector.SingleEntitySelectorParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserParameters;

@API(
   status = API.Status.INTERNAL
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

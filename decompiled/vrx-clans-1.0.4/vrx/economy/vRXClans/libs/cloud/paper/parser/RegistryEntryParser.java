package vrx.economy.vRXClans.libs.cloud.paper.parser;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.bukkit.parser.NamespacedKeyParser;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.MappedArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestion;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProvider;

@API(
   status = API.Status.EXPERIMENTAL
)
public final class RegistryEntryParser<C, E extends Keyed>
   implements ArgumentParser<C, RegistryEntryParser.RegistryEntry<E>>,
   SuggestionProvider<C>,
   MappedArgumentParser<C, NamespacedKey, RegistryEntryParser.RegistryEntry<E>> {
   private final ParserDescriptor<C, NamespacedKey> keyParser = NamespacedKeyParser.namespacedKeyParser();
   private final RegistryKey<E> registryKey;

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C, E extends Keyed> ParserDescriptor<C, RegistryEntryParser.RegistryEntry<E>> registryEntryParser(
      final RegistryKey<E> registryKey, final TypeToken<E> elementType
   ) {
      return ParserDescriptor.of(
         new RegistryEntryParser<>(registryKey),
         (TypeToken<RegistryEntryParser.RegistryEntry<E>>)TypeToken.get(
            TypeFactory.parameterizedClass(RegistryEntryParser.RegistryEntry.class, elementType.getType())
         )
      );
   }

   public RegistryEntryParser(final RegistryKey<E> registryKey) {
      this.registryKey = registryKey;
   }

   @NonNull
   @Override
   public ArgumentParseResult<RegistryEntryParser.RegistryEntry<E>> parse(
      @NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput
   ) {
      return this.keyParser
         .parser()
         .parse(commandContext, commandInput)
         .flatMapSuccess(
            key -> {
               Registry<E> registry = RegistryAccess.registryAccess().getRegistry(this.registryKey);
               E value = (E)registry.get(key);
               return value == null
                  ? ArgumentParseResult.failure(new RegistryEntryParser.ParseException(key.asString(), this.registryKey, commandContext))
                  : ArgumentParseResult.success(RegistryEntryImpl.of(value, key));
            }
         );
   }

   @NonNull
   @Override
   public ArgumentParser<C, NamespacedKey> baseParser() {
      return this.keyParser.parser();
   }

   @NonNull
   @Override
   public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
      @NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input
   ) {
      List<Suggestion> completions = new ArrayList<>();
      Registry<E> registry = RegistryAccess.registryAccess().getRegistry(this.registryKey);
      registry.stream().<NamespacedKey>map(registry::getKeyOrThrow).forEach(key -> {
         if (input.hasRemainingInput() && key.getNamespace().equals("minecraft")) {
            completions.add(Suggestion.suggestion(key.getKey()));
         }

         completions.add(Suggestion.suggestion(key.getNamespace() + ':' + key.getKey()));
      });
      return CompletableFuture.completedFuture(completions);
   }

   public static final class ParseException extends ParserException {
      private final String input;
      private final RegistryKey<Object> registryKey;

      public ParseException(@NonNull final String input, @NonNull final RegistryKey<Object> registryKey, @NonNull final CommandContext<?> context) {
         super(
            RegistryEntryParser.class,
            context,
            BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_MISSING,
            CaptionVariable.of("input", input),
            CaptionVariable.of("registry", registryKey.key().asString())
         );
         this.input = input;
         this.registryKey = registryKey;
      }

      @NonNull
      public String input() {
         return this.input;
      }

      @NonNull
      public RegistryKey<Object> registryKey() {
         return this.registryKey;
      }
   }

   @Immutable
   public interface RegistryEntry<E> {
      E value();

      NamespacedKey key();
   }
}

package vrx.economy.vRXClans.libs.cloud.bukkit.parser;

import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitParserParameters;
import vrx.economy.vRXClans.libs.cloud.caption.Caption;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.BlockingSuggestionProvider;

public final class NamespacedKeyParser<C> implements ArgumentParser<C, NamespacedKey>, BlockingSuggestionProvider.Strings<C> {
   private final boolean requireExplicitNamespace;
   private final String defaultNamespace;

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, NamespacedKey> namespacedKeyParser() {
      return namespacedKeyParser(false);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, NamespacedKey> namespacedKeyParser(final boolean requireExplicitNamespace) {
      return namespacedKeyParser(requireExplicitNamespace, "minecraft");
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, NamespacedKey> namespacedKeyParser(final boolean requireExplicitNamespace, @NonNull final String defaultNamespace) {
      return ParserDescriptor.of(new NamespacedKeyParser<>(requireExplicitNamespace, defaultNamespace), NamespacedKey.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, NamespacedKey> namespacedKeyComponent() {
      return CommandComponent.<C, NamespacedKey>builder().parser(namespacedKeyParser());
   }

   public NamespacedKeyParser(final boolean requireExplicitNamespace, final String defaultNamespace) {
      this.requireExplicitNamespace = requireExplicitNamespace;
      this.defaultNamespace = defaultNamespace;
   }

   @NonNull
   @Override
   public ArgumentParseResult<NamespacedKey> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.peekString();
      String[] split = input.split(":");
      int maxSemi = split.length > 1 ? 1 : 0;
      if (input.length() - input.replace(":", "").length() > maxSemi) {
         return ArgumentParseResult.failure(
            new NamespacedKeyParser.NamespacedKeyParseException(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, input, commandContext)
         );
      } else {
         try {
            NamespacedKey ret;
            if (split.length == 1) {
               if (this.requireExplicitNamespace) {
                  return ArgumentParseResult.failure(
                     new NamespacedKeyParser.NamespacedKeyParseException(
                        BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE, input, commandContext
                     )
                  );
               }

               ret = new NamespacedKey(this.defaultNamespace, commandInput.readString());
            } else {
               if (split.length != 2) {
                  return ArgumentParseResult.failure(
                     new NamespacedKeyParser.NamespacedKeyParseException(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, input, commandContext)
                  );
               }

               ret = new NamespacedKey(commandInput.readUntilAndSkip(':'), commandInput.readString());
            }

            return ArgumentParseResult.success(ret);
         } catch (IllegalArgumentException var8) {
            Caption caption = var8.getMessage().contains("namespace")
               ? BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE
               : BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY;
            return ArgumentParseResult.failure(new NamespacedKeyParser.NamespacedKeyParseException(caption, input, commandContext));
         }
      }
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      List<String> ret = new ArrayList<>();
      ret.add(this.defaultNamespace + ":");
      String token = input.peekString();
      if (!token.contains(":") && !token.isEmpty()) {
         ret.add(token + ":");
      }

      return ret;
   }

   private static <C> void registerParserSupplier(@NonNull final CommandManager<C> commandManager) {
      commandManager.parserRegistry()
         .registerParserSupplier(
            TypeToken.get(NamespacedKey.class),
            params -> new NamespacedKeyParser<>(
               params.has(BukkitParserParameters.REQUIRE_EXPLICIT_NAMESPACE), params.get(BukkitParserParameters.DEFAULT_NAMESPACE, "minecraft")
            )
         );
   }

   public static final class NamespacedKeyParseException extends ParserException {
      private final String input;

      public NamespacedKeyParseException(@NonNull final Caption caption, @NonNull final String input, @NonNull final CommandContext<?> context) {
         super(NamespacedKeyParser.class, context, caption, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            NamespacedKeyParser.NamespacedKeyParseException that = (NamespacedKeyParser.NamespacedKeyParseException)o;
            return this.input.equals(that.input) && this.errorCaption().equals(that.errorCaption());
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input, this.errorCaption());
      }
   }
}

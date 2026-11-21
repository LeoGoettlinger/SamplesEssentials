package de.oliver.cloud.bukkit.parser;

import de.oliver.cloud.bukkit.BukkitCaptionKeys;
import de.oliver.cloud.caption.CaptionVariable;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.exception.parsing.ParserException;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.BlockingSuggestionProvider;
import de.oliver.cloud.suggestion.Suggestion;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MaterialParser<C> implements ArgumentParser<C, Material>, BlockingSuggestionProvider<C> {
   @API(
      status = Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Material> materialParser() {
      return ParserDescriptor.of(new MaterialParser<>(), Material.class);
   }

   @API(
      status = Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Material> materialComponent() {
      return CommandComponent.<C, Material>builder().parser(materialParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<Material> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();

      try {
         Material material = Material.valueOf(input.toUpperCase(Locale.ROOT));
         return ArgumentParseResult.success(material);
      } catch (IllegalArgumentException var5) {
         return ArgumentParseResult.failure(new MaterialParser.MaterialParseException(input, commandContext));
      }
   }

   @NonNull
   @Override
   public Iterable<Suggestion> suggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return Arrays.stream(Material.values()).map(Enum::name).map(String::toLowerCase).map(Suggestion::suggestion).collect(Collectors.toList());
   }

   public static final class MaterialParseException extends ParserException {
      private final String input;

      public MaterialParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(MaterialParser.class, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_MATERIAL, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }
   }
}

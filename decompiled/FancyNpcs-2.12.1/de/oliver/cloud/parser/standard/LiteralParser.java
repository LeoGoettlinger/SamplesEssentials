package de.oliver.cloud.parser.standard;

import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.BlockingSuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class LiteralParser<C> implements ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {
   private final Set<String> allAcceptedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   private final Set<String> alternativeAliases = new HashSet<>();
   private final String name;

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, String> literal(@NonNull final String name, @NonNull final String... aliases) {
      return ParserDescriptor.of(new LiteralParser<>(name, aliases), String.class);
   }

   private LiteralParser(@NonNull final String name, final String... aliases) {
      validateNames(name, aliases);
      this.name = name;
      this.allAcceptedAliases.add(this.name);
      this.allAcceptedAliases.addAll(Arrays.asList(aliases));
      this.alternativeAliases.addAll(Arrays.asList(aliases));
   }

   @NonNull
   @Override
   public ArgumentParseResult<String> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String string = commandInput.peekString();
      if (this.allAcceptedAliases.contains(string)) {
         commandInput.readString();
         return ArgumentParseResult.success(this.name);
      } else {
         return ArgumentParseResult.failure(new IllegalArgumentException(string));
      }
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return Collections.singletonList(this.name);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public Collection<String> aliases() {
      return Collections.unmodifiableCollection(this.allAcceptedAliases);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public Collection<String> alternativeAliases() {
      return Collections.unmodifiableCollection(this.alternativeAliases);
   }

   public void insertAlias(@NonNull final String alias) {
      validateNames("valid", new String[]{alias});
      this.allAcceptedAliases.add(alias);
      this.alternativeAliases.add(alias);
   }

   private static void validateNames(final String name, final String[] aliases) {
      List<String> errors = null;
      errors = validateName(name, false, errors);

      for (String alias : aliases) {
         errors = validateName(alias, true, errors);
      }

      if (errors != null && !errors.isEmpty()) {
         throw new IllegalArgumentException(String.join("\n", errors));
      }
   }

   @Nullable
   private static List<String> validateName(@NonNull final String name, final boolean alias, @Nullable List<String> errors) {
      int found = name.codePoints().filter(Character::isWhitespace).findFirst().orElse(Integer.MIN_VALUE);
      if (found != Integer.MIN_VALUE) {
         if (errors == null) {
            errors = new ArrayList<>();
         }

         errors.add(String.format("%s '%s' is invalid: contains whitespace", alias ? "Alias" : "Name", name));
      }

      return errors;
   }
}

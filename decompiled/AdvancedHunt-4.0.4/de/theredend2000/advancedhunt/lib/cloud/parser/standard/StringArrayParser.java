package de.theredend2000.advancedhunt.lib.cloud.parser.standard;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public final class StringArrayParser<C> implements ArgumentParser<C, String[]> {
   private static final Pattern FLAG_PATTERN = Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");
   private final boolean flagYielding;

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, String[]> stringArrayParser() {
      return ParserDescriptor.of(new StringArrayParser<>(), String[].class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, String[]> flagYieldingStringArrayParser() {
      return ParserDescriptor.of(new StringArrayParser<>(true), String[].class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, String[]> characterComponent() {
      return CommandComponent.<C, String[]>builder().parser(stringArrayParser());
   }

   public StringArrayParser() {
      this.flagYielding = false;
   }

   @API(
      status = API.Status.STABLE
   )
   public StringArrayParser(final boolean flagYielding) {
      this.flagYielding = flagYielding;
   }

   @NonNull
   @Override
   public ArgumentParseResult<String[]> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      int size = commandInput.remainingTokens();
      if (this.flagYielding) {
         List<String> result = new LinkedList<>();

         for (int i = 0; i < size; i++) {
            String string = commandInput.peekString();
            if (string.isEmpty() || FLAG_PATTERN.matcher(string).matches()) {
               break;
            }

            result.add(commandInput.readString());
         }

         return ArgumentParseResult.success(result.toArray(new String[0]));
      } else {
         String[] result = new String[size];

         for (int i = 0; i < result.length; i++) {
            result[i] = commandInput.readString();
         }

         return ArgumentParseResult.success(result);
      }
   }
}

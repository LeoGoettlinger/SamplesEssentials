package de.oliver.cloud.annotations.string;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PropertyReplacingStringProcessor extends PatternReplacingStringProcessor {
   public static final Pattern PROPERTY_REGEX = Pattern.compile("\\$\\{(\\S+)}");

   public PropertyReplacingStringProcessor(@NonNull final Function<String, String> replacementProvider) {
      super(PROPERTY_REGEX, new PropertyReplacingStringProcessor.PropertyReplacementProvider(replacementProvider));
   }

   private static final class PropertyReplacementProvider implements Function<MatchResult, String> {
      private final Function<String, String> replacementProvider;

      private PropertyReplacementProvider(@NonNull final Function<String, String> replacementProvider) {
         this.replacementProvider = replacementProvider;
      }

      @Nullable
      public String apply(@NonNull final MatchResult matchResult) {
         return this.replacementProvider.apply(matchResult.group(1));
      }
   }
}

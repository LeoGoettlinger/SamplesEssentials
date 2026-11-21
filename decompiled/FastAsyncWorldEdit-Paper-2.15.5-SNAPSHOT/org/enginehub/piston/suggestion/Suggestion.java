package org.enginehub.piston.suggestion;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Suggestion {
   public static Suggestion.Builder builder() {
      return new AutoValue_Suggestion.Builder();
   }

   Suggestion() {
   }

   public abstract String getSuggestion();

   public abstract int getReplacedArgument();

   public abstract Suggestion.Builder toBuilder();

   @com.google.auto.value.AutoValue.Builder
   public interface Builder {
      Suggestion.Builder suggestion(String var1);

      Suggestion.Builder replacedArgument(int var1);

      Suggestion build();
   }
}

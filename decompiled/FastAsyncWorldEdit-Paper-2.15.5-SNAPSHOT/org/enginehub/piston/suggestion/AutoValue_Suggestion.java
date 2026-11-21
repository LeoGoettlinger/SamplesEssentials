package org.enginehub.piston.suggestion;

final class AutoValue_Suggestion extends Suggestion {
   private final String suggestion;
   private final int replacedArgument;

   private AutoValue_Suggestion(String suggestion, int replacedArgument) {
      this.suggestion = suggestion;
      this.replacedArgument = replacedArgument;
   }

   @Override
   public String getSuggestion() {
      return this.suggestion;
   }

   @Override
   public int getReplacedArgument() {
      return this.replacedArgument;
   }

   @Override
   public String toString() {
      return "Suggestion{suggestion=" + this.suggestion + ", replacedArgument=" + this.replacedArgument + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof Suggestion that) ? false : this.suggestion.equals(that.getSuggestion()) && this.replacedArgument == that.getReplacedArgument();
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.suggestion.hashCode();
      h$ *= 1000003;
      return h$ ^ this.replacedArgument;
   }

   @Override
   public Suggestion.Builder toBuilder() {
      return new AutoValue_Suggestion.Builder(this);
   }

   static final class Builder implements Suggestion.Builder {
      private String suggestion;
      private int replacedArgument;
      private byte set$0;

      Builder() {
      }

      Builder(Suggestion source) {
         this.suggestion = source.getSuggestion();
         this.replacedArgument = source.getReplacedArgument();
         this.set$0 = 1;
      }

      @Override
      public Suggestion.Builder suggestion(String suggestion) {
         if (suggestion == null) {
            throw new NullPointerException("Null suggestion");
         } else {
            this.suggestion = suggestion;
            return this;
         }
      }

      @Override
      public Suggestion.Builder replacedArgument(int replacedArgument) {
         this.replacedArgument = replacedArgument;
         this.set$0 = (byte)(this.set$0 | 1);
         return this;
      }

      @Override
      public Suggestion build() {
         if (this.set$0 == 1 && this.suggestion != null) {
            return new AutoValue_Suggestion(this.suggestion, this.replacedArgument);
         } else {
            StringBuilder missing = new StringBuilder();
            if (this.suggestion == null) {
               missing.append(" suggestion");
            }

            if ((this.set$0 & 1) == 0) {
               missing.append(" replacedArgument");
            }

            throw new IllegalStateException("Missing required properties:" + missing);
         }
      }
   }
}

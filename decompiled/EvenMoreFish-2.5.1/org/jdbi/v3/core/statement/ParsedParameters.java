package org.jdbi.v3.core.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ParsedParameters {
   static final ParsedParameters NONE = new ParsedParameters(true, Collections.emptyList());
   private final boolean positional;
   private final List<String> parameterNames;

   ParsedParameters(boolean positional, List<String> parameterNames) {
      this.positional = positional;
      this.parameterNames = Collections.unmodifiableList(new ArrayList<>(parameterNames));
   }

   public boolean isPositional() {
      return this.positional;
   }

   public int getParameterCount() {
      return this.parameterNames.size();
   }

   public List<String> getParameterNames() {
      return this.parameterNames;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ParsedParameters that = (ParsedParameters)o;
         return this.positional == that.positional && Objects.equals(this.parameterNames, that.parameterNames);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.positional, this.parameterNames);
   }

   @Override
   public String toString() {
      return "ParsedParameters{positional=" + this.positional + ", parameterNames=" + this.parameterNames + "}";
   }

   public static ParsedParameters named(List<String> names) {
      if (names.contains("?")) {
         throw new IllegalArgumentException("Named parameters list must not contain positional parameter \"?\"");
      } else {
         return new ParsedParameters(false, names);
      }
   }

   public static ParsedParameters positional(int count) {
      return new ParsedParameters(true, Collections.nCopies(count, "?"));
   }
}

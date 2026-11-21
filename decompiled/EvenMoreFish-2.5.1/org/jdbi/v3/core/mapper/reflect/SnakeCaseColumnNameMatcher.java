package org.jdbi.v3.core.mapper.reflect;

public class SnakeCaseColumnNameMatcher extends AbstractSeparatorCharColumnNameMatcher {
   public SnakeCaseColumnNameMatcher() {
      super('_');
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName();
   }
}

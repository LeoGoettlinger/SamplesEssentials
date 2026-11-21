package org.jdbi.v3.core.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ParsedSql {
   static final String POSITIONAL_PARAM = "?";
   private final String sql;
   private final ParsedParameters parameters;

   private ParsedSql(String sql, ParsedParameters parameters) {
      this.sql = sql;
      this.parameters = parameters;
   }

   public String getSql() {
      return this.sql;
   }

   public ParsedParameters getParameters() {
      return this.parameters;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ParsedSql that = (ParsedSql)o;
         return Objects.equals(this.sql, that.sql) && Objects.equals(this.parameters, that.parameters);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.sql, this.parameters);
   }

   @Override
   public String toString() {
      return "ParsedSql{sql='" + this.sql + "', parameters=" + this.parameters + "}";
   }

   public static ParsedSql of(String sql, ParsedParameters parameters) {
      return new ParsedSql(sql, parameters);
   }

   public static ParsedSql.Builder builder() {
      return new ParsedSql.Builder();
   }

   public static class Builder {
      private final StringBuilder sql = new StringBuilder();
      private boolean positional = false;
      private boolean named = false;
      private final List<String> parameterNames = new ArrayList<>();

      private Builder() {
      }

      public ParsedSql.Builder append(String sqlFragment) {
         this.sql.append(sqlFragment);
         return this;
      }

      public ParsedSql.Builder appendPositionalParameter() {
         this.positional = true;
         this.parameterNames.add("?");
         return this.append("?");
      }

      public ParsedSql.Builder appendNamedParameter(String name) {
         this.named = true;
         this.parameterNames.add(name);
         return this.append("?");
      }

      public ParsedSql build() {
         if (this.positional && this.named) {
            throw new UnableToExecuteStatementException("Cannot mix named and positional parameters in a SQL statement: " + this.parameterNames);
         } else {
            ParsedParameters parameters = new ParsedParameters(this.positional, this.parameterNames);
            return new ParsedSql(this.sql.toString(), parameters);
         }
      }
   }
}

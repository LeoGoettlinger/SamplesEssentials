package org.postgresql.core.v3;

import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.SqlCommand;

class CompositeQuery implements Query {
   private final SimpleQuery[] subqueries;
   private final int[] offsets;

   CompositeQuery(SimpleQuery[] subqueries, int[] offsets) {
      this.subqueries = subqueries;
      this.offsets = offsets;
   }

   @Override
   public ParameterList createParameterList() {
      SimpleParameterList[] subparams = new SimpleParameterList[this.subqueries.length];

      for (int i = 0; i < this.subqueries.length; i++) {
         subparams[i] = (SimpleParameterList)this.subqueries[i].createParameterList();
      }

      return new CompositeParameterList(subparams, this.offsets);
   }

   @Override
   public String toString(@Nullable ParameterList parameters) {
      StringBuilder sbuf = new StringBuilder(this.subqueries[0].toString());

      for (int i = 1; i < this.subqueries.length; i++) {
         sbuf.append(';');
         sbuf.append(this.subqueries[i]);
      }

      return sbuf.toString();
   }

   @Override
   public String getNativeSql() {
      StringBuilder sbuf = new StringBuilder(this.subqueries[0].getNativeSql());

      for (int i = 1; i < this.subqueries.length; i++) {
         sbuf.append(';');
         sbuf.append(this.subqueries[i].getNativeSql());
      }

      return sbuf.toString();
   }

   @Nullable
   @Override
   public SqlCommand getSqlCommand() {
      return null;
   }

   @Override
   public String toString() {
      return this.toString(null);
   }

   @Override
   public void close() {
      for (SimpleQuery subquery : this.subqueries) {
         subquery.close();
      }
   }

   @Override
   public Query[] getSubqueries() {
      return this.subqueries;
   }

   @Override
   public boolean isStatementDescribed() {
      for (SimpleQuery subquery : this.subqueries) {
         if (!subquery.isStatementDescribed()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean isEmpty() {
      for (SimpleQuery subquery : this.subqueries) {
         if (!subquery.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int getBatchSize() {
      return 0;
   }

   @Nullable
   @Override
   public Map<String, Integer> getResultSetColumnNameIndexMap() {
      return null;
   }
}

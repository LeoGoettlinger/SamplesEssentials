package org.postgresql.core;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public final class CommandCompleteParser {
   private long oid;
   private long rows;

   public long getOid() {
      return this.oid;
   }

   public long getRows() {
      return this.rows;
   }

   void set(long oid, long rows) {
      this.oid = oid;
      this.rows = rows;
   }

   public void parse(String status) throws PSQLException {
      if (!Parser.isDigitAt(status, status.length() - 1)) {
         this.set(0L, 0L);
      } else {
         long oid = 0L;
         long rows = 0L;

         try {
            int lastSpace = status.lastIndexOf(32);
            if (Parser.isDigitAt(status, lastSpace + 1)) {
               rows = Parser.parseLong(status, lastSpace + 1, status.length());
               if (Parser.isDigitAt(status, lastSpace - 1)) {
                  int penultimateSpace = status.lastIndexOf(32, lastSpace - 1);
                  if (Parser.isDigitAt(status, penultimateSpace + 1)) {
                     oid = Parser.parseLong(status, penultimateSpace + 1, lastSpace);
                  }
               }
            }
         } catch (NumberFormatException var8) {
            throw new PSQLException(GT.tr("Unable to parse the count in command completion tag: {0}.", status), PSQLState.CONNECTION_FAILURE, var8);
         }

         this.set(oid, rows);
      }
   }

   @Override
   public String toString() {
      return "CommandStatus{oid=" + this.oid + ", rows=" + this.rows + '}';
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CommandCompleteParser that = (CommandCompleteParser)o;
         return this.oid != that.oid ? false : this.rows == that.rows;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = (int)(this.oid ^ this.oid >>> 32);
      return 31 * result + (int)(this.rows ^ this.rows >>> 32);
   }
}

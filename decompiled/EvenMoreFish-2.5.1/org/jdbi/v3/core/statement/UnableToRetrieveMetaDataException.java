package org.jdbi.v3.core.statement;

public class UnableToRetrieveMetaDataException extends StatementException {
   private static final long serialVersionUID = 1L;

   UnableToRetrieveMetaDataException(Exception cause, StatementContext ctx) {
      super(cause, ctx);
   }
}

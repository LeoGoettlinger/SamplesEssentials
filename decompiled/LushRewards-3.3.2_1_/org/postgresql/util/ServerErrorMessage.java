package org.postgresql.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.EncodingPredictor;

public class ServerErrorMessage implements Serializable {
   private static final Logger LOGGER = Logger.getLogger(ServerErrorMessage.class.getName());
   private static final Character SEVERITY = 'S';
   private static final Character MESSAGE = 'M';
   private static final Character DETAIL = 'D';
   private static final Character HINT = 'H';
   private static final Character POSITION = 'P';
   private static final Character WHERE = 'W';
   private static final Character FILE = 'F';
   private static final Character LINE = 'L';
   private static final Character ROUTINE = 'R';
   private static final Character SQLSTATE = 'C';
   private static final Character INTERNAL_POSITION = 'p';
   private static final Character INTERNAL_QUERY = 'q';
   private static final Character SCHEMA = 's';
   private static final Character TABLE = 't';
   private static final Character COLUMN = 'c';
   private static final Character DATATYPE = 'd';
   private static final Character CONSTRAINT = 'n';
   private final Map<Character, String> mesgParts = new HashMap<>();

   public ServerErrorMessage(EncodingPredictor.DecodeResult serverError) {
      this(serverError.result);
      if (serverError.encoding != null) {
         this.mesgParts
            .put(
               MESSAGE,
               this.mesgParts.get(MESSAGE)
                  + GT.tr(
                     " (pgjdbc: autodetected server-encoding to be {0}, if the message is not readable, please check database logs and/or host, port, dbname, user, password, pg_hba.conf)",
                     serverError.encoding
                  )
            );
      }
   }

   public ServerErrorMessage(String serverError) {
      char[] chars = serverError.toCharArray();
      int pos = 0;

      for (int length = chars.length; pos < length; pos++) {
         char mesgType = chars[pos];
         if (mesgType != 0) {
            int startString = ++pos;

            while (pos < length && chars[pos] != 0) {
               pos++;
            }

            String mesgPart = new String(chars, startString, pos - startString);
            this.mesgParts.put(mesgType, mesgPart);
         }
      }
   }

   @Nullable
   public String getSQLState() {
      return this.mesgParts.get(SQLSTATE);
   }

   @Nullable
   public String getMessage() {
      return this.mesgParts.get(MESSAGE);
   }

   @Nullable
   public String getSeverity() {
      return this.mesgParts.get(SEVERITY);
   }

   @Nullable
   public String getDetail() {
      return this.mesgParts.get(DETAIL);
   }

   @Nullable
   public String getHint() {
      return this.mesgParts.get(HINT);
   }

   public int getPosition() {
      return this.getIntegerPart(POSITION);
   }

   @Nullable
   public String getWhere() {
      return this.mesgParts.get(WHERE);
   }

   @Nullable
   public String getSchema() {
      return this.mesgParts.get(SCHEMA);
   }

   @Nullable
   public String getTable() {
      return this.mesgParts.get(TABLE);
   }

   @Nullable
   public String getColumn() {
      return this.mesgParts.get(COLUMN);
   }

   @Nullable
   public String getDatatype() {
      return this.mesgParts.get(DATATYPE);
   }

   @Nullable
   public String getConstraint() {
      return this.mesgParts.get(CONSTRAINT);
   }

   @Nullable
   public String getFile() {
      return this.mesgParts.get(FILE);
   }

   public int getLine() {
      return this.getIntegerPart(LINE);
   }

   @Nullable
   public String getRoutine() {
      return this.mesgParts.get(ROUTINE);
   }

   @Nullable
   public String getInternalQuery() {
      return this.mesgParts.get(INTERNAL_QUERY);
   }

   public int getInternalPosition() {
      return this.getIntegerPart(INTERNAL_POSITION);
   }

   private int getIntegerPart(Character c) {
      String s = this.mesgParts.get(c);
      return s == null ? 0 : Integer.parseInt(s);
   }

   String getNonSensitiveErrorMessage() {
      StringBuilder totalMessage = new StringBuilder();
      String message = this.mesgParts.get(SEVERITY);
      if (message != null) {
         totalMessage.append(message).append(": ");
      }

      message = this.mesgParts.get(MESSAGE);
      if (message != null) {
         totalMessage.append(message);
      }

      return totalMessage.toString();
   }

   @Override
   public String toString() {
      StringBuilder totalMessage = new StringBuilder();
      String message = this.mesgParts.get(SEVERITY);
      if (message != null) {
         totalMessage.append(message).append(": ");
      }

      message = this.mesgParts.get(MESSAGE);
      if (message != null) {
         totalMessage.append(message);
      }

      message = this.mesgParts.get(DETAIL);
      if (message != null) {
         totalMessage.append("\n  ").append(GT.tr("Detail: {0}", message));
      }

      message = this.mesgParts.get(HINT);
      if (message != null) {
         totalMessage.append("\n  ").append(GT.tr("Hint: {0}", message));
      }

      message = this.mesgParts.get(POSITION);
      if (message != null) {
         totalMessage.append("\n  ").append(GT.tr("Position: {0}", message));
      }

      message = this.mesgParts.get(WHERE);
      if (message != null) {
         totalMessage.append("\n  ").append(GT.tr("Where: {0}", message));
      }

      if (LOGGER.isLoggable(Level.FINEST)) {
         String internalQuery = this.mesgParts.get(INTERNAL_QUERY);
         if (internalQuery != null) {
            totalMessage.append("\n  ").append(GT.tr("Internal Query: {0}", internalQuery));
         }

         String internalPosition = this.mesgParts.get(INTERNAL_POSITION);
         if (internalPosition != null) {
            totalMessage.append("\n  ").append(GT.tr("Internal Position: {0}", internalPosition));
         }

         String file = this.mesgParts.get(FILE);
         String line = this.mesgParts.get(LINE);
         String routine = this.mesgParts.get(ROUTINE);
         if (file != null || line != null || routine != null) {
            totalMessage.append("\n  ").append(GT.tr("Location: File: {0}, Routine: {1}, Line: {2}", file, routine, line));
         }

         message = this.mesgParts.get(SQLSTATE);
         if (message != null) {
            totalMessage.append("\n  ").append(GT.tr("Server SQLState: {0}", message));
         }
      }

      return totalMessage.toString();
   }
}

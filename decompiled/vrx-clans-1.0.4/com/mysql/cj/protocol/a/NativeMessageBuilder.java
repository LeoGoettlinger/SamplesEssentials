package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Constants;
import com.mysql.cj.MessageBuilder;
import com.mysql.cj.Messages;
import com.mysql.cj.MysqlType;
import com.mysql.cj.NativeQueryAttributesBindings;
import com.mysql.cj.NativeQueryBindValue;
import com.mysql.cj.PreparedQuery;
import com.mysql.cj.Query;
import com.mysql.cj.QueryAttributesBindings;
import com.mysql.cj.QueryBindings;
import com.mysql.cj.Session;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.util.StringUtils;
import java.util.List;

public class NativeMessageBuilder implements MessageBuilder<NativePacketPayload> {
   private boolean supportsQueryAttributes = true;

   public NativeMessageBuilder(boolean supportsQueryAttributes) {
      this.supportsQueryAttributes = supportsQueryAttributes;
   }

   public NativePacketPayload buildSqlStatement(String statement) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public NativePacketPayload buildSqlStatement(String statement, List<Object> args) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public NativePacketPayload buildClose() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public NativePacketPayload buildComQuery(NativePacketPayload sharedPacket, Session sess, byte[] query) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(query.length + 1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 3L);
      if (this.supportsQueryAttributes) {
         BindValue queryAttribute = new NativeQueryBindValue(sess);
         sess.getTelemetryHandler().propagateContext((k, v) -> {
            queryAttribute.setName(k);
            queryAttribute.setBinding(v, MysqlType.CHAR, 0, null);
         });
         if (queryAttribute.getName() == null) {
            packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 0L);
            packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
         } else {
            packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
            packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);
            packet.writeInteger(NativeConstants.IntegerDataType.INT2, queryAttribute.getFieldType());
            packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, queryAttribute.getName().getBytes());
            queryAttribute.writeAsQueryAttribute(packet);
         }
      }

      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, query);
      return packet;
   }

   public NativePacketPayload buildComQuery(NativePacketPayload sharedPacket, Session sess, String query) {
      return this.buildComQuery(sharedPacket, sess, StringUtils.getBytes(query));
   }

   public NativePacketPayload buildComQuery(NativePacketPayload sharedPacket, Session sess, String query, String encoding) {
      return this.buildComQuery(sharedPacket, sess, StringUtils.getBytes(query, encoding));
   }

   public NativePacketPayload buildComQuery(NativePacketPayload sharedPacket, Session sess, String query, Query callingQuery, String characterEncoding) {
      String statementComment = sess.getQueryComment();
      if (sess.getPropertySet().getBooleanProperty(PropertyKey.includeThreadNamesAsStatementComment).getValue()) {
         statementComment = (statementComment != null ? statementComment + ", " : "") + "java thread: " + Thread.currentThread().getName();
      }

      byte[] commentAsBytes = StringUtils.getBytes(statementComment, characterEncoding);
      QueryAttributesBindings queryAttributesBindings = null;
      if (!this.supportsQueryAttributes && callingQuery != null && callingQuery.getQueryAttributesBindings().getCount() > 0) {
         sess.getLog().logWarn(Messages.getString("QueryAttributes.SetButNotSupported"));
      }

      if (this.supportsQueryAttributes && callingQuery != null) {
         queryAttributesBindings = callingQuery.getQueryAttributesBindings();
      } else {
         queryAttributesBindings = new NativeQueryAttributesBindings(sess);
      }

      boolean contextPropagationAttributeWasInjected = false;
      NativePacketPayload sendPacket;
      if (sharedPacket != null) {
         sendPacket = sharedPacket;
      } else {
         int packLength = 1 + query.length() * 4 + 2;
         if (commentAsBytes.length > 0) {
            packLength += commentAsBytes.length;
            packLength += 6;
         }

         if (this.supportsQueryAttributes) {
            if (!queryAttributesBindings.containsAttribute(sess.getTelemetryHandler().getContextPropagationKey())) {
               sess.getTelemetryHandler().propagateContext(queryAttributesBindings::setAttribute);
               contextPropagationAttributeWasInjected = true;
            }

            if (queryAttributesBindings.getCount() > 0) {
               packLength += 10;
               packLength += (queryAttributesBindings.getCount() + 7) / 8 + 1;

               for (int i = 0; i < queryAttributesBindings.getCount(); i++) {
                  BindValue queryAttribute = queryAttributesBindings.getAttributeValue(i);
                  packLength = (int)(packLength + 2 + queryAttribute.getName().length() + queryAttribute.getBinaryLength());
               }
            } else {
               packLength += 2;
            }
         }

         sendPacket = new NativePacketPayload(packLength);
      }

      sendPacket.setPosition(0);
      sendPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 3L);
      if (this.supportsQueryAttributes) {
         if (queryAttributesBindings != null && queryAttributesBindings.getCount() > 0) {
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, queryAttributesBindings.getCount());
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
            byte[] nullBitsBuffer = new byte[(queryAttributesBindings.getCount() + 7) / 8];

            for (int i = 0; i < queryAttributesBindings.getCount(); i++) {
               if (queryAttributesBindings.getAttributeValue(i).isNull()) {
                  nullBitsBuffer[i >>> 3] = (byte)(nullBitsBuffer[i >>> 3] | 1 << (i & 7));
               }
            }

            sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_VAR, nullBitsBuffer);
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);
            queryAttributesBindings.runThroughAll(a -> {
               sendPacket.writeInteger(NativeConstants.IntegerDataType.INT2, a.getFieldType());
               sendPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, a.getName().getBytes());
            });
            queryAttributesBindings.runThroughAll(a -> {
               if (!a.isNull()) {
                  a.writeAsQueryAttribute(sendPacket);
               }
            });
         } else {
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 0L);
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
         }

         if (contextPropagationAttributeWasInjected) {
            queryAttributesBindings.removeAttribute(sess.getTelemetryHandler().getContextPropagationKey());
         }
      }

      sendPacket.setTag("QUERY");
      if (commentAsBytes.length > 0) {
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SLASH_STAR_SPACE_AS_BYTES);
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, commentAsBytes);
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
      }

      if (!sess.getServerSession().getCharsetSettings().doesPlatformDbCharsetMatches() && StringUtils.startsWithIgnoreCaseAndWs(query, "LOAD DATA")) {
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, StringUtils.getBytes(query));
      } else {
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, StringUtils.getBytes(query, characterEncoding));
      }

      return sendPacket;
   }

   public NativePacketPayload buildComQuery(
      NativePacketPayload sharedPacket, Session sess, PreparedQuery preparedQuery, QueryBindings bindings, String characterEncoding
   ) {
      NativePacketPayload sendPacket = sharedPacket != null ? sharedPacket : new NativePacketPayload(9);
      QueryAttributesBindings queryAttributesBindings = preparedQuery.getQueryAttributesBindings();
      BindValue[] bindValues = bindings.getBindValues();
      sendPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 3L);
      boolean contextPropagationAttributeWasInjected = false;
      if (this.supportsQueryAttributes) {
         if (!queryAttributesBindings.containsAttribute(sess.getTelemetryHandler().getContextPropagationKey())) {
            sess.getTelemetryHandler().propagateContext(queryAttributesBindings::setAttribute);
            contextPropagationAttributeWasInjected = true;
         }

         if (queryAttributesBindings.getCount() <= 0) {
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 0L);
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
         } else {
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, queryAttributesBindings.getCount());
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, 1L);
            byte[] nullBitsBuffer = new byte[(queryAttributesBindings.getCount() + 7) / 8];

            for (int i = 0; i < queryAttributesBindings.getCount(); i++) {
               if (queryAttributesBindings.getAttributeValue(i).isNull()) {
                  nullBitsBuffer[i >>> 3] = (byte)(nullBitsBuffer[i >>> 3] | 1 << (i & 7));
               }
            }

            sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_VAR, nullBitsBuffer);
            sendPacket.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);
            queryAttributesBindings.runThroughAll(a -> {
               sendPacket.writeInteger(NativeConstants.IntegerDataType.INT2, a.getFieldType());
               sendPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, a.getName().getBytes());
            });
            queryAttributesBindings.runThroughAll(a -> {
               if (!a.isNull()) {
                  a.writeAsQueryAttribute(sendPacket);
               }
            });
         }

         if (contextPropagationAttributeWasInjected) {
            queryAttributesBindings.removeAttribute(sess.getTelemetryHandler().getContextPropagationKey());
         }
      } else if (queryAttributesBindings.getCount() > 0) {
         sess.getLog().logWarn(Messages.getString("QueryAttributes.SetButNotSupported"));
      }

      sendPacket.setTag("QUERY");
      boolean useStreamLengths = sess.getPropertySet().getBooleanProperty(PropertyKey.useStreamLengthsInPrepStmts).getValue();
      int ensurePacketSize = 0;
      String statementComment = sess.getQueryComment();
      byte[] commentAsBytes = null;
      if (statementComment != null) {
         commentAsBytes = StringUtils.getBytes(statementComment, characterEncoding);
         ensurePacketSize += commentAsBytes.length;
         ensurePacketSize += 6;
      }

      for (int ix = 0; ix < bindValues.length; ix++) {
         if (bindValues[ix].isStream() && useStreamLengths) {
            ensurePacketSize = (int)(ensurePacketSize + bindValues[ix].getScaleOrLength());
         }
      }

      if (ensurePacketSize != 0) {
         sendPacket.ensureCapacity(ensurePacketSize);
      }

      if (commentAsBytes != null) {
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SLASH_STAR_SPACE_AS_BYTES);
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, commentAsBytes);
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
      }

      byte[][] staticSqlStrings = preparedQuery.getQueryInfo().getStaticSqlParts();

      for (int ixx = 0; ixx < bindValues.length; ixx++) {
         bindings.checkParameterSet(ixx);
         sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, staticSqlStrings[ixx]);
         bindValues[ixx].writeAsText(sendPacket);
      }

      sendPacket.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, staticSqlStrings[bindValues.length]);
      return sendPacket;
   }

   public NativePacketPayload buildComInitDb(NativePacketPayload sharedPacket, byte[] dbName) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(dbName.length + 1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 2L);
      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, dbName);
      return packet;
   }

   public NativePacketPayload buildComInitDb(NativePacketPayload sharedPacket, String dbName) {
      return this.buildComInitDb(sharedPacket, StringUtils.getBytes(dbName));
   }

   public NativePacketPayload buildComShutdown(NativePacketPayload sharedPacket) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 8L);
      return packet;
   }

   public NativePacketPayload buildComSetOption(NativePacketPayload sharedPacket, int val) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(3);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 27L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT2, val);
      return packet;
   }

   public NativePacketPayload buildComPing(NativePacketPayload sharedPacket) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 14L);
      return packet;
   }

   public NativePacketPayload buildComQuit(NativePacketPayload sharedPacket) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);
      return packet;
   }

   public NativePacketPayload buildComStmtPrepare(NativePacketPayload sharedPacket, byte[] query) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(query.length + 1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 22L);
      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, query);
      return packet;
   }

   public NativePacketPayload buildComStmtPrepare(NativePacketPayload sharedPacket, String queryString, String characterEncoding) {
      return this.buildComStmtPrepare(sharedPacket, StringUtils.getBytes(queryString, characterEncoding));
   }

   public NativePacketPayload buildComStmtClose(NativePacketPayload sharedPacket, long serverStatementId) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(5);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 25L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      return packet;
   }

   public NativePacketPayload buildComStmtReset(NativePacketPayload sharedPacket, long serverStatementId) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(5);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 26L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      return packet;
   }

   public NativePacketPayload buildComStmtFetch(NativePacketPayload sharedPacket, long serverStatementId, long numRowsToFetch) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(9);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 28L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, numRowsToFetch);
      return packet;
   }

   public NativePacketPayload buildComStmtSendLongData(NativePacketPayload sharedPacket, long serverStatementId, int parameterIndex, byte[] longData) {
      NativePacketPayload packet = this.buildComStmtSendLongDataHeader(sharedPacket, serverStatementId, parameterIndex);
      packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, longData);
      return packet;
   }

   public NativePacketPayload buildComStmtSendLongDataHeader(NativePacketPayload sharedPacket, long serverStatementId, int parameterIndex) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(9);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 24L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      packet.writeInteger(NativeConstants.IntegerDataType.INT2, parameterIndex);
      return packet;
   }

   public NativePacketPayload buildComStmtExecute(
      NativePacketPayload sharedPacket, long serverStatementId, byte flags, boolean sendQueryAttributes, PreparedQuery preparedQuery
   ) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(5);
      Session sess = preparedQuery.getSession();
      int parameterCount = preparedQuery.getParameterCount();
      QueryBindings queryBindings = preparedQuery.getQueryBindings();
      BindValue[] parameterBindings = queryBindings.getBindValues();
      QueryAttributesBindings queryAttributesBindings = preparedQuery.getQueryAttributesBindings();
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 23L);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, serverStatementId);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, flags);
      packet.writeInteger(NativeConstants.IntegerDataType.INT4, 1L);
      boolean contextPropagationAttributeWasInjected = false;
      int parametersAndAttributesCount = parameterCount;
      if (this.supportsQueryAttributes) {
         if (sendQueryAttributes) {
            if (!queryAttributesBindings.containsAttribute(sess.getTelemetryHandler().getContextPropagationKey())) {
               sess.getTelemetryHandler().propagateContext(queryAttributesBindings::setAttribute);
               contextPropagationAttributeWasInjected = true;
            }

            parametersAndAttributesCount = parameterCount + queryAttributesBindings.getCount();
         }

         if (sendQueryAttributes || parametersAndAttributesCount > 0) {
            packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, parametersAndAttributesCount);
         }
      }

      if (parametersAndAttributesCount > 0) {
         int nullCount = (parametersAndAttributesCount + 7) / 8;
         int nullBitsPosition = packet.getPosition();

         for (int i = 0; i < nullCount; i++) {
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
         }

         byte[] nullBitsBuffer = new byte[nullCount];
         if (queryBindings.getSendTypesToServer().get() || sendQueryAttributes && queryAttributesBindings.getCount() > 0) {
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 1L);

            for (int i = 0; i < parameterCount; i++) {
               packet.writeInteger(NativeConstants.IntegerDataType.INT2, parameterBindings[i].getFieldType());
               if (this.supportsQueryAttributes) {
                  packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, "".getBytes());
               }
            }

            if (sendQueryAttributes) {
               queryAttributesBindings.runThroughAll(a -> {
                  packet.writeInteger(NativeConstants.IntegerDataType.INT2, a.getFieldType());
                  packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, a.getName().getBytes());
               });
            }
         } else {
            packet.writeInteger(NativeConstants.IntegerDataType.INT1, 0L);
         }

         for (int ix = 0; ix < parameterCount; ix++) {
            if (!parameterBindings[ix].isStream()) {
               if (!parameterBindings[ix].isNull()) {
                  parameterBindings[ix].writeAsBinary(packet);
               } else {
                  nullBitsBuffer[ix >>> 3] = (byte)(nullBitsBuffer[ix >>> 3] | 1 << (ix & 7));
               }
            }
         }

         if (sendQueryAttributes) {
            for (int ixx = 0; ixx < queryAttributesBindings.getCount(); ixx++) {
               if (queryAttributesBindings.getAttributeValue(ixx).isNull()) {
                  int b = ixx + parameterCount;
                  nullBitsBuffer[b >>> 3] = (byte)(nullBitsBuffer[b >>> 3] | 1 << (b & 7));
               }
            }

            queryAttributesBindings.runThroughAll(a -> {
               if (!a.isNull()) {
                  a.writeAsQueryAttribute(packet);
               }
            });
         }

         int endPosition = packet.getPosition();
         packet.setPosition(nullBitsPosition);
         packet.writeBytes(NativeConstants.StringLengthDataType.STRING_FIXED, nullBitsBuffer);
         packet.setPosition(endPosition);
      }

      if (contextPropagationAttributeWasInjected) {
         queryAttributesBindings.removeAttribute(sess.getTelemetryHandler().getContextPropagationKey());
      }

      return packet;
   }

   public NativePacketPayload buildComResetConnection(NativePacketPayload sharedPacket) {
      NativePacketPayload packet = sharedPacket != null ? sharedPacket : new NativePacketPayload(1);
      packet.writeInteger(NativeConstants.IntegerDataType.INT1, 31L);
      return packet;
   }
}

package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.lushplugins.lushrewards.libraries.mysql.cj.BindValue;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.WrongArgumentException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ServerSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.TimeUtil;

public class StringValueEncoder extends AbstractValueEncoder {
   private CharsetEncoder charsetEncoder;

   @Override
   public void init(PropertySet pset, ServerSession serverSess, ExceptionInterceptor excInterceptor) {
      super.init(pset, serverSess, excInterceptor);
      if (this.serverSession.getCharsetSettings().getRequiresEscapingEncoder()) {
         this.charsetEncoder = Charset.forName(this.charEncoding.getValue()).newEncoder();
      }
   }

   @Override
   public byte[] getBytes(BindValue binding) {
      switch (binding.getMysqlType()) {
         case NULL:
            return StringUtils.getBytes("null");
         case CHAR:
         case ENUM:
         case SET:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
         case JSON:
         case BINARY:
         case GEOMETRY:
         case VARBINARY:
         case TINYBLOB:
         case BLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            String x = (String)binding.getValue();
            if (binding.isNational() && !this.charEncoding.getValue().equalsIgnoreCase("UTF-8") && !this.charEncoding.getValue().equalsIgnoreCase("utf8")) {
               StringBuilder buf = new StringBuilder((int)(x.length() * 1.1 + 4.0));
               buf.append("_utf8");
               StringUtils.escapeString(buf, x, this.serverSession.useAnsiQuotedIdentifiers(), null);
               return StringUtils.getBytes(buf.toString(), "UTF-8");
            } else {
               int stringLength = x.length();
               if (this.serverSession.isNoBackslashEscapesSet()) {
                  if (!this.isEscapeNeededForString(x, stringLength)) {
                     return StringUtils.getBytesWrapped(x, '\'', '\'', this.charEncoding.getValue());
                  }

                  return this.escapeBytesIfNeeded(StringUtils.getBytes(x, this.charEncoding.getValue()));
               } else {
                  if (this.isEscapeNeededForString(x, stringLength)) {
                     String escString = StringUtils.escapeString(
                           new StringBuilder((int)(x.length() * 1.1)), x, this.serverSession.useAnsiQuotedIdentifiers(), this.charsetEncoder
                        )
                        .toString();
                     return StringUtils.getBytes(escString, this.charEncoding.getValue());
                  }

                  return StringUtils.getBytesWrapped(x, '\'', '\'', this.charEncoding.getValue());
               }
            }
         default:
            return StringUtils.getBytes(this.getString(binding), this.charEncoding.getValue());
      }
   }

   @Override
   public String getString(BindValue binding) {
      String x = (String)binding.getValue();
      switch (binding.getMysqlType()) {
         case NULL:
            return "null";
         case CHAR:
         case ENUM:
         case SET:
         case VARCHAR:
         case TINYTEXT:
         case TEXT:
         case MEDIUMTEXT:
         case LONGTEXT:
         case JSON:
         case BINARY:
         case GEOMETRY:
         case VARBINARY:
         case TINYBLOB:
         case BLOB:
         case MEDIUMBLOB:
         case LONGBLOB:
            StringBuilder sb = new StringBuilder("'");
            sb.append(x);
            sb.append("'");
            return sb.toString();
         case BOOLEAN:
         case BIT:
            Boolean b = null;
            if ("true".equalsIgnoreCase(x) || "Y".equalsIgnoreCase(x)) {
               b = true;
            } else if (!"false".equalsIgnoreCase(x) && !"N".equalsIgnoreCase(x)) {
               if (!x.matches("-?\\d+\\.?\\d*")) {
                  throw (WrongArgumentException)ExceptionFactory.createException(
                     WrongArgumentException.class, Messages.getString("PreparedStatement.66", new Object[]{x}), this.exceptionInterceptor
                  );
               }

               b = !x.matches("-?[0]+[.]*[0]*");
            } else {
               b = false;
            }

            return String.valueOf(b ? 1 : 0);
         case TINYINT:
         case TINYINT_UNSIGNED:
         case SMALLINT:
         case SMALLINT_UNSIGNED:
         case MEDIUMINT:
         case MEDIUMINT_UNSIGNED:
         case INT:
            return String.valueOf(Integer.parseInt(x));
         case INT_UNSIGNED:
         case BIGINT:
            return String.valueOf(Long.parseLong(x));
         case BIGINT_UNSIGNED:
            return String.valueOf(new BigInteger(x).longValue());
         case FLOAT:
         case FLOAT_UNSIGNED:
            return StringUtils.fixDecimalExponent(Float.toString(Float.parseFloat(x)));
         case DOUBLE:
         case DOUBLE_UNSIGNED:
            return StringUtils.fixDecimalExponent(Double.toString(Double.parseDouble(x)));
         case DECIMAL:
         case DECIMAL_UNSIGNED:
            return this.getScaled(new BigDecimal(x), binding.getScaleOrLength()).toPlainString();
         case DATE:
            Object dtxxx = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dtxxx instanceof LocalDate) {
               StringBuilder sbx = new StringBuilder("'");
               sbx.append(((LocalDate)dtxxx).format(TimeUtil.DATE_FORMATTER));
               sbx.append("'");
               return sbx.toString();
            } else {
               if (dtxxx instanceof LocalDateTime) {
                  StringBuilder sbx = new StringBuilder("'");
                  sbx.append(((LocalDateTime)dtxxx).format(TimeUtil.DATE_FORMATTER));
                  sbx.append("'");
                  return sbx.toString();
               }

               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class,
                  Messages.getString("PreparedStatement.67", new Object[]{dtxxx.getClass().getName(), binding.getMysqlType().toString()}),
                  this.exceptionInterceptor
               );
            }
         case DATETIME:
         case TIMESTAMP:
            Object dtxx = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dtxx instanceof LocalDate) {
               StringBuilder sbx = new StringBuilder("'");
               sbx.append(LocalDateTime.of((LocalDate)dtxx, TimeUtil.DEFAULT_TIME).format(TimeUtil.DATETIME_FORMATTER_WITH_OPTIONAL_MICROS));
               sbx.append("'");
               return sbx.toString();
            } else {
               if (dtxx instanceof LocalDateTime) {
                  StringBuilder sbx = new StringBuilder("'");
                  sbx.append(this.adjustLocalDateTime((LocalDateTime)dtxx, binding.getField()).format(TimeUtil.DATETIME_FORMATTER_WITH_OPTIONAL_MICROS));
                  sbx.append("'");
                  return sbx.toString();
               }

               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class,
                  Messages.getString("PreparedStatement.67", new Object[]{dtxx.getClass().getName(), binding.getMysqlType().toString()}),
                  this.exceptionInterceptor
               );
            }
         case TIME:
            Object dtx = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dtx instanceof LocalTime) {
               StringBuilder sbx = new StringBuilder("'");
               sbx.append(this.adjustLocalTime((LocalTime)dtx, binding.getField()).format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS));
               sbx.append("'");
               return sbx.toString();
            } else if (dtx instanceof LocalDateTime) {
               StringBuilder sbx = new StringBuilder("'");
               sbx.append(this.adjustLocalTime(((LocalDateTime)dtx).toLocalTime(), binding.getField()).format(TimeUtil.TIME_FORMATTER_WITH_OPTIONAL_MICROS));
               sbx.append("'");
               return sbx.toString();
            } else {
               if (dtx instanceof Duration) {
                  StringBuilder sbx = new StringBuilder("'");
                  sbx.append(TimeUtil.getDurationString(this.adjustDuration((Duration)dtx, binding.getField())));
                  sbx.append("'");
                  return sbx.toString();
               }

               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class,
                  Messages.getString("PreparedStatement.67", new Object[]{dtx.getClass().getName(), binding.getMysqlType().toString()}),
                  this.exceptionInterceptor
               );
            }
         case YEAR:
            Object dt = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dt instanceof LocalDate) {
               return String.valueOf(((LocalDate)dt).getYear());
            } else {
               if (dt instanceof LocalDateTime) {
                  return String.valueOf(((LocalDateTime)dt).getYear());
               }

               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class,
                  Messages.getString("PreparedStatement.67", new Object[]{dt.getClass().getName(), binding.getMysqlType().toString()}),
                  this.exceptionInterceptor
               );
            }
         default:
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class,
               Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}),
               this.exceptionInterceptor
            );
      }
   }

   @Override
   public void encodeAsQueryAttribute(Message msg, BindValue binding) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      String x = (String)binding.getValue();
      intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(x, this.charEncoding.getValue()));
   }

   @Override
   public void encodeAsBinary(Message param1, BindValue param2) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.OutOfMemoryError: Java heap space
      //   at java.base/java.util.HashSet.<init>(HashSet.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.isVersionToBeReplaced(StackVarsProcessor.java:858)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.iterateExprent(StackVarsProcessor.java:464)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.iterateStatements(StackVarsProcessor.java:189)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:72)
      //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:40)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:231)
      //
      // Bytecode:
      // 000: aload 1
      // 001: checkcast org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload
      // 004: astore 3
      // 005: aload 2
      // 006: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getValue ()Ljava/lang/Object; 1
      // 00b: checkcast java/lang/String
      // 00e: astore 4
      // 010: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder$1.$SwitchMap$com$mysql$cj$MysqlType [I
      // 013: aload 2
      // 014: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getMysqlType ()Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType; 1
      // 019: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/MysqlType.ordinal ()I
      // 01c: iaload
      // 01d: tableswitch 844 2 40 447 447 447 447 447 447 447 447 447 447 447 447 447 447 447 447 171 171 313 313 326 326 326 326 339 339 352 352 365 365 382 382 398 398 590 647 647 704 781
      // 0c8: aconst_null
      // 0c9: astore 5
      // 0cb: ldc "true"
      // 0cd: aload 4
      // 0cf: invokevirtual java/lang/String.equalsIgnoreCase (Ljava/lang/String;)Z
      // 0d2: ifne 0df
      // 0d5: ldc "Y"
      // 0d7: aload 4
      // 0d9: invokevirtual java/lang/String.equalsIgnoreCase (Ljava/lang/String;)Z
      // 0dc: ifeq 0e8
      // 0df: bipush 1
      // 0e0: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 0e3: astore 5
      // 0e5: goto 141
      // 0e8: ldc "false"
      // 0ea: aload 4
      // 0ec: invokevirtual java/lang/String.equalsIgnoreCase (Ljava/lang/String;)Z
      // 0ef: ifne 0fc
      // 0f2: ldc "N"
      // 0f4: aload 4
      // 0f6: invokevirtual java/lang/String.equalsIgnoreCase (Ljava/lang/String;)Z
      // 0f9: ifeq 105
      // 0fc: bipush 0
      // 0fd: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 100: astore 5
      // 102: goto 141
      // 105: aload 4
      // 107: ldc "-?\\d+\\.?\\d*"
      // 109: invokevirtual java/lang/String.matches (Ljava/lang/String;)Z
      // 10c: ifeq 126
      // 10f: aload 4
      // 111: ldc "-?[0]+[.]*[0]*"
      // 113: invokevirtual java/lang/String.matches (Ljava/lang/String;)Z
      // 116: ifne 11d
      // 119: bipush 1
      // 11a: goto 11e
      // 11d: bipush 0
      // 11e: invokestatic java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;
      // 121: astore 5
      // 123: goto 141
      // 126: ldc org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/WrongArgumentException
      // 128: ldc "PreparedStatement.66"
      // 12a: bipush 1
      // 12b: anewarray 196
      // 12e: dup
      // 12f: bipush 0
      // 130: aload 4
      // 132: aastore
      // 133: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/Messages.getString (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
      // 136: aload 0
      // 137: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.exceptionInterceptor Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;
      // 13a: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionFactory.createException (Ljava/lang/Class;Ljava/lang/String;Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/CJException;
      // 13d: checkcast org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/WrongArgumentException
      // 140: athrow
      // 141: aload 3
      // 142: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT1 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 145: aload 5
      // 147: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 14a: ifeq 151
      // 14d: lconst_1
      // 14e: goto 152
      // 151: lconst_0
      // 152: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 155: return
      // 156: aload 3
      // 157: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT1 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 15a: aload 4
      // 15c: invokestatic java/lang/Long.parseLong (Ljava/lang/String;)J
      // 15f: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 162: return
      // 163: aload 3
      // 164: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT2 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 167: aload 4
      // 169: invokestatic java/lang/Long.parseLong (Ljava/lang/String;)J
      // 16c: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 16f: return
      // 170: aload 3
      // 171: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT4 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 174: aload 4
      // 176: invokestatic java/lang/Long.parseLong (Ljava/lang/String;)J
      // 179: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 17c: return
      // 17d: aload 3
      // 17e: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT8 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 181: aload 4
      // 183: invokestatic java/lang/Long.parseLong (Ljava/lang/String;)J
      // 186: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 189: return
      // 18a: aload 3
      // 18b: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT4 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 18e: aload 4
      // 190: invokestatic java/lang/Float.parseFloat (Ljava/lang/String;)F
      // 193: invokestatic java/lang/Float.floatToIntBits (F)I
      // 196: i2l
      // 197: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 19a: return
      // 19b: aload 3
      // 19c: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT8 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 19f: aload 4
      // 1a1: invokestatic java/lang/Double.parseDouble (Ljava/lang/String;)D
      // 1a4: invokestatic java/lang/Double.doubleToLongBits (D)J
      // 1a7: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 1aa: return
      // 1ab: aload 0
      // 1ac: new java/math/BigDecimal
      // 1af: dup
      // 1b0: aload 4
      // 1b2: invokespecial java/math/BigDecimal.<init> (Ljava/lang/String;)V
      // 1b5: aload 2
      // 1b6: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getScaleOrLength ()J 1
      // 1bb: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.getScaled (Ljava/math/BigDecimal;J)Ljava/math/BigDecimal;
      // 1be: astore 6
      // 1c0: aload 3
      // 1c1: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$StringSelfDataType.STRING_LENENC Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$StringSelfDataType;
      // 1c4: aload 6
      // 1c6: invokevirtual java/math/BigDecimal.toPlainString ()Ljava/lang/String;
      // 1c9: aload 0
      // 1ca: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.charEncoding Lorg/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty;
      // 1cd: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty.getValue ()Ljava/lang/Object; 1
      // 1d2: checkcast java/lang/String
      // 1d5: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/util/StringUtils.getBytes (Ljava/lang/String;Ljava/lang/String;)[B
      // 1d8: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeBytes (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$StringSelfDataType;[B)V
      // 1db: return
      // 1dc: aload 2
      // 1dd: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.isNational ()Z 1
      // 1e2: ifeq 21b
      // 1e5: aload 0
      // 1e6: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.charEncoding Lorg/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty;
      // 1e9: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty.getValue ()Ljava/lang/Object; 1
      // 1ee: checkcast java/lang/String
      // 1f1: ldc "UTF-8"
      // 1f3: invokevirtual java/lang/String.equalsIgnoreCase (Ljava/lang/String;)Z
      // 1f6: ifne 21b
      // 1f9: aload 0
      // 1fa: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.charEncoding Lorg/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty;
      // 1fd: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty.getValue ()Ljava/lang/Object; 1
      // 202: checkcast java/lang/String
      // 205: ldc "utf8"
      // 207: invokevirtual java/lang/String.equalsIgnoreCase (Ljava/lang/String;)Z
      // 20a: ifne 21b
      // 20d: ldc_w "ServerPreparedStatement.31"
      // 210: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/Messages.getString (Ljava/lang/String;)Ljava/lang/String;
      // 213: aload 0
      // 214: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.exceptionInterceptor Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;
      // 217: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionFactory.createException (Ljava/lang/String;Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/CJException;
      // 21a: athrow
      // 21b: aload 3
      // 21c: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$StringSelfDataType.STRING_LENENC Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$StringSelfDataType;
      // 21f: aload 4
      // 221: aload 0
      // 222: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.charEncoding Lorg/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty;
      // 225: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty.getValue ()Ljava/lang/Object; 1
      // 22a: checkcast java/lang/String
      // 22d: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/util/StringUtils.getBytes (Ljava/lang/String;Ljava/lang/String;)[B
      // 230: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeBytes (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$StringSelfDataType;[B)V
      // 233: goto 26a
      // 236: astore 7
      // 238: new java/lang/StringBuilder
      // 23b: dup
      // 23c: invokespecial java/lang/StringBuilder.<init> ()V
      // 23f: ldc_w "ServerPreparedStatement.31"
      // 242: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/Messages.getString (Ljava/lang/String;)Ljava/lang/String;
      // 245: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 248: aload 0
      // 249: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.charEncoding Lorg/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty;
      // 24c: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/conf/RuntimeProperty.getValue ()Ljava/lang/Object; 1
      // 251: checkcast java/lang/String
      // 254: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 257: ldc_w "'"
      // 25a: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 25d: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 260: aload 7
      // 262: aload 0
      // 263: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.exceptionInterceptor Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;
      // 266: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionFactory.createException (Ljava/lang/String;Ljava/lang/Throwable;Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/CJException;
      // 269: athrow
      // 26a: return
      // 26b: aload 4
      // 26d: aload 2
      // 26e: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getMysqlType ()Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType; 1
      // 273: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/util/TimeUtil.parseToDateTimeObject (Ljava/lang/String;Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType;)Ljava/lang/Object;
      // 276: astore 7
      // 278: aload 7
      // 27a: instanceof java/time/LocalDate
      // 27d: ifeq 28e
      // 280: aload 0
      // 281: aload 1
      // 282: aload 7
      // 284: checkcast java/time/LocalDate
      // 287: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalDate.from (Ljava/time/LocalDate;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalDate;
      // 28a: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.writeDate (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/Message;Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalDate;)V
      // 28d: return
      // 28e: aload 7
      // 290: instanceof java/time/LocalDateTime
      // 293: ifeq 369
      // 296: aload 0
      // 297: aload 1
      // 298: aload 7
      // 29a: checkcast java/time/LocalDateTime
      // 29d: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp.from (Ljava/time/LocalDateTime;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp;
      // 2a0: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.writeDateTime (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/Message;Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp;)V
      // 2a3: return
      // 2a4: aload 4
      // 2a6: aload 2
      // 2a7: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getMysqlType ()Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType; 1
      // 2ac: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/util/TimeUtil.parseToDateTimeObject (Ljava/lang/String;Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType;)Ljava/lang/Object;
      // 2af: astore 7
      // 2b1: aload 7
      // 2b3: instanceof java/time/LocalDate
      // 2b6: ifeq 2c7
      // 2b9: aload 0
      // 2ba: aload 1
      // 2bb: aload 7
      // 2bd: checkcast java/time/LocalDate
      // 2c0: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp.from (Ljava/time/LocalDate;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp;
      // 2c3: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.writeDateTime (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/Message;Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp;)V
      // 2c6: return
      // 2c7: aload 7
      // 2c9: instanceof java/time/LocalDateTime
      // 2cc: ifeq 369
      // 2cf: aload 0
      // 2d0: aload 1
      // 2d1: aload 7
      // 2d3: checkcast java/time/LocalDateTime
      // 2d6: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp.from (Ljava/time/LocalDateTime;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp;
      // 2d9: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.writeDateTime (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/Message;Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTimestamp;)V
      // 2dc: return
      // 2dd: aload 4
      // 2df: aload 2
      // 2e0: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getMysqlType ()Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType; 1
      // 2e5: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/util/TimeUtil.parseToDateTimeObject (Ljava/lang/String;Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType;)Ljava/lang/Object;
      // 2e8: astore 7
      // 2ea: aload 7
      // 2ec: instanceof java/time/LocalTime
      // 2ef: ifeq 300
      // 2f2: aload 0
      // 2f3: aload 1
      // 2f4: aload 7
      // 2f6: checkcast java/time/LocalTime
      // 2f9: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTime.from (Ljava/time/LocalTime;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTime;
      // 2fc: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.writeTime (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/Message;Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTime;)V
      // 2ff: return
      // 300: aload 7
      // 302: instanceof java/time/Duration
      // 305: ifeq 369
      // 308: aload 0
      // 309: aload 1
      // 30a: aload 0
      // 30b: aload 2
      // 30c: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getValue ()Ljava/lang/Object; 1
      // 311: checkcast java/time/Duration
      // 314: invokevirtual java/time/Duration.toNanos ()J
      // 317: invokestatic java/time/Duration.ofNanos (J)Ljava/time/Duration;
      // 31a: aload 2
      // 31b: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getField ()Lorg/lushplugins/lushrewards/libraries/mysql/cj/result/Field; 1
      // 320: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.adjustDuration (Ljava/time/Duration;Lorg/lushplugins/lushrewards/libraries/mysql/cj/result/Field;)Ljava/time/Duration;
      // 323: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTime.from (Ljava/time/Duration;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTime;
      // 326: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.writeTime (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/Message;Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/InternalTime;)V
      // 329: return
      // 32a: aload 4
      // 32c: aload 2
      // 32d: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getMysqlType ()Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType; 1
      // 332: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/util/TimeUtil.parseToDateTimeObject (Ljava/lang/String;Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType;)Ljava/lang/Object;
      // 335: astore 7
      // 337: aload 7
      // 339: instanceof java/time/LocalDate
      // 33c: ifeq 350
      // 33f: aload 3
      // 340: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT4 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 343: aload 7
      // 345: checkcast java/time/LocalDate
      // 348: invokevirtual java/time/LocalDate.getYear ()I
      // 34b: i2l
      // 34c: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 34f: return
      // 350: aload 7
      // 352: instanceof java/time/LocalDateTime
      // 355: ifeq 369
      // 358: aload 3
      // 359: getstatic org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType.INT4 Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;
      // 35c: aload 7
      // 35e: checkcast java/time/LocalDateTime
      // 361: invokevirtual java/time/LocalDateTime.getYear ()I
      // 364: i2l
      // 365: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativePacketPayload.writeInteger (Lorg/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/NativeConstants$IntegerDataType;J)V
      // 368: return
      // 369: ldc org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/WrongArgumentException
      // 36b: ldc_w "PreparedStatement.67"
      // 36e: bipush 2
      // 36f: anewarray 196
      // 372: dup
      // 373: bipush 0
      // 374: aload 2
      // 375: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getValue ()Ljava/lang/Object; 1
      // 37a: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 37d: invokevirtual java/lang/Class.getName ()Ljava/lang/String;
      // 380: aastore
      // 381: dup
      // 382: bipush 1
      // 383: aload 2
      // 384: invokeinterface org/lushplugins/lushrewards/libraries/mysql/cj/BindValue.getMysqlType ()Lorg/lushplugins/lushrewards/libraries/mysql/cj/MysqlType; 1
      // 389: invokevirtual org/lushplugins/lushrewards/libraries/mysql/cj/MysqlType.toString ()Ljava/lang/String;
      // 38c: aastore
      // 38d: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/Messages.getString (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
      // 390: aload 0
      // 391: getfield org/lushplugins/lushrewards/libraries/mysql/cj/protocol/a/StringValueEncoder.exceptionInterceptor Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;
      // 394: invokestatic org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionFactory.createException (Ljava/lang/Class;Ljava/lang/String;Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/ExceptionInterceptor;)Lorg/lushplugins/lushrewards/libraries/mysql/cj/exceptions/CJException;
      // 397: checkcast org/lushplugins/lushrewards/libraries/mysql/cj/exceptions/WrongArgumentException
      // 39a: athrow
   }

   private boolean isEscapeNeededForString(String x, int stringLength) {
      for (int i = 0; i < stringLength; i++) {
         char c = x.charAt(i);
         switch (c) {
            case '\u0000':
            case '\n':
            case '\r':
            case '\u001a':
            case '"':
            case '\'':
            case '\\':
               return true;
         }
      }

      return false;
   }
}

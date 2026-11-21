package com.mysql.cj.protocol.a;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.InternalDate;
import com.mysql.cj.protocol.InternalTime;
import com.mysql.cj.protocol.InternalTimestamp;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TimeUtil;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
   public void encodeAsBinary(Message msg, BindValue binding) {
      NativePacketPayload intoPacket = (NativePacketPayload)msg;
      String x = (String)binding.getValue();
      switch (binding.getMysqlType()) {
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
            if (binding.isNational() && !this.charEncoding.getValue().equalsIgnoreCase("UTF-8") && !this.charEncoding.getValue().equalsIgnoreCase("utf8")) {
               throw ExceptionFactory.createException(Messages.getString("ServerPreparedStatement.31"), this.exceptionInterceptor);
            }

            try {
               intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(x, this.charEncoding.getValue()));
               return;
            } catch (CJException var8) {
               throw ExceptionFactory.createException(
                  Messages.getString("ServerPreparedStatement.31") + this.charEncoding.getValue() + "'", var8, this.exceptionInterceptor
               );
            }
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

            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, b ? 1L : 0L);
            return;
         case TINYINT:
         case TINYINT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT1, Long.parseLong(x));
            return;
         case SMALLINT:
         case SMALLINT_UNSIGNED:
         case MEDIUMINT:
         case MEDIUMINT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT2, Long.parseLong(x));
            return;
         case INT:
         case INT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, Long.parseLong(x));
            return;
         case BIGINT:
         case BIGINT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, Long.parseLong(x));
            return;
         case FLOAT:
         case FLOAT_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, Float.floatToIntBits(Float.parseFloat(x)));
            return;
         case DOUBLE:
         case DOUBLE_UNSIGNED:
            intoPacket.writeInteger(NativeConstants.IntegerDataType.INT8, Double.doubleToLongBits(Double.parseDouble(x)));
            return;
         case DECIMAL:
         case DECIMAL_UNSIGNED:
            BigDecimal bd = this.getScaled(new BigDecimal(x), binding.getScaleOrLength());
            intoPacket.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, StringUtils.getBytes(bd.toPlainString(), this.charEncoding.getValue()));
            return;
         case DATE:
            Object dtxxx = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dtxxx instanceof LocalDate) {
               this.writeDate(msg, InternalDate.from((LocalDate)dtxxx));
               return;
            }

            if (dtxxx instanceof LocalDateTime) {
               this.writeDateTime(msg, InternalTimestamp.from((LocalDateTime)dtxxx));
               return;
            }
            break;
         case DATETIME:
         case TIMESTAMP:
            Object dtxx = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dtxx instanceof LocalDate) {
               this.writeDateTime(msg, InternalTimestamp.from((LocalDate)dtxx));
               return;
            }

            if (dtxx instanceof LocalDateTime) {
               this.writeDateTime(msg, InternalTimestamp.from((LocalDateTime)dtxx));
               return;
            }
            break;
         case TIME:
            Object dtx = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dtx instanceof LocalTime) {
               this.writeTime(msg, InternalTime.from((LocalTime)dtx));
               return;
            }

            if (dtx instanceof Duration) {
               this.writeTime(msg, InternalTime.from(this.adjustDuration(Duration.ofNanos(((Duration)binding.getValue()).toNanos()), binding.getField())));
               return;
            }
            break;
         case YEAR:
            Object dt = TimeUtil.parseToDateTimeObject(x, binding.getMysqlType());
            if (dt instanceof LocalDate) {
               intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, ((LocalDate)dt).getYear());
               return;
            }

            if (dt instanceof LocalDateTime) {
               intoPacket.writeInteger(NativeConstants.IntegerDataType.INT4, ((LocalDateTime)dt).getYear());
               return;
            }
      }

      throw (WrongArgumentException)ExceptionFactory.createException(
         WrongArgumentException.class,
         Messages.getString("PreparedStatement.67", new Object[]{binding.getValue().getClass().getName(), binding.getMysqlType().toString()}),
         this.exceptionInterceptor
      );
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

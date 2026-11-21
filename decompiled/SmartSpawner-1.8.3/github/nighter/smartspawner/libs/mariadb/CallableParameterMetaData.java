package github.nighter.smartspawner.libs.mariadb;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;

public class CallableParameterMetaData implements java.sql.ParameterMetaData {
   private final List<CallableParameterMetaData.ParamInfo> params;
   private final boolean isFunction;

   public CallableParameterMetaData(ResultSet rs, boolean isFunction) throws SQLException {
      List<CallableParameterMetaData.ParamInfo> list = new ArrayList<>();

      while (rs.next()) {
         CallableParameterMetaData.ParamInfo p = new CallableParameterMetaData.ParamInfo();
         p.parameterName = rs.getString(1);
         p.parameterMode = rs.getString(2);
         String dt = rs.getString(3);
         p.dataType = dt == null ? null : dt.toUpperCase(Locale.ROOT);
         String dtd = rs.getString(4);
         p.dtdIdentifier = dtd == null ? null : dtd.toUpperCase(Locale.ROOT);
         p.numericPrecision = rs.getInt(5);
         p.numericScale = rs.getInt(6);
         p.characterMaxLength = (int)Math.min(rs.getLong(7), 2147483647L);
         if (p.numericPrecision == 7 && "MEDIUMINT".equals(p.dataType) && p.dtdIdentifier != null && p.dtdIdentifier.contains(" UNSIGNED")) {
            p.numericPrecision = 8;
         }

         list.add(p);
      }

      this.params = list;
      this.isFunction = isFunction;
   }

   CallableParameterMetaData(List<CallableParameterMetaData.ParamInfo> params, boolean isFunction) {
      this.params = params;
      this.isFunction = isFunction;
   }

   private CallableParameterMetaData.ParamInfo at(int index) throws SQLException {
      if (index >= 1 && index <= this.params.size()) {
         return this.params.get(index - 1);
      } else {
         throw new SQLException("invalid parameter index " + index);
      }
   }

   @Override
   public int getParameterCount() {
      return this.params.size();
   }

   @Override
   public int isNullable(int index) throws SQLException {
      this.at(index);
      return 2;
   }

   @Override
   public boolean isSigned(int index) throws SQLException {
      String dtd = this.at(index).dtdIdentifier;
      return dtd != null && !dtd.contains(" UNSIGNED");
   }

   @Override
   public int getPrecision(int index) throws SQLException {
      CallableParameterMetaData.ParamInfo p = this.at(index);
      return p.numericPrecision > 0 ? p.numericPrecision : p.characterMaxLength;
   }

   @Override
   public int getScale(int index) throws SQLException {
      return this.at(index).numericScale;
   }

   public String getParameterName(int index) throws SQLException {
      return this.at(index).parameterName;
   }

   @Override
   public int getParameterType(int index) throws SQLException {
      CallableParameterMetaData.ParamInfo p = this.at(index);
      String str = p.dataType == null ? "" : p.dataType;
      switch (str) {
         case "BIT":
            return -7;
         case "TINYINT":
            if ("TINYINT(1)".equals(p.dtdIdentifier)) {
               return 16;
            }

            return -6;
         case "SMALLINT":
         case "YEAR":
            return 5;
         case "MEDIUMINT":
         case "INT":
         case "INT24":
         case "INTEGER":
            return 4;
         case "LONG":
         case "BIGINT":
            return -5;
         case "REAL":
         case "DOUBLE":
            return 8;
         case "FLOAT":
            return 6;
         case "DECIMAL":
            return 3;
         case "CHAR":
            return 1;
         case "VARCHAR":
         case "ENUM":
         case "TINYTEXT":
         case "SET":
            return 12;
         case "DATE":
            return 91;
         case "TIME":
            return 92;
         case "TIMESTAMP":
         case "DATETIME":
            return 93;
         case "BINARY":
            return -2;
         case "VARBINARY":
            return -3;
         case "TINYBLOB":
         case "BLOB":
         case "MEDIUMBLOB":
         case "LONGBLOB":
         case "GEOMETRY":
            return 2004;
         case "TEXT":
         case "MEDIUMTEXT":
         case "LONGTEXT":
            return 2005;
         default:
            return 1111;
      }
   }

   @Override
   public String getParameterTypeName(int index) throws SQLException {
      CallableParameterMetaData.ParamInfo p = this.at(index);
      return "TINYINT(1)".equals(p.dtdIdentifier) ? "BOOLEAN" : p.dataType;
   }

   @Override
   public String getParameterClassName(int index) throws SQLException {
      CallableParameterMetaData.ParamInfo p = this.at(index);
      String str = p.dataType == null ? "" : p.dataType;
      switch (str) {
         case "BIT":
            return BitSet.class.getName();
         case "TINYINT":
            if ("TINYINT(1)".equals(p.dtdIdentifier)) {
               return boolean.class.getName();
            }

            return byte.class.getName();
         case "SMALLINT":
         case "YEAR":
            return short.class.getName();
         case "MEDIUMINT":
         case "INT":
         case "INTEGER":
            return int.class.getName();
         case "BINARY":
         case "SET":
         case "GEOMETRY":
         case "VARBINARY":
         case "TINYBLOB":
            return byte[].class.getName();
         case "BIGINT":
            return long.class.getName();
         case "FLOAT":
            return float.class.getName();
         case "DECIMAL":
            return BigDecimal.class.getName();
         case "REAL":
         case "DOUBLE":
            return double.class.getName();
         case "CHAR":
         case "VARCHAR":
         case "ENUM":
         case "TINYTEXT":
            return String.class.getName();
         case "TEXT":
         case "MEDIUMTEXT":
         case "LONGTEXT":
            return Clob.class.getName();
         case "DATE":
            return Date.class.getName();
         case "TIME":
            return Time.class.getName();
         case "TIMESTAMP":
         case "DATETIME":
            return Timestamp.class.getName();
         case "BLOB":
         case "MEDIUMBLOB":
         case "LONGBLOB":
            return Blob.class.getName();
         default:
            return Object.class.getName();
      }
   }

   @Override
   public int getParameterMode(int index) throws SQLException {
      CallableParameterMetaData.ParamInfo p = this.at(index);
      if (this.isFunction) {
         return 4;
      } else {
         String str = p.parameterMode == null ? "" : p.parameterMode;
         switch (str) {
            case "IN":
               return 1;
            case "OUT":
               return 4;
            case "INOUT":
               return 2;
            default:
               return 0;
         }
      }
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      if (this.isWrapperFor(iface)) {
         return iface.cast(this);
      } else {
         throw new SQLException("The receiver is not a wrapper for " + iface.getName());
      }
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) {
      return iface.isInstance(this);
   }

   static final class ParamInfo {
      String parameterName;
      String parameterMode;
      String dataType;
      String dtdIdentifier;
      int numericPrecision;
      int numericScale;
      int characterMaxLength;
   }
}

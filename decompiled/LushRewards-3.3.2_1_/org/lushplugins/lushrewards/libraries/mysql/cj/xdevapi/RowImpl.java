package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.TimeZone;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.DataReadException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.BigDecimalValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.BooleanValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.ByteValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.DoubleValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.IntegerValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.LongValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.SqlDateValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.SqlTimeValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.SqlTimestampValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.StringValueFactory;

public class RowImpl implements Row {
   private org.lushplugins.lushrewards.libraries.mysql.cj.result.Row row;
   private ColumnDefinition metadata;
   private TimeZone defaultTimeZone;
   private PropertySet pset;

   public RowImpl(org.lushplugins.lushrewards.libraries.mysql.cj.result.Row row, ColumnDefinition metadata, TimeZone defaultTimeZone, PropertySet pset) {
      this.row = row;
      this.metadata = metadata;
      this.defaultTimeZone = defaultTimeZone;
      this.pset = pset;
   }

   private int fieldNameToIndex(String fieldName) {
      int idx = this.metadata.findColumn(fieldName, true, 0);
      if (idx == -1) {
         throw new DataReadException("Invalid column");
      } else {
         return idx;
      }
   }

   @Override
   public BigDecimal getBigDecimal(String fieldName) {
      return this.getBigDecimal(this.fieldNameToIndex(fieldName));
   }

   @Override
   public BigDecimal getBigDecimal(int pos) {
      return this.row.getValue(pos, new BigDecimalValueFactory(this.pset));
   }

   @Override
   public boolean getBoolean(String fieldName) {
      return this.getBoolean(this.fieldNameToIndex(fieldName));
   }

   @Override
   public boolean getBoolean(int pos) {
      Boolean res = this.row.getValue(pos, new BooleanValueFactory(this.pset));
      return res == null ? false : res;
   }

   @Override
   public byte getByte(String fieldName) {
      return this.getByte(this.fieldNameToIndex(fieldName));
   }

   @Override
   public byte getByte(int pos) {
      Byte res = this.row.getValue(pos, new ByteValueFactory(this.pset));
      return res == null ? 0 : res;
   }

   @Override
   public Date getDate(String fieldName) {
      return this.getDate(this.fieldNameToIndex(fieldName));
   }

   @Override
   public Date getDate(int pos) {
      return this.row.getValue(pos, new SqlDateValueFactory(this.pset, null, this.defaultTimeZone));
   }

   @Override
   public DbDoc getDbDoc(String fieldName) {
      return this.getDbDoc(this.fieldNameToIndex(fieldName));
   }

   @Override
   public DbDoc getDbDoc(int pos) {
      return this.row.getValue(pos, new DbDocValueFactory(this.pset));
   }

   @Override
   public double getDouble(String fieldName) {
      return this.getDouble(this.fieldNameToIndex(fieldName));
   }

   @Override
   public double getDouble(int pos) {
      Double res = this.row.getValue(pos, new DoubleValueFactory(this.pset));
      return res == null ? 0.0 : res;
   }

   @Override
   public int getInt(String fieldName) {
      return this.getInt(this.fieldNameToIndex(fieldName));
   }

   @Override
   public int getInt(int pos) {
      Integer res = this.row.getValue(pos, new IntegerValueFactory(this.pset));
      return res == null ? 0 : res;
   }

   @Override
   public long getLong(String fieldName) {
      return this.getLong(this.fieldNameToIndex(fieldName));
   }

   @Override
   public long getLong(int pos) {
      Long res = this.row.getValue(pos, new LongValueFactory(this.pset));
      return res == null ? 0L : res;
   }

   @Override
   public String getString(String fieldName) {
      return this.getString(this.fieldNameToIndex(fieldName));
   }

   @Override
   public String getString(int pos) {
      return this.row.getValue(pos, new StringValueFactory(this.pset));
   }

   @Override
   public Time getTime(String fieldName) {
      return this.getTime(this.fieldNameToIndex(fieldName));
   }

   @Override
   public Time getTime(int pos) {
      return this.row.getValue(pos, new SqlTimeValueFactory(this.pset, null, this.defaultTimeZone));
   }

   @Override
   public Timestamp getTimestamp(String fieldName) {
      return this.getTimestamp(this.fieldNameToIndex(fieldName));
   }

   @Override
   public Timestamp getTimestamp(int pos) {
      return this.row.getValue(pos, new SqlTimestampValueFactory(this.pset, null, this.defaultTimeZone, this.defaultTimeZone));
   }
}

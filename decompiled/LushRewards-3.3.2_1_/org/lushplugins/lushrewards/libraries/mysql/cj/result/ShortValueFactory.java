package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.lushplugins.lushrewards.libraries.mysql.cj.Constants;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.NumberOutOfRange;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.DataTypeUtil;

public class ShortValueFactory extends AbstractNumericValueFactory<Short> {
   public ShortValueFactory(PropertySet pset) {
      super(pset);
   }

   public Short createFromBigInteger(BigInteger i) {
      if (!this.jdbcCompliantTruncationForReads
         || i.compareTo(Constants.BIG_INTEGER_MIN_SHORT_VALUE) >= 0 && i.compareTo(Constants.BIG_INTEGER_MAX_SHORT_VALUE) <= 0) {
         return (short)i.intValue();
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{i, this.getTargetTypeName()}));
      }
   }

   public Short createFromLong(long l) {
      if (!this.jdbcCompliantTruncationForReads || l >= -32768L && l <= 32767L) {
         return (short)l;
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{Long.valueOf(l).toString(), this.getTargetTypeName()}));
      }
   }

   public Short createFromBigDecimal(BigDecimal d) {
      if (!this.jdbcCompliantTruncationForReads
         || d.compareTo(Constants.BIG_DECIMAL_MIN_SHORT_VALUE) >= 0 && d.compareTo(Constants.BIG_DECIMAL_MAX_SHORT_VALUE) <= 0) {
         return (short)d.longValue();
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{d, this.getTargetTypeName()}));
      }
   }

   public Short createFromDouble(double d) {
      if (!this.jdbcCompliantTruncationForReads || !(d < -32768.0) && !(d > 32767.0)) {
         return (short)d;
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{d, this.getTargetTypeName()}));
      }
   }

   public Short createFromBit(byte[] bytes, int offset, int length) {
      long l = DataTypeUtil.bitToLong(bytes, offset, length);
      if (this.jdbcCompliantTruncationForReads && l >> 16 != 0L) {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{Long.valueOf(l).toString(), this.getTargetTypeName()}));
      } else {
         return (short)l;
      }
   }

   @Override
   public String getTargetTypeName() {
      return Short.class.getName();
   }
}

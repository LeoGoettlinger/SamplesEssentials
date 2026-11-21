package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.lushplugins.lushrewards.libraries.mysql.cj.Constants;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.NumberOutOfRange;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.DataTypeUtil;

public class LongValueFactory extends AbstractNumericValueFactory<Long> {
   public LongValueFactory(PropertySet pset) {
      super(pset);
   }

   public Long createFromBigInteger(BigInteger i) {
      if (!this.jdbcCompliantTruncationForReads
         || i.compareTo(Constants.BIG_INTEGER_MIN_LONG_VALUE) >= 0 && i.compareTo(Constants.BIG_INTEGER_MAX_LONG_VALUE) <= 0) {
         return i.longValue();
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{i, this.getTargetTypeName()}));
      }
   }

   public Long createFromLong(long l) {
      if (!this.jdbcCompliantTruncationForReads || l >= Long.MIN_VALUE && l <= Long.MAX_VALUE) {
         return l;
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{Long.valueOf(l).toString(), this.getTargetTypeName()}));
      }
   }

   public Long createFromBigDecimal(BigDecimal d) {
      if (!this.jdbcCompliantTruncationForReads
         || d.compareTo(Constants.BIG_DECIMAL_MIN_LONG_VALUE) >= 0 && d.compareTo(Constants.BIG_DECIMAL_MAX_LONG_VALUE) <= 0) {
         return d.longValue();
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{d, this.getTargetTypeName()}));
      }
   }

   public Long createFromDouble(double d) {
      if (!this.jdbcCompliantTruncationForReads || !(d < -9.223372E18F) && !(d > 9.223372E18F)) {
         return (long)d;
      } else {
         throw new NumberOutOfRange(Messages.getString("ResultSet.NumberOutOfRange", new Object[]{d, this.getTargetTypeName()}));
      }
   }

   public Long createFromBit(byte[] bytes, int offset, int length) {
      return DataTypeUtil.bitToLong(bytes, offset, length);
   }

   @Override
   public String getTargetTypeName() {
      return Long.class.getName();
   }
}

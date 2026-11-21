package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.DataConversionException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.MysqlTextValueDecoder;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public abstract class AbstractNumericValueFactory<T> extends DefaultValueFactory<T> {
   public AbstractNumericValueFactory(PropertySet pset) {
      super(pset);
   }

   @Override
   public T createFromBytes(byte[] bytes, int offset, int length, Field f) {
      if (length == 0 && this.pset.getBooleanProperty(PropertyKey.emptyStringsConvertToZero).getValue()) {
         return this.createFromLong(0L);
      } else {
         String s = StringUtils.toString(bytes, offset, length, f.getEncoding());
         byte[] newBytes = s.getBytes();
         if (s.contains("e") || s.contains("E") || s.matches("-?\\d*\\.\\d*")) {
            return this.createFromDouble(MysqlTextValueDecoder.getDouble(newBytes, 0, newBytes.length));
         } else if (!s.matches("-?\\d+")) {
            throw new DataConversionException(Messages.getString("ResultSet.UnableToInterpretString", new Object[]{s}));
         } else {
            return s.charAt(0) != '-' && (length > 19 || newBytes[0] < 48 || newBytes[0] > 56)
               ? this.createFromBigInteger(MysqlTextValueDecoder.getBigInteger(newBytes, 0, newBytes.length))
               : this.createFromLong(MysqlTextValueDecoder.getLong(newBytes, 0, newBytes.length));
         }
      }
   }

   @Override
   public T createFromYear(long l) {
      return this.createFromLong(l);
   }
}

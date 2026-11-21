package org.sethomegui.SetHomeGUI.libs.mysql.cj.result;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalDate;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTimestamp;

public interface ValueFactory<T> {
   void setPropertySet(PropertySet var1);

   T createFromDate(InternalDate var1);

   T createFromTime(InternalTime var1);

   T createFromTimestamp(InternalTimestamp var1);

   T createFromDatetime(InternalTimestamp var1);

   T createFromLong(long var1);

   T createFromBigInteger(BigInteger var1);

   T createFromDouble(double var1);

   T createFromBigDecimal(BigDecimal var1);

   T createFromBytes(byte[] var1, int var2, int var3, Field var4);

   T createFromBit(byte[] var1, int var2, int var3);

   T createFromYear(long var1);

   T createFromNull();

   String getTargetTypeName();
}

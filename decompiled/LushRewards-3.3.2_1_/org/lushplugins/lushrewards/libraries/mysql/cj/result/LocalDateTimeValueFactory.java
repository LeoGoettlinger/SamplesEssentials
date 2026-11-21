package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import java.time.LocalDateTime;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.DataReadException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalDate;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTime;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTimestamp;

public class LocalDateTimeValueFactory extends AbstractDateTimeValueFactory<LocalDateTime> {
   public LocalDateTimeValueFactory(PropertySet pset) {
      super(pset);
   }

   public LocalDateTime localCreateFromDate(InternalDate idate) {
      return this.createFromTimestamp(new InternalTimestamp(idate.getYear(), idate.getMonth(), idate.getDay(), 0, 0, 0, 0, 0));
   }

   public LocalDateTime localCreateFromTime(InternalTime it) {
      if (it.getHours() >= 0 && it.getHours() < 24) {
         return this.createFromTimestamp(new InternalTimestamp(1970, 1, 1, it.getHours(), it.getMinutes(), it.getSeconds(), it.getNanos(), it.getScale()));
      } else {
         throw new DataReadException(Messages.getString("ResultSet.InvalidTimeValue", new Object[]{it.toString()}));
      }
   }

   public LocalDateTime localCreateFromTimestamp(InternalTimestamp its) {
      if (its.getYear() == 0 && its.getMonth() == 0 && its.getDay() == 0) {
         throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
      } else {
         return LocalDateTime.of(its.getYear(), its.getMonth(), its.getDay(), its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos());
      }
   }

   public LocalDateTime localCreateFromDatetime(InternalTimestamp its) {
      if (its.getYear() == 0 && its.getMonth() == 0 && its.getDay() == 0) {
         throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
      } else {
         return LocalDateTime.of(its.getYear(), its.getMonth(), its.getDay(), its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos());
      }
   }

   @Override
   public String getTargetTypeName() {
      return LocalDateTime.class.getName();
   }
}

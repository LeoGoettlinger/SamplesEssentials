package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import java.time.Duration;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalDate;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTime;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTimestamp;

public class DurationValueFactory extends AbstractDateTimeValueFactory<Duration> {
   public DurationValueFactory(PropertySet pset) {
      super(pset);
   }

   Duration localCreateFromDate(InternalDate idate) {
      return this.unsupported("DATE");
   }

   public Duration localCreateFromTime(InternalTime it) {
      String ptn = (it.getHours() < 0 ? "-PT" : "PT")
         + (it.getHours() < 0 ? -it.getHours() : it.getHours())
         + "H"
         + it.getMinutes()
         + "M"
         + it.getSeconds()
         + "."
         + it.getNanos()
         + "S";
      return Duration.parse(ptn);
   }

   public Duration localCreateFromTimestamp(InternalTimestamp its) {
      return this.unsupported("TIMESTAMP");
   }

   public Duration localCreateFromDatetime(InternalTimestamp its) {
      return this.unsupported("DATETIME");
   }

   @Override
   public String getTargetTypeName() {
      return Duration.class.getName();
   }
}

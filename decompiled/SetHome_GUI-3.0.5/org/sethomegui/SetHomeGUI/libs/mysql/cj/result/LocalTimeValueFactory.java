package org.sethomegui.SetHomeGUI.libs.mysql.cj.result;

import java.time.LocalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.WarningListener;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.DataReadException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalDate;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTimestamp;

public class LocalTimeValueFactory extends AbstractDateTimeValueFactory<LocalTime> {
   private WarningListener warningListener;

   public LocalTimeValueFactory(PropertySet pset) {
      super(pset);
   }

   public LocalTimeValueFactory(PropertySet pset, WarningListener warningListener) {
      this(pset);
      this.warningListener = warningListener;
   }

   LocalTime localCreateFromDate(InternalDate idate) {
      return LocalTime.of(0, 0);
   }

   public LocalTime localCreateFromTime(InternalTime it) {
      if (it.getHours() >= 0 && it.getHours() < 24) {
         return LocalTime.of(it.getHours(), it.getMinutes(), it.getSeconds(), it.getNanos());
      } else {
         throw new DataReadException(Messages.getString("ResultSet.InvalidTimeValue", new Object[]{it.toString()}));
      }
   }

   public LocalTime localCreateFromTimestamp(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{this.getTargetTypeName()}));
      }

      return this.createFromTime(new InternalTime(its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos(), its.getScale()));
   }

   public LocalTime localCreateFromDatetime(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{this.getTargetTypeName()}));
      }

      return this.createFromTime(new InternalTime(its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos(), its.getScale()));
   }

   @Override
   public String getTargetTypeName() {
      return LocalTime.class.getName();
   }
}

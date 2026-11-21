package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import java.time.LocalDate;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.WarningListener;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.DataReadException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalDate;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTime;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTimestamp;

public class LocalDateValueFactory extends AbstractDateTimeValueFactory<LocalDate> {
   private WarningListener warningListener;

   public LocalDateValueFactory(PropertySet pset) {
      super(pset);
   }

   public LocalDateValueFactory(PropertySet pset, WarningListener warningListener) {
      this(pset);
      this.warningListener = warningListener;
   }

   public LocalDate localCreateFromDate(InternalDate idate) {
      if (idate.getYear() == 0 && idate.getMonth() == 0 && idate.getDay() == 0) {
         throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
      } else {
         return LocalDate.of(idate.getYear(), idate.getMonth(), idate.getDay());
      }
   }

   public LocalDate localCreateFromTimestamp(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{this.getTargetTypeName()}));
      }

      return this.createFromDate(its);
   }

   public LocalDate localCreateFromDatetime(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{this.getTargetTypeName()}));
      }

      return this.createFromDate(its);
   }

   LocalDate localCreateFromTime(InternalTime it) {
      return LocalDate.of(1970, 1, 1);
   }

   @Override
   public String getTargetTypeName() {
      return LocalDate.class.getName();
   }
}

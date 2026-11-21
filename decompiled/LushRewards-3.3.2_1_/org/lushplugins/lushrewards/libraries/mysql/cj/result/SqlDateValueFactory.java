package org.lushplugins.lushrewards.libraries.mysql.cj.result;

import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.WarningListener;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.DataReadException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.WrongArgumentException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalDate;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTime;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.InternalTimestamp;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.TimeUtil;

public class SqlDateValueFactory extends AbstractDateTimeValueFactory<Date> {
   private WarningListener warningListener;
   private Calendar cal;

   public SqlDateValueFactory(PropertySet pset, Calendar calendar, TimeZone tz) {
      super(pset);
      if (calendar != null) {
         this.cal = (Calendar)calendar.clone();
      } else {
         this.cal = Calendar.getInstance(tz, Locale.US);
         this.cal.set(14, 0);
         this.cal.setLenient(false);
      }
   }

   public SqlDateValueFactory(PropertySet pset, Calendar calendar, TimeZone tz, WarningListener warningListener) {
      this(pset, calendar, tz);
      this.warningListener = warningListener;
   }

   public Date localCreateFromDate(InternalDate idate) {
      synchronized (this.cal) {
         Date var10000;
         try {
            if (idate.isZero()) {
               throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
            }

            this.cal.clear();
            this.cal.set(idate.getYear(), idate.getMonth() - 1, idate.getDay());
            long ms = this.cal.getTimeInMillis();
            var10000 = new Date(ms);
         } catch (IllegalArgumentException var6) {
            throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var6.getMessage(), var6);
         }

         return var10000;
      }
   }

   public Date localCreateFromTime(InternalTime it) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.ImplicitDatePartWarning", new Object[]{"java.sql.Date"}));
      }

      return Date.valueOf(TimeUtil.DEFAULT_DATE);
   }

   public Date localCreateFromTimestamp(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{"java.sql.Date"}));
      }

      return this.createFromDate(its);
   }

   public Date localCreateFromDatetime(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{"java.sql.Date"}));
      }

      return this.createFromDate(its);
   }

   @Override
   public String getTargetTypeName() {
      return Date.class.getName();
   }
}

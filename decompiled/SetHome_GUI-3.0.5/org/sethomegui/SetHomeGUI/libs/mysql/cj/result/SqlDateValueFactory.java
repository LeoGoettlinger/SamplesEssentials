package org.sethomegui.SetHomeGUI.libs.mysql.cj.result;

import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.WarningListener;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.DataReadException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalDate;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTimestamp;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.TimeUtil;

public class SqlDateValueFactory extends AbstractDateTimeValueFactory<Date> {
   private WarningListener warningListener;
   private final Calendar cal;
   private final Lock calLock = new ReentrantLock();

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
      this.calLock.lock();

      Date var4;
      try {
         if (idate.isZero()) {
            throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
         }

         this.cal.clear();
         this.cal.set(idate.getYear(), idate.getMonth() - 1, idate.getDay());
         long ms = this.cal.getTimeInMillis();
         var4 = new Date(ms);
      } catch (IllegalArgumentException var8) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var8.getMessage(), var8);
      } finally {
         this.calLock.unlock();
      }

      return var4;
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

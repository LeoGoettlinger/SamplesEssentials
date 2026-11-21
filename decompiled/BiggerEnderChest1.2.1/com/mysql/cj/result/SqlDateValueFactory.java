package com.mysql.cj.result;

import com.mysql.cj.Messages;
import com.mysql.cj.WarningListener;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.DataReadException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.InternalDate;
import com.mysql.cj.protocol.InternalTime;
import com.mysql.cj.protocol.InternalTimestamp;
import com.mysql.cj.util.TimeUtil;
import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

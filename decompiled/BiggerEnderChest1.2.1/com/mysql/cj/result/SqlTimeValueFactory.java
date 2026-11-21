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
import java.sql.Time;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SqlTimeValueFactory extends AbstractDateTimeValueFactory<Time> {
   private WarningListener warningListener;
   private final Calendar cal;
   private final Lock calLock = new ReentrantLock();

   public SqlTimeValueFactory(PropertySet pset, Calendar calendar, TimeZone tz) {
      super(pset);
      if (calendar != null) {
         this.cal = (Calendar)calendar.clone();
      } else {
         this.cal = Calendar.getInstance(tz, Locale.US);
         this.cal.setLenient(false);
      }
   }

   public SqlTimeValueFactory(PropertySet pset, Calendar calendar, TimeZone tz, WarningListener warningListener) {
      this(pset, calendar, tz);
      this.warningListener = warningListener;
   }

   Time localCreateFromDate(InternalDate idate) {
      this.calLock.lock();

      Time e;
      try {
         this.cal.clear();
         e = new Time(this.cal.getTimeInMillis());
      } catch (IllegalArgumentException var6) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var6.getMessage(), var6);
      } finally {
         this.calLock.unlock();
      }

      return e;
   }

   public Time localCreateFromTime(InternalTime it) {
      if (it.getHours() >= 0 && it.getHours() < 24) {
         this.calLock.lock();

         Time var4;
         try {
            this.cal.set(1970, 0, 1, it.getHours(), it.getMinutes(), it.getSeconds());
            this.cal.set(14, 0);
            long ms = it.getNanos() / 1000000 + this.cal.getTimeInMillis();
            var4 = new Time(ms);
         } catch (IllegalArgumentException var8) {
            throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var8.getMessage(), var8);
         } finally {
            this.calLock.unlock();
         }

         return var4;
      } else {
         throw new DataReadException(Messages.getString("ResultSet.InvalidTimeValue", new Object[]{it.toString()}));
      }
   }

   public Time localCreateFromDatetime(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{"java.sql.Time"}));
      }

      return this.createFromTime(new InternalTime(its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos(), its.getScale()));
   }

   public Time localCreateFromTimestamp(InternalTimestamp its) {
      if (this.warningListener != null) {
         this.warningListener.warningEncountered(Messages.getString("ResultSet.PrecisionLostWarning", new Object[]{"java.sql.Time"}));
      }

      return this.createFromTime(new InternalTime(its.getHours(), its.getMinutes(), its.getSeconds(), its.getNanos(), its.getScale()));
   }

   @Override
   public String getTargetTypeName() {
      return Time.class.getName();
   }
}

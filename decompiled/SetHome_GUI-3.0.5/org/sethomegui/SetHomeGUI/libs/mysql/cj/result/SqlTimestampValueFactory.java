package org.sethomegui.SetHomeGUI.libs.mysql.cj.result;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.DataReadException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalDate;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTime;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.InternalTimestamp;

public class SqlTimestampValueFactory extends AbstractDateTimeValueFactory<Timestamp> {
   private final Calendar cal;
   private final TimeZone defaultTimeZone;
   private final Lock defaultTimeZoneLock = new ReentrantLock();
   private final TimeZone connectionTimeZone;

   public SqlTimestampValueFactory(PropertySet pset, Calendar calendar, TimeZone defaultTimeZone, TimeZone connectionTimeZone) {
      super(pset);
      this.defaultTimeZone = defaultTimeZone;
      this.connectionTimeZone = connectionTimeZone;
      this.cal = calendar != null ? (Calendar)calendar.clone() : null;
   }

   public Timestamp localCreateFromDate(InternalDate idate) {
      if (idate.getYear() == 0 && idate.getMonth() == 0 && idate.getDay() == 0) {
         throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
      } else {
         this.defaultTimeZoneLock.lock();

         Timestamp e;
         try {
            Calendar c;
            if (this.cal != null) {
               c = this.cal;
            } else {
               c = Calendar.getInstance(this.defaultTimeZone, Locale.US);
               c.setLenient(false);
            }

            try {
               c.clear();
               c.set(idate.getYear(), idate.getMonth() - 1, idate.getDay(), 0, 0, 0);
               e = new Timestamp(c.getTimeInMillis());
            } catch (IllegalArgumentException var7) {
               throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var7.getMessage(), var7);
            }
         } finally {
            this.defaultTimeZoneLock.unlock();
         }

         return e;
      }
   }

   public Timestamp localCreateFromTime(InternalTime it) {
      if (it.getHours() >= 0 && it.getHours() < 24) {
         this.defaultTimeZoneLock.lock();

         Timestamp var4;
         try {
            Calendar c;
            if (this.cal != null) {
               c = this.cal;
            } else {
               c = Calendar.getInstance(this.defaultTimeZone, Locale.US);
               c.setLenient(false);
            }

            try {
               c.set(1970, 0, 1, it.getHours(), it.getMinutes(), it.getSeconds());
               Timestamp ts = new Timestamp(c.getTimeInMillis());
               ts.setNanos(it.getNanos());
               var4 = ts;
            } catch (IllegalArgumentException var8) {
               throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var8.getMessage(), var8);
            }
         } finally {
            this.defaultTimeZoneLock.unlock();
         }

         return var4;
      } else {
         throw new DataReadException(Messages.getString("ResultSet.InvalidTimeValue", new Object[]{it.toString()}));
      }
   }

   public Timestamp localCreateFromTimestamp(InternalTimestamp its) {
      if (its.getYear() == 0 && its.getMonth() == 0 && its.getDay() == 0) {
         throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
      } else {
         this.defaultTimeZoneLock.lock();

         Timestamp var4;
         try {
            Calendar c;
            if (this.cal != null) {
               c = this.cal;
            } else {
               c = Calendar.getInstance(
                  this.pset.getBooleanProperty(PropertyKey.preserveInstants).getValue() ? this.connectionTimeZone : this.defaultTimeZone, Locale.US
               );
               c.setLenient(false);
            }

            try {
               c.set(its.getYear(), its.getMonth() - 1, its.getDay(), its.getHours(), its.getMinutes(), its.getSeconds());
               Timestamp ts = new Timestamp(c.getTimeInMillis());
               ts.setNanos(its.getNanos());
               var4 = ts;
            } catch (IllegalArgumentException var8) {
               throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var8.getMessage(), var8);
            }
         } finally {
            this.defaultTimeZoneLock.unlock();
         }

         return var4;
      }
   }

   public Timestamp localCreateFromDatetime(InternalTimestamp its) {
      if (its.getYear() == 0 && its.getMonth() == 0 && its.getDay() == 0) {
         throw new DataReadException(Messages.getString("ResultSet.InvalidZeroDate"));
      } else {
         this.defaultTimeZoneLock.lock();

         Timestamp var4;
         try {
            Calendar c;
            if (this.cal != null) {
               c = this.cal;
            } else {
               c = Calendar.getInstance(
                  this.pset.getBooleanProperty(PropertyKey.preserveInstants).getValue() ? this.connectionTimeZone : this.defaultTimeZone, Locale.US
               );
               c.setLenient(false);
            }

            try {
               c.set(its.getYear(), its.getMonth() - 1, its.getDay(), its.getHours(), its.getMinutes(), its.getSeconds());
               Timestamp ts = new Timestamp(c.getTimeInMillis());
               ts.setNanos(its.getNanos());
               var4 = ts;
            } catch (IllegalArgumentException var8) {
               throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, var8.getMessage(), var8);
            }
         } finally {
            this.defaultTimeZoneLock.unlock();
         }

         return var4;
      }
   }

   @Override
   public String getTargetTypeName() {
      return Timestamp.class.getName();
   }
}

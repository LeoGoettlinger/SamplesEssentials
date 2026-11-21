package org.postgresql.util;

import java.sql.Timestamp;
import java.util.Calendar;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PGTimestamp extends Timestamp {
   private static final long serialVersionUID = -6245623465210738466L;
   @Nullable
   private Calendar calendar;

   public PGTimestamp(long time) {
      this(time, null);
   }

   public PGTimestamp(long time, @Nullable Calendar calendar) {
      super(time);
      this.calendar = calendar;
   }

   public void setCalendar(@Nullable Calendar calendar) {
      this.calendar = calendar;
   }

   @Nullable
   public Calendar getCalendar() {
      return this.calendar;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = super.hashCode();
      return 31 * result + (this.calendar == null ? 0 : this.calendar.hashCode());
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         PGTimestamp that = (PGTimestamp)o;
         return this.calendar != null ? this.calendar.equals(that.calendar) : that.calendar == null;
      }
   }

   @Override
   public Object clone() {
      PGTimestamp clone = (PGTimestamp)super.clone();
      Calendar calendar = this.getCalendar();
      if (calendar != null) {
         clone.setCalendar((Calendar)calendar.clone());
      }

      return clone;
   }
}

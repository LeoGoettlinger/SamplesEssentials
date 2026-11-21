package ch.njol.skript.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import org.jetbrains.annotations.Nullable;

public class Timeperiod implements YggdrasilSerializable {
   public final int start;
   public final int end;

   public Timeperiod() {
      this.start = this.end = 0;
   }

   public Timeperiod(int start, int end) {
      this.start = (start + 24000) % 24000;
      this.end = (end + 24000) % 24000;
   }

   public Timeperiod(int time) {
      this.start = this.end = (time + 24000) % 24000;
   }

   public boolean contains(int time) {
      return this.start <= this.end ? time >= this.start && time <= this.end : time <= this.end || time >= this.start;
   }

   public boolean contains(Time t) {
      return this.contains(t.getTicks());
   }

   @Override
   public String toString() {
      return Time.toString(this.start) + (this.start == this.end ? "" : "-" + Time.toString(this.end));
   }

   @Override
   public int hashCode() {
      return this.start + this.end << 16;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj == this) {
         return true;
      } else {
         return !(obj instanceof Timeperiod other) ? false : this.end == other.end && this.start == other.start;
      }
   }
}

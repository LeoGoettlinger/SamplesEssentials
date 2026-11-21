package com.artillexstudios.axtrade.libs.lamp.exception;

import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class CooldownException extends RuntimeException {
   private final long timeLeft;

   public CooldownException(long timeLeft) {
      this.timeLeft = timeLeft;
   }

   public CooldownException(TimeUnit unit, long timeLeft) {
      this.timeLeft = unit.toMillis(timeLeft);
   }

   public long getTimeLeftMillis() {
      return this.timeLeft;
   }

   public long getTimeLeft(@NotNull TimeUnit unit) {
      Preconditions.notNull(unit, "unit");
      return unit.convert(this.timeLeft, TimeUnit.MILLISECONDS);
   }
}

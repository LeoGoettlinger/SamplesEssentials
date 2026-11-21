package com.oheers.fish.utils;

import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jspecify.annotations.NonNull;

public record DurationFormatter(@NonNull TimeUnit timeUnit) {
   public Component format(long value) {
      long seconds = this.timeUnit.toSeconds(value);
      Duration duration = Duration.ofSeconds(seconds);
      long days = duration.toDaysPart();
      long hours = duration.toHoursPart();
      long minutes = duration.toMinutesPart();
      long remainingSeconds = duration.toSecondsPart();
      List<Component> list = new ArrayList<>();
      appendUnit(list, days, DurationFormatter.Unit.DAY);
      appendUnit(list, hours, DurationFormatter.Unit.HOUR);
      appendUnit(list, minutes, DurationFormatter.Unit.MINUTE);
      appendUnit(list, remainingSeconds, DurationFormatter.Unit.SECOND);
      return Component.join(JoinConfiguration.separator(Component.space()), list);
   }

   private static void appendUnit(@NonNull List<Component> list, long value, @NonNull DurationFormatter.Unit timeUnit) {
      if (value > 0L) {
         list.add(timeUnit.getFormat(value).getComponentMessage());
      }
   }

   private static enum Unit {
      DAY(ConfigMessage.DURATION_DAY::getMessage, "{day}"),
      HOUR(ConfigMessage.DURATION_HOUR::getMessage, "{hour}"),
      MINUTE(ConfigMessage.DURATION_MINUTE::getMessage, "{minute}"),
      SECOND(ConfigMessage.DURATION_SECOND::getMessage, "{second}");

      private final Supplier<EMFMessage> formatSupplier;
      private final String variable;

      private Unit(@NonNull Supplier<EMFMessage> formatSupplier, @NonNull String variable) {
         this.formatSupplier = formatSupplier;
         this.variable = variable;
      }

      public EMFMessage getFormat(long value) {
         EMFMessage message = this.formatSupplier.get();
         message.setVariable(this.variable, value);
         return message;
      }
   }
}

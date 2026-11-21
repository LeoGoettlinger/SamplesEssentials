package com.github.retrooper.packetevents.protocol.score;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ScoreFormat {
   static BlankScoreFormat blankScore() {
      return BlankScoreFormat.INSTANCE;
   }

   static StyledScoreFormat styledScore(Style style) {
      return new StyledScoreFormat(style);
   }

   static FixedScoreFormat fixedScore(Component value) {
      return new FixedScoreFormat(value);
   }

   static ScoreFormat readTyped(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(ScoreFormatTypes.getRegistry()).read(wrapper);
   }

   static <T extends ScoreFormat> void writeTyped(PacketWrapper<?> wrapper, T format) {
      wrapper.writeMappedEntity(format.getType());
      ((ScoreFormatType<T>)format.getType()).write(wrapper, format);
   }

   Component format(int score);

   ScoreFormatType<?> getType();
}

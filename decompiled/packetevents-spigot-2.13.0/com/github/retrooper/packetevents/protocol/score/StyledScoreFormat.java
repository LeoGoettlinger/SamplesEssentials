package com.github.retrooper.packetevents.protocol.score;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;

public final class StyledScoreFormat implements ScoreFormat {
   private final Style style;

   public StyledScoreFormat(Style style) {
      this.style = style;
   }

   public static StyledScoreFormat read(PacketWrapper<?> wrapper) {
      return new StyledScoreFormat(wrapper.readStyle());
   }

   public static void write(PacketWrapper<?> wrapper, StyledScoreFormat format) {
      wrapper.writeStyle(format.style);
   }

   @Override
   public Component format(int score) {
      return Component.text(Integer.toString(score), this.style);
   }

   @Override
   public ScoreFormatType<StyledScoreFormat> getType() {
      return ScoreFormatTypes.STYLED;
   }

   public Style getStyle() {
      return this.style;
   }
}

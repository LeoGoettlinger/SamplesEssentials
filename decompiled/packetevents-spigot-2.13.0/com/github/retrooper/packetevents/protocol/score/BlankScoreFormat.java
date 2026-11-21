package com.github.retrooper.packetevents.protocol.score;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public final class BlankScoreFormat implements ScoreFormat {
   public static final BlankScoreFormat INSTANCE = new BlankScoreFormat();

   private BlankScoreFormat() {
   }

   public static BlankScoreFormat read(PacketWrapper<?> wrapper) {
      return INSTANCE;
   }

   public static void write(PacketWrapper<?> wrapper, BlankScoreFormat format) {
   }

   @Override
   public Component format(int score) {
      return Component.empty();
   }

   @Override
   public ScoreFormatType<BlankScoreFormat> getType() {
      return ScoreFormatTypes.BLANK;
   }
}

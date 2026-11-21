package com.github.retrooper.packetevents.protocol.score;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public final class FixedScoreFormat implements ScoreFormat {
   private final Component value;

   public FixedScoreFormat(Component value) {
      this.value = value;
   }

   public static FixedScoreFormat read(PacketWrapper<?> wrapper) {
      return new FixedScoreFormat(wrapper.readComponent());
   }

   public static void write(PacketWrapper<?> wrapper, FixedScoreFormat format) {
      wrapper.writeComponent(format.value);
   }

   @Override
   public Component format(int score) {
      return this.value;
   }

   @Override
   public ScoreFormatType<FixedScoreFormat> getType() {
      return ScoreFormatTypes.FIXED;
   }

   public Component getValue() {
      return this.value;
   }
}

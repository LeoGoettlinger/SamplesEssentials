package com.earth2me.essentials.libs.configurate.yaml;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.DumperOptions;

public enum NodeStyle {
   BLOCK(DumperOptions.FlowStyle.BLOCK),
   FLOW(DumperOptions.FlowStyle.FLOW);

   private final DumperOptions.FlowStyle snake;

   private NodeStyle(final DumperOptions.FlowStyle snake) {
      this.snake = snake;
   }

   static DumperOptions.FlowStyle asSnakeYaml(@Nullable final NodeStyle style) {
      return style == null ? DumperOptions.FlowStyle.AUTO : style.snake;
   }
}

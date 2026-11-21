package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.configurate.ConfigurationNode;

public enum MoveStrategy {
   MERGE {
      @Override
      public <T extends ConfigurationNode> void move(final T source, final T target) {
         target.mergeFrom(source);
      }
   },
   OVERWRITE {
      @Override
      public <T extends ConfigurationNode> void move(final T source, final T target) {
         target.from(source);
      }
   };

   private MoveStrategy() {
   }

   public abstract <T extends ConfigurationNode> void move(T source, T target);
}

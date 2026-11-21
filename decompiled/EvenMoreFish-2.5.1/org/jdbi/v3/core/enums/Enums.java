package org.jdbi.v3.core.enums;

import org.jdbi.v3.core.config.JdbiConfig;

public class Enums implements JdbiConfig<Enums> {
   private EnumStrategy strategy;

   public Enums() {
      this.strategy = EnumStrategy.BY_NAME;
   }

   private Enums(Enums other) {
      this.strategy = other.strategy;
   }

   public EnumStrategy getDefaultStrategy() {
      return this.strategy;
   }

   public Enums setEnumStrategy(EnumStrategy enumStrategy) {
      this.strategy = enumStrategy;
      return this;
   }

   public Enums createCopy() {
      return new Enums(this);
   }
}

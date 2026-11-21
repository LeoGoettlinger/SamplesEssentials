package com.earth2me.essentials.config.entities;

import com.earth2me.essentials.config.processors.DeleteIfIncompleteProcessor;
import java.util.regex.Pattern;

public class CommandCooldown implements DeleteIfIncompleteProcessor.IncompleteEntity {
   private Pattern pattern;
   private Long value;

   public Pattern pattern() {
      return this.pattern;
   }

   public void pattern(final Pattern value) {
      this.pattern = value;
   }

   public Long value() {
      return this.value;
   }

   public void value(final Long value) {
      this.value = value;
   }

   @Override
   public boolean isIncomplete() {
      return this.value == null || this.pattern == null;
   }
}

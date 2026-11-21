package org.enginehub.piston.config;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import javax.annotation.Nullable;

public class TextConfig extends Config<String> {
   private static final TextConfig COMMAND_PREFIX = new TextConfig("piston.text.command.prefix");

   public static TextConfig commandPrefix() {
      return COMMAND_PREFIX;
   }

   public static Component commandPrefixValue() {
      return commandPrefix().value();
   }

   private TextConfig(String key) {
      super(key, "");
   }

   @Override
   protected Config<String> copyForDefault() {
      return new TextConfig(this.getKey());
   }

   public TextConfig value(@Nullable String value) {
      super.value(value);
      return this;
   }

   protected void checkValue(@Nullable String value) {
      Preconditions.checkNotNull(value);
   }

   @Override
   protected Component apply(TranslatableComponent placeholder) {
      Preconditions.checkState(placeholder.args().isEmpty(), "TextConfig takes no arguments");
      return TextComponent.builder(this.getValue()).mergeStyle(placeholder).append(placeholder.children()).build();
   }
}

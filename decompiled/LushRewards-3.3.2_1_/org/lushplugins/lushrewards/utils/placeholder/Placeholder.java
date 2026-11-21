package org.lushplugins.lushrewards.utils.placeholder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Placeholder {
   protected final String content;
   private Collection<Placeholder> children;

   public Placeholder(String content) {
      this.content = content;
   }

   abstract boolean matches(String var1);

   abstract String parse(String[] var1, Player var2);

   public String getContent() {
      return this.content;
   }

   @NotNull
   public Collection<Placeholder> getChildren() {
      return (Collection<Placeholder>)(this.children != null ? this.children : Collections.emptyList());
   }

   public Placeholder addChild(Placeholder placeholder) {
      if (this.children == null) {
         this.children = new ArrayList<>();
      }

      this.children.add(placeholder);
      return this;
   }
}

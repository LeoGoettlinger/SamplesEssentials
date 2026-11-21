package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.events;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.Mark;

public abstract class NodeEvent extends Event {
   private final String anchor;

   public NodeEvent(String anchor, Mark startMark, Mark endMark) {
      super(startMark, endMark);
      this.anchor = anchor;
   }

   public String getAnchor() {
      return this.anchor;
   }

   @Override
   protected String getArguments() {
      return "anchor=" + this.anchor;
   }
}

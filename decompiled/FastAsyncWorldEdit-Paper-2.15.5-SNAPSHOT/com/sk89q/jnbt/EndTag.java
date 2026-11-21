package com.sk89q.jnbt;

import org.enginehub.linbus.tree.LinEndTag;

@Deprecated
public final class EndTag extends Tag<Object, LinEndTag> {
   public EndTag() {
      super(LinEndTag.instance());
   }

   @Override
   public int getTypeCode() {
      return 0;
   }
}

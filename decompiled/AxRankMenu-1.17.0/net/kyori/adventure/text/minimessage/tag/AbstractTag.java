package net.kyori.adventure.text.minimessage.tag;

import net.kyori.adventure.internal.Internals;
import net.kyori.examination.Examinable;

abstract class AbstractTag implements Tag, Examinable {
   @Override
   public final String toString() {
      return Internals.toString(this);
   }
}

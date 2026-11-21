package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag;

import xyz.lychee.lagfixer.libs.kyori.adventure.internal.Internals;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;

abstract class AbstractTag implements Tag, Examinable {
   @Override
   public final String toString() {
      return Internals.toString(this);
   }
}

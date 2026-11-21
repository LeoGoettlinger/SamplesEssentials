package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag;

import com.earth2me.essentials.libs.kyori.adventure.internal.Internals;
import com.earth2me.essentials.libs.kyori.examination.Examinable;

abstract class AbstractTag implements Tag, Examinable {
   @Override
   public final String toString() {
      return Internals.toString(this);
   }
}

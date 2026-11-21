package com.earth2me.essentials.textreader;

import java.util.ArrayList;
import java.util.List;

public class SimpleTranslatableText implements ITranslatableText {
   private final List<ITranslatableText.TranslatableText> lines = new ArrayList<>();

   public void addLine(final String tlKey, final Object... args) {
      this.lines.add(new ITranslatableText.TranslatableText(tlKey, args));
   }

   @Override
   public List<ITranslatableText.TranslatableText> getLines() {
      return this.lines;
   }
}

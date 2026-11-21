package com.viaversion.viaversion.libs.mcstructs.text.stringformat.matcher;

import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.StringFormat;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.TextStringReader;
import javax.annotation.Nullable;

public class BungeeRgbStringFormat extends StringFormat {
   public BungeeRgbStringFormat(char colorChar) {
      super(colorChar);
   }

   @Override
   public boolean matches(TextStringReader reader) {
      return reader.canRead(14) && reader.read() == this.colorChar && reader.peek() == 'x';
   }

   @Nullable
   @Override
   public TextFormatting read(TextStringReader reader) {
      reader.skip(2);
      String hex = reader.skip().read(1)
         + reader.skip().read(1)
         + reader.skip().read(1)
         + reader.skip().read(1)
         + reader.skip().read(1)
         + reader.skip().read(1);

      try {
         return new TextFormatting(Integer.parseInt(hex, 16));
      } catch (Throwable var4) {
         return null;
      }
   }

   @Override
   public boolean canWrite(TextFormatting formatting) {
      return formatting.isRGBColor();
   }

   @Override
   public void write(StringBuilder builder, TextFormatting formatting) {
      builder.append(this.colorChar).append("x");
      String hex = String.format("%06X", formatting.getRgbValue());

      for (int i = 0; i < hex.length(); i++) {
         builder.append(this.colorChar).append(hex.charAt(i));
      }
   }
}

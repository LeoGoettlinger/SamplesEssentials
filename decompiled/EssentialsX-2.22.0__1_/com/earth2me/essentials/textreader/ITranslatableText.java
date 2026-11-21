package com.earth2me.essentials.textreader;

import java.util.List;

public interface ITranslatableText extends IResolvable {
   List<ITranslatableText.TranslatableText> getLines();

   @Override
   default int getLineCount() {
      return this.getLines().size();
   }

   public static final class TranslatableText {
      private final String key;
      private final Object[] args;

      public TranslatableText(String key, Object[] args) {
         this.key = key;
         this.args = args;
      }

      public String getKey() {
         return this.key;
      }

      public Object[] getArgs() {
         return this.args;
      }
   }
}

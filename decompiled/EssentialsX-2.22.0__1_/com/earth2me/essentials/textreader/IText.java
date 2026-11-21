package com.earth2me.essentials.textreader;

import java.util.List;
import java.util.Map;

public interface IText extends IResolvable {
   List<String> getLines();

   List<String> getChapters();

   Map<String, Integer> getBookmarks();

   @Override
   default int getLineCount() {
      return this.getLines().size();
   }
}

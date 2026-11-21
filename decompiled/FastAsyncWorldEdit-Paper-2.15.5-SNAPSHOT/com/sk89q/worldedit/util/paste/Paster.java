package com.sk89q.worldedit.util.paste;

import java.net.URL;
import java.util.concurrent.Callable;

public interface Paster {
   default Callable<URL> paste(String content) {
      return this.paste(content, new PasteMetadata());
   }

   Callable<URL> paste(String content, PasteMetadata metadata);
}

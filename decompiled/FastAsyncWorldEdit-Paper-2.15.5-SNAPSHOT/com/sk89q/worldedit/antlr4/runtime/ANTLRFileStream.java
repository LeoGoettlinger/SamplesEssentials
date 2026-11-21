package com.sk89q.worldedit.antlr4.runtime;

import com.sk89q.worldedit.antlr4.runtime.misc.Utils;
import java.io.IOException;

@Deprecated
public class ANTLRFileStream extends ANTLRInputStream {
   protected String fileName;

   public ANTLRFileStream(String fileName) throws IOException {
      this(fileName, null);
   }

   public ANTLRFileStream(String fileName, String encoding) throws IOException {
      this.fileName = fileName;
      this.load(fileName, encoding);
   }

   public void load(String fileName, String encoding) throws IOException {
      this.data = Utils.readFile(fileName, encoding);
      this.n = this.data.length;
   }

   @Override
   public String getSourceName() {
      return this.fileName;
   }
}

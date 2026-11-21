package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public abstract class YamlOutputStreamWriter extends OutputStreamWriter implements StreamDataWriter {
   public YamlOutputStreamWriter(OutputStream out, Charset cs) {
      super(out, cs);
   }

   public abstract void processIOException(IOException var1);

   @Override
   public void flush() {
      try {
         super.flush();
      } catch (IOException var2) {
         this.processIOException(var2);
      }
   }

   @Override
   public void write(String str, int off, int len) {
      try {
         super.write(str, off, len);
      } catch (IOException var5) {
         this.processIOException(var5);
      }
   }

   @Override
   public void write(String str) {
      try {
         super.write(str);
      } catch (IOException var3) {
         this.processIOException(var3);
      }
   }
}

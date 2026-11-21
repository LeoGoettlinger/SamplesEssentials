package com.fastasyncworldedit.core.history.change;

import com.fastasyncworldedit.core.internal.io.FaweInputStream;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

public interface StreamChange {
   void flushChanges(FaweOutputStream out) throws IOException;

   void undoChanges(FaweInputStream in) throws IOException;

   void redoChanges(FaweInputStream in) throws IOException;

   default void flushChanges(File file) throws IOException {
      try (
         BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
         LZ4BlockOutputStream compressed = new LZ4BlockOutputStream(out);
         FaweOutputStream fos = new FaweOutputStream(compressed);
      ) {
         this.flushChanges(fos);
      }
   }

   default void undoChanges(File file) throws IOException {
      try (
         BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
         LZ4BlockInputStream compressed = new LZ4BlockInputStream(in);
         FaweInputStream fis = new FaweInputStream(compressed);
      ) {
         this.undoChanges(fis);
      }
   }

   default void redoChanges(File file) throws IOException {
      try (
         BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
         LZ4BlockInputStream compressed = new LZ4BlockInputStream(in);
         FaweInputStream fis = new FaweInputStream(compressed);
      ) {
         this.redoChanges(fis);
      }
   }
}

package com.sk89q.worldedit.util.io.file;

import com.sk89q.worldedit.util.formatting.text.Component;

public class InvalidFilenameException extends FilenameException {
   public InvalidFilenameException(String filename) {
      super(filename);
   }

   public InvalidFilenameException(String filename, Component msg) {
      super(filename, msg);
   }

   @Deprecated
   public InvalidFilenameException(String filename, String msg) {
      super(filename, msg);
   }
}

package com.sk89q.worldedit.util.io.file;

import com.sk89q.worldedit.util.formatting.text.Component;

public class FileSelectionAbortedException extends FilenameException {
   public FileSelectionAbortedException() {
      super("");
   }

   @Deprecated
   public FileSelectionAbortedException(String msg) {
      super("", msg);
   }

   public FileSelectionAbortedException(Component msg) {
      super("", msg);
   }
}

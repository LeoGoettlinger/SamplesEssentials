package com.fastasyncworldedit.core.internal.exception;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import java.io.File;

public class FaweClipboardVersionMismatchException extends FaweException {
   private final int expected;
   private final int version;

   @Deprecated(
      forRemoval = true,
      since = "2.3.0"
   )
   public FaweClipboardVersionMismatchException() {
      this(2, -1);
   }

   public FaweClipboardVersionMismatchException(int expected, int version) {
      super(
         Caption.of(
            "fawe.error.clipboard.on.disk.version.mismatch",
            expected,
            version,
            Fawe.<IFawe>platform().getDirectory().getName() + File.separator + Settings.settings().PATHS.CLIPBOARD
         ),
         FaweException.Type.CLIPBOARD
      );
      this.expected = expected;
      this.version = version;
   }

   public int getClipboardVersion() {
      return this.version;
   }

   public int getExpectedVersion() {
      return this.expected;
   }
}

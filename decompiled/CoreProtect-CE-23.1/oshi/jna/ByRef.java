package oshi.jna;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTRByReference;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32.ByReference;
import com.sun.jna.platform.win32.WinDef.LONGLONGByReference;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import oshi.util.Util;

public interface ByRef {
   public static class CloseableHANDLEByReference extends HANDLEByReference implements AutoCloseable {
      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseableIntByReference extends IntByReference implements AutoCloseable {
      public CloseableIntByReference() {
      }

      public CloseableIntByReference(int value) {
         super(value);
      }

      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseableLONGLONGByReference extends LONGLONGByReference implements AutoCloseable {
      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseableLongByReference extends LongByReference implements AutoCloseable {
      public CloseableLongByReference() {
      }

      public CloseableLongByReference(long value) {
         super(value);
      }

      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseableNativeLongByReference extends NativeLongByReference implements AutoCloseable {
      public CloseableNativeLongByReference() {
      }

      public CloseableNativeLongByReference(NativeLong nativeLong) {
         super(nativeLong);
      }

      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseablePROCESSENTRY32ByReference extends ByReference implements AutoCloseable {
      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseablePointerByReference extends PointerByReference implements AutoCloseable {
      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseableSizeTByReference extends com.sun.jna.platform.unix.LibCAPI.size_t.ByReference implements AutoCloseable {
      public CloseableSizeTByReference() {
      }

      public CloseableSizeTByReference(long value) {
         super(value);
      }

      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }

   public static class CloseableULONGptrByReference extends ULONG_PTRByReference implements AutoCloseable {
      @Override
      public void close() {
         Util.freeMemory(this.getPointer());
      }
   }
}

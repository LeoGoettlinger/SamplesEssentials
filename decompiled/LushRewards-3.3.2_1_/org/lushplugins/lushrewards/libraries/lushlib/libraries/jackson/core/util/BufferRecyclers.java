package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util;

import java.lang.ref.SoftReference;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.JsonStringEncoder;

public class BufferRecyclers {
   public static final String SYSTEM_PROPERTY_TRACK_REUSABLE_BUFFERS = "org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util.BufferRecyclers.trackReusableBuffers";
   private static final ThreadLocalBufferManager _bufferRecyclerTracker;
   protected static final ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef;

   public static BufferRecycler getBufferRecycler() {
      SoftReference<BufferRecycler> ref = _recyclerRef.get();
      BufferRecycler br = ref == null ? null : ref.get();
      if (br == null) {
         br = new BufferRecycler();
         if (_bufferRecyclerTracker != null) {
            ref = _bufferRecyclerTracker.wrapAndTrack(br);
         } else {
            ref = new SoftReference<>(br);
         }

         _recyclerRef.set(ref);
      }

      return br;
   }

   public static int releaseBuffers() {
      return _bufferRecyclerTracker != null ? _bufferRecyclerTracker.releaseBuffers() : -1;
   }

   @Deprecated
   public static JsonStringEncoder getJsonStringEncoder() {
      return JsonStringEncoder.getInstance();
   }

   @Deprecated
   public static byte[] encodeAsUTF8(String text) {
      return JsonStringEncoder.getInstance().encodeAsUTF8(text);
   }

   @Deprecated
   public static char[] quoteAsJsonText(String rawText) {
      return JsonStringEncoder.getInstance().quoteAsString(rawText);
   }

   @Deprecated
   public static void quoteAsJsonText(CharSequence input, StringBuilder output) {
      JsonStringEncoder.getInstance().quoteAsString(input, output);
   }

   @Deprecated
   public static byte[] quoteAsJsonUTF8(String rawText) {
      return JsonStringEncoder.getInstance().quoteAsUTF8(rawText);
   }

   static {
      boolean trackReusableBuffers = false;

      try {
         trackReusableBuffers = "true"
            .equals(System.getProperty("org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util.BufferRecyclers.trackReusableBuffers"));
      } catch (SecurityException var2) {
      }

      _bufferRecyclerTracker = trackReusableBuffers ? ThreadLocalBufferManager.instance() : null;
      _recyclerRef = new ThreadLocal<>();
   }
}

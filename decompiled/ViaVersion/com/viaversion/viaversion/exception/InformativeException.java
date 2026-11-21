package com.viaversion.viaversion.exception;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class InformativeException extends RuntimeException {
   private final List<InformativeException.DataEntry> dataEntries = new ArrayList<>();
   private boolean shouldBePrinted = true;
   private int sources;

   public InformativeException(Throwable cause) {
      super(cause);
   }

   public InformativeException set(String key, @Nullable Object value) {
      this.dataEntries.add(new InformativeException.DataEntry(key, value));
      return this;
   }

   public InformativeException addSource(Class<?> sourceClazz) {
      return this.set("Source " + this.sources++, this.getSource(sourceClazz));
   }

   private String getSource(Class<?> sourceClazz) {
      return sourceClazz.isAnonymousClass() ? sourceClazz.getName() + " (Anonymous)" : sourceClazz.getName();
   }

   public boolean shouldBePrinted() {
      return this.shouldBePrinted;
   }

   public void setShouldBePrinted(boolean shouldBePrinted) {
      this.shouldBePrinted = shouldBePrinted;
   }

   @Override
   public String getMessage() {
      StringBuilder builder = new StringBuilder(
         "Please report the full error (!) from the console on the Via support Discord or open an issue on the relevant GitHub repository.\n"
      );
      boolean first = true;

      for (InformativeException.DataEntry entry : this.dataEntries) {
         if (!first) {
            builder.append(", ");
         } else {
            first = false;
         }

         builder.append(entry.name()).append(": ");
         String s = String.valueOf(entry.value());
         if (!Via.getManager().isDebug() && s.length() > 10 && builder.length() + s.length() > Via.getConfig().maxErrorLength()) {
            int remaining = Math.max(0, Via.getConfig().maxErrorLength() - builder.length());
            s = s.substring(0, Math.min(remaining, s.length())) + "...";
         }

         builder.append(StringUtil.forLogging(s));
      }

      return builder.toString();
   }

   @Override
   public Throwable fillInStackTrace() {
      return this;
   }

   private record DataEntry(String name, @Nullable Object value) {
   }
}

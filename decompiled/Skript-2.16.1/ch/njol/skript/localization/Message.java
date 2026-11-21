package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class Message {
   private static final Collection<Message> messages = new ArrayList<>(50);
   private static boolean firstChange = true;
   public final String key;
   @Nullable
   private String value;
   boolean revalidate = true;

   public Message(String key) {
      this.key = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH));
      messages.add(this);
      if (Skript.testing() && Language.isInitialized() && !Language.keyExists(this.key)) {
         Language.missingEntryError(this.key);
      }
   }

   @Override
   public String toString() {
      this.validate();
      return this.value == null ? this.key : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.value);
   }

   @Nullable
   public final String getValue() {
      this.validate();
      return this.value;
   }

   public final String getValueOrDefault(String defaultValue) {
      this.validate();
      return this.value == null ? defaultValue : this.value;
   }

   public final boolean isSet() {
      this.validate();
      return this.value != null;
   }

   protected synchronized void validate() {
      if (this.revalidate) {
         this.revalidate = false;
         this.value = Language.get_(this.key);
         this.onValueChange();
      }
   }

   protected void onValueChange() {
   }

   static {
      Language.addListener(() -> {
         for (Message m : messages) {
            synchronized (m) {
               m.revalidate = true;
            }

            if (firstChange && Skript.testing() && !Language.keyExists(m.key)) {
               Language.missingEntryError(m.key);
            }
         }

         firstChange = false;
      });
   }
}

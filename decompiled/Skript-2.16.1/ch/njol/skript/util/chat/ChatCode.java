package ch.njol.skript.util.chat;

import org.jetbrains.annotations.Nullable;

@Deprecated(
   since = "2.15",
   forRemoval = true
)
public interface ChatCode {
   void updateComponent(MessageComponent var1, String var2);

   boolean hasParam();

   @Nullable
   String getColorCode();

   @Nullable
   String getLangName();

   default boolean isLocalized() {
      return false;
   }

   char getColorChar();
}

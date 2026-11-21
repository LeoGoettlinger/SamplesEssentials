package org.skriptlang.skript.localization;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.util.ViewProvider;

public interface Localizer extends ViewProvider<Localizer> {
   @Contract("_ -> new")
   static Localizer of(SkriptAddon addon) {
      return new LocalizerImpl(addon);
   }

   void setSourceDirectories(String var1, @Nullable String var2);

   @Nullable
   String languageFileDirectory();

   @Nullable
   String dataFileDirectory();

   @Nullable
   String translate(String var1);

   @Contract("-> new")
   default Localizer unmodifiableView() {
      return new LocalizerImpl.UnmodifiableLocalizer(this);
   }
}

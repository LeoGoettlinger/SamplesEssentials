package org.skriptlang.skript.lang.entry.util;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;

public class LiteralEntryData<T> extends KeyValueEntryData<T> {
   private final Class<T> type;

   public LiteralEntryData(String key, @Nullable T defaultValue, boolean optional, Class<T> type) {
      super(key, defaultValue, optional);
      this.type = type;
   }

   @Nullable
   @Override
   public T getValue(String value) {
      return Classes.parse(value, this.type, ParseContext.DEFAULT);
   }
}

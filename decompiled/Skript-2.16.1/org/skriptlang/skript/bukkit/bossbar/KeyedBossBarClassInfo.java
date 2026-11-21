package org.skriptlang.skript.bukkit.bossbar;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.KeyedBossBar;
import org.jetbrains.annotations.Nullable;

public class KeyedBossBarClassInfo extends ClassInfo<KeyedBossBar> {
   public KeyedBossBarClassInfo() {
      super(KeyedBossBar.class, "keyedbossbar");
      this.user(new String[]{"keyed boss ?bars?"})
         .name(ClassInfo.NO_DOC)
         .since("2.16")
         .parser(new KeyedBossBarClassInfo.KeyedBossBarParser())
         .changer(new KeyedBossBarClassInfo.KeyedBossBarChangeHandler())
         .supplier(Bukkit::getBossBars)
         .serializer(new KeyedBossBarClassInfo.KeyedBossBarSerializer())
         .defaultExpression(new EventValueExpression<>(KeyedBossBar.class));
   }

   private static class KeyedBossBarChangeHandler implements Changer<KeyedBossBar> {
      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.DELETE ? CollectionUtils.array() : null;
      }

      public void change(KeyedBossBar[] bars, @Nullable Object[] delta, Changer.ChangeMode mode) {
         for (KeyedBossBar bar : bars) {
            bar.removeAll();
            Bukkit.removeBossBar(bar.getKey());
         }
      }
   }

   private static class KeyedBossBarParser extends Parser<KeyedBossBar> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(KeyedBossBar bar, int flags) {
         boolean emptyTitle = bar.getTitle().isEmpty();
         return emptyTitle ? "boss bar with id '" + bar.getKey() + "'" : "boss bar with id '" + bar.getKey() + "' titled '" + bar.getTitle() + "'";
      }

      public String toVariableNameString(KeyedBossBar bar) {
         return this.toString(bar, 0);
      }
   }

   private static class KeyedBossBarSerializer extends Serializer<KeyedBossBar> {
      public Fields serialize(KeyedBossBar bar) {
         Fields fields = new Fields();
         fields.putObject("key", bar.getKey().toString());
         return fields;
      }

      public void deserialize(KeyedBossBar bar, Fields fields) {
         assert false;
      }

      protected KeyedBossBar deserialize(Fields fields) throws StreamCorruptedException {
         String stringKey = fields.getObject("key", String.class);
         NamespacedKey key = null;
         if (stringKey != null) {
            key = NamespacedKey.fromString(stringKey);
         }

         if (key == null) {
            throw new StreamCorruptedException();
         } else {
            KeyedBossBar bar = Bukkit.getBossBar(key);
            if (bar == null) {
               throw new StreamCorruptedException();
            } else {
               return bar;
            }
         }
      }

      @Override
      public boolean mustSyncDeserialization() {
         return true;
      }

      @Override
      public boolean canBeInstantiated() {
         return false;
      }
   }
}

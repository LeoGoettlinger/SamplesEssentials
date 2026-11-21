package de.theredend2000.advancedhunt.lib.kyori.adventure.sound;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Keyed;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Index;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.Range;

public sealed interface Sound permits SoundImpl {
   static Sound.Builder sound() {
      return new SoundImpl.BuilderImpl();
   }

   static Sound.Builder sound(final Sound existing) {
      return new SoundImpl.BuilderImpl(existing);
   }

   static Sound sound(final Consumer<Sound.Builder> configurer) {
      return AbstractBuilder.configureAndBuild(sound(), configurer);
   }

   static Sound sound(final Key name, final Sound.Source source, final float volume, final float pitch) {
      return sound().type(name).source(source).volume(volume).pitch(pitch).build();
   }

   static Sound sound(final Sound.Type type, final Sound.Source source, final float volume, final float pitch) {
      Objects.requireNonNull(type, "type");
      return sound(type.key(), source, volume, pitch);
   }

   static Sound sound(final Supplier<? extends Sound.Type> type, final Sound.Source source, final float volume, final float pitch) {
      return sound().type(type).source(source).volume(volume).pitch(pitch).build();
   }

   static Sound sound(final Key name, final Sound.Source.Provider source, final float volume, final float pitch) {
      return sound(name, source.soundSource(), volume, pitch);
   }

   static Sound sound(final Sound.Type type, final Sound.Source.Provider source, final float volume, final float pitch) {
      return sound(type, source.soundSource(), volume, pitch);
   }

   static Sound sound(final Supplier<? extends Sound.Type> type, final Sound.Source.Provider source, final float volume, final float pitch) {
      return sound(type, source.soundSource(), volume, pitch);
   }

   Key name();

   Sound.Source source();

   float volume();

   float pitch();

   OptionalLong seed();

   SoundStop asStop();

   public sealed interface Builder extends AbstractBuilder<Sound> permits SoundImpl.BuilderImpl {
      Sound.Builder type(final Key type);

      Sound.Builder type(final Sound.Type type);

      Sound.Builder type(final Supplier<? extends Sound.Type> typeSupplier);

      Sound.Builder source(final Sound.Source source);

      Sound.Builder source(final Sound.Source.Provider source);

      Sound.Builder volume(@Range(from = 0L,to = 2147483647L) final float volume);

      Sound.Builder pitch(@Range(from = -1L,to = 1L) final float pitch);

      Sound.Builder seed(final long seed);

      Sound.Builder seed(final OptionalLong seed);
   }

   public interface Emitter {
      static Sound.Emitter self() {
         return SoundImpl.EMITTER_SELF;
      }
   }

   public static enum Source {
      MASTER("master"),
      MUSIC("music"),
      RECORD("record"),
      WEATHER("weather"),
      BLOCK("block"),
      HOSTILE("hostile"),
      NEUTRAL("neutral"),
      PLAYER("player"),
      AMBIENT("ambient"),
      VOICE("voice"),
      UI("ui");

      public static final Index<String, Sound.Source> NAMES = Index.create(Sound.Source.class, source -> source.name);
      private final String name;

      private Source(final String name) {
         this.name = name;
      }

      public interface Provider {
         Sound.Source soundSource();
      }
   }

   public interface Type extends Keyed {
      @Override
      Key key();
   }
}

package com.github.retrooper.packetevents.protocol.item.jukebox;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import java.util.Collection;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class JukeboxSongs {
   private static final VersionedRegistry<IJukeboxSong> REGISTRY = new VersionedRegistry<>("jukebox_song");
   public static final IJukeboxSong THIRTEEN = define("13", Sounds.MUSIC_DISC_13, 178.0F, 1);
   public static final IJukeboxSong CAT = define("cat", Sounds.MUSIC_DISC_CAT, 185.0F, 2);
   public static final IJukeboxSong BLOCKS = define("blocks", Sounds.MUSIC_DISC_BLOCKS, 345.0F, 3);
   public static final IJukeboxSong CHIRP = define("chirp", Sounds.MUSIC_DISC_CHIRP, 185.0F, 4);
   public static final IJukeboxSong FAR = define("far", Sounds.MUSIC_DISC_FAR, 174.0F, 5);
   public static final IJukeboxSong MALL = define("mall", Sounds.MUSIC_DISC_MALL, 197.0F, 6);
   public static final IJukeboxSong MELLOHI = define("mellohi", Sounds.MUSIC_DISC_MELLOHI, 96.0F, 7);
   public static final IJukeboxSong STAL = define("stal", Sounds.MUSIC_DISC_STAL, 150.0F, 8);
   public static final IJukeboxSong STRAD = define("strad", Sounds.MUSIC_DISC_STRAD, 188.0F, 9);
   public static final IJukeboxSong WARD = define("ward", Sounds.MUSIC_DISC_WARD, 251.0F, 10);
   public static final IJukeboxSong ELEVEN = define("11", Sounds.MUSIC_DISC_11, 71.0F, 11);
   public static final IJukeboxSong WAIT = define("wait", Sounds.MUSIC_DISC_WAIT, 238.0F, 12);
   public static final IJukeboxSong PIGSTEP = define("pigstep", Sounds.MUSIC_DISC_PIGSTEP, 149.0F, 13);
   public static final IJukeboxSong OTHERSIDE = define("otherside", Sounds.MUSIC_DISC_OTHERSIDE, 195.0F, 14);
   public static final IJukeboxSong FIVE = define("5", Sounds.MUSIC_DISC_5, 178.0F, 15);
   public static final IJukeboxSong RELIC = define("relic", Sounds.MUSIC_DISC_RELIC, 218.0F, 14);
   public static final IJukeboxSong PRECIPICE = define("precipice", Sounds.MUSIC_DISC_PRECIPICE, 299.0F, 13);
   public static final IJukeboxSong CREATOR = define("creator", Sounds.MUSIC_DISC_CREATOR, 176.0F, 12);
   public static final IJukeboxSong CREATOR_MUSIC_BOX = define("creator_music_box", Sounds.MUSIC_DISC_CREATOR_MUSIC_BOX, 73.0F, 11);
   public static final IJukeboxSong TEARS = define("tears", Sounds.MUSIC_DISC_TEARS, 175.0F, 10);
   public static final IJukeboxSong LAVA_CHICKEN = define("lava_chicken", Sounds.MUSIC_DISC_LAVA_CHICKEN, 134.0F, 9);
   public static final IJukeboxSong BOUNCE = define("bounce", Sounds.MUSIC_DISC_BOUNCE, 234.0F, 8);

   private JukeboxSongs() {
   }

   private static String makeDescriptionId(String var0, @Nullable ResourceLocation var1) {
      return var1 == null ? var0 + ".unregistered_sadface" : var0 + "." + var1.getNamespace() + "." + var1.getKey().replace('/', '.');
   }

   @Internal
   public static IJukeboxSong define(String key, Sound sound, float lengthInSeconds, int comparatorOutput) {
      return REGISTRY.define(
         key,
         data -> new JukeboxSong(data, sound, Component.translatable(makeDescriptionId("jukebox_song", data.getName())), lengthInSeconds, comparatorOutput)
      );
   }

   public static VersionedRegistry<IJukeboxSong> getRegistry() {
      return REGISTRY;
   }

   public static IJukeboxSong getByName(String name) {
      return REGISTRY.getByName(name);
   }

   public static IJukeboxSong getById(ClientVersion version, int id) {
      return REGISTRY.getById(version, id);
   }

   public static Collection<IJukeboxSong> values() {
      return REGISTRY.getEntries();
   }

   static {
      REGISTRY.unloadMappings();
   }
}

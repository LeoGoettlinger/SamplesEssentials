package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.TagUtil;

public class BackwardsRegistryRewriter extends RegistryDataRewriter {
   private final BackwardsProtocol<?, ?, ?, ?> protocol;

   public BackwardsRegistryRewriter(BackwardsProtocol<?, ?, ?, ?> protocol) {
      super(protocol);
      this.protocol = protocol;
   }

   public RegistryEntry[] handle(UserConnection connection, String key, RegistryEntry[] entries) {
      if (Key.stripMinecraftNamespace(key).equals("worldgen/biome")) {
         for (RegistryEntry entry : entries) {
            CompoundTag biome = (CompoundTag)entry.tag();
            if (biome != null) {
               CompoundTag effects = biome.getCompoundTag("effects");
               this.updateBiomeEffects(effects);
               CompoundTag attributes = biome.getCompoundTag("attributes");
               if (attributes != null) {
                  this.updateAttributes(attributes);
               }
            }
         }
      }

      return super.handle(connection, key, entries);
   }

   public void updateJukeboxSongs(UserConnection connection, RegistryEntry[] entries) {
      super.updateJukeboxSongs(connection, entries);

      for (RegistryEntry entry : entries) {
         if (entry.tag() != null) {
            this.updateSound((CompoundTag)entry.tag(), "sound_event");
         }
      }
   }

   private void updateBiomeEffects(CompoundTag effects) {
      this.updateSound(effects.getCompoundTag("mood_sound"), "sound");
      this.updateSound(effects.getCompoundTag("additions_sound"), "sound");
      this.updateSound(effects.getCompoundTag("music"), "sound");
      this.updateSound(effects, "ambient_sound");
   }

   private void updateAttributes(CompoundTag attributes) {
      CompoundTag backgroundMusic = TagUtil.getNamespacedCompoundTag(attributes, "audio/background_music");
      if (backgroundMusic != null) {
         this.updateSound(backgroundMusic.getCompoundTag("default"), "sound");
      }

      CompoundTag ambientSounds = TagUtil.getNamespacedCompoundTag(attributes, "audio/ambient_sounds");
      if (ambientSounds != null) {
         this.updateSound(ambientSounds, "loop");
         this.updateSound(ambientSounds, "mood");
         this.updateSound(ambientSounds, "additions");
      }
   }

   private void updateSound(CompoundTag tag, String name) {
      if (tag != null) {
         String sound = tag.getString(name);
         if (sound != null) {
            String mappedSound = this.protocol.getMappingData().getMappedNamedSound(sound);
            if (mappedSound == null) {
               tag.putString(name, "minecraft:intentionally_empty");
            } else {
               tag.putString(name, mappedSound);
            }
         }
      }
   }
}

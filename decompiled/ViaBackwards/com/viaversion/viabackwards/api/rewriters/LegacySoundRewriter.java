package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap.Entry;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;

@Deprecated
public abstract class LegacySoundRewriter<T extends BackwardsProtocol<?, ?, ?, ?>> extends RewriterBase<T> {
   protected final Int2ObjectMap<LegacySoundRewriter.SoundData> soundRewrites = new Int2ObjectOpenHashMap(64);

   protected LegacySoundRewriter(T protocol) {
      super(protocol);
   }

   public LegacySoundRewriter.SoundData added(int id, int replacement) {
      return this.added(id, replacement, -1.0F);
   }

   public LegacySoundRewriter.SoundData added(int id, int replacement, float newPitch) {
      LegacySoundRewriter.SoundData data = new LegacySoundRewriter.SoundData(replacement, true, newPitch, true);
      this.soundRewrites.put(id, data);
      return data;
   }

   public LegacySoundRewriter.SoundData removed(int id) {
      LegacySoundRewriter.SoundData data = new LegacySoundRewriter.SoundData(-1, false, -1.0F, false);
      this.soundRewrites.put(id, data);
      return data;
   }

   public int handleSounds(int soundId) {
      int newSoundId = soundId;
      LegacySoundRewriter.SoundData data = (LegacySoundRewriter.SoundData)this.soundRewrites.get(soundId);
      if (data != null) {
         return data.replacementSound();
      } else {
         ObjectIterator var4 = this.soundRewrites.int2ObjectEntrySet().iterator();

         while (var4.hasNext()) {
            Entry<LegacySoundRewriter.SoundData> entry = (Entry<LegacySoundRewriter.SoundData>)var4.next();
            if (soundId > entry.getIntKey()) {
               if (((LegacySoundRewriter.SoundData)entry.getValue()).added()) {
                  newSoundId--;
               } else {
                  newSoundId++;
               }
            }
         }

         return newSoundId;
      }
   }

   public boolean hasPitch(int soundId) {
      LegacySoundRewriter.SoundData data = (LegacySoundRewriter.SoundData)this.soundRewrites.get(soundId);
      return data != null && data.changePitch();
   }

   public float handlePitch(int soundId) {
      LegacySoundRewriter.SoundData data = (LegacySoundRewriter.SoundData)this.soundRewrites.get(soundId);
      return data != null ? data.newPitch() : 1.0F;
   }

   public record SoundData(int replacementSound, boolean changePitch, float newPitch, boolean added) {
   }
}

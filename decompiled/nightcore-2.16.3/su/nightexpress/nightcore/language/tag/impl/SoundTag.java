package su.nightexpress.nightcore.language.tag.impl;

import org.bukkit.Sound;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.language.message.MessageOptions;
import su.nightexpress.nightcore.language.tag.MessageTag;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.sound.AbstractSound;
import su.nightexpress.nightcore.util.sound.VanillaSound;

@Deprecated
public class SoundTag extends MessageTag {
   public SoundTag() {
      super("sound");
   }

   @Deprecated
   @NonNull
   public String enclose(@NonNull Sound sound) {
      return this.wrap(sound);
   }

   @Deprecated
   @NonNull
   public String enclose(@NonNull NightSound sound) {
      return this.wrap(sound);
   }

   @Deprecated
   @NonNull
   public String wrap(@NonNull NightSound sound) {
      return this.wrap(sound.serialize());
   }

   @NonNull
   public String wrap(@NonNull Sound sound) {
      return this.wrap(VanillaSound.of(sound));
   }

   @NonNull
   public String wrap(@NonNull su.nightexpress.nightcore.bridge.wrap.NightSound sound) {
      return this.wrap(sound.serialize());
   }

   @Override
   public void apply(@NonNull MessageOptions options, @Nullable String tagContent) {
      if (tagContent != null) {
         options.setSound(AbstractSound.deserialize(tagContent));
      }
   }
}

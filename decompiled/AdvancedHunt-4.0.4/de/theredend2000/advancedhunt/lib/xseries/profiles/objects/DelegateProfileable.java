package de.theredend2000.advancedhunt.lib.xseries.profiles.objects;

import de.theredend2000.advancedhunt.lib.xseries.profiles.exceptions.ProfileException;
import de.theredend2000.advancedhunt.lib.xseries.profiles.gameprofile.MojangGameProfile;
import de.theredend2000.advancedhunt.lib.xseries.profiles.objects.transformer.ProfileTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface DelegateProfileable extends Profileable {
   @Internal
   @NotNull
   Profileable getDelegateProfile();

   @Nullable
   @Unmodifiable
   @Override
   default MojangGameProfile getProfile() {
      return this.getDelegateProfile().getProfile();
   }

   @Nullable
   @Override
   default ProfileException test() {
      return this.getDelegateProfile().test();
   }

   @Nullable
   @Override
   default MojangGameProfile getDisposableProfile() {
      return this.getDelegateProfile().getDisposableProfile();
   }

   @Override
   default boolean isReady() {
      return this.getDelegateProfile().isReady();
   }

   @NotNull
   @Override
   default Profileable transform(@NotNull ProfileTransformer... transformers) {
      return this.getDelegateProfile().transform(transformers);
   }

   @Nullable
   @Override
   default String getProfileValue() {
      return this.getDelegateProfile().getProfileValue();
   }
}

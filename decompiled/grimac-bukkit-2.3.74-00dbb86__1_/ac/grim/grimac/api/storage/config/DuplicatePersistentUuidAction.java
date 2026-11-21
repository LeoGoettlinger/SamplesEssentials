package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum DuplicatePersistentUuidAction {
   DISABLE_STORAGE,
   FAIL_STARTUP,
   ALLOW_UNSAFE;
}

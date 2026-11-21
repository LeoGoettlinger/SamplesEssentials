package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface Operation<R> {
   @NotNull
   Category<?> category();
}

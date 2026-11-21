package ac.grim.grimac.api.storage.check;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface CheckCatalogPersistence {
   Iterable<CheckCatalogRow> loadAll();

   int insert(String var1, @Nullable String var2, @Nullable String var3, @Nullable String var4, long var5);

   void upsert(CheckCatalogRow var1);

   void updateDisplayAndDescription(int var1, @Nullable String var2, @Nullable String var3);
}

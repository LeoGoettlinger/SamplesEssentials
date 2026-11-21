package ac.grim.grimac.internal.storage.checks;

import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@ApiStatus.Internal
public final class CheckRegistry {
   private final CheckCatalogPersistence persistence;
   private final Map<String, CheckCatalogRow> byStableKey = new ConcurrentHashMap<>();
   private final Map<Integer, CheckCatalogRow> byId = new ConcurrentHashMap<>();
   private final Map<String, CheckCatalogRow> byDisplay = new ConcurrentHashMap<>();
   private final Function<CheckCatalogRow, String> collisionPrefixResolver;
   public static final Function<CheckCatalogRow, String> DEFAULT_COLLISION_PREFIX = row -> {
      String v = row.introducedVersion();
      if (v != null && !v.isBlank()) {
         String trimmed = v.trim();
         int dot = trimmed.indexOf(46);
         String majorPart = dot < 0 ? trimmed : trimmed.substring(0, dot);
         int i = 0;

         while (i < majorPart.length() && !Character.isDigit(majorPart.charAt(i))) {
            i++;
         }

         int j = i;

         while (j < majorPart.length() && Character.isDigit(majorPart.charAt(j))) {
            j++;
         }

         return i == j ? "legacy/" : "V" + majorPart.substring(i, j).toUpperCase(Locale.ROOT) + "/";
      } else {
         return "legacy/";
      }
   };

   public CheckRegistry(CheckCatalogPersistence persistence) {
      this(persistence, DEFAULT_COLLISION_PREFIX);
   }

   public CheckRegistry(CheckCatalogPersistence persistence, Function<CheckCatalogRow, String> collisionPrefixResolver) {
      this.persistence = persistence;
      this.collisionPrefixResolver = collisionPrefixResolver;
   }

   public void reload() {
      this.byStableKey.clear();
      this.byId.clear();
      this.byDisplay.clear();

      for (CheckCatalogRow row : this.persistence.loadAll()) {
         this.byStableKey.put(row.stableKey(), row);
         this.byId.put(row.checkId(), row);
         if (row.display() != null) {
            this.byDisplay.put(row.display(), row);
         }
      }
   }

   public synchronized int intern(String stableKey, @Nullable String display, @Nullable String description, @Nullable String introducedVersion) {
      CheckCatalogRow existing = this.byStableKey.get(stableKey);
      if (existing == null) {
         if (display != null) {
            CheckCatalogRow collider = this.byDisplay.get(display);
            if (collider != null) {
               String prefix = this.collisionPrefixResolver.apply(collider);
               String prefixed = prefix + collider.display();
               this.persistence.updateDisplayAndDescription(collider.checkId(), prefixed, collider.description());
               CheckCatalogRow updated = new CheckCatalogRow(
                  collider.checkId(), collider.stableKey(), prefixed, collider.description(), collider.introducedVersion(), collider.introducedAt()
               );
               this.byStableKey.put(collider.stableKey(), updated);
               this.byId.put(collider.checkId(), updated);
               this.byDisplay.remove(display, collider);
               this.byDisplay.put(prefixed, updated);
            }
         }

         long now = System.currentTimeMillis();
         int id = this.persistence.insert(stableKey, display, description, introducedVersion, now);
         CheckCatalogRow row = new CheckCatalogRow(id, stableKey, display, description, introducedVersion, now);
         this.byStableKey.put(stableKey, row);
         this.byId.put(id, row);
         if (display != null) {
            this.byDisplay.put(display, row);
         }

         return id;
      } else {
         boolean displayChanged = display != null && !display.equals(existing.display());
         boolean descriptionChanged = description != null && !description.equals(existing.description());
         if (displayChanged || descriptionChanged) {
            String newDisplay = displayChanged ? display : existing.display();
            String newDescription = descriptionChanged ? description : existing.description();
            this.persistence.updateDisplayAndDescription(existing.checkId(), newDisplay, newDescription);
            CheckCatalogRow renamed = new CheckCatalogRow(
               existing.checkId(), existing.stableKey(), newDisplay, newDescription, existing.introducedVersion(), existing.introducedAt()
            );
            this.byStableKey.put(stableKey, renamed);
            this.byId.put(existing.checkId(), renamed);
            if (existing.display() != null) {
               this.byDisplay.remove(existing.display(), existing);
            }

            if (newDisplay != null) {
               this.byDisplay.put(newDisplay, renamed);
            }
         }

         return existing.checkId();
      }
   }

   public int intern(String stableKey, @Nullable String display) {
      return this.intern(stableKey, display, null, null);
   }

   public Optional<Integer> getId(String stableKey) {
      CheckCatalogRow r = this.byStableKey.get(stableKey);
      return r == null ? Optional.empty() : Optional.of(r.checkId());
   }

   public Optional<String> displayFor(int checkId) {
      CheckCatalogRow r = this.byId.get(checkId);
      return r == null ? Optional.empty() : Optional.ofNullable(r.display());
   }

   public Optional<String> descriptionFor(int checkId) {
      CheckCatalogRow r = this.byId.get(checkId);
      return r == null ? Optional.empty() : Optional.ofNullable(r.description());
   }

   public Optional<String> stableKeyFor(int checkId) {
      CheckCatalogRow r = this.byId.get(checkId);
      return r == null ? Optional.empty() : Optional.of(r.stableKey());
   }

   public int size() {
      return this.byStableKey.size();
   }

   public static Function<CheckCatalogRow, String> staticPrefix(String prefix) {
      return row -> prefix;
   }
}

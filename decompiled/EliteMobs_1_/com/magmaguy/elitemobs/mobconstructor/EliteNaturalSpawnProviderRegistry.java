package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnClaim;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnContext;
import com.magmaguy.elitemobs.api.mind.EliteNaturalSpawnProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

final class EliteNaturalSpawnProviderRegistry {
   private final List<EliteNaturalSpawnProviderRegistry.Registration> registrations = new ArrayList<>();

   boolean isEmpty() {
      return this.registrations.isEmpty();
   }

   void register(Plugin owner, EliteNaturalSpawnProvider provider) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(provider, "provider");
      if (!owner.isEnabled()) {
         throw new IllegalStateException("Natural-spawn provider owner is not enabled");
      } else {
         EliteNaturalSpawnProviderRegistry.Registration existing = this.registrations
            .stream()
            .filter(registration -> registration.owner() == owner)
            .findFirst()
            .orElse(null);
         if (existing == null) {
            this.registrations.add(new EliteNaturalSpawnProviderRegistry.Registration(owner, provider));
         } else if (existing.provider() != provider) {
            throw new IllegalArgumentException("Plugin " + owner.getName() + " already registered a natural-spawn provider");
         }
      }
   }

   Optional<EliteNaturalSpawnProviderRegistry.Selection> select(EliteNaturalSpawnContext context) {
      Objects.requireNonNull(context, "context");
      List<EliteNaturalSpawnProviderRegistry.Selection> claims = new ArrayList<>();

      for (EliteNaturalSpawnProviderRegistry.Registration registration : List.copyOf(this.registrations)) {
         if (registration.owner().isEnabled()) {
            registration.provider()
               .select(context)
               .ifPresent(claim -> claims.add(new EliteNaturalSpawnProviderRegistry.Selection(registration.owner(), Objects.requireNonNull(claim, "claim"))));
         }
      }

      if (claims.size() > 1) {
         String owners = claims.stream().map(selection -> selection.owner().getName()).sorted().reduce((left, right) -> left + ", " + right).orElseThrow();
         throw new IllegalStateException("Conflicting natural-spawn claims from " + owners);
      } else {
         return claims.stream().findFirst();
      }
   }

   void unregister(Plugin owner) {
      Objects.requireNonNull(owner, "owner");
      this.registrations.removeIf(registration -> registration.owner() == owner);
   }

   void clear() {
      this.registrations.clear();
   }

   private record Registration(Plugin owner, EliteNaturalSpawnProvider provider) {
   }

   record Selection(Plugin owner, EliteNaturalSpawnClaim claim) {
      Selection(Plugin owner, EliteNaturalSpawnClaim claim) {
         Objects.requireNonNull(owner, "owner");
         Objects.requireNonNull(claim, "claim");
         this.owner = owner;
         this.claim = claim;
      }
   }
}

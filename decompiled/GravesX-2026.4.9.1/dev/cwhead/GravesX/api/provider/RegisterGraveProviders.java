package dev.cwhead.GravesX.api.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RegisterGraveProviders {
   private static final Comparator<GraveProvider> ORDERING = Comparator.comparingInt(GraveProvider::order)
      .thenComparing(GraveProvider::id, String.CASE_INSENSITIVE_ORDER);
   private static final ConcurrentMap<String, GraveProvider> BY_ID = new ConcurrentHashMap<>();

   private RegisterGraveProviders() {
   }

   private static String norm(String id) {
      if (id == null) {
         return null;
      } else {
         String s = id.trim();
         return s.isEmpty() ? null : s.toLowerCase(Locale.ROOT);
      }
   }

   public static void bootstrapFromServices() {
      rebuildIndexFromServices();
   }

   public static void index(GraveProvider provider) {
      if (provider != null) {
         String id = norm(provider.id());
         if (id != null) {
            BY_ID.put(id, provider);
         }
      }
   }

   public static void unindex(GraveProvider provider) {
      if (provider != null) {
         String id = norm(provider.id());
         if (id != null) {
            BY_ID.remove(id, provider);
         }
      }
   }

   public static void clear() {
      BY_ID.clear();
   }

   public static GraveProvider getById(String providerId) {
      String id = norm(providerId);
      return id == null ? null : BY_ID.get(id);
   }

   public static List<GraveProvider> getHighestPriorityAll() {
      List<GraveProvider> all = getAll();
      if (all.isEmpty()) {
         return List.of();
      } else {
         int best = all.get(0).order();
         List<GraveProvider> out = new ArrayList<>();

         for (GraveProvider p : all) {
            if (p.order() != best) {
               break;
            }

            out.add(p);
         }

         return List.copyOf(out);
      }
   }

   public static List<GraveProvider> getAll() {
      Collection<RegisteredServiceProvider<GraveProvider>> regs = Bukkit.getServicesManager().getRegistrations(GraveProvider.class);
      List<GraveProvider> out = new ArrayList<>(regs.size());

      for (RegisteredServiceProvider<GraveProvider> rsp : regs) {
         GraveProvider p = (GraveProvider)rsp.getProvider();
         out.add(p);
      }

      out.sort(ORDERING);
      rebuildIndex(out);
      return List.copyOf(out);
   }

   public static Optional<GraveProvider> getHighestPriorityOne() {
      List<GraveProvider> all = getAll();
      return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
   }

   private static void rebuildIndexFromServices() {
      Collection<RegisteredServiceProvider<GraveProvider>> regs = Bukkit.getServicesManager().getRegistrations(GraveProvider.class);
      List<GraveProvider> providers = new ArrayList<>(regs.size());

      for (RegisteredServiceProvider<GraveProvider> rsp : regs) {
         GraveProvider p = (GraveProvider)rsp.getProvider();
         providers.add(p);
      }

      rebuildIndex(providers);
   }

   private static void rebuildIndex(List<GraveProvider> providers) {
      BY_ID.clear();

      for (GraveProvider p : providers) {
         String id = norm(p.id());
         if (id != null) {
            BY_ID.put(id, p);
         }
      }
   }
}

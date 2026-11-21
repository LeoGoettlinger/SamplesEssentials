package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.util.Utils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Registry;

@Internal
public final class FunctionRegistry implements Registry<Function<?>> {
   private static FunctionRegistry registry;
   static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile("[\\p{IsAlphabetic}_][\\p{IsAlphabetic}\\d_]*");
   private final FunctionRegistry.NamespaceIdentifier GLOBAL_NAMESPACE = new FunctionRegistry.NamespaceIdentifier(null);
   private final Map<FunctionRegistry.NamespaceIdentifier, FunctionRegistry.Namespace> namespaces = new ConcurrentHashMap<>();

   public static FunctionRegistry getRegistry() {
      if (registry == null) {
         registry = new FunctionRegistry();
      }

      return registry;
   }

   @NotNull
   @Unmodifiable
   @Override
   public Collection<Function<?>> elements() {
      Set<Function<?>> functions = new HashSet<>();

      for (FunctionRegistry.Namespace namespace : this.namespaces.values()) {
         functions.addAll(namespace.functions.values());
      }

      return Collections.unmodifiableSet(functions);
   }

   public void register(@Nullable String namespace, @NotNull Signature<?> signature) {
      Preconditions.checkNotNull(signature, "signature cannot be null");
      if (signature.isLocal() && namespace == null) {
         throw new IllegalArgumentException("Cannot register a local signature in the global namespace");
      } else if (!signature.isLocal() && namespace != null) {
         throw new IllegalArgumentException("Cannot register a global signature in a local namespace");
      } else {
         Skript.debug("Registering signature '%s'", signature.getName());
         FunctionRegistry.NamespaceIdentifier namespaceId;
         if (namespace != null) {
            namespaceId = new FunctionRegistry.NamespaceIdentifier(namespace);
         } else {
            namespaceId = this.GLOBAL_NAMESPACE;
         }

         FunctionRegistry.Namespace ns = this.namespaces.computeIfAbsent(namespaceId, n -> new FunctionRegistry.Namespace());
         FunctionRegistry.FunctionIdentifier identifier = FunctionRegistry.FunctionIdentifier.of(signature);
         synchronized (ns) {
            Set<FunctionRegistry.FunctionIdentifier> identifiersWithName = ns.identifiers.computeIfAbsent(identifier.name, s -> new HashSet<>());
            boolean exists = identifiersWithName.add(identifier);
            if (!exists) {
               alreadyRegisteredError(signature.getName(), identifier, namespaceId);
            }
         }

         Signature<?> existing = ns.signatures.putIfAbsent(identifier, signature);
         if (existing != null) {
            alreadyRegisteredError(signature.getName(), identifier, namespaceId);
         }
      }
   }

   public void register(@Nullable String namespace, @NotNull Function<?> function) {
      Preconditions.checkNotNull(function, "function cannot be null");
      if (function.getSignature().isLocal() && namespace == null) {
         throw new IllegalArgumentException("Cannot register a local function in the global namespace");
      } else if (!function.getSignature().isLocal() && namespace != null) {
         throw new IllegalArgumentException("Cannot register a global function in a local namespace");
      } else {
         Skript.debug("Registering function '%s'", function.getName());
         String name = function.getName();
         if (!FUNCTION_NAME_PATTERN.matcher(name).matches()) {
            throw new SkriptAPIException("Invalid function name '" + name + "'");
         } else {
            FunctionRegistry.NamespaceIdentifier namespaceId;
            if (namespace != null) {
               namespaceId = new FunctionRegistry.NamespaceIdentifier(namespace);
            } else {
               namespaceId = this.GLOBAL_NAMESPACE;
            }

            FunctionRegistry.FunctionIdentifier identifier = FunctionRegistry.FunctionIdentifier.of(function.getSignature());
            if (!this.signatureExists(namespaceId, identifier)) {
               this.register(namespace, function.getSignature());
            }

            FunctionRegistry.Namespace ns = this.namespaces.computeIfAbsent(namespaceId, n -> new FunctionRegistry.Namespace());
            Function<?> existing = ns.functions.putIfAbsent(identifier, function);
            if (existing != null) {
               alreadyRegisteredError(name, identifier, namespaceId);
            }
         }
      }
   }

   private static void alreadyRegisteredError(String name, FunctionRegistry.FunctionIdentifier identifier, FunctionRegistry.NamespaceIdentifier namespace) {
      throw new SkriptAPIException(
         "Function '%s' with parameters %s is already registered in %s"
            .formatted(name, Arrays.toString(Arrays.stream(identifier.args).map(Class::getSimpleName).toArray()), namespace)
      );
   }

   private boolean signatureExists(@NotNull FunctionRegistry.NamespaceIdentifier namespace, @NotNull FunctionRegistry.FunctionIdentifier identifier) {
      Preconditions.checkNotNull(namespace, "namespace cannot be null");
      Preconditions.checkNotNull(identifier, "identifier cannot be null");
      FunctionRegistry.Namespace ns = this.namespaces.get(namespace);
      if (ns == null) {
         return false;
      } else if (!ns.identifiers.containsKey(identifier.name)) {
         return false;
      } else {
         for (FunctionRegistry.FunctionIdentifier other : ns.identifiers.get(identifier.name)) {
            if (identifier.equals(other)) {
               return true;
            }
         }

         return false;
      }
   }

   @NotNull
   public FunctionRegistry.Retrieval<Function<?>> getFunction(@Nullable String namespace, @NotNull String name, @NotNull Class<?>... args) {
      FunctionRegistry.Retrieval<Function<?>> attempt = null;
      if (namespace != null) {
         attempt = this.getFunction(new FunctionRegistry.NamespaceIdentifier(namespace), FunctionRegistry.FunctionIdentifier.of(name, true, args));
      }

      if (attempt == null || attempt.result() == FunctionRegistry.RetrievalResult.NOT_REGISTERED) {
         attempt = this.getFunction(this.GLOBAL_NAMESPACE, FunctionRegistry.FunctionIdentifier.of(name, false, args));
      }

      return attempt;
   }

   @NotNull
   private FunctionRegistry.Retrieval<Function<?>> getFunction(
      @NotNull FunctionRegistry.NamespaceIdentifier namespace, @NotNull FunctionRegistry.FunctionIdentifier provided
   ) {
      Preconditions.checkNotNull(namespace, "namespace cannot be null");
      Preconditions.checkNotNull(provided, "provided cannot be null");
      FunctionRegistry.Namespace ns = this.namespaces.getOrDefault(namespace, new FunctionRegistry.Namespace());
      Set<FunctionRegistry.FunctionIdentifier> existing = ns.identifiers.get(provided.name);
      if (existing == null) {
         Skript.debug("No functions named '%s' exist in the '%s' namespace", provided.name, namespace.name);
         return new FunctionRegistry.Retrieval<>(FunctionRegistry.RetrievalResult.NOT_REGISTERED, null, null);
      } else {
         Set<FunctionRegistry.FunctionIdentifier> candidates = candidates(provided, existing, false);
         if (candidates.isEmpty()) {
            Skript.debug("Failed to find a function for '%s'", provided.name);
            return new FunctionRegistry.Retrieval<>(FunctionRegistry.RetrievalResult.NOT_REGISTERED, null, null);
         } else if (candidates.size() == 1) {
            if (Skript.debug()) {
               Skript.debug("Matched function for '%s': %s", provided.name, candidates.stream().findAny().orElse(null));
            }

            return new FunctionRegistry.Retrieval<>(FunctionRegistry.RetrievalResult.EXACT, ns.functions.get(candidates.stream().findAny().orElse(null)), null);
         } else {
            if (Skript.debug()) {
               String options = candidates.stream().map(Record::toString).collect(Collectors.joining(", "));
               Skript.debug("Failed to match an exact function for '%s'", provided.name);
               Skript.debug("Identifier: %s", provided);
               Skript.debug("Options: %s", options);
            }

            return new FunctionRegistry.Retrieval<>(
               FunctionRegistry.RetrievalResult.AMBIGUOUS, null, candidates.stream().map(FunctionRegistry.FunctionIdentifier::args).toArray(Class[][]::new)
            );
         }
      }
   }

   public FunctionRegistry.Retrieval<Signature<?>> getSignature(@Nullable String namespace, @NotNull String name, @NotNull Class<?>... args) {
      FunctionRegistry.Retrieval<Signature<?>> attempt = null;
      if (namespace != null) {
         attempt = this.getSignature(new FunctionRegistry.NamespaceIdentifier(namespace), FunctionRegistry.FunctionIdentifier.of(name, true, args), false);
      }

      if (attempt == null || attempt.result() == FunctionRegistry.RetrievalResult.NOT_REGISTERED) {
         attempt = this.getSignature(this.GLOBAL_NAMESPACE, FunctionRegistry.FunctionIdentifier.of(name, false, args), false);
      }

      return attempt;
   }

   FunctionRegistry.Retrieval<Signature<?>> getExactSignature(@Nullable String namespace, @NotNull String name, @NotNull Class<?>... args) {
      FunctionRegistry.Retrieval<Signature<?>> attempt = null;
      if (namespace != null) {
         attempt = this.getSignature(new FunctionRegistry.NamespaceIdentifier(namespace), FunctionRegistry.FunctionIdentifier.of(name, true, args), true);
      }

      if (attempt == null || attempt.result() == FunctionRegistry.RetrievalResult.NOT_REGISTERED) {
         attempt = this.getSignature(this.GLOBAL_NAMESPACE, FunctionRegistry.FunctionIdentifier.of(name, false, args), true);
      }

      return attempt;
   }

   @NotNull
   @Unmodifiable
   public Set<Signature<?>> getSignatures(@Nullable String namespace, @NotNull String name) {
      Preconditions.checkNotNull(name, "name cannot be null");
      Map<FunctionRegistry.FunctionIdentifier, Signature<?>> total = new HashMap<>();
      if (namespace != null) {
         FunctionRegistry.Namespace local = this.namespaces.getOrDefault(new FunctionRegistry.NamespaceIdentifier(namespace), new FunctionRegistry.Namespace());

         for (FunctionRegistry.FunctionIdentifier identifier : local.identifiers.getOrDefault(name, Collections.emptySet())) {
            total.putIfAbsent(identifier, local.signatures.get(identifier));
         }
      }

      FunctionRegistry.Namespace global = this.namespaces.getOrDefault(this.GLOBAL_NAMESPACE, new FunctionRegistry.Namespace());

      for (FunctionRegistry.FunctionIdentifier identifier : global.identifiers.getOrDefault(name, Collections.emptySet())) {
         total.putIfAbsent(identifier, global.signatures.get(identifier));
      }

      return Set.copyOf(total.values());
   }

   private FunctionRegistry.Retrieval<Signature<?>> getSignature(
      @NotNull FunctionRegistry.NamespaceIdentifier namespace, @NotNull FunctionRegistry.FunctionIdentifier provided, boolean exact
   ) {
      Preconditions.checkNotNull(namespace, "namespace cannot be null");
      Preconditions.checkNotNull(provided, "provided cannot be null");
      FunctionRegistry.Namespace ns = this.namespaces.getOrDefault(namespace, new FunctionRegistry.Namespace());
      if (!ns.identifiers.containsKey(provided.name)) {
         Skript.debug("No signatures named '%s' exist in the '%s' namespace", provided.name, namespace.name);
         return new FunctionRegistry.Retrieval<>(FunctionRegistry.RetrievalResult.NOT_REGISTERED, null, null);
      } else {
         Set<FunctionRegistry.FunctionIdentifier> candidates = candidates(provided, ns.identifiers.get(provided.name), exact);
         if (candidates.isEmpty()) {
            Skript.debug("Failed to find a signature for '%s'", provided.name);
            return new FunctionRegistry.Retrieval<>(FunctionRegistry.RetrievalResult.NOT_REGISTERED, null, null);
         } else if (candidates.size() == 1) {
            if (Skript.debug()) {
               Skript.debug("Matched signature for '%s': %s", provided.name, ns.signatures.get(candidates.stream().findAny().orElse(null)));
            }

            return new FunctionRegistry.Retrieval<>(FunctionRegistry.RetrievalResult.EXACT, ns.signatures.get(candidates.stream().findAny().orElse(null)), null);
         } else {
            if (Skript.debug()) {
               String options = candidates.stream().map(Record::toString).collect(Collectors.joining(", "));
               Skript.debug("Failed to match an exact signature for '%s'", provided.name);
               Skript.debug("Identifier: %s", provided);
               Skript.debug("Options: %s", options);
            }

            return new FunctionRegistry.Retrieval<>(
               FunctionRegistry.RetrievalResult.AMBIGUOUS, null, candidates.stream().map(FunctionRegistry.FunctionIdentifier::args).toArray(Class[][]::new)
            );
         }
      }
   }

   @NotNull
   @Unmodifiable
   private static Set<FunctionRegistry.FunctionIdentifier> candidates(
      @NotNull FunctionRegistry.FunctionIdentifier provided, Set<FunctionRegistry.FunctionIdentifier> existing, boolean exact
   ) {
      Set<FunctionRegistry.FunctionIdentifier> candidates = new HashSet<>();

      label86:
      for (FunctionRegistry.FunctionIdentifier candidate : existing) {
         if (Arrays.stream(candidate.args).filter(Class::isArray).count() == 1L && candidate.args.length == 1 && candidate.args[0].isArray()) {
            Class<?> arrayType = candidate.args[0].componentType();

            for (Class<?> arrayArg : provided.args) {
               arrayArg = Utils.getComponentType(arrayArg);
               if (!Converters.converterExists(arrayArg, arrayType)) {
                  continue label86;
               }
            }

            candidates.add(candidate);
         } else if (provided.args.length <= candidate.args.length && provided.args.length >= candidate.minArgCount) {
            for (int i = 0; i < provided.args.length; i++) {
               Class<?> providedType = Utils.getComponentType(provided.args[i]);
               Class<?> candidateType = Utils.getComponentType(candidate.args[i]);
               Class<?> providedArg = provided.args[i];
               if (exact ? providedArg != candidateType : !Converters.converterExists(providedType, candidateType)) {
                  continue label86;
               }
            }

            candidates.add(candidate);
         }
      }

      if (candidates.size() <= 1) {
         return Collections.unmodifiableSet(candidates);
      } else {
         Iterator<FunctionRegistry.FunctionIdentifier> iterator = candidates.iterator();

         while (iterator.hasNext()) {
            FunctionRegistry.FunctionIdentifier candidatex = iterator.next();
            int argIndex = 0;

            while (argIndex < provided.args.length) {
               if (provided.args[argIndex] == Object.class) {
                  argIndex++;
               } else {
                  if (provided.args[argIndex] != candidatex.args[argIndex]) {
                     iterator.remove();
                     break;
                  }

                  argIndex++;
               }
            }
         }

         return Collections.unmodifiableSet(candidates);
      }
   }

   public void remove(@NotNull Signature<?> signature) {
      Preconditions.checkNotNull(signature, "signature cannot be null");
      String name = signature.getName();
      FunctionRegistry.FunctionIdentifier identifier = FunctionRegistry.FunctionIdentifier.of(signature);
      FunctionRegistry.Namespace namespace;
      if (signature.isLocal()) {
         namespace = this.namespaces.get(new FunctionRegistry.NamespaceIdentifier(signature.namespace()));
      } else {
         namespace = this.namespaces.get(this.GLOBAL_NAMESPACE);
      }

      if (namespace != null) {
         for (FunctionRegistry.FunctionIdentifier other : namespace.identifiers.getOrDefault(name, Set.of())) {
            if (identifier.equals(other)) {
               this.removeUpdateMaps(namespace, other, name);
               return;
            }
         }
      }
   }

   private void removeUpdateMaps(FunctionRegistry.Namespace namespace, FunctionRegistry.FunctionIdentifier toRemove, String name) {
      namespace.identifiers.computeIfPresent(name, (k, set) -> {
         if (set.remove(toRemove)) {
            Skript.debug("Removed identifier '%s' from %s", toRemove, namespace);
         }

         return set.isEmpty() ? null : set;
      });
      if (namespace.functions.remove(toRemove) != null) {
         Skript.debug("Removed function '%s' from %s", toRemove, namespace);
      }

      if (namespace.signatures.remove(toRemove) != null) {
         Skript.debug("Removed signature '%s' from %s", toRemove, namespace);
      }
   }

   record FunctionIdentifier(@NotNull String name, boolean local, int minArgCount, @NotNull Class<?>... args) {
      static FunctionRegistry.FunctionIdentifier of(@NotNull String name, boolean local, @NotNull Class<?>... args) {
         Preconditions.checkNotNull(name, "name cannot be null");
         Preconditions.checkNotNull(args, "args cannot be null");
         return new FunctionRegistry.FunctionIdentifier(name, local, args.length, args);
      }

      static FunctionRegistry.FunctionIdentifier of(@NotNull Signature<?> signature) {
         Preconditions.checkNotNull(signature, "signature cannot be null");
         org.skriptlang.skript.common.function.Parameter<?>[] signatureParams = signature.parameters().all();
         Class<?>[] parameters = new Class[signatureParams.length];
         int optionalArgs = 0;

         for (int i = 0; i < signatureParams.length; i++) {
            org.skriptlang.skript.common.function.Parameter<?> param = signatureParams[i];
            if (param.hasModifier(org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL)) {
               optionalArgs++;
            }

            parameters[i] = param.type();
         }

         return new FunctionRegistry.FunctionIdentifier(signature.getName(), signature.isLocal(), parameters.length - optionalArgs, parameters);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.name, Arrays.hashCode((Object[])this.args));
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof FunctionRegistry.FunctionIdentifier other) {
            if (!this.name.equals(other.name)) {
               return false;
            } else if (this.args.length != other.args.length) {
               return false;
            } else {
               for (int i = 0; i < this.args.length; i++) {
                  if (this.args[i] != other.args[i]) {
                     return false;
                  }
               }

               return true;
            }
         } else {
            return false;
         }
      }

      @NotNull
      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this)
            .add("name", this.name)
            .add("local", this.local)
            .add("minArgCount", this.minArgCount)
            .add("args", Arrays.stream(this.args).map(Class::getSimpleName).collect(Collectors.joining(", ")))
            .toString();
      }
   }

   private static final class Namespace {
      private final Map<String, Set<FunctionRegistry.FunctionIdentifier>> identifiers = new HashMap<>();
      private final Map<FunctionRegistry.FunctionIdentifier, Function<?>> functions = new HashMap<>();
      private final Map<FunctionRegistry.FunctionIdentifier, Signature<?>> signatures = new HashMap<>();
   }

   private record NamespaceIdentifier(@Nullable String name) {
      public boolean local() {
         return this.name == null;
      }
   }

   public record Retrieval<T>(@NotNull FunctionRegistry.RetrievalResult result, T retrieved, Class<?>[][] conflictingArgs) {
   }

   public static enum RetrievalResult {
      NOT_REGISTERED,
      AMBIGUOUS,
      EXACT;
   }
}

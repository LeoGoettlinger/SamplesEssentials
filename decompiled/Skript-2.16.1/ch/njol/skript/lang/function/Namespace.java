package ch.njol.skript.lang.function;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class Namespace {
   private final Map<Namespace.Info, Signature<?>> signatures = new HashMap<>();
   private final Map<Namespace.Info, Function<?>> functions = new HashMap<>();

   @Nullable
   public Signature<?> getSignature(String name, boolean local) {
      return this.signatures.get(new Namespace.Info(name, local));
   }

   @Nullable
   public Signature<?> getSignature(String name) {
      Signature<?> signature = this.getSignature(name, true);
      return signature == null ? this.getSignature(name, false) : signature;
   }

   public void addSignature(Signature<?> sign) {
      Namespace.Info info = new Namespace.Info(sign.getName(), sign.isLocal());
      if (this.signatures.containsKey(info)) {
         throw new IllegalArgumentException("function name already used");
      } else {
         this.signatures.put(info, sign);
      }
   }

   public boolean removeSignature(Signature<?> sign) {
      Namespace.Info info = new Namespace.Info(sign.getName(), sign.isLocal());
      if (this.signatures.get(info) != sign) {
         return false;
      } else {
         this.signatures.remove(info);
         return true;
      }
   }

   public Collection<Signature<?>> getSignatures() {
      return this.signatures.values();
   }

   @Nullable
   public Function<?> getFunction(String name, boolean local) {
      return this.functions.get(new Namespace.Info(name, local));
   }

   @Nullable
   public Function<?> getFunction(String name) {
      Function<?> function = this.getFunction(name, true);
      return function == null ? this.getFunction(name, false) : function;
   }

   public void addFunction(Function<?> func) {
      Namespace.Info info = new Namespace.Info(func.getName(), func.getSignature().isLocal());

      assert this.signatures.containsKey(info) : "missing signature for function";

      this.functions.put(info, func);
   }

   public Collection<Function<?>> getFunctions() {
      return this.functions.values();
   }

   private static class Info {
      private final String name;
      private final boolean local;

      public Info(String name, boolean local) {
         this.name = name;
         this.local = local;
      }

      public String getName() {
         return this.name;
      }

      public boolean isLocal() {
         return this.local;
      }

      @Override
      public int hashCode() {
         int result = this.getName().hashCode();
         return 31 * result + (this.isLocal() ? 1 : 0);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof Namespace.Info info)) {
            return false;
         } else {
            return this.isLocal() != info.isLocal() ? false : this.getName().equals(info.getName());
         }
      }
   }

   public static class Key {
      private final Namespace.Origin origin;
      @Nullable
      private final String scriptName;

      public Key(Namespace.Origin origin, @Nullable String scriptName) {
         this.origin = origin;
         this.scriptName = scriptName;
      }

      public Namespace.Origin getOrigin() {
         return this.origin;
      }

      @Nullable
      public String getScriptName() {
         return this.scriptName;
      }

      @Override
      public int hashCode() {
         int result = this.origin.hashCode();
         return 31 * result + (this.scriptName != null ? this.scriptName.hashCode() : 0);
      }

      @Override
      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            Namespace.Key other = (Namespace.Key)object;
            return this.origin != other.origin ? false : Objects.equals(this.scriptName, other.scriptName);
         } else {
            return false;
         }
      }
   }

   public static enum Origin {
      JAVA,
      SCRIPT;
   }
}

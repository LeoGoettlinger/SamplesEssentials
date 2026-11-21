package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.SequencedCollection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.structure.StructureInfo;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

@Deprecated(
   since = "2.14",
   forRemoval = true
)
public class SyntaxElementInfo<E extends SyntaxElement> implements SyntaxInfo<E> {
   @Nullable
   private final SyntaxInfo<E> source;
   public final Class<E> elementClass;
   public final String[] patterns;
   public final String originClassPath;

   public SyntaxElementInfo(String[] patterns, Class<E> elementClass, String originClassPath) throws IllegalArgumentException {
      if (Modifier.isAbstract(elementClass.getModifiers())) {
         throw new SkriptAPIException("Class " + elementClass.getName() + " is abstract");
      } else {
         this.source = null;
         this.patterns = patterns;
         this.elementClass = elementClass;
         this.originClassPath = originClassPath;

         try {
            elementClass.getConstructor();
         } catch (NoSuchMethodException var5) {
            throw new Error(elementClass + " does not have a public nullary constructor", var5);
         } catch (SecurityException var6) {
            throw new IllegalStateException("Skript cannot run properly because a security manager is blocking it!");
         }
      }
   }

   @Internal
   protected SyntaxElementInfo(SyntaxInfo<E> source) throws IllegalArgumentException {
      this.source = source;
      this.patterns = source.patterns().toArray(new String[0]);
      this.elementClass = source.type();
      this.originClassPath = source.origin().name();
   }

   public Class<E> getElementClass() {
      return this.elementClass;
   }

   public String[] getPatterns() {
      return Arrays.copyOf(this.patterns, this.patterns.length);
   }

   public String getOriginClassPath() {
      return this.originClassPath;
   }

   @Internal
   @Contract("_ -> new")
   public static <I extends SyntaxElementInfo<E>, E extends SyntaxElement> I fromModern(SyntaxInfo<? extends E> info) {
      if (info instanceof SyntaxElementInfo<? extends E> oldInfo) {
         return (I)oldInfo;
      } else if (info instanceof BukkitSyntaxInfos.Event<?> event) {
         return (I)(new SkriptEventInfo(event));
      } else if (info instanceof DefaultSyntaxInfos.Structure<?> structure) {
         return (I)(new StructureInfo(structure));
      } else {
         return (I)(info instanceof DefaultSyntaxInfos.Expression<?, ?> expression ? new ExpressionInfo(expression) : new SyntaxElementInfo(info));
      }
   }

   @Internal
   @Override
   public SyntaxInfo.Builder<? extends SyntaxInfo.Builder<?, E>, E> toBuilder() {
      throw new UnsupportedOperationException();
   }

   @Internal
   @Override
   public Origin origin() {
      return this.source != null ? this.source.origin() : Origin.UNKNOWN;
   }

   @Internal
   @Override
   public Class<E> type() {
      return this.getElementClass();
   }

   @Internal
   @Override
   public E instance() {
      if (this.source != null) {
         return this.source.instance();
      } else {
         try {
            return this.type().getDeclaredConstructor().newInstance();
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException var2) {
            throw new RuntimeException(var2);
         }
      }
   }

   @Internal
   @Unmodifiable
   @Override
   public SequencedCollection<String> patterns() {
      return (SequencedCollection<String>)(this.source != null ? this.source.patterns() : List.of(this.getPatterns()));
   }

   @Internal
   @Override
   public Priority priority() {
      if (this.source != null) {
         this.source.priority();
      }

      return SyntaxInfo.COMBINED;
   }
}

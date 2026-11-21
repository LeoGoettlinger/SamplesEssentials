package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.DefaultExpression;
import java.util.function.Consumer;
import org.bukkit.Keyed;
import org.bukkit.Registry;

public class RegistryClassInfo<R extends Keyed> extends ClassInfo<R> {
   public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode) {
      this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass));
   }

   public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, Consumer<R> parseCallback) {
      this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass), parseCallback);
   }

   public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression) {
      this(registryClass, registry, codeName, languageNode, defaultExpression, ignored -> {});
   }

   public RegistryClassInfo(
      Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression, Consumer<R> parseCallback
   ) {
      super(registryClass, codeName);
      RegistryParser<R> registryParser = new RegistryParser<>(registry, languageNode, parseCallback);
      this.usage(new String[]{registryParser.getCombinedPatterns()})
         .supplier(registry::iterator)
         .serializer(new RegistrySerializer<>(registry))
         .defaultExpression(defaultExpression)
         .parser(registryParser);
   }

   @Deprecated(
      since = "2.16",
      forRemoval = true
   )
   public RegistryClassInfo(Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, boolean registerComparator) {
      this(registryClass, registry, codeName, languageNode, new EventValueExpression<>(registryClass));
   }

   @Deprecated(
      since = "2.16",
      forRemoval = true
   )
   public RegistryClassInfo(
      Class<R> registryClass, Registry<R> registry, String codeName, String languageNode, DefaultExpression<R> defaultExpression, boolean registerComparator
   ) {
      this(registryClass, registry, codeName, languageNode, defaultExpression);
   }
}

package ch.jalu.configme.beanmapper.leafvaluehandler;

import ch.jalu.configme.utils.TypeInformation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombiningLeafValueHandler implements LeafValueHandler {
   @NotNull
   private final Collection<LeafValueHandler> handlers;

   public CombiningLeafValueHandler(@NotNull LeafValueHandler... handlers) {
      this(Arrays.asList(handlers));
   }

   public CombiningLeafValueHandler(@NotNull Collection<LeafValueHandler> handlers) {
      this.handlers = Collections.unmodifiableCollection(handlers);
   }

   @Nullable
   @Override
   public Object convert(@NotNull TypeInformation typeInformation, @Nullable Object value) {
      return this.getFirstNonNull(t -> t.convert(typeInformation, value));
   }

   @Nullable
   @Override
   public Object toExportValue(@Nullable Object value) {
      return this.getFirstNonNull(t -> t.toExportValue(value));
   }

   @NotNull
   protected final Collection<LeafValueHandler> getHandlers() {
      return this.handlers;
   }

   @Nullable
   private Object getFirstNonNull(@NotNull Function<LeafValueHandler, Object> callback) {
      return this.handlers.stream().map(callback).filter(Objects::nonNull).findFirst().orElse(null);
   }
}

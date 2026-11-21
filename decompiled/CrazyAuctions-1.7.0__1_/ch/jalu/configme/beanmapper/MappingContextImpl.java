package ch.jalu.configme.beanmapper;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.utils.TypeInformation;
import org.jetbrains.annotations.NotNull;

public class MappingContextImpl implements MappingContext {
   private final String path;
   private final TypeInformation typeInformation;
   private final ConvertErrorRecorder errorRecorder;

   protected MappingContextImpl(@NotNull String path, @NotNull TypeInformation typeInformation, @NotNull ConvertErrorRecorder errorRecorder) {
      this.path = path;
      this.typeInformation = typeInformation;
      this.errorRecorder = errorRecorder;
   }

   @NotNull
   public static MappingContextImpl createRoot(@NotNull TypeInformation typeInformation, @NotNull ConvertErrorRecorder errorRecorder) {
      return new MappingContextImpl("", typeInformation, errorRecorder);
   }

   @NotNull
   @Override
   public MappingContext createChild(@NotNull String subPath, @NotNull TypeInformation typeInformation) {
      return this.path.isEmpty()
         ? new MappingContextImpl(subPath, typeInformation, this.errorRecorder)
         : new MappingContextImpl(this.path + "." + subPath, typeInformation, this.errorRecorder);
   }

   @NotNull
   @Override
   public TypeInformation getTypeInformation() {
      return this.typeInformation;
   }

   @NotNull
   @Override
   public String createDescription() {
      return "Path: '" + this.path + "', type: '" + this.typeInformation.getType() + "'";
   }

   @Override
   public void registerError(@NotNull String reason) {
      this.errorRecorder.setHasError("At path '" + this.path + "': " + reason);
   }

   @NotNull
   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "[" + this.createDescription() + "]";
   }
}

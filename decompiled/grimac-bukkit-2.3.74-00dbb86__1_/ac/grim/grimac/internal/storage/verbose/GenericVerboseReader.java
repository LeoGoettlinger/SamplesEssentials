package ac.grim.grimac.internal.storage.verbose;

import ac.grim.grimac.api.storage.verbose.VerboseBuf;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.api.storage.verbose.VerboseSchema;
import ac.grim.grimac.api.storage.verbose.VerboseSink;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class GenericVerboseReader {
   private GenericVerboseReader() {
   }

   public static void render(@NotNull VerboseSchema.Layout layout, @NotNull VerboseBuf in, @NotNull VerboseRenderContext ctx, @NotNull VerboseSink out) throws GenericVerboseReader.UnderflowException {
      try {
         for (VerboseSchema.Field field : layout.fields()) {
            renderField(field, in, out);
         }
      } catch (IllegalArgumentException | VerboseBuf.UnderflowException var6) {
         throw new GenericVerboseReader.UnderflowException(var6);
      }
   }

   private static void renderField(@NotNull VerboseSchema.Field field, @NotNull VerboseBuf in, @NotNull VerboseSink out) {
      out.key(field.name());
      switch (field.type()) {
         case F64:
            require(in, 8);
            out.num(in.rf64());
            break;
         case F32:
            require(in, 4);
            out.num(in.rf32());
            break;
         case VI:
         case ENUM:
            require(in, 1);
            out.num(in.rvi());
            break;
         case ZZ:
            require(in, 1);
            out.num(in.rzz());
            break;
         case VL:
            require(in, 1);
            out.num(in.rvl());
            break;
         case BOOL:
            require(in, 1);
            out.bool(in.rbool());
            break;
         case STR:
            require(in, 1);
            out.text(in.rstr());
      }
   }

   private static void require(@NotNull VerboseBuf in, int bytes) {
      if (in.remaining() < bytes) {
         throw new VerboseBuf.UnderflowException("verbose payload truncated");
      }
   }

   public static final class UnderflowException extends Exception {
      public UnderflowException(@NotNull Throwable cause) {
         super("verbose payload truncated", cause);
      }
   }
}

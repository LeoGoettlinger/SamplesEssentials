package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.annotation.specifier;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;
import org.apiguardian.api.API;

@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public @interface Decoder {
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   Class<? extends Decoder.Provider> value();

   @Documented
   @Target({ElementType.PARAMETER})
   @Retention(RetentionPolicy.RUNTIME)
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public @interface Json {
      @API(
         status = API.Status.STABLE,
         since = "2.0.0"
      )
      boolean downsampleColors() default false;
   }

   @Documented
   @Target({ElementType.PARAMETER})
   @Retention(RetentionPolicy.RUNTIME)
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public @interface Legacy {
      @API(
         status = API.Status.STABLE,
         since = "2.0.0"
      )
      char value() default '&';
   }

   @Documented
   @Target({ElementType.PARAMETER})
   @Retention(RetentionPolicy.RUNTIME)
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public @interface MiniMessage {
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public interface Provider {
      @API(
         status = API.Status.STABLE,
         since = "2.0.0"
      )
      Function<String, ? extends Component> decoder(TypeToken<?> parsedType);
   }
}

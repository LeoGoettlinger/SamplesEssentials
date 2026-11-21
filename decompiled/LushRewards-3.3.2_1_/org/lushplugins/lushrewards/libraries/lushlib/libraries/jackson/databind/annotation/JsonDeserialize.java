package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JacksonAnnotation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.KeyDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.Converter;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonDeserialize {
   Class<? extends JsonDeserializer> using() default JsonDeserializer.None.class;

   Class<? extends JsonDeserializer> contentUsing() default JsonDeserializer.None.class;

   Class<? extends KeyDeserializer> keyUsing() default KeyDeserializer.None.class;

   Class<?> builder() default Void.class;

   Class<? extends Converter> converter() default Converter.None.class;

   Class<? extends Converter> contentConverter() default Converter.None.class;

   Class<?> as() default Void.class;

   Class<?> keyAs() default Void.class;

   Class<?> contentAs() default Void.class;
}

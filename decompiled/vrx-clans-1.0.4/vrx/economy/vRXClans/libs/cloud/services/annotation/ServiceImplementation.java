package vrx.economy.vRXClans.libs.cloud.services.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import vrx.economy.vRXClans.libs.cloud.services.type.Service;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceImplementation {
   Class<? extends Service<?, ?>> value();
}

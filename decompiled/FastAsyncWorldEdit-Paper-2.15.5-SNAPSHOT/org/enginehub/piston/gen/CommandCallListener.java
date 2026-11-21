package org.enginehub.piston.gen;

import java.lang.reflect.Method;
import org.enginehub.piston.CommandParameters;

public interface CommandCallListener {
   default void beforeCall(Method commandMethod, CommandParameters parameters) {
   }

   default void afterCall(Method commandMethod, CommandParameters parameters) {
   }

   default void afterThrow(Method commandMethod, CommandParameters parameters, Throwable error) {
   }
}

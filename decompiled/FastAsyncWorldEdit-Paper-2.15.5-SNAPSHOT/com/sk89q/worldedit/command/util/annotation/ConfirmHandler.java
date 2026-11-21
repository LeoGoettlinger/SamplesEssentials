package com.sk89q.worldedit.command.util.annotation;

import com.fastasyncworldedit.core.configuration.Settings;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.lang.reflect.Method;
import java.util.Optional;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.exception.StopExecutionException;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.inject.Key;

public class ConfirmHandler implements CommandCallListener {
   @Override
   public void beforeCall(Method method, CommandParameters parameters) {
      Confirm confirmAnnotation = method.getAnnotation(Confirm.class);
      if (confirmAnnotation != null) {
         Optional<Actor> actorOpt = parameters.injectedValue(Key.of(Actor.class));
         if (!actorOpt.isEmpty()) {
            Actor actor = actorOpt.get();
            if (Settings.settings().getLimit(actor).CONFIRM_LARGE) {
               if (!confirmAnnotation.value().passes(actor, parameters, 1.0)) {
                  throw new StopExecutionException(TextComponent.empty());
               }
            }
         }
      }
   }
}

package com.sk89q.worldedit.command.util.annotation;

import com.fastasyncworldedit.core.configuration.Settings;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extension.platform.Actor;
import java.lang.reflect.Method;
import java.util.Optional;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.inject.Key;

public class PreloadHandler implements CommandCallListener {
   @Override
   public void beforeCall(Method method, CommandParameters parameters) {
      Preload preloadAnnotation = method.getAnnotation(Preload.class);
      if (preloadAnnotation != null) {
         Optional<Actor> actorOpt = parameters.injectedValue(Key.of(Actor.class));
         Optional<EditSession> editSessionOpt = parameters.injectedValue(Key.of(EditSession.class));
         if (!actorOpt.isEmpty() && !editSessionOpt.isEmpty()) {
            Actor actor = actorOpt.get();
            if (Settings.settings().QUEUE.PRELOAD_CHUNK_COUNT > 1) {
               preloadAnnotation.value().preload(actor, parameters);
            }
         }
      }
   }
}

package com.sk89q.worldedit.command.util.annotation;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.queue.implementation.preloader.Preloader;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.World;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.enginehub.piston.inject.InjectAnnotation;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@InjectAnnotation
public @interface Preload {
   Preload.PreloadCheck value() default Preload.PreloadCheck.NEVER;

   public static enum PreloadCheck {
      PRELOAD {
         @Override
         public void preload(Actor actor, InjectedValueAccess context) {
            World world = context.injectedValue(Key.of(EditSession.class)).get().getWorld();
            Preloader preloader = Fawe.<IFawe>platform().getPreloader(true);
            if (preloader != null) {
               preloader.update(actor, world);
            }
         }
      },
      NEVER {};

      public void preload(Actor actor, InjectedValueAccess context) {
      }
   }
}

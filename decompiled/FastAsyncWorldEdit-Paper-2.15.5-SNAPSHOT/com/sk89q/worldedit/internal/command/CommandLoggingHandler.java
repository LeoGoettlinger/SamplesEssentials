package com.sk89q.worldedit.internal.command;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.inject.Key;

public class CommandLoggingHandler implements CommandCallListener, AutoCloseable {
   private final WorldEdit worldEdit;
   private final Logger logger;

   public CommandLoggingHandler(WorldEdit worldEdit, Logger logger) {
      Preconditions.checkNotNull(worldEdit);
      Preconditions.checkNotNull(logger);
      this.worldEdit = worldEdit;
      this.logger = logger;
   }

   @Override
   public void beforeCall(Method method, CommandParameters parameters) {
      Logging loggingAnnotation = method.getAnnotation(Logging.class);
      StringBuilder builder = new StringBuilder();
      Logging.LogMode logMode;
      if (loggingAnnotation == null) {
         logMode = null;
      } else {
         logMode = loggingAnnotation.value();
      }

      Optional<Actor> actorOpt = parameters.injectedValue(Key.of(Actor.class));
      if (actorOpt.isPresent()) {
         Actor actor = actorOpt.get();

         World world;
         try {
            Optional<World> worldOpt = parameters.injectedValue(Key.of(World.class));
            if (!worldOpt.isPresent()) {
               return;
            }

            world = worldOpt.get();
         } catch (CommandException var15) {
            return;
         }

         builder.append("WorldEdit: ").append(actor.getName());
         builder.append(" (in \"").append(world.getName()).append("\")");
         builder.append(": ").append(parameters.getMetadata().getCalledName());
         builder.append(": ")
            .append(
               Stream.concat(Stream.of(parameters.getMetadata().getCalledName()), parameters.getMetadata().getArguments().stream())
                  .collect(Collectors.joining(" "))
            );
         if (logMode != null && actor instanceof Player player) {
            Vector3 position = player.getLocation().toVector();
            LocalSession session = this.worldEdit.getSessionManager().get(actor);
            switch (logMode) {
               case PLACEMENT:
                  try {
                     position = session.getPlacementPosition(actor).toVector3();
                  } catch (IncompleteRegionException var14) {
                     break;
                  }
               case POSITION:
                  builder.append(" - Position: ").append(position);
                  break;
               case ALL:
                  builder.append(" - Position: ").append(position);
               case ORIENTATION_REGION:
                  builder.append(" - Orientation: ").append(player.getCardinalDirection().name());
               case REGION:
                  try {
                     builder.append(" - Region: ").append(session.getSelection(world));
                  } catch (IncompleteRegionException var13) {
                  }
            }
         }

         this.logger.info(builder.toString());
      }
   }

   @Override
   public void close() {
      for (Handler h : this.logger.getHandlers()) {
         h.close();
      }
   }
}

package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.command.ArgumentStack;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandCategory;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.command.CommandPermission;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.core.reflect.MethodCaller;
import com.artillexstudios.axtrade.libs.lamp.process.ResponseHandler;
import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

class CommandExecutable implements ExecutableCommand {
   CommandHandler handler;
   boolean permissionSet = false;
   int id;
   CommandPath path;
   String name;
   String usage;
   String description;
   Method method;
   AnnotationReader reader;
   boolean secret;
   MethodCaller.BoundMethodCaller methodCaller;
   BaseCommandCategory parent;
   ResponseHandler responseHandler = CommandParser.VOID_HANDLER;
   private CommandPermission permission = CommandPermission.ALWAYS_TRUE;
   @Unmodifiable
   List<CommandParameter> parameters;
   @Unmodifiable
   Map<Integer, CommandParameter> resolveableParameters;

   @NotNull
   @Override
   public String getName() {
      return this.name;
   }

   @Range(
      from = 0L,
      to = Long.MAX_VALUE
   )
   @Override
   public int getId() {
      return this.id;
   }

   @NotNull
   @Override
   public String getUsage() {
      return this.usage;
   }

   @Nullable
   @Override
   public String getDescription() {
      return this.description;
   }

   @NotNull
   @Override
   public CommandPath getPath() {
      return this.path;
   }

   @Nullable
   @Override
   public CommandCategory getParent() {
      return this.parent;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<CommandParameter> getParameters() {
      return this.parameters;
   }

   @NotNull
   @Unmodifiable
   @Override
   public Map<Integer, CommandParameter> getValueParameters() {
      return this.resolveableParameters;
   }

   @NotNull
   @Override
   public CommandPermission getPermission() {
      return this.permission;
   }

   @NotNull
   @Override
   public CommandHandler getCommandHandler() {
      return this.handler;
   }

   @NotNull
   @Override
   public <T> ResponseHandler<T> getResponseHandler() {
      return this.responseHandler;
   }

   @Override
   public void execute(@NotNull CommandActor actor, @Nullable Collection<String> input) {
      Preconditions.notNull(actor, "actor");
      ArgumentStack arguments = ArgumentStack.copyExact(this.path.path);
      if (input != null) {
         arguments.addAll(input);
      }

      this.getCommandHandler().dispatch(actor, arguments);
   }

   @Override
   public void execute(@NotNull CommandActor actor, @Nullable String... input) {
      Preconditions.notNull(actor, "actor");
      ArgumentStack arguments = ArgumentStack.copyExact(this.path.path);
      if (input != null) {
         Collections.addAll(arguments, input);
      }

      this.getCommandHandler().dispatch(actor, arguments);
   }

   @Override
   public boolean isSecret() {
      return this.secret;
   }

   @Override
   public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotation) {
      return this.reader.get(annotation);
   }

   @Override
   public boolean hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
      return this.reader.contains(annotation);
   }

   public void parent(BaseCommandCategory cat, boolean isDefault) {
      this.parent = cat;
      if (cat != null) {
         if (isDefault) {
            if (cat.defaultAction != null && cat.defaultAction.method != this.method) {
               throw new IllegalArgumentException(
                  "Category '"
                     + cat.getPath().toRealString()
                     + "' has more than one default action! ("
                     + cat.defaultAction.method.toGenericString()
                     + " and "
                     + this.method.toGenericString()
                     + ")"
               );
            }

            cat.defaultAction = this;
         } else {
            cat.commands.put(this.path, this);
         }
      }
   }

   public void setPermission(@NotNull CommandPermission permission) {
      Preconditions.notNull(permission, "permission");
      this.permission = permission;
   }

   @Override
   public String toString() {
      return "ExecutableCommand{path=" + this.path + ", name='" + this.name + '\'' + '}';
   }

   public int compareTo(@NotNull ExecutableCommand o) {
      return this.path.compareTo(o.getPath());
   }
}

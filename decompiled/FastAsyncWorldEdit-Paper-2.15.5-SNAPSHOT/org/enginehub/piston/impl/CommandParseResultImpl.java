package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.CommandParseResult;

@AutoValue
abstract class CommandParseResultImpl implements CommandParseResult {
   static CommandParseResultImpl.Builder builder() {
      return new AutoValue_CommandParseResultImpl.Builder();
   }

   @Override
   public abstract ImmutableList<Command> getExecutionPath();

   @Override
   public abstract ImmutableList<ArgBinding> getBoundArguments();

   @Override
   public abstract CommandParameters getParameters();

   @com.google.auto.value.AutoValue.Builder
   abstract static class Builder {
      public final CommandParseResultImpl.Builder addCommand(Command command) {
         this.executionPathBuilder().add(command);
         return this;
      }

      abstract com.google.common.collect.ImmutableList.Builder<Command> executionPathBuilder();

      public abstract ImmutableList<Command> getExecutionPath();

      public final CommandParseResultImpl.Builder addArgument(ArgBinding argBinding) {
         this.boundArgumentsBuilder().add(argBinding);
         return this;
      }

      abstract com.google.common.collect.ImmutableList.Builder<ArgBinding> boundArgumentsBuilder();

      public abstract CommandParseResultImpl.Builder parameters(CommandParameters var1);

      public abstract CommandParseResultImpl build();
   }
}

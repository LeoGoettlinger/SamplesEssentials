package org.enginehub.piston.impl;

import com.google.common.collect.ImmutableList;
import org.enginehub.piston.ArgBinding;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParameters;

final class AutoValue_CommandParseResultImpl extends CommandParseResultImpl {
   private final ImmutableList<Command> executionPath;
   private final ImmutableList<ArgBinding> boundArguments;
   private final CommandParameters parameters;

   private AutoValue_CommandParseResultImpl(ImmutableList<Command> executionPath, ImmutableList<ArgBinding> boundArguments, CommandParameters parameters) {
      this.executionPath = executionPath;
      this.boundArguments = boundArguments;
      this.parameters = parameters;
   }

   @Override
   public ImmutableList<Command> getExecutionPath() {
      return this.executionPath;
   }

   @Override
   public ImmutableList<ArgBinding> getBoundArguments() {
      return this.boundArguments;
   }

   @Override
   public CommandParameters getParameters() {
      return this.parameters;
   }

   @Override
   public String toString() {
      return "CommandParseResultImpl{executionPath=" + this.executionPath + ", boundArguments=" + this.boundArguments + ", parameters=" + this.parameters + "}";
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof CommandParseResultImpl that)
            ? false
            : this.executionPath.equals(that.getExecutionPath())
               && this.boundArguments.equals(that.getBoundArguments())
               && this.parameters.equals(that.getParameters());
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.executionPath.hashCode();
      h$ *= 1000003;
      h$ ^= this.boundArguments.hashCode();
      h$ *= 1000003;
      return h$ ^ this.parameters.hashCode();
   }

   static final class Builder extends CommandParseResultImpl.Builder {
      private com.google.common.collect.ImmutableList.Builder<Command> executionPathBuilder$;
      private ImmutableList<Command> executionPath;
      private com.google.common.collect.ImmutableList.Builder<ArgBinding> boundArgumentsBuilder$;
      private ImmutableList<ArgBinding> boundArguments;
      private CommandParameters parameters;

      @Override
      com.google.common.collect.ImmutableList.Builder<Command> executionPathBuilder() {
         if (this.executionPathBuilder$ == null) {
            this.executionPathBuilder$ = ImmutableList.builder();
         }

         return this.executionPathBuilder$;
      }

      @Override
      public ImmutableList<Command> getExecutionPath() {
         if (this.executionPathBuilder$ != null) {
            return this.executionPathBuilder$.build();
         } else {
            if (this.executionPath == null) {
               this.executionPath = ImmutableList.of();
            }

            return this.executionPath;
         }
      }

      @Override
      com.google.common.collect.ImmutableList.Builder<ArgBinding> boundArgumentsBuilder() {
         if (this.boundArgumentsBuilder$ == null) {
            this.boundArgumentsBuilder$ = ImmutableList.builder();
         }

         return this.boundArgumentsBuilder$;
      }

      @Override
      public CommandParseResultImpl.Builder parameters(CommandParameters parameters) {
         if (parameters == null) {
            throw new NullPointerException("Null parameters");
         } else {
            this.parameters = parameters;
            return this;
         }
      }

      @Override
      public CommandParseResultImpl build() {
         if (this.executionPathBuilder$ != null) {
            this.executionPath = this.executionPathBuilder$.build();
         } else if (this.executionPath == null) {
            this.executionPath = ImmutableList.of();
         }

         if (this.boundArgumentsBuilder$ != null) {
            this.boundArguments = this.boundArgumentsBuilder$.build();
         } else if (this.boundArguments == null) {
            this.boundArguments = ImmutableList.of();
         }

         if (this.parameters == null) {
            String missing = " parameters";
            throw new IllegalStateException("Missing required properties:" + missing);
         } else {
            return new AutoValue_CommandParseResultImpl(this.executionPath, this.boundArguments, this.parameters);
         }
      }
   }
}

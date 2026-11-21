package com.artillexstudios.axafkzone.libs.lamp.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;

public class NumberNotInRangeException extends RuntimeException {
   private final CommandActor actor;
   private final CommandParameter parameter;
   private final Number input;
   private final double minimum;
   private final double maximum;

   public CommandActor getActor() {
      return this.actor;
   }

   public CommandParameter getParameter() {
      return this.parameter;
   }

   public Number getInput() {
      return this.input;
   }

   public double getMinimum() {
      return this.minimum;
   }

   public double getMaximum() {
      return this.maximum;
   }

   public NumberNotInRangeException(CommandActor actor, CommandParameter parameter, Number input, double minimum, double maximum) {
      this.actor = actor;
      this.parameter = parameter;
      this.input = input;
      this.minimum = minimum;
      this.maximum = maximum;
   }
}

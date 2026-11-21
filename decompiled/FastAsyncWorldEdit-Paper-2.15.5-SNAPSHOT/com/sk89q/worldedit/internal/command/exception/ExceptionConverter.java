package com.sk89q.worldedit.internal.command.exception;

import org.enginehub.piston.exception.CommandException;

public interface ExceptionConverter {
   void convert(Throwable t) throws CommandException;
}

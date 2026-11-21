package org.enginehub.piston.gen;

import java.lang.reflect.Method;
import org.enginehub.piston.Command;

public interface CommandConditionGenerator {
   Command.Condition generateCondition(Method var1);
}

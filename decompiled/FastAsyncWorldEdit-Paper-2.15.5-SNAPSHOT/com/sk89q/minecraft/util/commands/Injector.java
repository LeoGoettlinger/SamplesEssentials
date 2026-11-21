package com.sk89q.minecraft.util.commands;

import java.lang.reflect.InvocationTargetException;

public interface Injector {
   Object getInstance(Class<?> cls) throws InvocationTargetException, IllegalAccessException, InstantiationException;
}

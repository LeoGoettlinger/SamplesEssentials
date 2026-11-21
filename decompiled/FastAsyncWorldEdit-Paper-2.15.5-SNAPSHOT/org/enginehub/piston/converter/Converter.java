package org.enginehub.piston.converter;

import org.enginehub.piston.inject.InjectedValueAccess;

@FunctionalInterface
public interface Converter<T> {
   ConversionResult<T> convert(String var1, InjectedValueAccess var2);
}

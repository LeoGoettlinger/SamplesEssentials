package org.enginehub.linbus.common;

import java.io.IOException;

public interface IOFunction<T, R> {
   R apply(T t) throws IOException;
}

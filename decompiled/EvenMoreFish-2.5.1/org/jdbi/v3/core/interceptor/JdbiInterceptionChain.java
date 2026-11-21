package org.jdbi.v3.core.interceptor;

import org.jdbi.v3.meta.Alpha;

@Alpha
public interface JdbiInterceptionChain<T> {
   T next();
}

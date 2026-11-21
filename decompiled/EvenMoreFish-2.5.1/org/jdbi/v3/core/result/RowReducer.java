package org.jdbi.v3.core.result;

import java.util.stream.Stream;

public interface RowReducer<C, R> {
   C container();

   void accumulate(C container, RowView rowView);

   Stream<R> stream(C container);
}

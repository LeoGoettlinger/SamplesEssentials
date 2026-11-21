package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.QueryResult;
import java.util.Iterator;

public interface Result extends QueryResult {
   long getAffectedItemsCount();

   int getWarningsCount();

   Iterator<Warning> getWarnings();
}

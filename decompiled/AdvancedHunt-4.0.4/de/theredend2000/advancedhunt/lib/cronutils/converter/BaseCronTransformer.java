package de.theredend2000.advancedhunt.lib.cronutils.converter;

import de.theredend2000.advancedhunt.lib.cronutils.utils.StringUtils;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCronTransformer {
   protected static final Logger LOGGER = LoggerFactory.getLogger(BaseCronTransformer.class);
   protected String[] cronParts;
   protected Calendar calendarInstance;
   protected Integer calendarField;
   protected int cronFieldPosition;
   protected Integer cronFieldValue;
   Map<Integer, Integer> CRON_FIELDS_POSITION_MAP = Collections.unmodifiableMap(new HashMap<Integer, Integer>() {
      private static final long serialVersionUID = 911848294809282617L;

      {
         this.put(0, 12);
         this.put(1, 11);
         this.put(2, 5);
         this.put(3, 2);
         this.put(4, 7);
      }
   });

   public void apply(String[] cronParts, Calendar calendarInstance) {
      this.cronParts = cronParts;
      this.calendarInstance = calendarInstance;

      for (int i = 0; i < cronParts.length; i++) {
         if (StringUtils.isNumeric(cronParts[i])) {
            this.calendarField = this.CRON_FIELDS_POSITION_MAP.get(i);
            this.cronFieldPosition = i;
            this.cronFieldValue = Integer.parseInt(cronParts[i]);
            this.transform();
         }
      }
   }

   protected abstract void transform();
}

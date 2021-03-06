/*******************************************************************************
 * Copyright (c) 2014 Transcends, LLC.
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation; either version 2 of the 
 * License, or (at your option) any later version. This program is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You 
 * should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, 
 * USA. 
 * http://www.gnu.org/licenses/gpl-2.0.html
 *******************************************************************************/
/**
 * 
 */
package org.rifidi.edge.api.service.tagmonitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.edge.api.service.EsperUtil;
import org.rifidi.edge.api.service.RifidiAppEsperFactory;

/**
 * 
 * 
 * @author Matthew Dean - matt@transcends.co
 */
public class RSSIMonitorEsperFactory implements RifidiAppEsperFactory {

   /** The set of read zones to monitor */
   private final List<ReadZone> readzones;
   /** The name of the esper window to use */
   private final String windowName;
   
   private final String windowName_avg;
   
   private final String windowName_first;
   
   private final String windowName_max;

   /** The list of esper statements */
   private final List<String> statements;
   /** The time unit used for the departure wait time */
   private final TimeUnit timeUnit;
   /** The logger for this class */
   private final static Log logger = LogFactory
         .getLog(RSSIMonitorEsperFactory.class);
   private final String interval;
   private final boolean useRegex;
   
   private final Integer countThreshold;
   
   private final Double minAvgRSSIThreshold;
   
   private final Double changeRSSIThreshold;

   public RSSIMonitorEsperFactory(List<ReadZone> readzones, Integer windowID, Float windowTime, TimeUnit timeUnit,
         Integer countThreshold, Double mingAvgRSSIThreshold, Double changeRSSIThreshold, boolean useRegex) {
      this.readzones = new ArrayList<ReadZone>();
      if (readzones != null) {
         this.readzones.addAll(readzones);
      }
      this.windowName = "rssi_" + windowID;
      this.windowName_avg = windowName + "_avg_" + windowID;
      this.windowName_first = windowName + "_first_" + windowID;
      this.windowName_max = windowName + "_max_" + windowID;
      //this.slidingWindowName = "sliding_" + windowID;
      statements = new LinkedList<String>();
      //this.windowTime = windowTime;
      this.timeUnit = timeUnit;
      this.interval = EsperUtil.timeUnitToEsperTime(windowTime, timeUnit);
      this.countThreshold = countThreshold;
      this.minAvgRSSIThreshold = mingAvgRSSIThreshold;
      this.changeRSSIThreshold = changeRSSIThreshold;
      this.useRegex = useRegex;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.rifidi.edge.api.service.RifidiAppEsperFactory#createStatements()
    */
   @Override
   public List<String> createStatements() {
      statements.add("create window " + windowName_first + ".win:time_batch("+interval+") as TagReadEvent");
      statements.add(EsperUtil.buildInsertStatement(windowName_first, readzones, this.useRegex));
      statements.add("create window " + windowName_avg + ".win:time_batch("+interval+") as RSSITagReadEvent");
      statements.add("create window " + windowName_max + ".win:time_batch("+interval+") as RSSITagReadEvent");

      // Average RSSI
      statements.add("insert into "  + windowName_avg + " select tag.formattedID, readerID, antennaID, avg(cast(extraInformation('RSSI'),Double)) as avgRSSI, cast(count(*),Double) as tagCount from " + windowName_first + " group by tag.ID, readerID having cast(count(*),Double)>" + countThreshold + " and avg(cast(extraInformation('RSSI'),Double))>" + minAvgRSSIThreshold);

      // Max Avg
      statements.add("insert into " + windowName_max + " select tagID, max(avgRSSI) as avgRSSI, maxby(avgRSSI).readerID as readerID, antenna, tagCount from " + windowName_avg + " group by tagID");

      // Second Avg
      statements.add("insert into " + windowName_max + " select avg2.tagID as tagID, max(avg2b.avgRSSI) as avgRSSI, maxby(avg2b.avgRSSI).readerID as readerID from " + windowName_avg + " as avg2 inner join " + windowName_avg + " as avg2b on avg2.tagID=avg2b.tagID where avg2.avgRSSI>avg2b.avgRSSI group by avg2.tagID");

      // Threshold Check
      statements.add("insert into " + windowName + " select tagID, avgRSSI, readerID, antenna, tagCount from " + windowName_max + " group by tagID having (avgRSSI>min(avgRSSI)+" + changeRSSIThreshold + ") or (avgRSSI=min(avgRSSI)) and (avgRSSI=max(avgRSSI))");

      return statements;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.rifidi.edge.api.service.RifidiAppEsperFactory#createQuery()
    */
   @Override
   public String createQuery() {
      return "select irstream * from " + windowName + " output every " + interval;
   }
}
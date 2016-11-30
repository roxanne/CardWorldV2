/*

Copyright 2010, 2012 James A. Mason, York University, Toronto, Canada
http://www.yorku.ca/jmason

Unless required by applicable law or agreed to in writing, this software
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
ANY KIND, either express or implied.

You may use and modify it for research purposes, provided that
the copyright holder above is acknowledged as the original author.

 */

package com.asdnetworks.cardworld.cards;

/*
   CardWorld is the main driver for this example model
   of English-language understanding.
*/
public class CardWorld
{
   public static void main(String[] args)
   {
      table = new CardTable();
      agent = new CardAgent(table);
      table.setAgent(agent);
      window = new CardWorldFrame(agent, table);
      window.setVisible(true);
      agent.setUtteranceField(window.getUtteranceField());
      agent.setOutputPane(window.getOutputPane());
      agent.initialize();
   }

   static CardAgent agent;
   static CardTable table;
   static CardWorldFrame window;
}


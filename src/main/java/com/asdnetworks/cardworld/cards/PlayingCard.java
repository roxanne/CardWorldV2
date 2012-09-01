/*

Copyright 2010 James A. Mason, York University, Toronto, Canada
http://www.yorku.ca/jmason

Unless required by applicable law or agreed to in writing, this software
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
ANY KIND, either express or implied.

You may use and modify it for research purposes, provided that
the copyright holder above is acknowledged as the original author.

 */

package com.asdnetworks.cardworld.cards;

import java.io.*;  // for debugging

/*
   PlayingCard instances represent ordinary playing cards,
   including Jokers.
*/
public class PlayingCard extends Card
{
   PlayingCard(String frontFileName, String backFileName, AbstractPlayingCard absCard)
   {
      super(frontFileName, backFileName);
      represents = absCard;
      if (represents != null)
         setCardName(represents.getCardName());
   }

   PlayingCard(String frontFileName, String backFileName, int cardNumber)
   {  this(frontFileName, backFileName, new AbstractPlayingCard(cardNumber));
   }

   public String getCardName()
   {  if (represents != null)
         return super.getCardName();
      else
         return "";
   }

   public String getColorName()
   {
      if (represents != null)

         return represents.getColorName();
      else
         return "";
   }

   public String getRankName()
   {
      if (represents != null)
         return represents.getRankName();
      else
         return "";
   }

   public String getSuitName()
   {
      if (represents != null)
         return represents.getSuitName();
      else
         return "";

   }

   public AbstractPlayingCard getAbstractCard() { return represents; }

   public void setAbstractCard(AbstractPlayingCard absCard) { represents = absCard; }

   private AbstractPlayingCard represents = null;

} // end class PlayingCard

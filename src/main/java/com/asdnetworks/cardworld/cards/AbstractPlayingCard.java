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
   AbstractPlayingCard instances represent standard playing cards
   for a deck of 52 plus possible Jokers.  They have suits and
   ranks but lack visual images including backs.
 */
public class AbstractPlayingCard
{
   AbstractPlayingCard(int number)
   {
      if (number >= 0 && number <= 52)
         cardNumber = number;
      else
         // treat all bad card numbers as Jokers, for simplicity
         cardNumber = 52;

      rankNumber = cardNumber % 13;
      suitNumber = cardNumber / 13;
      setSuitName(suitNumber);
      setRankName(rankNumber);
      setCardName();
      setColorName();
   }


   void setCardName()
   {
      if (cardNumber < 0 || cardNumber > 51)
         cardName = "Joker";
      else
         cardName = rankName + " of " + suitName;
   }

   void setColorName()
   {
      if (cardNumber < 0 || cardNumber > 51)
         colorName = "";
      else if (suitNumber == 0 || suitNumber == 3)
         colorName = "black";
      else
         colorName = "red";
   }


   void setSuitName(int num)
   {
      if (num >= 0 && num <= 3)
         suitName = SUITS[num];
      else
         suitName = "";
   }

   void setRankName(int num)
   {
      if (num == 0)
         rankName = "Ace";
      else if (num >= 1 && num <= 9)
         rankName = "" + (num + 1);
      else if (num == 10)
         rankName = "Jack";
      else if (num == 11)
         rankName = "Queen";
      else if (num == 12)
         rankName = "King";
      else
         rankName = "";
   }

   public int getCardNumber() { return cardNumber; }
   public int getRankNumber() { return rankNumber; }
   public int getSuitNumber() { return suitNumber; }
   public String getCardName() { return cardName; }
   public String getColorName() { return colorName; }
   public String getRankName() { return rankName; }
   public String getSuitName() { return suitName; }

   int cardNumber = 52; // 0 through 52 (52 for Joker)
   int suitNumber = 4; // 0 through 4 (4 for Joker)
   int rankNumber = 13; // 0 through 12

   private String suitName = "";
   private String rankName = "";
   private String cardName = "";  // the name of the card in English
   private String colorName = ""; // "black", "red", or "" for Joker

   public static String[] SUITS
      = new String[]{"Clubs", "Diamonds", "Hearts", "Spades"};

} // end class AbstractPlayingCard
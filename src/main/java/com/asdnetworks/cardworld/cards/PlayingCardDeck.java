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
import java.io.*;
import java.util.*;

/*
   PlayingCardDeck is a CardPile of 52 standard playing cards
   plus a specified number of jokers.
*/
public class PlayingCardDeck extends CardPile
{
   PlayingCardDeck(int numberOfJokers)
   {
      super(STANDARD_SIZE + numberOfJokers);
      numberOfCards = STANDARD_SIZE + numberOfJokers;
   }

   PlayingCardDeck(int numberOfJokers, String fileType)
   {
      this(numberOfJokers);

      PlayingCard[] cardsInDeck = new PlayingCard[numberOfCards];
      if (fileType.equals("png"))
      {  int na = 0;
         int nb = 39;
         int nc = 26;
         int nd = 13;
         int fileNumber = 1;
         cardsInDeck[na] = new PlayingCard("1.png", "back.png", na);
         cardsInDeck[nb] = new PlayingCard("2.png", "back.png", nb);
         cardsInDeck[nc] = new PlayingCard("3.png", "back.png", nc);
         cardsInDeck[nd] = new PlayingCard("4.png", "back.png", nd);
         na += 12;
         nb += 12;
         nc += 12;
         nd += 12;

         for (int j = 1; j < 13; j++)
         {
            fileNumber += 4;
            cardsInDeck[na] = new PlayingCard(fileNumber + ".png", "back.png", na);
            cardsInDeck[nb] = new PlayingCard((fileNumber+1) + ".png", "back.png", nb);
            cardsInDeck[nc] = new PlayingCard((fileNumber+2) + ".png", "back.png", nc);
            cardsInDeck[nd] = new PlayingCard((fileNumber+3) + ".png", "back.png", nd);
            na--;
            nb--;
            nc--;
            nd--;
         }

         nb = STANDARD_SIZE;
         while (numberOfJokers > 0)
         {  cardsInDeck[nb] =
               new PlayingCard("joker.png", "back.png", nb);
            numberOfJokers--;
            nb++;
         }

         for (int j = 0; j < numberOfCards; j++)
         {
            this.addCard(cardsInDeck[j]);
         }

      }
   }

   public PlayingCard getCard(int position)
   {
      return (PlayingCard) super.getCard(position);
   }

//   public int getSize() { return numberOfCards; }

   private int numberOfCards = 0;
   public static int STANDARD_SIZE = 52;

} // end class PlayingCardDeck

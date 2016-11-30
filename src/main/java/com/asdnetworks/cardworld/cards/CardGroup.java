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

import java.io.*; // for debugging

import java.util.*;  // ArrayList, Random, etc.

/*
   CardGroup instances represent collections of Card instances.
*/
public class CardGroup
{
   CardGroup()
   {
      cardsInGroup = new ArrayList<Card>(13);
         // enough to hold 1/4 of a standard playing card deck
         // without jokers
   }

   CardGroup(int capacity)
   {
      cardsInGroup = new ArrayList<Card>(capacity);
   }

   CardGroup(Collection<Card> givenCards)
   {
      cardsInGroup = new ArrayList<Card>(givenCards);
   }

   CardGroup(Collection<Card> givenCards, String givenName)
   {
      this(givenCards);
      groupName = givenName;
   }

   /*
      Add a card to the group if it isn't already in the group.
    */
   public void addCard(Card newCard)
   {
      if (! cardsInGroup.contains(newCard))
         cardsInGroup.add(newCard);
   }

   public void addCard(int position, Card newCard)
   {
      if (! cardsInGroup.contains(newCard))
         cardsInGroup.add(position, newCard);
   }

   public boolean containsCard(Card givenCard)
   {
      return cardsInGroup.contains(givenCard);
   }

   public Card getCard(int j)
   {
      if (j >= 0 && j < cardsInGroup.size())
         return cardsInGroup.get(j);
      else
         return null;
   }

   public Iterator<Card> getIterator() { return cardsInGroup.iterator(); }

   public String getName() { return groupName; }

   public CardAgent getAgent() { return groupAgent; }

   /*
      Remove a card from the group if it is in the group.
      Return true if found, false if not.
    */
   public boolean removeCard(Card givenCard)
   {
      if (cardsInGroup.contains(givenCard))
      {
         cardsInGroup.remove(givenCard);
         return true;
      }
      return false;
   }

   public void setCard(int j, Card newValue)
   {
      if (j >= 0 && j < cardsInGroup.size())
         cardsInGroup.set(j, newValue);
   }

   public void setName(String newName) { groupName = newName; }

   public void setAgent(CardAgent newAgent) { groupAgent = newAgent; }

   public int size() { return cardsInGroup.size(); }

   public void shuffle()
   {
      Random generator = new Random();
      int nCards = this.size();
      int n = nCards;
      Card selectedCard = null;
      // Work from back to front of list
      while (n > 1)
      {
         // Select random position from n remaining positions:
         int positionSelected = generator.nextInt(n);
         --n;
         if (positionSelected < n) // not current last position
         {  // Swap card at positionSelected with card at n
            selectedCard = cardsInGroup.get(positionSelected);
            cardsInGroup.set(positionSelected, cardsInGroup.get(n));
            cardsInGroup.set(n, selectedCard);
         }  // otherwise the current last position was selected;
            // so no swap is required.
      }
   }

   ArrayList<Card> cardsInGroup = null;
   private String groupName = null;
   private CardAgent groupAgent = null;

} // end class CardGroup

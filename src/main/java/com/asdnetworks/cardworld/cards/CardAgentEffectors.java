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

import java.awt.*;  // for Point
import java.util.*;  // for ArrayList

/*
   CardAgent2Effectors defines operations that a CardAgent2
   can perform on Card and CardPile instances on a CardTable.
*/
public class CardAgentEffectors
{
   CardAgentEffectors(CardAgent a, CardTable t)
   {
      agent = a;
      table = t;
   }

   public void addCard(Card c, int xPos, int yPos)
   {  table.addCard(c, xPos, yPos);
      c.repaint();
   }

/* Unlike stackUpPileAt, this doesn't offset the cards to give a 3-D
   shadowing effect:
   public void moveCardsInPileTo(CardPile givenPile, Point givenPoint)
   {
      Card c;
      if (givenPile == null || givenPile.size() == 0)
         return;
      int newX = (int) givenPoint.getX();
      int newY = (int) givenPoint.getY();
      Iterator it = givenPile.getIterator();
      while(it.hasNext())
      {
          c = (Card) it.next();
          CardTable table = c.getContext();
          table.removeCard(c);
          table.addCard(c, newX, newY);
          c.setLocation(newX, newY);
      }
      agent.cardsMoved(true);
   }
*/
   public void movePileContaining(Card givenCard, int deltaX, int deltaY)
   {
      CardPile pile = table.whichPileHasCard(givenCard);
      if (pile == null) return;
      Card c;
      Iterator it = pile.getIterator();
      while(it.hasNext())
      {  c = (Card) it.next();
         int newX = c.getX() + deltaX;
         int newY = c.getY() + deltaY;
         CardTable table = c.getContext();
         // Cards must be removed from table and added back to it so they will
         // appear on top of any pile(s) they may be moved onto:
         table.removeCard(c);
         table.addCard(c, newX, newY);
         c.setLocation(newX, newY);
            // inherited from java.awt.Component
   //      c.revalidate();   // apparently not needed
   //      c.repaint();      // apparently not needed
      }
      agent.cardsMoved(true);
   }

   public CardPile shufflePile(CardPile p)
   {
      p.shuffle();
      return p;
   }

   public ArrayList<CardPile> spreadOutAllPiles()
   {
      ArrayList<CardPile> allPiles = table.getPiles();
      Iterator it = allPiles.iterator();
      while (it.hasNext())
         ((CardPile) it.next()).spreadOut(0.2);
      return allPiles;
   }

   public CardPile spreadOutPile(CardPile pile)
   {
      Point home = pile.getHomePosition();
      return spreadOutPileAt(pile, home);
   }

   public CardPile spreadOutPileAt(CardPile pile, Point p)
   {
      if (pile != null)
         pile.spreadOutAt(0.2, p);
      return pile;
   }

   public CardPile spreadOutPileContaining(Card c)
   {
      return spreadOutPile(table.whichPileHasCard(c));
   }

   public CardPile spreadOutPileContainingAt(Card c, Point p)
   {
      return spreadOutPileAt(table.whichPileHasCard(c), p);
   }

   public ArrayList<CardPile> stackUpAllPiles()
   {
      ArrayList<CardPile> allPiles = table.getPiles();
      Iterator it = allPiles.iterator();
      while (it.hasNext())
         ((CardPile) it.next()).stackUp();
      return allPiles;
   }

   public CardPile stackUpPile(CardPile pile)
   {
      Point home = pile.getHomePosition();
      return stackUpPileAt(pile, home);
   }

   public CardPile stackUpPileAt(CardPile pile, Point p)
   {
      if (pile != null)
         pile.stackUpAt(p);
      return pile;
   }

   public CardPile stackUpPileContaining(Card c)
   {
      return stackUpPile(table.whichPileHasCard(c));
   }

   public CardPile stackUpPileContainingAt(Card c, Point p)
   {
      return stackUpPileAt(table.whichPileHasCard(c), p);
   }

   public void turnCard(Card card, String direction)
   {
      if (direction == null)
         card.turnOver();
      else if (direction.equals("down"))
         card.turnDown();
      else if (direction.equals("over"))
         card.turnOver();
      else if (direction.equals("up"))
         card.turnUp();
   }

   public void turnCard(Card card, String direction, Point p)
   {
      if (p != null)
      {
         turnCard(card, direction);
         table.moveCard(card, p);
      }
   }

   public void turnEachCardInPile(CardPile pile, String direction)
   {
      if (direction == null)
         return;
      if (direction.equals("down"))
         pile.turnDownEach();
      else if (direction.equals("up"))
         pile.turnUpEach();
      else if (direction.equals("over"))
         pile.turnOverEach();
   }

   public ArrayList<CardPile> turnPile(CardPile pile, String direction)
   {
      ArrayList<CardPile> result = new ArrayList<CardPile>(1);
      if (direction == null)
      {
         pile.turnOver();
         result.add(pile);
      }
      else if (direction.equals("down"))
         result = this.turnDownEachInPile(pile);
      else if (direction.equals("up"))
         result = this.turnUpEachInPile(pile);
      else if (direction.equals("over"))
      {
         pile.turnOver();
         result.add(pile);
      }
      return result;
   }

   public ArrayList<CardPile> turnDownEachInPile(CardPile pile)
   {
      ArrayList<CardPile> result = new ArrayList<CardPile>(1);
      if (pile != null)
      {  pile.turnDownEach();
         result.add(pile);
      }
      return result;
   }

   public ArrayList<CardPile> turnDownEachInPileContaining(Card c)
   {
      return turnDownEachInPile(table.whichPileHasCard(c));
   }

   public ArrayList<CardPile> turnOverEachInPile(CardPile pile)
   {
      ArrayList<CardPile> result = new ArrayList<CardPile>(1);
      if (pile != null)
      {  pile.turnOverEach();
         result.add(pile);
      }
      return result;
   }

   public ArrayList<CardPile> turnOverEachInPileContaining(Card c)
   {
      return turnOverEachInPile(table.whichPileHasCard(c));
   }

   public ArrayList<CardPile> turnOverPile(CardPile pile)
   {
      ArrayList<CardPile> result = new ArrayList<CardPile>(1);
      if (pile != null)
      {  pile.turnOver();
         result.add(pile);
      }
      return result;
   }

   public ArrayList<CardPile> turnOverPileContaining(Card c)
   {
      return turnOverPile(table.whichPileHasCard(c));
   }

   public ArrayList<CardPile> turnUpEachInPile(CardPile pile)
   {
      ArrayList<CardPile> result = new ArrayList<CardPile>(1);
      if (pile != null)
      {
         pile.turnUpEach();
         result.add(pile);
      }
      return result;
   }

   public ArrayList<CardPile> turnUpEachInPileContaining(Card c)
   {
      return turnUpEachInPile(table.whichPileHasCard(c));
   }

   private CardAgent agent = null;
   private CardTable table = null;

} 
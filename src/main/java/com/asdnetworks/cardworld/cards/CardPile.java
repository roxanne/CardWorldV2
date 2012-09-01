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

import java.util.*;  // for ArrayList etc.

import java.awt.*; // for Point

/*
   CardPile instances represents piles of cards on a CardTable,
   with methods for stacking, spreading, and moving the cards around
   on the table.  A CardPile also has a position on the CardTable.
 */
public class CardPile extends CardGroup
{
   CardPile()
   {
      super();
   }

   CardPile(Point p)
   {
      super();
      setHomePosition(p);
   }

   CardPile(int capacity)
   {
      super(capacity);
   }

   CardPile(int capacity, Point p)
   {
      super(capacity);
      setHomePosition(p);
   }

   public CardTable getCardTable() { return table; }

   public Point getHomePosition() { return pileHome; }

   public boolean isStacked()
      // returns true if all of the cards in the pile are stacked
   {
      boolean pileIsStacked = true;
      Iterator it = cardsInGroup.iterator();
      while (pileIsStacked && it.hasNext())
         pileIsStacked = pileIsStacked &&
            ((Card)it.next()).isStacked();

      return pileIsStacked;
   }

   /**
      Partition the CardPile (which may no longer by transitively overlapping)
      into equivalence classes of Cards that form overlapping sub-piles.
      and return an ArrayList of those sub-piles.  If this CardPile is not on a
      CardTable, return null.

    */
   public ArrayList<CardPile> partition()
   {
      if (table == null)
         return null;
      if (size() == 0)
         return new ArrayList<CardPile> (0);

      // Create a list of the rectangular bounds of all the cards
      // in this CardPile:
      ArrayList<Rectangle> cardBounds = new ArrayList<Rectangle> (size());
      Iterator it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         cardBounds.add(thisCard.getBounds());
      }

      // As the overlap relationships between card bounds are explored,
      // each developing part of the partition will have a "leader" card
      // which temporarily represents its part of the partition.  Other cards
      // in the same part of the partition will point, directly or indirectly,
      // to the leader of that part of the partition.  When a new card is found
      // to overlap one or more previous cards with different leaders, the parts
      // of the partition to which those cards belong will be merged by making their
      // leaders (or themselves, if they are leaders) point to the new card, which.
      // becomes a leader.
      // Links from cards to leaders can be represented by integers in an array
      // whose indices correspond to positions of cards in the cardsInGroup
      // ArrayList inherited from class CardGroup.  Those links will always be
      // indices higher in the leaderPosition array.

      int[] leaderPosition = new int[size()];
      for (int p = 0; p < size(); ++p)
         leaderPosition[p] = -1; // represents a null pointer

      // Go over all of the card bounds and check for overlaps:
      for (int curr = 1; curr < size(); ++curr)
      {
         Rectangle current = cardBounds.get(curr);

         boolean firstOverlap = true;
         for (int prev = curr-1; prev >= 0; --prev)
         {
            Rectangle previous = cardBounds.get(prev);
            if (current.intersects(previous))
            {
               // Find the leader for the previous card that the current one overlaps:
               int leader = prev;
               while (leaderPosition[leader] != -1)
                  leader = leaderPosition[leader];

//               if (leaderPosition[curr] == -1) // first overlap found
               if (firstOverlap)
               {
                  // Set the current card's leader to the one found
//                  leaderPosition[curr] = leader;
                  // Set that leader's leader to the current card
                  leaderPosition[leader] = curr;
                  firstOverlap = false;
               }
               else // another overlap has been found
               {
//                  if (leader != leaderPosition[curr]) // the new leader is in a different
                  if (leader != curr)
                      // part of the partition
                     // Make the current card's leader be the new leader for that old leader
                     // (which no longer will be a leader).
                     // Note: this can make the leaderPosition links indirect.
                     leaderPosition[leader] = curr;
               }

            }

         }

      }

      CardPile[] newCardPile = new CardPile[size()]; // positions correspond to
         // those in the leaderPosition array

      // Create a new CardPile for each part of the partition, corresponding to
      // each remaining leader card position:

      int numberOfPiles = 0;
      // Must go from highest to lowest index, because leaderPosition values are
      // of indices higher in the array, and the corresponding CardPiles must be
      // created before cards can be added to them.
      for (int p = size() - 1; p >= 0; --p)
      {
         Card thisCard = getCard(p);  // get the corresponding card
         if (leaderPosition[p] == -1) // the card is still a leader
         {
            // Establish a CardPile for a new part of the partition.
//            newCardPile[p] = new CardPile(thisCard.getLocation());
            newCardPile[p] = new CardPile();
            newCardPile[p].setCardTable(this.getCardTable());
            newCardPile[p].addCard(thisCard);
            ++numberOfPiles;
         }
         else // this card is not a leader
         {  // There will be no new pile for this card:
            newCardPile[p] = null;
            // Find the leader for the current card and add the card to
            // that leader's part of the partition:
            int leader = leaderPosition[p];
            while (leaderPosition[leader] != -1)
               leader = leaderPosition[leader];
            newCardPile[leader].addCard(0, thisCard); // add at the front, because
               // it was earlier in the CardPile being partitioned.
         }
      }

      // Put the new CardPiles into an ArrayList to return, and set their home positions:
      ArrayList<CardPile> result = new ArrayList<CardPile> (numberOfPiles);
      for (int p = 0; p < size(); ++p)
         if (newCardPile[p] != null)
         {
            CardPile pile = newCardPile[p];
            result.add(pile);
            pile.setHomePosition(pile.getCard(0).getLocation());
         }

      return result;
   } // end partition

   public void setCardTable(CardTable newTable)
   {
      table = newTable;
   }

   public void setHomePosition(Point newPosition)
   {
      pileHome = newPosition;
   }

   public void setHomePosition(int newX, int newY)
   {
      pileHome = new Point(newX, newY);
   }


   public void shuffle()
   {
      super.shuffle();
      Iterator it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         if (thisCard.getContext() == table)
         {
            table.removeCard(thisCard);
            table.addCard(thisCard,
               (int) thisCard.getX(), (int) thisCard.getY());
         }

      }
//      this.stackUp();  // leave it arranged as it was;
      // so the user must specify changes in arrangement explicitly
   }

   /*
      Spread out the pile sideways, with distance between edges
      the given multiple of the card width.  The cards will remain
      overlapping if relativeWidth is < 1.
    */
   public boolean spreadOut(double relativeWidth)
   {
      if (table == null || size() == 0)
         return false;  // not on a CardTable, or empty pile
      if (pileHome == null)
         setHomePosition(getCard(size() - 1).getLocation());
      int deltaX = (int) (relativeWidth * (double) getCard(0).getWidth() + 0.5);
      int xPos = (int) pileHome.getX();
      int yPos = (int) pileHome.getY();

      Iterator it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         if (thisCard.getContext() == table)
            table.removeCard(thisCard);
         thisCard.setStacked(false);
      }

      it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         table.addCard(thisCard, xPos, yPos);
         xPos += deltaX;
      }

      return true;
   } // end spreadOut

   public boolean spreadOutAt(double relativeWidth, Point newHome)
   {
      Point oldHome = pileHome;
      pileHome = newHome;
      if (this.spreadOut(relativeWidth))
         return true;
      pileHome = oldHome; // revert to former position
      return false;
   } // end spreadOutAt

   public boolean stackUp()
   {
      if (table == null || size() == 0)
         return false;  // not on a CardTable, or empty pile
      if (pileHome == null)
         setHomePosition(getCard(0).getLocation());
//         setHomePosition(getCard(size() - 1).getLocation());
      double xPos = pileHome.getX();
      double yPos = pileHome.getY();

      Iterator it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         if (thisCard.getContext() == table)
            table.removeCard(thisCard);
         thisCard.setStacked(true);
      }

      double offset = 0.0;

      it = super.getIterator();
      while (it.hasNext())
      {
         table.addCard((Card) it.next(), (int) (xPos - offset), (int) (yPos - offset));
         offset += pixelOffset;
      }
      getCard(size()-1).repaint();  // needed to prevent only partial repaint
         // in some cases -- e.g. when a pile stacked face up is shuffled

      return true;
   } // end stackUp

   public boolean stackUpAt(Point newHome)
   {
      Point oldHome = pileHome;
      pileHome = newHome;
      if (this.stackUp())
         return true;
      pileHome = oldHome; // revert to former position
      return false;
   } // end stackUpAt

   public boolean stackUpAt(int x, int y)
   {
      return this.stackUpAt(new Point(x, y));
   }

   public boolean turnDownEach()
   {
      if (table == null || size() == 0)
         return false;  // not on a CardTable, or empty pile
      Iterator it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         thisCard.turnDown();
      }
      return true;
   }

   public void turnOver()
   {
      if (table == null || size() == 0)
         return;  // not on a CardTable, or empty pile
      ArrayList<Card> queue = new ArrayList<Card>(this.size());
      Card c = null;  // temporary holder
      // Remove cards in the pile from the table, from top to bottom
      // and put them in the queue:

      boolean isStacked = true;

      while (this.size() > 0)
      {
         c = cardsInGroup.remove(this.size() - 1);
         table.removeCard(c);
         queue.add(c);
         isStacked = isStacked && c.isStacked();
      }

      // Put the cards in the pile back on the table in reverse order,
      // turning each one over:
      Iterator it = queue.iterator();
      while (it.hasNext())
      {
         c = (Card) it.next();
         c.turnOver();
         table.addCard(c, c.getX(), c.getY());
         cardsInGroup.add(c);
      }
      if (isStacked) // restore correct shadowing
         stackUp();
   }

/* Old version; doesn't correctly reverse the order
   of cards displayed:
   public void turnOver()
   {
      if (table == null || size() == 0)
         return;  // not on a CardTable, or empty pile
      int lowPos = 0;
      int highPos = size() - 1;
      while(lowPos < highPos)
      {
         Card temp1 = getCard(lowPos);
         Card temp2 = getCard(highPos);
         temp1.turnOver();
         temp2.turnOver();
         setCard(lowPos, temp2);
         setCard(highPos, temp1);
         ++lowPos;
         --highPos;
      }
      if (lowPos == highPos)
         getCard(lowPos).turnOver();
//      stackUp();
   }
 */

   public boolean turnOverEach()
   {
      if (table == null || size() == 0)
         return false;  // not on a CardTable, or empty pile
      Iterator it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         thisCard.turnOver();
      }
      return true;
   }

   public boolean turnUpEach()
   {
      if (table == null || size() == 0)
         return false;  // not on a CardTable, or empty pile
      Iterator it = super.getIterator();
      while (it.hasNext())
      {
         Card thisCard = (Card) it.next();
         thisCard.turnUp();
      }
      return true;
   }

   private CardTable table = null;
   private Point pileHome = null;
   private final double pixelOffset = 0.2;

} // end class CardPile

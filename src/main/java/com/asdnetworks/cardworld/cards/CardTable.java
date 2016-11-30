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

import java.io.*;  // for debugging

import java.util.*;        // HashSet, Iterator
import java.awt.*;         // Graphics, Color
import javax.swing.*;
import java.awt.event.*; // ActionEvent, ActionListener,
import java.lang.reflect.*; // for various exceptions
import javax.swing.event.*;  // ListSelectionListener

/*
   A CardTable instance represents a card table top
   on which PlayingCard and CardPile instances can be displayed.
 */
class CardTable extends JPanel
   implements MouseListener
{
   /**
      Constructs an empty CardTable with a green background.
    */
   public CardTable()
   {
      backgroundColor = Color.green;
      setBackground(backgroundColor);
      setLayout(null);  // instead of the default FlowLayout
      setDoubleBuffered(false); // attempt to solve "phantom card" problem
      displayedCards = new ArrayList<Card>();
      addMouseListener((MouseListener) this);
   }

   public void addCard(Card card, int x, int y)
   {  card.setAgent(agent);
      add(card, 0);  // inherited function
      card.setContext(this);
      card.setLocation(x, y);
      displayedCards.add(card);
      agent.cardsMoved(true);
//      card.revalidate();  // apparently not needed
      card.repaint();;    // needed e.g. if card has been turned over
   }

   void clear()
   {  removeAll();
      repaint();
      displayedCards = new ArrayList<Card>();
      this.revalidate();
      this.repaint();
//      if (window != null)
//         window.validate();
   }

   public CardPile findOnlyMultiCardPile()
   {
      findPiles();
      CardPile lastMultiCardPile = null;
      int n = 0;
      Iterator it = currentPiles.iterator();
      while (it.hasNext())
      {
         CardPile pile = (CardPile) it.next();
         if ( pile.size() > 1 )
         {  ++n;
            lastMultiCardPile = pile;
         }
      }
      if (n == 1)
         return lastMultiCardPile;
      else
         return null;
   }

   public PlayingCard findOnlySingletonCard()
   {
      findPiles();
      CardPile lastSingletonCardPile = null;
      int n = 0;
      Iterator it = currentPiles.iterator();
      while (it.hasNext())
      {
         CardPile pile = (CardPile) it.next();
         if ( pile.size() == 1 )
         {  ++n;
            lastSingletonCardPile = pile;
         }
      }
      if (n == 1)
         return (PlayingCard) lastSingletonCardPile.getCard(0);
      else
         return null;
   }

   /**
      Update the list of currentPiles displayed on the table.
    */
   public void findPiles()
   {
      CardPile allCards = new CardPile(displayedCards.size());
      allCards.setCardTable(this);
      Iterator it = displayedCards.iterator();
      while (it.hasNext())
      {
         PlayingCard card = (PlayingCard) it.next();
         allCards.addCard(card);
      }
      currentPiles = allCards.partition();
   }

   public PlayingCard getTopCardAt(Point position)
   {
      Component comp = this.getComponentAt(position);
      if (comp instanceof PlayingCard)
         return (PlayingCard) comp;
      return null;
   }

   public ArrayList<PlayingCard> getCards()
   {
      /* This old code is rejected as unsafe by Java 1.7:
      Object cloned = displayedCards.clone();
      if (cloned instanceof ArrayList)
         return (ArrayList<Card>) cloned;
      return null;
      */
      if (displayedCards == null)
         return null;
      ArrayList<PlayingCard> result = new ArrayList<PlayingCard>(displayedCards.size());
      for (int pos = 0; pos < displayedCards.size(); pos++)
         result.add((PlayingCard) displayedCards.get(pos));
      return result;
   }

   public CardPile getPileAt(Point position)
   {
      PlayingCard c = getTopCardAt(position);
      if (c == null)
         return null;
      return this.whichPileHasCard(c);
   }

   public ArrayList<CardPile> getPiles()
   {
      /* This old code is rejected as unsafe by Java 1.7:
      findPiles();
      Object cloned = currentPiles.clone();
      if (cloned instanceof ArrayList)
         return (ArrayList<CardPile>) cloned;
      return null;
      */
      if (currentPiles == null)
         return null;
      ArrayList<CardPile> result
         = new ArrayList<CardPile>(currentPiles.size());
      for (int pos = 0; pos < currentPiles.size(); pos++)
         result.add(currentPiles.get(pos));
      return result;
   }

   public CardAgent getAgent() { return agent; }

   boolean isDisplaying(PlayingCard card)
   {
      return displayedCards.contains(card);
   }

   /**
      This function is ESSENTIAL to prevent overlapping card images to be
      displayed correctly.  Without it, in some cases the lowest card in a
      stack or spread-out pile is displayed on top of others, even though
      mouse clicks don't find it there (what I refer to elsewhere as the
      "phantom card" problem).
    */
   public boolean isOptimizedDrawingEnabled()
   {
      return false; // override inherited default
   }

   public boolean isCardInAMultiCardPile(PlayingCard card)
   {
      return this.whichPileHasCard(card).size() > 1;
   }

   public void mouseClicked(MouseEvent e)
   {  // needed for MouseListener interface
   }

   public void mouseEntered(MouseEvent e)
   {  // needed for MouseListener interface
   }

   public void mouseExited(MouseEvent e)
   {  // needed for MouseListener interface
   }

   public void mousePressed(MouseEvent e)
   {  // needed for MouseListener interface
      agent.pointIndicated(e.getPoint());
   }

   public void mouseReleased(MouseEvent e)
   {  // needed for MouseListener interface
   }

   public void moveCard(Card c, MouseEvent e)
   {
      int newX = c.getX() + e.getX();
      int newY = c.getY() + e.getY();
      // Card must be removed from table and added back to it so it will appear at
      // the top of any pile it may be moved onto:
      this.removeCard(c);
      this.addCard(c, newX, newY);
      c.setLocation(newX, newY);
//      agent.cardIndicated(c); // not needed; done by mousePressed
         // inherited from java.awt.Component
//      c.revalidate();   // apparently not needed
//      c.repaint();      // apparently not needed
   }

   public void moveCard(Card c, Point p)
   {
      int newX = (int) p.getX();
      int newY = (int) p.getY();
      this.removeCard(c);
      this.addCard(c, newX, newY);
      c.setLocation(newX, newY);
   }

   public void movePileContaining(Card givenCard, int deltaX, int deltaY)
   {
      CardPile pile = this.whichPileHas((PlayingCard) givenCard);
// The following alternative makes the piles "sticky" as they move:
//      CardPile pile = this.whichPileHasCard((PlayingCard) givenCard);
      if (pile == null) return;
      Card c;
      Iterator it = pile.getIterator();
      while(it.hasNext())
      {  c = (Card) it.next();
         int newX = c.getX() + deltaX;
         int newY = c.getY() + deltaY;
         // Cards must be removed from table and added back to it so they will
         // appear on top of any pile(s) they may be moved onto:
         this.removeCard(c);
         this.addCard(c, newX, newY);
         c.setLocation(newX, newY);
            // inherited from java.awt.Component
   //      c.revalidate();   // apparently not needed
   //      c.repaint();      // apparently not needed
      }
      agent.cardsMoved(true);
   }

   public int numberOfCards()
   {
      return displayedCards.size();
   }

   public int numberOfPiles()
   {
      return numberOfPiles(true);
   }

   /**
      false argument provides number of piles without re-partitioning,
      to avoid redundant partitioning when possible.
    */
   public int numberOfPiles(boolean findBeforeCount)
   {
      if (findBeforeCount)
         findPiles();
      return currentPiles.size();
   }

/*
   void print()
   {  PrintJob job
         = Toolkit.getDefaultToolkit().getPrintJob(agent.getWindow(),
            "Card Table", null);
      if (job != null)
      {  Graphics g = job.getGraphics();
         if (g != null)
         {  setBackground(Color.white);
            paintComponent(g);
            print(g);
            g.dispose();
         }
         job.end();
         setBackground(Color.green);
      }
   }
*/

   public void removeCard(Card card)
   {
      if (card == null) return;
      remove(card);
      displayedCards.remove(card); // inherited function
      card.setContext(null);
      agent.cardsMoved(true);
//      this.revalidate();   // apparently not needed
      this.repaint();      // Is this needed?
   }

   public void setAgent(CardAgent a) { agent = a; }

   void setWindow(CardWorldFrame win) { window = win; }

   void toggleBackgroundColor()
   {  if (backgroundColor == Color.green)
         backgroundColor = Color.white;
      else
         backgroundColor = Color.green;
      setBackground(backgroundColor);
   }

//   public CardPile whichPileHasCard(PlayingCard card)
   public CardPile whichPileHasCard(Card card)
   {
      findPiles();  // find the current non-overlapping piles
      return whichPileHas(card);
   }

//   public CardPile whichPileHas(PlayingCard card)
   public CardPile whichPileHas(Card card)
   // This has been made public in CardWorld 2, so CardAgent2
   // can call it without repeating the call to findPiles().
   {
      if (currentPiles == null || currentPiles.size() == 0)
         return null;
      Iterator it = currentPiles.iterator();
      while (it.hasNext())
      {
         CardPile pile = (CardPile) it.next();
         if (pile.containsCard(card))
            return pile;
      }
      return null;  // no pile contains the card
   }

   private CardAgent agent;
   private CardWorldFrame window = null;
   private ArrayList<Card> displayedCards; // the Cards
                                   // currently displayed on the CardTable
   private ArrayList<CardPile> currentPiles = null;

   Color backgroundColor;  // current color of the panel background
} // end class CardTable

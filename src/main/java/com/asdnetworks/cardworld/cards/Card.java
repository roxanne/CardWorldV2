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

import java.util.*;  // ArrayList, Iterator
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;  // ChangeListener

/*
   Card instances represent rectangular cards with two sides.
 */
public class Card extends JButton
   // needs to extend JToggleButton rather than just JButton
   // only in case Card instances are to be put in a ButtonGroup
   implements MouseListener, MouseMotionListener, ChangeListener
{
   Card(String frontFileName, String backFileName)
   {
      super();
//      setOpaque(true);  // apparently not needed
      // Java Web Start
       ClassLoader cldr = this.getClass().getClassLoader();
        
        URL frontImageurl=  Thread.currentThread().getContextClassLoader().getResource(frontFileName);
       frontImage  = new ImageIcon(frontImageurl);

        backImage = new ImageIcon(//backFileName);
        getClass().getClassLoader().getResource("back.png"));
       
//      frontImage = new ImageIcon(frontFileName);
     // backImage = new ImageIcon(backFileName);
//      setBorder(BorderFactory.createLineBorder(Color.black, 1));
//      setBorder(null);
      setIcon(frontImage);
      setSize(frontImage.getIconWidth(), frontImage.getIconHeight());
      frontShowing = 1;
//      setRolloverEnabled(false);  // prevents card image borders
           // from being highlighted as mouse rolls over them;
           // that may be desired for "realism".
      addMouseListener((MouseListener) this);
//      addChangeListener((ChangeListener) this);
      addMouseMotionListener((MouseMotionListener) this);
  }

   Card(String frontFileName, String backFileName,
         int xPos, int yPos)
   {  this(frontFileName, backFileName);
      setLocation(xPos, yPos);
   }

   Card(String frontFileName, String backFileName,
         short xPos, short yPos)
   {  this(frontFileName, backFileName);
      setLocation(xPos, yPos);
   }

   public String getCardName() { return cardName; }

   public CardTable getContext() { return table; }

   public CardAgent getAgent() { return agent; }

   public boolean isFrontShowing()
   {
      if (frontShowing == 1)
         return true;
      else
         return false;
   }

   public boolean isStacked()
   {
      return stacked;
   }

   public void mouseDragged(MouseEvent e)
   {  // normal drag
      if (e.isShiftDown())
         table.movePileContaining(this, e.getX(), e.getY());
      else
      {
         table.moveCard(this, e);
         this.setStacked(false);
      }
   }

   public void mouseMoved(MouseEvent e)
   {  // needed for MouseMotionListener interface
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
      if (e.isControlDown())
         this.turnOver();
      table.findPiles(); // for possible start of pile drag
      agent.cardIndicated(this);
   }

   public void mouseReleased(MouseEvent e)
   {  // needed for MouseListener interface
      table.findPiles(); // for end of pile drag
   }

   public boolean overlaps(Card otherCard)
   {
      return getBounds().intersects(otherCard.getBounds());
   }

   public void setCardName(String newName)
   {
      cardName = newName;
   }

   public void setContext(CardTable tabl) { table = tabl; }

   public void setAgent(CardAgent agnt)
   {  agent = agnt;
   }

   public void setStacked(boolean value)
   {
      stacked = value;
   }

   public void stateChanged(ChangeEvent e)
      // required by interface ChangeListener
   {
      if (this.isSelected() && this.getModel().isPressed())
         // the second conjunct is needed to avoid selection just by
         // rollover
         this.setSelected(true);
   }

   public void turnDown()
   {
      frontShowing = 0;
      setIcon(backImage);
//      repaint();  // apparently not needed
   }

   public void turnOver()
   {
      frontShowing = 1 - frontShowing;
      if (frontShowing == 1)
      {
         setIcon(frontImage);
      }
      else // back is showing
      {
         setIcon(backImage);
      }
//     repaint();  // apparently not needed
   }

   public void turnUp()
   {
      frontShowing = 1;
      setIcon(frontImage);
//      repaint();  // apparently not needed
   }

 //  private final int VERTICAL_PAD = 10;
   private String cardName = null;
   private ImageIcon frontImage;
   private ImageIcon backImage;
   private int frontShowing = 1; // 1 means true, 0 means false
   private boolean stacked = false;
      // indicates whether card is in a stacked pile;
      // this is needed so that if a pile is turned over and
      // contains only stacked cards, it is re-stacked to show
      // its shadowing (3D view) correctly.
   private CardTable table;
   private CardAgent agent;

} // end class Card

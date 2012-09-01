package com.asdnetworks.cardworld.cards;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class PopupListener extends MouseAdapter
{  PopupListener(JPopupMenu m)
   {  menu = m;
   }

   public void mousePressed(MouseEvent e)
   {  maybeShowPopup(e);
   }

   public void mouseReleased(MouseEvent e)
   {  maybeShowPopup(e);
   }

   private void maybeShowPopup(MouseEvent e)
   {  if (e.isPopupTrigger())
        menu.show(e.getComponent(), e.getX(), e.getY());
   }

   private JPopupMenu menu;
}
/*

Copyright 2012 James A. Mason, York University, Toronto, Canada
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
   AbstractPlayingCardIdea instances represent partial information about
   standard playing cards for a deck of 52 plus possible Jokers.
   They have suits, ranks, colors, etc. but lack visual images including backs
   and lack locations in space or time.
 */
public class AbstractPlayingCardIdea
{
   AbstractPlayingCardIdea()
   {
   }

   boolean checkColorSuitConsistency()
   {
      if (colorName == null || suitName == null)
         return true;
      if (colorName.equals("black")
               && (suitName.equals("Diamonds")
                   || suitName.equals("Hearts"))
             ||
          colorName.equals("red")
               && (suitName.equals("Clubs")
                   || suitName.equals("Spades")) )
          return false;;
      return true;
   }

   boolean checkRankStandingConsistency()
   {
      if (rankName == null || standingName == null)
         return true;
      if (standingName.equals("Joker") &&
            (rankName == null || rankName.equals("Joker") ))
         return true;
      if (standingName.equals("face") &&
             (rankName.equals("Jack") || rankName.equals("Queen")
              || rankName.equals("King") ))
         return true;
      if (standingName.equals("spot") &&
             (rankName.equals("Jack") || rankName.equals("Queen")
              || rankName.equals("King") || rankName.equals("Joker") ))
         return false;
      return true;
   }

   AbstractPlayingCardIdea copy()
   {
      AbstractPlayingCardIdea result = new AbstractPlayingCardIdea();
      if ( result != null )
      {
         result.setColorName(this.getColorName());
         result.setRankName(this.getRankName());
         result.setSuitName(this.getSuitName());
         result.setStandingName(this.getStandingName());
      }
      return result;
   }

   public String getColorName() { return colorName; }
   public String getRankName() { return rankName; }
   public String getSuitName() { return suitName; }
   public String getStandingName() { return standingName; }

   public boolean isConsistent() { return consistent; }

   boolean isConsistentWithAnyAbstractCard()
   {
      // return true if the AbstractPlayingCardIdea is vacuously
      // consistent with any AbstractPlayingCard
      return colorName == null && rankName == null && standingName == null
             && suitName == null;
   }

   boolean isConsistentWith(AbstractPlayingCard card)
   {
      // return true if the AbstractPlayingCardIdea is consistent with
      // the given AbstractPlayingCard
      if (standingName != null && standingName.equals("Joker")
          && !(card.getSuitName()).equals("") )
         return false;
      if (standingName != null && standingName.equals("face")
          && ! ( card.getRankName().equals("Jack")
                 || card.getRankName().equals("Queen")
                 || card.getRankName().equals("King") ))
         return false;
      if (standingName != null && standingName.equals("spot")
          && ( card.getRankName().equals("Jack")
               || card.getRankName().equals("Queen")
               || card.getRankName().equals("King")
               || card.getColorName().equals("") )) // Joker
         return false;
      if (colorName != null && ! (colorName.equals(card.getColorName() ) ))
         return false;
      if (rankName != null && ! (rankName.equals(card.getRankName() ) ))
         return false;
      if (suitName != null && ! (suitName.equals(card.getSuitName() ) ))
         return false;
      return true;  // all tests passed
   }

   boolean isConsistentWith(PlayingCard card)
   {
      return this.isConsistentWith(card.getAbstractCard());
   }

   boolean isFullySpecified()
   {
      // return true if the idea represents a single card type
      // (e.g. Ace of Spades) possibly Joker
      if ( ! consistent || standingName == null)
         return false;
      if (standingName.equals("Joker"))
         return true;
      if (rankName == null || suitName == null)
         return false;
      return true;
   }

   AbstractPlayingCardIdea mergeWith(AbstractPlayingCardIdea other)
   {
      if (other == null) return null;
      AbstractPlayingCardIdea result = other.copy();
      String otherColor = other.getColorName();
      String otherStanding = other.getStandingName();
      String otherRank = other.getRankName();
      String otherSuit = other.getSuitName();
      if (otherColor == null)
         result.setColorName(colorName);
      else if (colorName != null && ! colorName.equals(otherColor) )
         return null; // incompatible color names
      if (otherStanding == null)
         result.setStandingName(standingName);
      else if (standingName != null && ! standingName.equals(otherStanding) )
         return null; // incompatible standing names
      if (otherRank == null)
         result.setRankName(rankName);
      else if (rankName != null && ! rankName.equals(otherRank) )
         return null; // incompatibl3e rank names
      if (otherSuit == null)
         result.setSuitName(suitName);
      else if (suitName != null && ! suitName.equals(otherSuit) )
         return null; // incompatible suit names
      if ( result.isConsistent() )
         return result;
      return null;
   }

   void setColorName(String color)
   {
      colorName = color;
      if (suitName == null) return;
      consistent = consistent && checkColorSuitConsistency();
   }

   boolean setFeature(String feature, String featureValue)
   {
      if (feature == null)
         return consistent;
      if (feature.equals("color"))
         setColorName(featureValue);
      else if (feature.equals("rank"))
         setRankName(featureValue);
      else if (feature.equals("suit"))
         setSuitName(featureValue);
      else if (feature.equals("standing"))
         setStandingName(featureValue);
      return consistent;
   }

   void setRankName(String rank)
   {
      if (rank == null)
         return;
      if (rankName == null)
      {
         rankName = rank;
         if (standingName == null)
         {
            if (rankName.equals("Joker"))
               standingName = "Joker";
            else if (rankName.equals("Jack") ||
                     rankName.equals("Queen") ||
                     rankName.equals("King") )
               standingName = "face";
            else
               standingName = "spot";
         }
         else
            consistent = consistent && checkRankStandingConsistency();
      }
      else if ( ! rank.equals(rankName) )
         consistent = false;
   }

   void setSuitName(String suit)
   {
      if (suit == null)
         return;
      if (suitName == null)
      {
         suitName = suit;
         if (colorName == null)
         {
            if (suitName.equals("Diamonds")
                || suitName.equals("Hearts"))
                colorName = "red";
            else if (suitName.equals("Clubs")
                || suitName.equals("Spades"))
                colorName = "black";
         }
         else
            consistent = consistent && checkColorSuitConsistency();
      }
      else if ( ! suit.equals(suitName) )
         consistent = false;
   }

   void setStandingName(String standing)
   {
      if (standing == null)
         return;
      if (standingName == null)
      {
         standingName = standing;
         consistent = consistent && checkRankStandingConsistency();
      }
      else if ( ! standing.equals(standingName) )
         consistent = false;
   }

   private String suitName = null;
   private String rankName = null;
   private String colorName = null; // "black", "red", or "" for Joker
   private String standingName = null;  // "face", "spot", or "Joker"
   private boolean consistent = true;

} // end class AbstractPlayingCardIdea


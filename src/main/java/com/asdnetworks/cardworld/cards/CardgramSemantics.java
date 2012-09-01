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

import com.asdnetworks.cardworld.asd.ASDParser;
import java.util.ArrayList;  // for debugging
import java.util.HashMap;

/*
   Cardgram1Semantics defines the operations of setting
   and testing values of semantic features that are specified
   in the semantic action and semantic value augmentations
   of nodes in the ASD grammar Cardgram1.grm .
*/
public class CardgramSemantics
{
   public CardgramSemantics()
   {
   }

   public void setParser(ASDParser p)
   {
      parser = p;
   }

// Following are functions corresponding to and invoked by grammar nodes
// in Cardgram2.grm, along with some helping functions:

   public HashMap features() { return (HashMap) parser.features(); }

   public Object nodeValue() { return parser.currentNode().value(); }

   public void raiseFeatures() { parser.raiseFeatures(); }

   public void raiseFeaturesChecking() { parser.raiseFeaturesChecking(); }

   public void set(String feature, Object value) { parser.set(feature, value); }

   public Object valueOf(String given) { return parser.valueOf(given); }

   public String cardgram_$$_3()
   {
      String command = (String) valueOf("command");
      String direction = (String) valueOf("direction");
      String where = (String) valueOf("where");
      if (command.equals("spread"))
      {
         if (direction != null && ! direction.equals("out"))
            return parser.NOADVANCE;
      }
      else if (command.equals("shuffle") || command.equals("stack"))
      {
         if ( ! (direction == null || direction.equals("up")) )
            return parser.NOADVANCE;
      }
// This check is left for the pragmatics.  e.g. the command could be
// put all the piles together, without pointing to a place:
//      else if (command.equals("put"))
//      {
//         if (where == null)
//            return parser.NOADVANCE;
//      }
      else if (command.equals("turn"))
      {
         if (direction == null)
         {  String orientation = (String) valueOf("orientation");
            if (orientation == null)
               set("direction", "over");
            else
               set("direction", orientation); // from "face down" or "face up"
         }
         else if ( ! (direction.equals("down") || direction.equals("over")
            || direction.equals("up") ) )
            return parser.NOADVANCE;
      }

      return null;
   }

   public Object cardgram_$$_4_v()
   {  // See comment on cardgram_MAINCLAUSE_1
//      return valueOf("clauses");
        ArrayList<HashMap> result = new ArrayList<HashMap>(1);
        result.add((HashMap) valueOf("clause1"));
        return result;
   }

   public String cardgram_ace_1()
   {  // this is also the action for cardgram_aces_1
      set("feature", "rank");
      set("value", "Ace");
      return null;
   }

   public String cardgram_ADJ_1()
   {
      raiseFeatures();
      Object feature = valueOf("feature");
/* debug output:
      if (feature == null)
         System.out.println("adjective feature not found");
      else
         System.out.println("adjective feature is " + feature);
*/
      set("adjFeature", feature);
      set("adjFeatureValue", valueOf("value"));
     return null;
   }

   public String cardgram_ADVDIR_1()
   {
      set("direction", nodeValue());
      return null;
   }

   public String cardgram_ADVDIR_2()
   {
      String direction = (String) valueOf("direction");
      if (direction != null &&
          ! direction.equals((String) nodeValue()))
         return parser.NOADVANCE;
      set("direction", nodeValue());
      return null;
   }

   public String cardgram_all_1()
   {
      set("number", "plural");
      set("quantity", "several");
      set("inclusivity", "all");
      return null;
   }

   public String cardgram_all_2()
   {
      set("inclusivity", "all");
      return null;
   }

   public String cardgram_als0_1()
   {
      set("additional", "also");
      return null;
   }

   public String cardgram_black_1()
   {
      set("feature", "color");
      set("value", "black");
      return null;
   }

   public String cardgram_card_1()
   {  // this is also the action for cardgram_cards_1
      set("feature", "structureType");
      set("value", "card");
      return null;
   }

   public Object cardgram_CARDINALNUMBER_1_v()
   {
      Long value = new Long((String) parser.currentNode().value());
      return value;
   }

   public String cardgram_club_1()
   {  // this is also the action for cardgram_clubs_1
      set("feature", "suit");
      set("value", "Clubs");
      return null;
   }

   public String cardgram_diamond_1()
   {  // this is also the action for cardgram_diamonds_1
      set("feature", "suit");
      set("value", "Diamonds");
      return null;
   }

   public String cardgram_down_2()
   {
      set("orientation", "down");
      return null;
   }

   public String cardgram_each_1()
   {
      set("aggregation", "each");
      set("inclusivity", "all");
      set("number", "singular");
      set("quantity", "several");
      set("which", "quantified");
      return null;
   }

   public String cardgram_eight_1()
   {  // this is also the action for cardgram_eights_1,
      // and cardgram_8s_1
      set("feature", "rank");
      set("value", "8");
      return null;
   }

   public String cardgram_every_1()
   {
      set("aggregation", "each");
      set("inclusivity", "all");
      set("number", "singular");
      set("quantity", "several");
      set("which", "quantified");
      return null;
   }

   public String cardgram_face_2()
   {
      set("feature", "standing");
      set("value", "face");
      return null;
   }

   public String cardgram_five_1()
   {  // this is also the action for cardgram_fives_1,
      // and cardgram_5s_1
      set("feature", "rank");
      set("value", "5");
      return null;
   }

   public String cardgram_four_1()
   {  // this is also the action for cardgram_fours_1,
      // and cardgram_4s_1
      set("feature", "rank");
      set("value", "4");
      return null;
   }

   public String cardgram_heart_1()
   {  // this is also the action for cardgram_hearts_1
      set("feature", "suit");
      set("value", "Hearts");
      return null;
   }

   public String cardgram_here_1()
   {
      set("where", "here");
      return null;
   }

   public String cardgram_it_1()
   {
      set("which", "anaphoric");
      set("quantity", "one");
      set("number", "singular");
      return null;
   }

   public String cardgram_jack_1()
   {  // this is also the action for cardgram_jacks_1
      set("feature", "rank");
      set("value", "Jack");
      return null;
   }

   public String cardgram_joker_1()
   {  // this is also the action for cardgram_jokers_1
      set("feature", "standing");
      set("value", "Joker");
      return null;
   }

   public String cardgram_king_1()
   {  // this is also the action for cardgram_kings_1
      set("feature", "rank");
      set("value", "King");
      return null;
   }

   public String cardgram_MAINCLAUSE_1()
   {
      Object nodeVal = nodeValue();
      if (nodeVal instanceof HashMap)
      {
         set("clause1", nodeVal);
         return null;
      }
/* This old code, along with the code commented out
   in cardgram_MAINCLAUSE_2 and cardgram_MAINCLAUSE_2_V
   causes an error in semantic feature computation on
   a phrase like
   stack the clubs here and stack the spade face cards there
   in which parser backup occurs from a partial sentence
   ending at "spade".  The semantic features are not recovered
   correctly after backup.
      ArrayList<HashMap> clauses
         = new ArrayList<HashMap>(2);
      Object nodeVal = nodeValue();
      if (nodeVal instanceof HashMap)
      {
         clauses.add((HashMap) nodeVal);
         set("clauses", clauses);
         return null;
      }
 */
      else
         return parser.NOADVANCE;
   }

   public String cardgram_MAINCLAUSE_2()
   {
      Object nodeVal = nodeValue();
      if (nodeVal instanceof HashMap)
      {
         set("clause2", nodeVal);
         return null;
      }
/* See comment in cardgram_MAINCLAUSE_1
      Object nodeVal = nodeValue();
      Object clauseVal = valueOf("clauses");
      if (clauseVal instanceof ArrayList
          && nodeVal instanceof HashMap)
      {
         ((ArrayList) clauseVal).add(nodeVal);
         return null;
      }
*/
      else
         return parser.NOADVANCE;
   }

   public Object cardgram_MAINCLAUSE_2_v()
   {
//      return valueOf("clauses");// See comment in cardgram_MAINCLAUSE_1
        ArrayList<HashMap> result = new ArrayList<HashMap>(2);
        result.add((HashMap) valueOf("clause1"));
        result.add((HashMap) valueOf("clause2"));
        return result;
   }

   public String cardgram_many_1()
   {
      set("command", "count");
      return null;
   }

   public String cardgram_MNPL_1()
   {
      raiseFeatures();
      if (valueOf("quantity") == null)
         set("quantity", "several");
      set("number", "plural");
      return null;
   }

   public String cardgram_MNPL_2()
   {
      raiseFeatures();
      if (valueOf("quantity") == null)
         set("quantity", "several");
      set("number", "plural");
      return null;
   }

   public Object cardgram_MNPL_3_v()
   {
      String newAdjFeature = (String) valueOf("adjFeature");
      String newAdjFeatureValue = (String) valueOf("adjFeatureValue");
      raiseFeatures();
      AbstractPlayingCardIdea idea = (AbstractPlayingCardIdea) valueOf("abstractPlayingCardIdea");
      idea.setFeature(newAdjFeature, newAdjFeatureValue);
      if ( ! idea.isConsistent() )
      {
          return parser.NOADVANCE;
      }
      return features();
   }

   public String cardgram_MNPL_4()
   {
      AbstractPlayingCardIdea leftIdea
         = (AbstractPlayingCardIdea) valueOf("abstractPlayingCardIdea");
      raiseFeatures();
      AbstractPlayingCardIdea rightIdea
         = (AbstractPlayingCardIdea) valueOf("abstractPlayingCardIdea");
      AbstractPlayingCardIdea newIdea
         = leftIdea.mergeWith(rightIdea);
      if (newIdea == null)
         return parser.NOADVANCE;
      set("abstractPlayingCardIdea", newIdea);
      return null;
   }

   public String cardgram_MNPL_5()
   {
       raiseFeatures();
       return null;
   }

   public String cardgram_MNPL_6()
   {
      AbstractPlayingCardIdea leftIdea
         = (AbstractPlayingCardIdea) valueOf("abstractPlayingCardIdea");
      String leftNumber = (String) valueOf("number"); // singular or plural
      raiseFeatures();
      AbstractPlayingCardIdea rightIdea
         = (AbstractPlayingCardIdea) valueOf("abstractPlayingCardIdea");
      AbstractPlayingCardIdea newIdea
         = leftIdea.mergeWith(rightIdea);
      if (newIdea == null)
         return parser.NOADVANCE;
      set("abstractPlayingCardIdea", newIdea);
      set("number", leftNumber);
      return null;
   }

   public String cardgram_MNSING_1()
   {
      raiseFeatures();
      if (valueOf("quantity") == null)
         set("quantity", "one");
      set("number", "singular");
      return null;
   }

   public String cardgram_MNSING_2()
   {
      raiseFeatures();
      return null;
   }

   public String cardgram_MNSING_3()
   {
      String newAdjFeature = (String) valueOf("adjFeature");
      String newAdjFeatureValue = (String) valueOf("adjFeatureValue");
      raiseFeatures();
      AbstractPlayingCardIdea idea
         = (AbstractPlayingCardIdea) valueOf("abstractPlayingCardIdea");
      idea.setFeature(newAdjFeature, newAdjFeatureValue);
      if ( ! idea.isConsistent() )
      {
          System.out.println("Inconsistent AbstractPlayingCardIdea");
          return parser.NOADVANCE;
      }
      return null;
   }

   public String cardgram_MNSING_4()
   {
       raiseFeatures();
       return null;
   }

   public String cardgram_next_1()
   {
      set("when", "next");
      return null;
   }

   public String cardgram_nine_1()
   {  // this is also the action for cardgram_nines_1,
      // and cardgram_9s_1
      set("feature", "rank");
      set("value", "9");
      return null;
   }

   public String cardgram_now_1()
   {
      set("when", "now");
      return null;
   }

   public String cardgram_NPD_1()
   {
      set("what", nodeValue());
      return null;
   }

   public String cardgram_NPD1_2()
   {
      String oldAggregation = (String) valueOf("aggregation");
      raiseFeatures();
      if (oldAggregation != null) // e.g. "each" or "all"
      {  set("aggregation", oldAggregation);
         if (oldAggregation.equals("each"))
            // e.g. "each of the piles"
            set("number", "singular");
      }

      return null;
   }

   public String cardgram_NPD2_1()
   {
      raiseFeatures();
//      set("what", nodeValue());
      return null;
   }

   public String cardgram_NPL_1()
   {
      raiseFeatures();
      Object feature = valueOf("feature");
/* debug output:
      if (feature == null)
         System.out.println("feature not found");
      else
         System.out.println("feature is " + feature);
*/
      String nounFeature = (String) feature;
      String featureValue = (String) valueOf("value");
/* debug output:
      if (featureValue == null)
         System.out.println("featureValue not found");
      else
         System.out.println("featureValue is " + featureValue);
 */
      AbstractPlayingCardIdea idea = new AbstractPlayingCardIdea();
      set("abstractPlayingCardIdea", idea);
      if ( nounFeature.equals("structureType") ) // "card" or "pile"
         set("structureType", featureValue);
      else
         {
         set("structureType", "card");
         idea.setFeature(nounFeature, featureValue);
         }
      set("quantity", "several");
      set("number", "plural");
      return null;
   }
/*
   public String cardgram_NPL_2()
   {
      set("quantity", "several");
      set("structureType", nodeValue());
      set("number", "plural");
      return null;
   }

   public String cardgram_NPL_3()
   {
      set("structureType", nodeValue());
      return null;
   }
*/

   public String cardgram_NSADJ_1()
   {
      raiseFeatures();
      set((String) valueOf("feature"), valueOf("value"));
      return null;
   }

   public String cardgram_NSADJ_2()
   {
      raiseFeatures();
      set((String) valueOf("feature"), valueOf("value"));
      return null;
   }

   public String cardgram_NSING_1()
   {
      raiseFeatures();
      Object feature = valueOf("feature");
/* debug output:
      if (feature == null)
         System.out.println("Noun feature not found");
      else
         System.out.println("Noun feature is " + feature);
 */
      String nounFeature = (String) feature;
      String featureValue = (String) valueOf("value");
//      System.out.println("featureValue  is " + featureValue);
      AbstractPlayingCardIdea idea = new AbstractPlayingCardIdea();
      set("abstractPlayingCardIdea", idea);
      if ( nounFeature.equals("structureType") ) // "card" or "pile"
         set("structureType", featureValue);
      else
         {
         set("structureType", "card");
         idea.setFeature(nounFeature, featureValue);
         }
      return null;
   }

   public Object cardgram_NUMBER_1_v()
   {  // return the actual number (a String)
      return parser.currentNode().word();
   }

   public String cardgram_pile_1()
   {  // this is also the action for cardgram_piles_1,
      // cardgram_stack_2, and cardgram_stacks_1
      set("feature", "structureType");
      set("value", "pile");
      return null;
   }

   public String cardgram_pile_2()
   {
      set("verb", "pile");
      set("command", "stack");
      return null;
   }

   public String cardgram_put_1()
   {
      set("verb", "put");
      set("command", "put");
      return null;
   }

   public String cardgram_queen_1()
   {  // this is also the action for cardgram_queens_1
      set("feature", "rank");
      set("value", "Queen");
      return null;
   }

   public String cardgram_RANKNUMBER_1()
   {
      long value = ((Long) parser.currentNode().value()).longValue();
      if (value < 2 || value > 10) // not a valid rank number
      {
         System.out.println("Invalid rank number: " + value);
         return parser.NOADVANCE;
      }
      set("feature", "rank");
      set("value", value + "");  // value as a String
      return null;
   }

   public String cardgram_red_1()
   {
      set("feature", "color");
      set("value", "red");
      return null;
   }

   public String cardgram_separately_1()
   {
      set("aggregation", "each");
      return null;
   }

   public String cardgram_seven_1()
   {  // this is also the action for cardgram_sevens_1,
      // and cardgram_7s_1
      set("feature", "rank");
      set("value", "7");
      return null;
   }

   public String cardgram_shuffle_1()
   {
      set("verb", "shuffle");
      set("command", "shuffle");
      return null;
   }

   public String cardgram_six_1()
   {  // this is also the action for cardgram_sixes_1,
      // and cardgram_6s_1
      set("feature", "rank");
      set("value", "6");
      return null;
   }

   public String cardgram_spade_1()
   {  // this is also the action for cardgram_spades_1
      set("feature", "suit");
      set("value", "Spades");
      return null;
   }

   public String cardgram_spot_1()
   {
      set("feature", "standing");
      set("value", "spot");
      return null;
   }

   public String cardgram_spread_1()
   {
      set("verb", "spread");
      set("command", "spread");
      return null;
   }

   public String cardgram_stack_1()
   {
      set("verb", "stack");
      set("command", "stack");
      return null;
   }

   public String cardgram_ten_1()
   {  // this is also the action for cardgram_tens_1,
      // and cardgram_10s_1
      set("feature", "rank");
      set("value", "10");
      return null;
   }

   public String cardgram_the_1()
   {
      set("which", "quantified");
      return null;
   }

   public String cardgram_that_1()
   {
      set("which", "deictic"); // (or anaphoric without pointing)
      set("number", "singular");
      set("quantity", "one");
      return null;
   }

   public String cardgram_them_1()
   {
      set("which", "anaphoric");
      set("number", "plural");
      set("quantity", "several");
      return null;
   }

   public String cardgram_then_1()
   {
      set("when", "then");
      return null;
   }

   public String cardgram_there_1()
   {
      set("where", "there");
      return null;
   }

   public String cardgram_these_1()
   {
      set("which", "deictic");  // or definite without pointing
      set("number", "plural");
      set("quantity", "several");
      return null;
   }

   public String cardgram_this_1()
   {
      set("which", "deictic");  // or definite without pointing
      set("number", "singular");
      set("quantity", "one");
      return null;
   }

   public String cardgram_those_1()
   {
      set("which", "deictic");  // or anaphoric without pointing
      set("number", "plural");
      set("quantity", "several");
      return null;
   }

   public String cardgram_three_1()
   {  // this is also the action for cardgram_threes_1,
      // and cardgram_3s_1
      set("feature", "rank");
      set("value", "3");
      return null;
   }

   public String cardgram_together_1()
   {
      set("aggregation", "together");
      return null;
   }

   public String cardgram_too_1()
   {
      set("additional", "too");
      return null;
   }

   public String cardgram_turn_1()
   {
      set("verb", "turn");
      set("command", "turn");
      return null;
   }

   public String cardgram_two_1()
   {  // this is also the action for cardgram_twos_1,
      // and cardgram_2s_1
      set("feature", "rank");
      set("value", "2");
      return null;
   }

   public String cardgram_up_2()
   {
      set("orientation", "up");
      return null;
   }

   public String cardgram_VERB_1()
   {
      parser.raiseFeatures();
      return null;
   }

   // Instance variables:

   private ASDParser parser = null;

} 
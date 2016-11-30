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

import java.io.*; // for testing, debugging, and reporting unexpected errors

import com.asdnetworks.cardworld.asd.*;
import java.util.*;
import java.util.regex.*; // for Pattern and Matcher
import java.awt.*;  // for Point
import java.awt.event.*;  // for MouseEvent
import javax.swing.*;  // for JTextArea

/*
   CardAgent2 instances can respond to English-language commands
   to perform actions on Card and CardPile instances on a CardTable.
 */
public class CardAgent
{
   public CardAgent(CardTable t)
   {
      table = t;
      semantics = new CardgramSemantics();
      parser = new ASDParser(semantics);
      parser.setSaveUniquelyParsedSubphrases(false);
      semantics.setParser(parser);
      parser.useGrammar("http://www.asdnetworks.com/grammars/Cardgram2.grm");
      effectors = new CardAgentEffectors(this, table);
   }

// These functions are used by CardWorld to establish links to fields
// in the CardWorldFrame1 window, for linguistic communication with
// the user, and to initialize the state of the card table:

   public void setOutputPane(JTextArea out)
   {
      outputPane = out;
   }

   public void setUtteranceField(JTextField field)
   {
      utteranceField = field;
   }

   public void initialize()
   {  table.clear();
      deck = new PlayingCardDeck(2, "png"); // with two jokers
      deck.setAgent(this);
      deck.setCardTable(table);
      deck.setHomePosition(20, 40);
      deck.stackUp();
      referentPiles = table.getPiles();
        // needed to initialize currentPiles in CardTable instance ??
   }

// These functions are used to provide linguistic output to the user:

   private void echoInput(String input)
   {  if (outputPane != null)
         outputPane.append("You: " + input + "\n");
   }

   private void outputMessage(String m)
   {
      if (outputPane != null)
         outputPane.append("CardWorld: " + m + "\n");
   }


// These functions act as "sensory input" to the Card Agent,
// receiving messages from the current PlayingCard or CardTable instance:

   public void cardIndicated(Card givenCard)
      // invoked by a Card or CardTable instance
      // when the card is pointed to or moved.
   {
      if ( ! newCardIndicated ) // first new card indicated so far
      {                         // for this command
         cardsIndicated = new ArrayList<PlayingCard>(1);
         lastCardsAttendedTo = new ArrayList<PlayingCard>(1);
         pilesIndicated = new ArrayList<CardPile>(1);
         lastPilesAttendedTo = new ArrayList<CardPile>(1);
         newCardIndicated = true;
      }
      if ( ! cardsIndicated.contains(givenCard) )
      {
         cardsIndicated.add((PlayingCard) givenCard);
         lastCardsAttendedTo.add((PlayingCard) givenCard);
         // Don't update pilesIndicated until all pointings to cards for
         // the current command have been completed.  See interpretCommand.
       }
   }

   public void cardsMoved(boolean value)
      // invoked by a CardTable when a card is moved
   {
      cardsMoved = value;
   }

   public void pointIndicated(Point mousePoint)
   {
      newPointIndicated = true;
      lastPointIndicated = mousePoint;
   }

// These functions are used in shifting attention focus from piles to cards
// and from cards to piles, and from unexamined cards to cards that are
// consistent with a given AbstractPlayingCardIdea

   public ArrayList<PlayingCard>
      listCardsInPile(CardPile givenPile)
   {
      ArrayList<PlayingCard> cardList = new ArrayList<PlayingCard>(13);
      if (givenPile == null)
         return cardList;
      Iterator it = givenPile.getIterator();
      while (it.hasNext())
      {
            PlayingCard nextCard = (PlayingCard) it.next();
               cardList.add(nextCard);
      }
      return cardList;
   }

   public ArrayList<PlayingCard>
      listCardsInPiles(ArrayList<CardPile> givenPiles)
   {
      ArrayList<PlayingCard> cardList = new ArrayList<PlayingCard>(13);
      if (givenPiles == null)
         return cardList;
      Iterator pileIterator = givenPiles.iterator();
      while (pileIterator.hasNext())
      {
         CardPile nextPile = (CardPile) pileIterator.next();
         Iterator cardIterator = nextPile.getIterator();
         while (cardIterator.hasNext())
         {
            PlayingCard nextCard = (PlayingCard) cardIterator.next();
            if ( ! cardList.contains(nextCard) )  // for safety
               cardList.add(nextCard);
         }
      }
      return cardList;
   }

   public ArrayList<CardPile>
      listPilesContainingCards(ArrayList<PlayingCard> givenCards)
   {
      ArrayList<CardPile> pilesList = new ArrayList<CardPile>(1);
      if (givenCards == null)
         return pilesList;
      table.findPiles();  // must only be done once; see below
      Iterator it = givenCards.iterator();
      while (it.hasNext())
      {
         PlayingCard nextCard = (PlayingCard) it.next();
         // table.whichPileHas(nextCard) must be used
         // rather than table.whichPileHasCard(nextCard),
         // so table.findPiles() will be called only once:
         CardPile whatPile
            = table.whichPileHas(nextCard);
         if ( ! pilesList.contains(whatPile) )
            pilesList.add(whatPile);
      }
      return pilesList;
   }

   public ArrayList<PlayingCard> listCardsConsistentWithIdea(
         ArrayList<PlayingCard> givenCards, AbstractPlayingCardIdea idea)
   {
      if (givenCards == null)
         return new ArrayList<PlayingCard> (1); // empty result
      ArrayList<PlayingCard> cardList
         = new ArrayList<PlayingCard>(givenCards.size());
      Iterator it = givenCards.iterator();
      while (it.hasNext())
      {
         PlayingCard nextCard = (PlayingCard) it.next();
         if (idea == null || idea.isConsistentWith(nextCard) )
            cardList.add(nextCard);
      }
      return cardList;
   }

   public CardPile findOnlyMultiCardPileAmong(ArrayList<CardPile> givenPiles)
   {  // returns null if there is not a unique multi-card pile among
      // the fiven piles
      CardPile result = null;
      boolean found = false;
      Iterator it = givenPiles.iterator();
      while (it.hasNext())
      {
         CardPile nextPile = (CardPile) it.next();
         if (nextPile != null && nextPile.size() > 1) // multi-card pile
         {
            if (found) // previous multi-card pile found
               return null;
            result = nextPile;
            found = true;
         }
      }
      return result;
   }

// The interpret... funtions interpret input sentences from the user
// as commands to perform actions in the Card World:

   public void interpretCommand(String givenCommand)
      // interprets an entire input command string
      // in the context of any pointing since the last command.
   {
      // Remember what piles have been pointed to for this command, and
      // if there are any, make them the last piles attended to:

      if ( newCardIndicated && cardsIndicated != null
           && cardsIndicated.size() > 0)
      {
         pilesIndicated = listPilesContainingCards(cardsIndicated);
         lastPilesAttendedTo = listPilesContainingCards(cardsIndicated);
      }
      // Prepare list of expected phrase types for ASDParser:
      ArrayList<String> expected = new ArrayList<String>(1);
      expected.add("SENTENCE");

      // Echo the input command to the output pane:
      outputPane.setText(null); // clear the output pane
      echoInput(givenCommand);

      // Reduce the command to all lower-case letters and remove
      // final punctuation, if any:
      String command = givenCommand.trim().toLowerCase();
      while (command.length() > 0 &&
             !( Character.isLetter(command.charAt(command.length()-1))
               || Character.isDigit(command.charAt(command.length()-1)) )
           )
         // remove final non-letter character, if any:
         command = command.substring(0, command.length()-1);
      if (command.length()==0)
         return;  // Ignore empty or non-alphanumeric command.

      // Check the command for possible unknown words:
      ASDGrammar lexicon = parser.lexicon();
      String[] words = command.split("\\s");
      Pattern p = Pattern.compile("[0-9]+"); // regular expression for a string of digits
      boolean unknownWord = false;
      for (int n = 0; n < words.length; ++n)
      {
         Matcher m = p.matcher(words[n]);
         if ( ! m.matches() && lexicon.lookupWord(words[n]) == null)
         {  outputMessage("Unknown word \"" + words[n] + "\"");
            unknownWord = true;
         }
      }
      if (unknownWord)
         return;

      sentenceUsedExplicitDeictic = false;
         // keeps track of how long the last point indicated
         // should continue to be remembered.

      // Attempt to parse and interpret the command:
      parser.initialize(command, expected);
      boolean commandUnderstood = false;

      while(! commandUnderstood && parser.parse())
      {  // command has not been understood yet,
         // but a successful parse has been found.
         Object nodeValue = parser.phraseStructure().nextNode().value();
         ArrayList clauseValues = null;
         if (nodeValue instanceof ArrayList)
            clauseValues = (ArrayList) nodeValue;
            // each HashMap in the ArrayList holds
            // the semantics of a clause in the command.
         else
         {
            System.err.println(
               "CardAgent2 146: Semantic value of command is not "
               + "an ArrayList<HashMap> as expected.");
            commandUnderstood = false;
         }
         // Attempt to interpret the semantics of the clauses
         // in the current pragmatic context:
         commandUnderstood = this.interpretClauses(clauseValues);
      }

      // Adjust deictic and other memory for next command:
      newCardIndicated = false;
      newPointIndicated = false;

/*
     {
         lastPilesAttendedTo
            = new ArrayList<CardPile>(lastCardsAttendedTo.size());
         Iterator it = lastCardsAttendedTo.iterator();
         while (it.hasNext())
         {
            PlayingCard nextCard = (PlayingCard) it.next();
            CardPile whatPile
               = table.whichPileHasCard(nextCard);
            if ( ! lastPilesAttendedTo.contains(whatPile) )
               lastPilesAttendedTo.add(whatPile);
         }
      }
*/

      // Forget the last cards attended to,
      // if previous command was about pile(s):
/*
      if (lastTypeReferredTo.equals("pile"))
         lastCardsAttendedTo = null;
*/
      // If the last command dealt with all the cards or piles
      // or all the cards in a particular pile,
      // forget about what card was last pointed to or manipulated:
/*
      if (lastIncludedAllCardsExplicitly
          || inclusivity.equals("all")
          || aggregation.equals("each")
          || eachUsed)
         lastCardsAttendedTo = null;
*/
      // Similarly for memory of the last pile(s) attended to:
//      if (lastIncludedAllCardsExplicitly
//          && ! lastTypeReferredTo.equals("pile"))
//         lastPilesAttendedTo = null;

      // Objects cannot continue to be referred to separately by "it" or
      // "that pile" after a command using "each" if explicit deixis was
      // used.  e.g. "spread out each pile",
      // [pointing] "turn that one over","stack it up".
      if (actualWhich.equals("deictic") && !aggregation.equals("each") )
         eachUsed = false;

      // For now, cards can't continue to be referred to by "it"
      // after a command using "each card", because memory
      // for more than lastCardsAttendedTo is not kept:
// This has been overridden, because memory for the cards can be
// in lastPilesAttendedTo:
//      if (lastTypeReferredTo.equals("card") )
//         eachUsed = false;


      // If the sentence didn't use an explicit deictic adverb,
      // forget the last place that was pointed to:
      if ( ! sentenceUsedExplicitDeictic )
         lastPointIndicated = null;

      if (commandUnderstood)
         outputMessage("OK");
      else
         outputMessage("Command not understood.");

      utteranceField.setText(""); // clear input line for next command

   } // end interpretCommand


   private boolean interpretClauses(ArrayList clauses)
      // attempts to interpret the semantics of each clause in the command.
   {
      Iterator it = clauses.iterator();
      while (it.hasNext())
      {
         Object nextClauseSemantics = it.next();
         if (! (nextClauseSemantics instanceof HashMap) ||
             ! interpretClause((HashMap) nextClauseSemantics))
            return false;  // clause not understood
      }
      return true;  // all clauses understood
   } // end interpretClauses


   // This function takes semantic feature values representing the meaning
   // of a clause, together with the current situation on the CardTable, and
   // decides what pragmatic operation(s) to perform:
   private boolean interpretClause(HashMap clauseFeatures)
   {
/* debug printing:
      HashMap what = (HashMap) clauseFeatures.get("what");
      AbstractPlayingCardIdea cardIdea =
         (AbstractPlayingCardIdea) what.get("abstractPlayingCardIdea");
      if ( cardIdea != null )
         System.out.println("clause feature AbstractPlayingCardIdea = " + cardIdea.getColorName()
                + " " + cardIdea.getRankName() + " " + cardIdea.getSuitName()
                + " " + cardIdea.getStandingName());
      String number = (String) what.get("number");
      System.out.println("clause feature number = " + number);
*/
      // Attempt to find the things on the CardTable
      // to which the clause refers, and unpack values of other
      // features of the clause meaning:
      if (! findReferents(clauseFeatures)) // null command, or
         return false;  // referents couldn't be found unambiguously

      // Apply the command according to referentType
      // and referentCards or referentPiles:
      if (commandNeedsMoreThanOneCard) // "shuffle", "spread", "stack"
      {  if ( ! interpretMultiCardCommand() )
            return false;   // interpretation failed
      }
      else if (command.equals("turn"))
      {  if ( ! interpretTurnCommand() )
            return false;   // interpretation failed
      }
      else if (command.equals("put"))
      {
         if ( ! interpretPutCommand() )
            return false;   // interpretation failed
      }
      else // This case shouldn't happen.
      {
         outputMessage("Unexpected type of command.");
         return false;
      }

      // Interpretation of the clause has been successful.
      // Update relevant short-term memory items before the next clause:
      lastTypeReferredTo = referentType;
      if (eachUsed && number.equals("plural"))
         eachUsed = false;  // e.g. "them" or "the piles" after
                            // "spread out each pile"
      if (referentCards != null)
         lastCardsAttendedTo = referentCards;
      else if (lastPilesAttendedTo != null && lastPilesAttendedTo.size() > 0)
         // put all fo the cards in the last pile(s) attended to into
         // the lastCardsAttendedTo
         lastCardsAttendedTo = listCardsInPiles(lastPilesAttendedTo);
      // else no change in lastCardsAttendedTo

      return true;  // clause was interpreted successfully

   }  // end interpretClause


   private boolean interpretMultiCardCommand()
      // interprets a command ("shuffle", "stack", or "spread")
      // that requires more than one card.
   {
      if (referentType.equals("null") || referentType.equals("pile"))
      {  // structureType of referent is unspecified or "pile"

         if (aggregation.equals("null") || aggregation.equals("each"))
         {
            // aggregation is not "together"; apply the command to
            // each referent pile separately:
            if (aggregation.equals("each")
                && referentPiles.size() == 1)
            {
               outputMessage("I assume you mean all of the piles.");
               referentPiles = table.getPiles();
               if (referentPiles.size() == 1)
                  outputMessage("There is only one pile.");
            }
            lastPilesAttendedTo = new ArrayList<CardPile>(4);
               // remembers the piles manipulated, for possible
               // anaphoric reference.subsequently.
            if (referentPiles.size() == 1)
            {
               CardPile pile = referentPiles.get(0);
               if (commandNeedsMoreThanOneCard && pile.size() == 1)
                  // A one-card pile was referred to.
                  // e.g. move a card alone, "turn it over" "spread it out"
                  // when the last structureType referred to was "pile";
                  // or "spread that pile out" when last pile referred to
                  // has only one card
               outputMessage("Warning: That pile has only one card.");
            }

            // Apply the command to each referent pile separately:
            Iterator it = referentPiles.iterator();
            while (it.hasNext())
            {
               CardPile pile = (CardPile) it.next();
               // Before shuffling, stacking or spreading the pile,
               // turn each card in the pile down, over, or up,
               // according to orientation, if specified:
               if (orientation != "null")
                  effectors.turnEachCardInPile(pile, orientation);
               if (command.equals("spread"))
               {
                  if (where.equals("null") || lastPointIndicated == null)
                     lastPilesAttendedTo.add(
                        effectors.spreadOutPile(pile));
                  else
                     lastPilesAttendedTo.add(
                        effectors.spreadOutPileAt(pile,
                                                  lastPointIndicated));
               }
               else if ( command.equals("shuffle"))
               {
                  lastPilesAttendedTo.add(effectors.shufflePile(pile));
                  // After shuffling, leave the pile arranged as it was,
                  // so the user must state explicitly if and how it
                  // should be rearranged.
               }
               else if (command.equals("stack"))
               {
                  if (where.equals("null") || lastPointIndicated == null)
                     lastPilesAttendedTo.add(
                        effectors.stackUpPile(pile));
                  else
                     lastPilesAttendedTo.add(
                        effectors.stackUpPileAt(pile,
                                                lastPointIndicated));
               }
            }

         }
         else if (aggregation.equals("together"))
         {
            // Apply the command to all of the referent piles together:
            Point pointIndicated = lastPointIndicated;
            PlayingCard oneCardInResultPile
               = (PlayingCard) referentPiles.get(0).getCard(0);
// System.out.println("527: oneCardInResultPile = " + oneCardInResultPile);
// System.out.println("528: where = " + where);
            if (referentType.equals("pile") && referentPiles.size() == 1)
               outputMessage("That's only one pile.");
            lastPilesAttendedTo = new ArrayList<CardPile>(4);
            if (where.equals("null") || lastPointIndicated == null)
            {  // no place indicated;
               // stack them at the home of the first pile
               pointIndicated
                  = referentPiles.get(0).getHomePosition();
                  // assumes at least one pile
            }
            Iterator it = referentPiles.iterator();
            while (it.hasNext())
            {
               CardPile pile = (CardPile) it.next();
               effectors.turnEachCardInPile(pile, orientation);
               effectors.stackUpPileAt(pile, pointIndicated);
            }
            lastPointIndicated = pointIndicated;
            table.findPiles();  // Ask table to re-partition the piles.
            CardPile newPile = table.whichPileHasCard(oneCardInResultPile);
//            if (lastPointIndicated != null)
//               newPile = table.getPileAt(lastPointIndicated);
            if (newPile != null)
            {
               if (command.equals("shuffle"))
                  effectors.shufflePile(newPile);
               if (command.equals("spread"))
                  lastPilesAttendedTo.add(effectors.spreadOutPileAt(
                     newPile, lastPointIndicated));
               else // command is "shuffle" or "stack"
               {
                  effectors.stackUpPileAt(newPile, lastPointIndicated);
                  lastPilesAttendedTo.add(newPile);
               }
            }
            else // this case shouldn't happen
            {
               System.err.println("CardAgent2 565: No result pile was found.");
               return false;
            }
         }
         lastCardsAttendedTo = listCardsInPiles(lastPilesAttendedTo);
      }
      else if (referentType.equals("card"))
      {  // e.g. "shuffle/spread/stack [all] the cards",
         // "shuffle/spread/stack those cards",
         // but not "shuffle/spread/stack each card"
         CardPile relevantPile = null;
         PlayingCard oneCardInResultPile = null;
         Point pointIndicated = null;
         if (referentCards == null || referentCards.size() == 0)
            referentCards = lastCardsAttendedTo;
         if (referentCards == null || referentCards.size() == 0)
         {  // Assume the cards were all in one pile:
            if (referentPiles.size() == 1) // all the cards are in one pile
            {
               relevantPile = referentPiles.get(0);
               // pointIndicated set here is provisional; the check for
               // deictic reference and pointing comes later:
               pointIndicated = relevantPile.getHomePosition();
               oneCardInResultPile = (PlayingCard) relevantPile.getCard(0);
            }
            else // this case shouldn't happen
            {
               outputMessage("I don't know which cards you mean.");
               return false;
            }
         }
         else if (referentCards.size() <= 1)
         {
            outputMessage("I don't know how to " + command
                          + " single cards, only two or more.");
            return false;
         }
         else // referentCards.size > 1
         {  // cards referred to were not necessarily all in an existing pile
            relevantPile = new CardPile(referentCards.size());
            oneCardInResultPile = referentCards.get(0);
            Iterator it = referentCards.iterator();
            while (it.hasNext())
               relevantPile.addCard((PlayingCard) it.next());
            relevantPile.setCardTable(table);
            pointIndicated = new Point((int)oneCardInResultPile.getX(),
                                   (int)oneCardInResultPile.getY());
            relevantPile.setHomePosition(pointIndicated);
         }
         effectors.turnEachCardInPile(relevantPile, orientation);

            // needed for commands like
            // "shuffle all the cards and spread them out together"
         if ( ! where.equals("null") && lastPointIndicated != null)
            // deictic reference was used
            pointIndicated = lastPointIndicated;
         else
         // Note: lastPointIndicated mustn't be changed prematurely
         // if where == null, because the old value of
         // lastPointIndicated may be needed by a later clause
         // in the same sentence: e.g.
         // "shuffle the cards and spread them out here"
         {
            if (lastPointIndicated == null)
               lastPointIndicated = pointIndicated;
         }
         if (command.equals("shuffle"))
         {
            if (pointIndicated != null)
               effectors.stackUpPileAt(relevantPile, pointIndicated);
            else
            {
               effectors.stackUpPileAt(relevantPile, deck.getHomePosition() );
               relevantPile.setHomePosition(deck.getHomePosition() );
            }
            effectors.shufflePile(relevantPile);
            referentCards = listCardsInPile(relevantPile);
               // update the list after the shuffle
         }
         else if (command.equals("spread"))
            effectors.spreadOutPileAt(relevantPile, pointIndicated);
         else if (command.equals("stack"))
            effectors.stackUpPileAt(relevantPile, pointIndicated);
         else if (command.equals("shuffle")
                  && aggregation.equals("together"))
            effectors.stackUpPileAt(relevantPile, pointIndicated);
         // If the command is "shuffle", but aggregation is not
         // "together", leave the pile arranged as it was, so the
         // user must specify explicitly if and how to rearrange it.
         table.findPiles();
         lastPilesAttendedTo = new ArrayList<CardPile>(1);
         lastPilesAttendedTo.add(table.whichPileHas(oneCardInResultPile));
//         lastPilesAttendedTo.add(relevantPile);
         lastCardsAttendedTo = listCardsInPiles(lastPilesAttendedTo);
      }

      return true;
   }  // end interpretMultiCommand


    private boolean interpretPutCommand()
    {
       // "put" is like the "stack" command, but it can be done on
       // single cards as well as multi-card piles.
      if (referentType.equals("null") || referentType.equals("pile"))
      {  // structureType of referent is unspecified or "pile"

         if (aggregation.equals("null") || aggregation.equals("each"))
         {
            // aggregation is not "together"; apply the command to
            // each referent pile separately:
            if (aggregation.equals("each")
                && referentPiles.size() == 1)
            {
               outputMessage("I assume you mean all of the piles.");
               referentPiles = table.getPiles();
               if (referentPiles.size() == 1)
                  outputMessage("There is only one pile.");
            }
            lastPilesAttendedTo = new ArrayList<CardPile>(4);
               // remembers the piles manipulated, for possible
               // anaphoric reference.subsequently.

            // Apply the command to each referent pile separately:
            Iterator it = referentPiles.iterator();
            while (it.hasNext())
            {
               CardPile pile = (CardPile) it.next();
               // Before shuffling, stacking or spreading the pile,
               // turn each card in the pile down, over, or up,
               // according to orientation, if specified:
               if (orientation != "null")
                  effectors.turnEachCardInPile(pile, orientation);
               if ( ! where.equals("null") && lastPointIndicated != null)
                  lastPilesAttendedTo.add(
                     effectors.stackUpPileAt(pile,
                                             lastPointIndicated));
               else
               {
                  outputMessage("I don't know where to put them.");
                  return false;
               }
            }

         }
         else if (aggregation.equals("together"))
         {
            // Apply the command to all of the referent piles together:
            Point pointIndicated = lastPointIndicated;
            if (referentType.equals("pile") && referentPiles.size() == 1)
               outputMessage("That's only one pile.");
            CardPile firstPile = referentPiles.get(0);
            Card firstCard = firstPile.getCard(0);
            lastPilesAttendedTo = new ArrayList<CardPile>(4);
            if ( ! where.equals("null") && lastPointIndicated == null)
            {
               outputMessage("I don't know where to put them.");
               return false;
            }
            else if ( where.equals("null") )
            {
               pointIndicated = firstPile.getHomePosition();
            }
            Iterator it = referentPiles.iterator();
            while (it.hasNext())
            {
               CardPile pile = (CardPile) it.next();
               if (firstPile == null)
                  firstPile = pile;
               effectors.turnEachCardInPile(pile, orientation);
               effectors.stackUpPileAt(pile, pointIndicated);
            }
            table.findPiles();  // Ask table to re-partition the piles.
            CardPile newPile = null;
            if (pointIndicated != null)
               newPile = table.whichPileHasCard(firstCard);
            if (newPile != null)
            {
               effectors.stackUpPileAt(newPile, pointIndicated);
               lastPilesAttendedTo.add(newPile);
            }
            else // this case shouldn't happen
            {
               System.err.println("CardAgent2 728: No pile was found"
                                  + " at the point indicated.");
               return false;
            }
            lastPointIndicated = pointIndicated;
         }
         lastCardsAttendedTo = listCardsInPiles(lastPilesAttendedTo);
      }
      else if (referentType.equals("card"))
      {  // e.g. "put [all] the cards here/there",
         CardPile relevantPile = null;
         PlayingCard oneCardInResultPile = null;
         Point pointIndicated = lastPointIndicated;
         if (referentCards == null || referentCards.size() == 0)
            referentCards = lastCardsAttendedTo;
         if (referentCards == null || referentCards.size() == 0)
         {  // Assume the cards were all in one pile:
            if (referentPiles.size() == 1) // all the cards are in one pile
            {
               relevantPile = referentPiles.get(0);
               oneCardInResultPile = (PlayingCard) relevantPile.getCard(0);
            }
            else // this case shouldn't happen
            {
               outputMessage("I don't know which cards you mean.");
               return false;
            }
         }
         else // referentCards.size > 1
         {  // cards referred to were not necessarily all in an existing pile
            oneCardInResultPile = referentCards.get(0);
            if (lastPointIndicated != null)
               // deictic reference was used
               pointIndicated = lastPointIndicated;
            else if (aggregation.equals("together") )
            {
               pointIndicated = new Point((int)oneCardInResultPile.getX(),
                                      (int)oneCardInResultPile.getY());
               lastPointIndicated = pointIndicated;
            }
            else
            {
               outputMessage("I don't know where you want to put them.");
               return false;
            }

            relevantPile = new CardPile(referentCards.size());
            Iterator it = referentCards.iterator();
            while (it.hasNext())
               relevantPile.addCard((PlayingCard) it.next());
            relevantPile.setCardTable(table);
            relevantPile.setHomePosition(pointIndicated);
         }

         if (orientation != null && ! orientation.equals("null") )
            effectors.turnEachCardInPile(relevantPile, orientation);

         effectors.stackUpPileAt(relevantPile, pointIndicated);
         table.findPiles();
         lastPilesAttendedTo = new ArrayList<CardPile>(1);
         lastPilesAttendedTo.add(table.whichPileHas(oneCardInResultPile));
//         lastPilesAttendedTo.add(relevantPile);
         lastCardsAttendedTo = listCardsInPiles(lastPilesAttendedTo);
      }

      return true;
    } // end interpretPutCommand

   private boolean interpretTurnCommand()
   {
      // direction should be "down", "over", or "up";
      // if it is null, use orientation "down" or "up", instead,
      // or use "over" as the default.  Note: This is redundant
      // with logic in Cardgram2Semantics cardgram_$$_3.
      if (direction.equals("null"))
      {
         if (orientation.equals("null"))
            direction = "over"; // default is "over"
         else // clause had phrase "face down" or "face up"
            direction = orientation;
      }
      else if ( ! ( direction.equals("down") || direction.equals("over")
                  || direction.equals("up") ) )
         return false;

      if (referentType.equals("null"))
      {
         outputMessage("I don't know whether you want "
            + "to turn the entire pile or just that one card.");
         return false;
      }
      else if (referentType.equals("card"))
      {  // The command is to turn card(s).
         if (number.equals("singular") && quantity.equals("one"))
         {
            if (referentCards.size() == 1)
            {  // Turn just the one card:
               PlayingCard c = (PlayingCard) referentCards.get(0);
               if ( ! where.equals("null") && lastPointIndicated != null)
                  // "turn [down/over/up] that card" with pointing
                  effectors.turnCard(c, direction, lastPointIndicated);
               else
                  effectors.turnCard(c, direction);
            }
            else if (referentCards.size() == 0 && referentPiles.size() == 1)
            {  // Turn each of the cards in the pile referred to
               CardPile p = (CardPile) referentPiles.get(0);
               effectors.turnEachCardInPile(p, direction);
            }
            else
            {
               outputMessage("I'm not sure which of those cards"
                             + " you want to turn.");
               return false;
            }
         }
         else if (number.equals("plural") || quantity.equals("several"))
         {
            // e.g. "the cards", "each card"
            if (referentCards != null && referentCards.size() > 0)
            {
               // Turn all of the cards referred to:
               PlayingCard c = null;
               Iterator it = referentCards.iterator();
               while (it.hasNext())
               {
                  c = (PlayingCard) it.next();
                  if ( ! where.equals("null") && lastPointIndicated != null)
                     // "turn [down/over/up] that card" with pointing
                     effectors.turnCard(c, direction, lastPointIndicated);
                  else
                     effectors.turnCard(c, direction);
               }
               lastPilesAttendedTo = null;

               // If all of the cards were in the same pile,
               // remember that pile:
               boolean samePile = false;
               CardPile p = null;
               it = referentCards.iterator();
               if (it.hasNext())
               {
                  c = (PlayingCard) it.next();
                  p = table.whichPileHasCard(c);
                  samePile = true;
               }
               while (samePile && it.hasNext())
               {
                  c = (PlayingCard) it.next();
                  if ( p != table.whichPileHasCard(c))
                     samePile = false;
               }
               if (samePile)
               {
                  lastPilesAttendedTo = new ArrayList<CardPile>(1);
                  lastPilesAttendedTo.add(p);
               }
            }
            else if (referentPiles != null)
            {
               // Turn all of the cards in the piles referred to:
               Iterator it = referentPiles.iterator();
               while (it.hasNext())
               {
                  CardPile p = (CardPile) it.next();
                  // Assume each card is to be turned individually:
                  effectors.turnEachCardInPile(p, direction);
               }
               lastPilesAttendedTo = referentPiles;
            }
         }
      }
      else if (referentType.equals("pile"))
      {
         // The command is to turn pile(s).
         if ( ! aggregation.equals("together") )
         {
            if (referentPiles == null) // This shouldn't occur.
            {
               outputMessage("I don't know which pile(s) "
                  + "you want me to turn.");
               return false;
            }

            // Turn each specified pile separately:
            lastPilesAttendedTo = new ArrayList<CardPile>(4);
            Iterator it = referentPiles.iterator();
            while (it.hasNext())
            {
               CardPile pile = (CardPile) it.next();
               ArrayList<CardPile> newPiles
                  = effectors.turnPile(pile, direction);
               CardPile newPile = newPiles.get(0);
               if ( ! where.equals("null") && lastPointIndicated != null )
                  effectors.stackUpPileAt(newPile, lastPointIndicated);
               else
                  lastPilesAttendedTo.add(newPile);
            }

            if ( ! where.equals("null") && lastPointIndicated != null )
            {  // the piles have been put together
               // Ask the table to re-partition the piles:
               table.findPiles();
               CardPile newPile = table.getPileAt(lastPointIndicated);
               if (newPile != null)
               {
                  // Re-stack the consolidated pile
                  effectors.stackUpPileAt(newPile, lastPointIndicated);
                  lastPilesAttendedTo.add(newPile);
               }
               else // this case shouldn't happen
               {
                  System.err.println(
                     "CardAgent2 583: No pile was found"
                     + " at the point indicated.");
                  return false;
               }
            }
         }
         else // aggregation.equals("together")
         {
            // Turn each of the specified piles at the same place:
            if (referentPiles.size() == 1)
               outputMessage("That's only one pile.");

            lastPilesAttendedTo = new ArrayList<CardPile>(4);
            if (where.equals("null")) // no place specified
            {  // stack them at the home of the first pile
               lastPointIndicated
                  = referentPiles.get(0).getHomePosition();
                  // assumes at least one pile
            }
            else if (lastPointIndicated == null)
            {
               outputMessage("I don't know where you mean.");
               return false;
            }
            Iterator it = referentPiles.iterator();
            while (it.hasNext())
            {
               CardPile pile = (CardPile) it.next();
               effectors.turnPile(pile, direction);
               effectors.stackUpPileAt(pile, lastPointIndicated);
            }
            CardPile newPile = table.getPileAt(lastPointIndicated);
            if (newPile != null)
               lastPilesAttendedTo.add(newPile);
            else // this case shouldn't happen
            {
               System.err.println(
                  "CardAgent2 620: No pile was found"
                  + " at the point indicated.");
               return false;
            }
         }
      }

      return true;
   } // end interpretTurnCommand


// The findReferents function finds the referents for the noun/pronoun
// phrase and (optional) deictic adverb in a clause:  This version uses
// a decision network expressed as an ASDGrammar file, CardDecisionNet2.grm,
// which is searched by the program ASDDecider to determine the structureType of
// referent that the noun phrase in a given clause refers to.  That replaces
// many lines of nested if ... else logic in an earlier version of CargAgent1.

   private boolean findReferents(HashMap clauseFeatures)
   {
      // Unpack the semantic features and set default values:
      command = (String) clauseFeatures.get("command");
      if (command == null)
         return false;
      direction = (String) clauseFeatures.get("direction");
         // null, "down", "over", or "up"
      if (direction == null) direction = "null";
      orientation = (String) clauseFeatures.get("orientation");
         // null, "down", or "up"
      if (orientation == null) orientation = "null";
      where = (String) clauseFeatures.get("where");
      if (where == null)
         where = "null";
      else
      {  // a deictic adverb "here" or "there" has been used
         if ( where.equals("here") )
         {
            if ( ! newPointIndicated && ! newCardIndicated )
            {  // "here" shouldn't be used anaphorically
               outputMessage("I don't know where you are referring to.");
               return false;
            }
            else if ( ! newPointIndicated && lastCardsAttendedTo != null
               && lastCardsAttendedTo.size() == 1)
               // If no place has been indicated, use the location of
               // the last card indicated or moved, if any:
               lastPointIndicated = lastCardsAttendedTo.get(0).getLocation();
         }
         else // where.equals("there") // could be deictic or anaphoric
         if ( lastPointIndicated == null) // no place has been indicated
         {
            if (lastCardsAttendedTo != null && lastCardsAttendedTo.size()>0 )
            // use the location of the last card indicated or moved, if any:
               lastPointIndicated = lastCardsAttendedTo.get(0).getLocation();
            else if (lastPilesAttendedTo != null
               && lastPilesAttendedTo.size()==1)
            {  // use the location of the last card in the last pile attended to
               CardPile lastPile = lastPilesAttendedTo.get(0);
               lastPointIndicated = lastPile.getHomePosition();
            }
//            else
//               outputMessage("I'm not sure where you mean.");
         }
         // Also, remember that an explicit deictic adverb was
         // used, so that memory to the last point indicated can be
         // continued to the next sentence, if necessary:
         sentenceUsedExplicitDeictic = true;
      }
      what = (HashMap) clauseFeatures.get("what");
      AbstractPlayingCardIdea cardIdea =
         (AbstractPlayingCardIdea) what.get("abstractPlayingCardIdea");
      if ( cardIdea != null )
         System.out.println("\nAbstractPlayingCardIdea = " + cardIdea.getColorName()
                + " " + cardIdea.getRankName() + " " + cardIdea.getSuitName()
                + " " + cardIdea.getStandingName());
      structureType = (String) what.get("structureType");
      if (structureType == null) structureType = "null";
      aggregation = (String) clauseFeatures.get("aggregation");
         // null, "each", or "together"
      String nounPhraseAggregation = (String) what.get("aggregation");
      if (aggregation == null)
         aggregation = nounPhraseAggregation;
      if (aggregation == null) aggregation = "null";
      // Both number and quantity are needed, because of phrases like
      // "each card" or "each one", which yield number "singular"
      // but quantity "several"
      number = (String) what.get("number"); // "singular" or "plural"
      quantity = (String) what.get("quantity"); // "one" or "several"
      which = (String) what.get("which"); // null, "anaphoric" or "deictic"
      if (which == null) which = "null";
      inclusivity = (String) what.get("inclusivity"); // null or "all"
      if (inclusivity == null) inclusivity = "null";

      // variables to keep track of the things actually referred to
      // and their structureType:
      actualWhich = "quantified";  // "anaphoric", "deictic" or "quantified",
         // "quantified" being the default

      if (eachUsed) // "each", "every", or "separately" was used in
      {             // the previous command
         if (! structureType.equals("null")
             && ! lastTypeReferredTo.equals("null")
             && ! structureType.equals(lastTypeReferredTo)
             || aggregation.equals("together")
             || which.equals("deictic") && newCardIndicated
            )
            // The previous command used it with respect to a different
            // structureType, or the current one cancels the separation.
            eachUsed = false;
      }
      eachUsed = eachUsed ||
                 aggregation.equals("each") && number.equals("singular");
         // "each" or "every" was used explicitly in this clause,
         // or has just been used to indicate
         // more than one item with a singular noun phrase.
         // This is needed to interpret "it" in a command like
         // "shuffle each pile and spread it out" correctly.
         // Note: The grammar gives a clause like
         // "spread out each of the piles" a singular number.

      referentType = structureType;
      if (referentType.equals("null"))
         referentType = lastTypeReferredTo;
         // structureType of actual referent: unspecified, "card", or "pile"
//      lastCardsAttendedTo = referentCards;
      referentCards = null;  // referent(s) if referentType is "card"
      referentPiles = null; // referent(s) if referentType is "pile"

      commandNeedsMoreThanOneCard
         = command.equals("shuffle") || command.equals("spread")
           || command.equals("stack");

      // Find the referent and actual referentType ("card" or "pile")
      // for the command:

      // Get ready to accumulate either cards or piles as referents:
      referentCards = new ArrayList<PlayingCard>(13);
      referentPiles = new ArrayList<CardPile>(4);

      // Prepare to search CardDecisionNet for referents
      decider = new ASDDecider();
      decider.useNetwork("http://www.asdnetworks.com/grammars/CardDecisionNet2.grm");
      StringBuilder decBuilder = new StringBuilder(150);
      decBuilder.append("command:" + command);
      decBuilder.append(" structureType:" + structureType);
      decBuilder.append(" lastType:" + lastTypeReferredTo);
      decBuilder.append(" number:" + number);
      decBuilder.append(" eachUsed:" + eachUsed);
      decBuilder.append(" needs2orMore:" + commandNeedsMoreThanOneCard);
      decBuilder.append(" which:");
      if (which.equals("null"))
         decBuilder.append("quantified");
      else
         decBuilder.append(which);
//      decBuilder.append(" aggregation:" + aggregation);
//      decBuilder.append(" inclusivity:" + inclusivity);
      decBuilder.append(" newCardIndicated:" + newCardIndicated);
      decBuilder.append(" lastCardAttendedTo:");
      if (lastCardsAttendedTo == null)
         decBuilder.append("null");
      else
         decBuilder.append("notNull");
      decBuilder.append(" lastPilesAttendedTo:");
      if (lastPilesAttendedTo == null)
         decBuilder.append("0");
      else
      {
         int s = lastPilesAttendedTo.size();
         if (s < 2)
            decBuilder.append("" + s);
         else
            decBuilder.append("2orMore");
      }

      CardPile p = table.findOnlyMultiCardPile();
      decBuilder.append(" onlyOneMultiCardPile:" + (p != null));

System.out.println("\n" + decBuilder);
      String[] structureTypes = EXPECTED_REFERENT_TYPES.split("\\s");
      ArrayList<String> expectedReferentTypes
         = new ArrayList<String>(structureTypes.length);
      for (int j=0; j<structureTypes.length; j++)
         expectedReferentTypes.add(structureTypes[j]);
      decider.initialize(decBuilder.toString(), expectedReferentTypes);

      boolean searchResult = decider.parse();
      if (! searchResult)
      {
         outputMessage("I don't know what you are referring to.");
         return false;
      }

      ASDPhraseNode structure = decider.decisionStructure();
      String referentDecision = structure.nextNode().word();
System.out.println("Decision network search result = " + referentDecision);

      // Set the referent structureType and actual referents
      if (referentDecision.equals("A1")) // (one of) the last card(s) referred to,
      {                                  // without pointing
         referentType = "card";
         if ((cardIdea == null || cardIdea.isConsistentWithAnyAbstractCard() )
             && lastCardsAttendedTo.size() > 1)
         {
            outputMessage("I don't know which card you are referring to.");
            return false;
         }
         referentCards = listCardsConsistentWithIdea(lastCardsAttendedTo, cardIdea);
         if (referentCards.size() > 1)
         {
            outputMessage(
               "I don't know which of those cards you are referring to.");
            referentCards = null;
            return false;
         }
         else if (referentCards.size() == 0)
         {  // See if there is a unique matching card among the last pile(s)
            // attended to, rather than among the last cards attended to:
            ArrayList<PlayingCard> candidateCards
               = listCardsInPiles(lastPilesAttendedTo);
            referentCards = listCardsConsistentWithIdea(candidateCards, cardIdea);
            if (referentCards.size() != 1)
            {
               outputMessage(
                  "I don't know which of those cards you are referring to.");
               referentCards = null;
               return false;
            }
         }
         actualWhich = "anaphoric";
         lastCardsAttendedTo = referentCards;
         lastIncludedAllCardsExplicitly = false;
         return true;
      }
      else if (referentDecision.equals("A2")) // the last pile referred
      {                                       //  to, without pointing
         if (! structureType.equals("card")
            && lastPilesAttendedTo != null && lastPilesAttendedTo.size() == 1)
         {
            referentType = "pile";
            CardPile ref = null;
            ref = (CardPile) lastPilesAttendedTo.get(0);
            referentPiles.add(ref);
            actualWhich = "anaphoric";
            lastIncludedAllCardsExplicitly = false;
            actualWhich = "anaphoric";
            return true;
         }
         else if (lastCardsAttendedTo != null && lastCardsAttendedTo.size() > 0)
         {
            referentPiles = listPilesContainingCards(lastCardsAttendedTo);
            if (referentPiles.size() == 1)
            {
               referentType = "pile";
               actualWhich = "anaphoric";
               return true;
            }
            else
            {
               outputMessage("I don't know which pile you are referring to.");
               return false;
            }
         }
      }
      else if (referentDecision.equals("A3A5")) // the last cards referred
      {                                       // to, separately or together
         if (lastCardsAttendedTo != null && lastCardsAttendedTo.size() > 0)
         {
            if (lastCardsAttendedTo.size() == 1)
            {
               CardPile pp = table.whichPileHasCard(lastCardsAttendedTo.get(0));
               if (number.equals("plural") && pp.size()==1)
               {
                  outputMessage("I don't know which cards you are referring to.");
                  return false;
               }
               referentPiles = new ArrayList<CardPile>(1);
               referentPiles.add(pp);
               referentCards = listCardsConsistentWithIdea(
                                 listCardsInPiles(referentPiles), cardIdea);
               if (referentCards.size() == 0)
               {
                  outputMessage("I don't know which you are referring to.");
                  return false;
               }
               referentType = "card";
               actualWhich = "anaphoric";
               return true;
            }
            else
            {
               referentCards = listCardsConsistentWithIdea(
                                 lastCardsAttendedTo, cardIdea);
               if (referentCards.size() == 0)
               {
                  outputMessage("I don't know which you are referring to.");
                  return false;
               }
               lastCardsAttendedTo = referentCards;
               referentType = "card";
               actualWhich = "anaphoric";
               return true;
            }
         }
         else if (lastPilesAttendedTo != null)
         {
            referentType = "card";
            actualWhich = "anaphoric";
            ArrayList<PlayingCard> cardList
               = listCardsInPiles(lastPilesAttendedTo);
            referentCards = listCardsConsistentWithIdea(cardList, cardIdea);
            if (referentCards.size() == 0)
            {
               outputMessage("I don't know which you are referring to.");
               return false;
            }
//            referentPiles = lastPilesAttendedTo;
//            if (referentPiles.size() == table.numberOfPiles())
            if (referentCards.size() == table.numberOfCards() )
               lastIncludedAllCardsExplicitly = true;
            return true; // referent has been found
         }
         else if (lastIncludedAllCardsExplicitly)
         {
            referentType = "card";
            actualWhich = "anaphoric";
            ArrayList<PlayingCard> allCards = table.getCards();
            referentCards = listCardsConsistentWithIdea(allCards, cardIdea);
            if (referentCards.size() == 0)
            {
               outputMessage("I don't know which you are referring to.");
               return false;
            }
            return true;
         }
         else
         {
            outputMessage("I don't know which you are referring to.");
            return false;
         }
      }
      else if (referentDecision.equals("A4A6"))
      {

         // the last piles referred to, separately or together
         if (lastPilesAttendedTo == null || lastPilesAttendedTo.size() == 0)
         {
            outputMessage("I don't know what you are referring to.");
            return false;
         }
         else if (lastPilesAttendedTo.size() > 1)
         {
            actualWhich = "anaphoric";
            referentPiles = lastPilesAttendedTo;
            lastIncludedAllCardsExplicitly = false;
         }
         else
         {
             outputMessage("I assume you're referring to all the piles.");
             referentPiles = table.getPiles();
             lastPilesAttendedTo = referentPiles;
             actualWhich = "quantified";
         }
         referentType = "pile";
         return true;
      }
      else if (referentDecision.equals("D1")) // the card just
      {  // pointed to or moved, or a unique card in the pile(s) just
         // pointed to or moved
         referentType = "card";
         if ((cardIdea == null || cardIdea.isConsistentWithAnyAbstractCard() )
             && cardsIndicated.size() > 1)
         {
            outputMessage("I don't know which card you are referring to.");
            return false;
         }
         referentCards
            = listCardsConsistentWithIdea(cardsIndicated, cardIdea);
         if (referentCards.size() > 1)
         {
            outputMessage(
               "I don't know which of those cards you are referring to.");
            referentCards = null;
            return false;
         }
         else if (referentCards.size() == 0)
         {  // See if the card can be found in the pile containing the card
            // that was pointed to:
            ArrayList<PlayingCard> candidateCards
               = listCardsInPiles(pilesIndicated);
            referentCards
               = listCardsConsistentWithIdea(candidateCards, cardIdea);
            if (referentCards.size() == 0)
            {
               outputMessage(
                  "I don't know which card you are referring to.");
               referentCards = null;
               return false;
            }
            else if (referentCards.size() > 1)
            {
               outputMessage(
                  "I don't know which of those cards you are referring to.");
               referentCards = null;
               return false;
            }
         }
         lastCardsAttendedTo = referentCards;
         cardsIndicated.remove(cardsIndicated.get(0));
         if (cardsIndicated.size() == 0)
            newCardIndicated = false;  // all pointings have been used up
         actualWhich = "deictic";
         lastIncludedAllCardsExplicitly = false;
         return true;
      }
      else if (referentDecision.equals("D2")) // the pile containing
      {  // the card(s) most recently pointed to
  // System.out.println("cardsIndicated size = " + cardsIndicated.size());
         referentPiles = listPilesContainingCards(cardsIndicated);
  // System.out.println("referentPiles size = " + referentPiles.size());
         if (referentPiles.size() == 0)
         {
            outputMessage("I don't know which pile you are referring to.");
            return false;
         }
         else if (referentPiles.size() > 1)
         {
            CardPile onlyPile = findOnlyMultiCardPileAmong(referentPiles);
            if (onlyPile == null)
            {
               outputMessage("I don't know which of those piles you are referring to.");
               return false;
            }
            referentPiles = new ArrayList<CardPile>(1);
            referentPiles.add(onlyPile);
         }
//         else
//         {
         actualWhich = "deictic";
         referentType = "pile";
         lastIncludedAllCardsExplicitly = false;
         lastCardsAttendedTo = cardsIndicated;
         cardsIndicated = new ArrayList<PlayingCard>(1);
         newCardIndicated = false;
            // card pointings have all been used up
         lastPilesAttendedTo = referentPiles;
         return true;
//         }
      }
      else if (referentDecision.equals("D3D4"))
      {
         if (cardsIndicated.size() == 1)
         {  // each or all cards in the pile containing the card
            // pointed to
            referentType = "card";
            CardPile currentPile
               = table.whichPileHasCard(cardsIndicated.get(0));
            referentPiles = new ArrayList<CardPile>(1);
            referentPiles.add(currentPile);
            lastPilesAttendedTo = referentPiles;
            ArrayList<PlayingCard> candidateCards
               = listCardsInPiles(referentPiles);
            referentCards
               = listCardsConsistentWithIdea(candidateCards, cardIdea);
            if (referentCards.size() == 0)
            {
               outputMessage("I don't know which cards you are referring to.");
               return false;
            }
            lastCardsAttendedTo = referentCards;
            newCardIndicated = false;
            cardsIndicated = new ArrayList<PlayingCard>(1);
               // pointings for current command have been used up
            actualWhich = "deictic";
            lastIncludedAllCardsExplicitly = false;
            return true;
         }
         else if (cardsIndicated.size() > 1)
         {
            referentType = "card";
            actualWhich = "deictic";
            lastIncludedAllCardsExplicitly = false;
            if (cardIdea == null || cardIdea.isConsistentWithAnyAbstractCard() )
               referentCards = cardsIndicated;
            else
               referentCards
                  = listCardsConsistentWithIdea(cardsIndicated, cardIdea);
         }
         if (referentCards.size() == 0)
         {
            outputMessage("I don't know which card(s) you are referring to.");
            return false;
         }
         lastCardsAttendedTo = cardsIndicated;
         newCardIndicated = false;
         cardsIndicated = new ArrayList<PlayingCard>(1);
            // pointings for current command have been used up
         return true;
      }
      else if (referentDecision.equals("D5")) // two or more piles containing
      {  // cards just pointed to
         referentPiles = listPilesContainingCards(cardsIndicated);
         if (referentPiles.size() > 1)
         {
            actualWhich = "deictic";
            referentType = "pile";
            lastIncludedAllCardsExplicitly = false;
            lastCardsAttendedTo = cardsIndicated;
            newCardIndicated = false;
            cardsIndicated = new ArrayList<PlayingCard>(1);
            return true;
         }
         else if (referentPiles.size() == 1)
         {
            outputMessage(
               "You only pointed to one pile; I'll assume you want them all.");
            actualWhich = "quantified";
            actualWhich = "deictic";
            referentType = "pile";
            table.findPiles();
            referentPiles = table.getPiles();
            lastIncludedAllCardsExplicitly = false;
            lastCardsAttendedTo = table.getCards();
            newCardIndicated = false;
            cardsIndicated = new ArrayList<PlayingCard>(1);
            return true;
         }
      }
      else if (referentDecision.equals("Q1Q2")) // each or all cards
      {                                         // on the table
         referentType = "card";
         actualWhich = "quantified";
         if (cardIdea == null || cardIdea.isConsistentWithAnyAbstractCard() )
            lastIncludedAllCardsExplicitly = true;
         ArrayList<PlayingCard> allCards = table.getCards();
         referentCards = listCardsConsistentWithIdea(allCards, cardIdea);
         if (referentCards.size() == 0)
         {
            outputMessage("I don't know which cards you are referring to.");
            return false;
         }
         return true;
      }
      else if (referentDecision.equals("Q3Q4")) // each or all piles
      {                                       // on the table
         referentType = "pile";
         actualWhich = "quantified";
         referentPiles = table.getPiles();
         if (referentPiles.size() > 1)
         {
            referentType = "pile";
            lastIncludedAllCardsExplicitly = false;
         }
         else // only one pile, which includes all cards
         {
//            referentType = "card";
            lastIncludedAllCardsExplicitly
               = number.equals("plural");
         }
         return true;
      }
      else if (referentDecision.equals("Q5")) // the only card the
      {                        // command could sensibly refer to
         referentType = "card";
         actualWhich = "quantified";
         if ( cardIdea == null || cardIdea.isConsistentWithAnyAbstractCard() )
         {  // "the card"
            PlayingCard cd = null;
            if (lastCardsAttendedTo != null && lastCardsAttendedTo.size() == 1)
               cd = lastCardsAttendedTo.get(0);
            else
               cd = table.findOnlySingletonCard();
            if ( cd == null )
            {
               outputMessage("I don't know which card you are referring to.");
               return false;
            }
            referentCards = new ArrayList<PlayingCard>(1);
            referentCards.add(cd);
            lastCardsAttendedTo = new ArrayList<PlayingCard>(1);
            lastCardsAttendedTo.add(cd);
            lastIncludedAllCardsExplicitly = false;
            return true;
         }
         else if ( cardIdea.isFullySpecified() )
         {  // look for a single match of the cardIdea
            // among all the cards on the table
            ArrayList<PlayingCard> allCards = table.getCards();
            referentCards = listCardsConsistentWithIdea(allCards, cardIdea);
            if (referentCards.size() != 1)
            {
               outputMessage(
                  "I don't know which of those cards you are referring to.");
               return false;
            }
            return true;
         }
         else // the cardIdea is not fully specified;
         {  // look for a single match of the cardIdea
            // among the cards last referred to
            referentCards
               = listCardsConsistentWithIdea(lastCardsAttendedTo, cardIdea);
            if (referentCards.size() != 1)
            {
               outputMessage(
                  "I don't know which of those cards you are referring to.");
               referentCards = null;
               return false;
            }
            actualWhich = "anaphoric";
            lastIncludedAllCardsExplicitly = false;
            return true;
         }
      }
      else if (referentDecision.equals("Q6")) // the only pile the
      {                        // command could sensibly refer to
         referentType = "pile";
         actualWhich = "quantified";
         referentPiles = new ArrayList<CardPile>(1);
         referentPiles.add(table.findOnlyMultiCardPile());
         lastPilesAttendedTo = referentPiles;
         lastIncludedAllCardsExplicitly = false;
         return true;
      }
      else if (referentDecision.equals("E1")) // command doesn't make
      {                   // sense when applied to only one card
         outputMessage("I don't know how to " + command
            + " individual cards.");
         return false;
      }
      else
      {
         outputMessage("I don't know what you are referring to.");
         return false;
      }

      return true;

   } // end findReferents


// *** instance variables:

// These variables provide links to the agent's "world" and to other
// modules of its "mind":
   private CardTable table = null;
   private PlayingCardDeck deck = null;
   private ASDParser parser = null;
   private CardgramSemantics semantics = null;
      // semantic functions for Cardgram1
   private CardAgentEffectors effectors = null;
      // functions that operate on cards and piles on the CardTable
   private ASDDecider decider = null;
   private JTextField utteranceField = null;
      // for input sentences from the user
   private JTextArea outputPane = null;
      // for messages to the user

// These variables represent semantic feature values for the current
// command being processed:
   private String command = null;
   private String aggregation = "null";  // "null", "each", or "together"
   private String direction = "null";   // "null", "down", "over", or "up"
   private String orientation = "null";  // "null", "down", or "up"
   private String where = "null";
   private HashMap what = null;
   private String structureType = "null";
   // Both number and quantity are needed, because of phrases like
   // "each card" or "each one", which yield number "singular"
   // but quantity "several"
   private String number = "null"; // "singular" or "plural"
   private String quantity = "null"; // "one" or "several"
   private String which = "null"; // null, "anaphoric" or "deictic"
   private String inclusivity = "null"; // null or "all"

// These variables keep track of the things actually referred to
// and their structureType:
   private String actualWhich = "quantified";
      // "anaphoric", "deictic" or "quantified"
   private String referentType = "null";
      // structureType of actual referent, "card" or "pile"
   // The following two variables might ultimately be replaced by
   // a more general ArrayList<Object> referentObjects.
   private ArrayList<PlayingCard> referentCards = null;
      // referent(s) if referentType is "card"
   private ArrayList<CardPile> referentPiles = null;
      // referent(s) if referentType is "pile"

// These variables represent part of the "short term memory" of the agent:
   private boolean commandNeedsMoreThanOneCard = false;
      // true for "shuffle", "spread", "stack"
   private boolean newCardIndicated = false;
      // Has a new card been pointed to or moved?
   private boolean cardIsInAPile = false;
      // Is that card in a pile of two or more cards?
   private boolean newPointIndicated = false;
      // Has a new place on the table top been pointed to?
   private boolean lastIncludedAllCardsExplicitly = true;
      // Indicates whether the last operation was applied to all the cards
      // rather than to all of the piles or just some of the cards.
      // This is necessary to handle commands like
      // "turn over each card and stack them together" or
      // "shuffle all the cards and turn each over"
   private Point lastPointIndicated = null;  // last location pointed to,
      // or where the last operation was done.
   private boolean sentenceUsedExplicitDeictic = false;
      // Indicates whether the current sentence used an explicit deictic
      // adverb ("here" or "there").  If not, the lastPointIndicated
      // can be forgotten for the next sentence, unless a new point
      // is indicated for that sentence.
   private boolean cardsMoved = false; // indicates whether any cards
   // or piles have been moved since the last English input command
   private boolean eachUsed = false; // indicates whether the current
   // clause or the previous one in the same sentence used "each", as in
   // "shuffle each pile and spread it out", so that the singular "it"
   // can correctly be interpreted as referring to more than one thing.

   private String lastTypeReferredTo = "null"; //  "null", "card" or "pile"

   // referent structureTypes expected in search of decision network:
   private final String EXPECTED_REFERENT_TYPES =
      "A1 A2 A3A5 A4A6 D1 D2 D3D4 D5 E1 Q1Q2 Q3Q4 Q5 Q6";

// The following variable can be declared for future use in making
// the agent more general in its handling of referents:
//   private ArrayList<Object> lastThingsAttendedTo = null;

   private ArrayList<PlayingCard> cardsIndicated = null;
   private ArrayList<CardPile> pilesIndicated = null;
      // cards and piles pointed to for the current command.
      // They are remembered in lastCardsAttendedTo,
      // but as deictic references to them are identified,
      // they are removed from cardsIndicated, so they will not
      // be considered referred to deictically more than once.
      // Instead, subsequent references to them in the same
      // command should be considered anaphoric.
// The following two variables should be replaceable
// ultimately by use of lastThingsAttendedTo:
//   private PlayingCard lastCardAttendedTo = null;
   private ArrayList<PlayingCard> lastCardsAttendedTo = null;
      // for short-term memory
   private ArrayList<CardPile> lastPilesAttendedTo = null;
      // for short-term memory
}

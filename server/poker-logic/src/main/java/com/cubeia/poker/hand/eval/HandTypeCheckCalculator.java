package com.cubeia.poker.hand.eval;

import static com.cubeia.poker.hand.HandType.FLUSH;
import static com.cubeia.poker.hand.HandType.FULL_HOUSE;
import static com.cubeia.poker.hand.HandType.HIGH_CARD;
import static com.cubeia.poker.hand.HandType.PAIR;
import static com.cubeia.poker.hand.HandType.STRAIGHT;
import static com.cubeia.poker.hand.HandType.THREE_OF_A_KIND;
import static com.cubeia.poker.hand.HandType.TWO_PAIRS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.HandType;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.hand.Suit;
import com.cubeia.poker.hand.calculator.ByRankCardComparator;

/**
 * <p>Component for checking a hand for specific hand types, e.g. full house, flush etc.</p>
 * 
 * <p>The idea is that this component shall be portable over different variants of poker
 * since most hand checks will be the same even if the type ranking differs.</p>
 * 
 * <p><em>Design Note: If you are extending or copy-pasting from this class you are
 * probably not doing it correctly.</em></p>
 *
 * <p><em>Design Note 2: Try to keep as litle state as possible in this class.</em></p>
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class HandTypeCheckCalculator {
	
	/**
	 * Some poker variants (e.g. Telesina) allows for different deck sizes.
	 * In the case of a cut deck we need to know the lower rank for the cases
	 * where Aces are allowed as high or low card in straights.
	 */
	private Rank deckLowestRank = Rank.TWO;

	
	
	/* ----------------------------------------------------
	 * 	
	 * 	CONSTRUCTORS
	 *  
	 *  ---------------------------------------------------- */
	
	public HandTypeCheckCalculator() {}
	
	public HandTypeCheckCalculator(Rank deckLowestRank) {
		this.deckLowestRank = deckLowestRank;
	}
	
	
	
	/* ----------------------------------------------------
	 * 	
	 * 	INSPECT HAND METHODS
	 * 
	 * 	Inspect hand for specific hand types and get the 
	 *  corresponding hand strength. 
	 *  
	 *  ---------------------------------------------------- */

	public HandStrength checkStraightFlush(Hand hand) {
		HandStrength strength = null;
		if (checkFlush(hand) != null && checkStraight(hand) != null) {
			strength = new HandStrength(HandType.STRAIGHT_FLUSH);
			strength.setHighestRank(hand.sort().getCards().get(0).getRank());
		}
		return strength;
	}
	
	/**
	 * <p>Checks if all cards are the same suit, regardless of the number of cards.</p>
	 * 
	 * @return HandStrenght, null if not flush.
	 */
	public HandStrength checkFlush(Hand hand) {
		return checkFlush(hand, 1);
	}
	
	/**
	 * <p>Checks if all cards are the same suit and the number of cards are enough</p>
	 * 
	 * @return HandStrenght, null if not flush.
	 */
	@SuppressWarnings("unchecked")
	public HandStrength checkFlush(Hand hand, int minimumNumberOfCards) {
		if (hand.getCards().isEmpty() || hand.getNumberOfCards() < minimumNumberOfCards) {
			return null;
		}
		
		boolean flush = true;
		Suit lastSuit = null;
		HandStrength strength = null;
		for (Card card : hand.getCards()) {
			if (lastSuit != null && !card.getSuit().equals(lastSuit)) {
				flush = false;
				break;
			}
			lastSuit = card.getSuit();
		}
		if (flush) {
			strength = new HandStrength(FLUSH);
			strength.setHighestRank(hand.sort().getCards().get(0).getRank());
			
			// Sort all cards used
			List<Card> sorted = new ArrayList<Card>(hand.getCards());
			Collections.sort(sorted, ByRankCardComparator.ACES_HIGH_DESC);
			strength.setCardsUsedInHand(sorted);
			
			// Insert group
			strength.setGroups(sorted);
		}
		
		return strength;
	}
	
	/**
	 * <p>Checks if all cards are a straight, regardless of the number of cards.</p>
	 * 
	 * <p><strong>Note: </strong><em>Assumes that you have executed a sort (Hand.sortAscending) on the hand first!</em></p>
	 */
	public HandStrength checkStraight(Hand hand) {
		return checkStraight(hand, false);
	}
	
	/**
	 * Check for straights. Hands with only cards will never be reported 
	 * as a straight.
	 * 
	 * @param hand
	 * @param acesAreLow
	 * 
	 * @return HandStrength, null if not a straight
	 */
	// FIXME: Refactor this to be more *nice*
	@SuppressWarnings("unchecked")
	public HandStrength checkStraight(Hand hand, boolean acesAreLow) {
		
		if (hand.getCards().isEmpty() || hand.getNumberOfCards() < 2) {
			return null;
		}
		
		List<Card> cards = hand.getCards();
		if (acesAreLow) {
			Collections.sort(cards, ByRankCardComparator.ACES_LOW_DESC);
		} else {
			Collections.sort(cards, ByRankCardComparator.ACES_HIGH_DESC);
		}
		
		HandStrength strength = null;
		boolean straight = true;
		Rank lastRank = null;
		for (Card card : cards) {
			if (lastRank != null) {
				if (card.getRank().ordinal() != lastRank.ordinal() - 1 ) {
					
					if (acesAreLow) {
						// Check if ACE in the bottom of the straight
						if (acesAreLow && !(lastRank.equals(deckLowestRank) && card.getRank().equals(Rank.ACE))) {
							straight = false;
							break;
						}
					} else {
						straight = false;
						break;
					}
					
				}
			}
			lastRank = card.getRank();
		}
		
		if (straight) {
			strength = new HandStrength(STRAIGHT);
			strength.setHighestRank(cards.get(0).getRank());
			
			// Sort all cards used
			List<Card> sorted = new ArrayList<Card>(hand.getCards());
			if (acesAreLow) {
				Collections.sort(sorted, ByRankCardComparator.ACES_LOW_ASC);
			} else {
				Collections.sort(sorted, ByRankCardComparator.ACES_HIGH_ASC);
			}
			
			strength.setCardsUsedInHand(sorted);
			
			// Insert group
			List<Card> highestCard = Arrays.asList(sorted.get(sorted.size() - 1));
			List<Card> secondCard = Arrays.asList(sorted.get(sorted.size() - 2));
			strength.setGroups(highestCard, secondCard);
		}
		
		return strength;
	}
	
	
	/**
	 * Check for three and four of a kind. Will return with the 
	 * highest rank that matches the number of cards that is looked for.
	 * 
	 * @param hand
	 * @param number, number of same rank to look for, i.e. 3 = three of a kind
	 * @return the highest match found or null if not found
	 */
	@SuppressWarnings("unchecked")
	public HandStrength checkManyOfAKind(Hand hand, int number) {
		List<Card> cards = hand.sort().getCards();
		
		HandStrength strength = null;
		Rank lastRank = null;
		int count = 1;
		
		for (Card card : cards) {
			if (lastRank != null) {
				if (card.getRank().ordinal() == lastRank.ordinal()) {
					// We have found another card with the same rank.
					count++;
					if (count == number) {
						strength = new HandStrength(getType(number));
						strength.setHighestRank(card.getRank());

						// Get kicker cards, remove them from main card list,
						// sort them and then add them to main card list again.
						// The idea is that we want the pair cards first and
						// then the kickers in an ordered fashion.
						List<Card> kickers = removeAllRanks(card.getRank(), cards);
						cards.removeAll(kickers);
						
						Collections.sort(kickers, ByRankCardComparator.ACES_HIGH_DESC);
						cards.addAll(kickers);
						
						strength.setKickerCards(kickers);
						strength.setCardsUsedInHand(cards);
						
						// See javadoc in HandStrength for group values
						List<Card> unsuitedPair = Arrays.asList(new Card(card.getRank(), Suit.HEARTS));
						List<Card> cardSet = getAllWithRank(card.getRank(), cards);
						
						strength.setGroups(unsuitedPair, kickers, cardSet);
						
						break; // Break since we are starting with highest rank
					}
				} else {
					// Not a match so reset the counter
					count = 1;
				}
			}
			lastRank = card.getRank();
		}
		
		return strength;
	}

	public HandStrength checkFullHouse(Hand hand) {
		return checkDoubleManyCards(hand, 3);
	}

	public HandStrength checkTwoPairs(Hand hand) {
		return checkDoubleManyCards(hand, 2);
	}

	/**
	 * 
	 * @param hand
	 * @param number, the number to check highest multiple. I.e. 2 = two pair, 3 = full house
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private HandStrength checkDoubleManyCards(Hand hand, int number) {
		HandStrength strength = null;
		HandStrength firstPair = checkManyOfAKind(hand, number);
		if (firstPair != null) {
				
			List<Card> cards = removeAllRanks(firstPair.getHighestRank(), hand.getCards());
			
			Hand secondPairHand = new Hand(cards);
			HandStrength secondPair = checkManyOfAKind(secondPairHand, 2);
			
			if (secondPair != null) {
				if (number == 2) {
					strength = new HandStrength(TWO_PAIRS);
				} else if (number == 3) {
					strength = new HandStrength(FULL_HOUSE);
				}
				
				strength.setHighestRank(firstPair.getHighestRank());
				strength.setSecondRank(secondPair.getHighestRank());
				
				List<Card> kickers = removeAllRanks(secondPair.getHighestRank(), secondPairHand.getCards());
				strength.setKickerCards(kickers);
				
				List<Card> usedCards = new ArrayList<Card>();
				usedCards.addAll(getAllWithRank(firstPair.getHighestRank(), hand.getCards()));
				usedCards.addAll(getAllWithRank(secondPair.getHighestRank(), hand.getCards()));
				usedCards.addAll(kickers);
				strength.setCardsUsedInHand(usedCards);
			
				// See Javadoc in HandStrength for the proper group values for PAIR
				List<Card> unsuitedPair = Arrays.asList(new Card(strength.getHighestRank(), Suit.HEARTS));
				List<Card> unsuitedSecondPair = Arrays.asList(new Card(strength.getSecondRank(), Suit.HEARTS));
				List<Card> cardSet = getAllWithRank(strength.getHighestRank(), hand.getCards());
				
				strength.setGroups(unsuitedPair, unsuitedSecondPair, kickers, cardSet);
				
			}
		}
		return strength;
	}
	
	@SuppressWarnings("unchecked")
	public HandStrength checkHighCard(Hand hand) {
		
		if (hand.getCards().isEmpty()) {
			return null;
		}
		
		HandStrength strength = new HandStrength(HIGH_CARD);
		
		List<Card> sorted = new LinkedList<Card>(hand.getCards());
		Collections.sort(sorted, ByRankCardComparator.ACES_HIGH_DESC);
		
		strength.setHighestRank(sorted.get(0).getRank());
		if (sorted.size() >= 2) {
			strength.setSecondRank(sorted.get(1).getRank());
		}
		strength.setKickerCards(sorted);
		
		strength.setCardsUsedInHand(sorted);
		strength.setGroups(sorted);
		return strength;
	}
	
	/* ----------------------------------------------------
	 * 	
	 * 	PRIVATE METHODS
	 *  
	 *  ---------------------------------------------------- */
	
	private List<Card> removeAllRanks(Rank rank, List<Card> cards) {
		List<Card> result = new ArrayList<Card>();
		for (Card card : cards) {
			if (!card.getRank().equals(rank)) {
				result.add(card);
			}
		}
		return result;
	}
	
	private List<Card> getAllWithRank(Rank rank, List<Card> cards) {
		List<Card> result = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getRank().equals(rank)) {
				result.add(card);
			}
		}
		return result;
	}


	private HandType getType(int number) {
		switch (number) {
			case 2: return PAIR;
			case 3: return THREE_OF_A_KIND;
			case 4: return HandType.FOUR_OF_A_KIND;
			default: throw new IllegalArgumentException("Invalid number of cards for hand type");
		}
	}


	
}

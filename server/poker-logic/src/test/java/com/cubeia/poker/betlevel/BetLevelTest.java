/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.poker.betlevel;

import com.cubeia.poker.AbstractTexasHandTester;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.action.PokerActionType;

/**
 * Integration test for poker logic.
 */
public class BetLevelTest extends AbstractTexasHandTester {

	/**
	 * Mock Game is staked at 500/250
	 * Mock Players have a chip count of 5000.
	 */
	public void testSimpleHoldemHand() {
		MockPlayer[] mp = testUtils.createMockPlayers(4);
		int[] p = testUtils.createPlayerIdArray(mp);
		assertEquals(4, p.length);
		addPlayers(game, mp);
		assertEquals(4, game.getSeatedPlayers().size());

		// Force start
		game.timeout();
		assertEquals(101, mockServerAdapter.getActionRequest().getPlayerId());
		assertTrue(mp[1].isActionPossible(PokerActionType.SMALL_BLIND));
		assertEquals(250, mockServerAdapter.getActionRequest().getOption(PokerActionType.SMALL_BLIND).getMinAmount());
		assertEquals(250, mockServerAdapter.getActionRequest().getOption(PokerActionType.SMALL_BLIND).getMaxAmount());

		// Blinds
		act(p[1], PokerActionType.SMALL_BLIND);
		assertTrue(mp[2].isActionPossible(PokerActionType.BIG_BLIND));
		assertEquals(500, mockServerAdapter.getActionRequest().getOption(PokerActionType.BIG_BLIND).getMinAmount());
		assertEquals(500, mockServerAdapter.getActionRequest().getOption(PokerActionType.BIG_BLIND).getMaxAmount());
		act(p[2], PokerActionType.BIG_BLIND); // 
		
		// Now player 3 should be able to FOLD, CALL or RAISE. 
		// Verify available actions and amounts
		assertEquals(103, mockServerAdapter.getActionRequest().getPlayerId());
		assertEquals(0, mockServerAdapter.getActionRequest().getOption(PokerActionType.FOLD).getMinAmount());
		assertEquals(0, mockServerAdapter.getActionRequest().getOption(PokerActionType.FOLD).getMaxAmount());
		assertEquals(500, mockServerAdapter.getActionRequest().getOption(PokerActionType.CALL).getMinAmount());
		assertEquals(500, mockServerAdapter.getActionRequest().getOption(PokerActionType.CALL).getMaxAmount());
		assertEquals(1000, mockServerAdapter.getActionRequest().getOption(PokerActionType.RAISE).getMinAmount());
		assertEquals(mp[3].getBalance(), mockServerAdapter.getActionRequest().getOption(PokerActionType.RAISE).getMaxAmount());
		
		act(p[3], PokerActionType.CALL); 
		act(p[0], PokerActionType.CALL);
		act(p[1], PokerActionType.CALL);
		act(p[2], PokerActionType.CHECK);
		
		// Family pot, all remaining balances = 4500
		assertEquals(2000, game.getPotHolder().getTotalPotSize());
		
		// Trigger deal community cards
		game.timeout();
		
		assertEquals(p[1], mockServerAdapter.getActionRequest().getPlayerId());
		assertTrue(mp[1].isActionPossible(PokerActionType.CHECK));
		assertEquals(500, mockServerAdapter.getActionRequest().getOption(PokerActionType.BET).getMinAmount());
		assertEquals(4500, mockServerAdapter.getActionRequest().getOption(PokerActionType.BET).getMaxAmount());
		act(p[1], PokerActionType.BET, 1000);
		
		assertTrue(mp[2].isActionPossible(PokerActionType.CALL));
		assertEquals(1000, mockServerAdapter.getActionRequest().getOption(PokerActionType.CALL).getMinAmount());
		assertEquals(1000, mockServerAdapter.getActionRequest().getOption(PokerActionType.CALL).getMaxAmount());
		assertTrue(mp[2].isActionPossible(PokerActionType.RAISE));
		// Minimum raise is by last bet
		assertEquals(2000, mockServerAdapter.getActionRequest().getOption(PokerActionType.RAISE).getMinAmount());
		assertEquals(mp[2].getBalance(), mockServerAdapter.getActionRequest().getOption(PokerActionType.RAISE).getMaxAmount());
		
		act(p[2], PokerActionType.CALL);
		act(p[3], PokerActionType.RAISE, 2000);
		
		assertEquals(p[0], mockServerAdapter.getActionRequest().getPlayerId());
		assertTrue(mp[0].isActionPossible(PokerActionType.CALL));
		assertEquals(2000, mockServerAdapter.getActionRequest().getOption(PokerActionType.CALL).getMinAmount());
		assertEquals(2000, mockServerAdapter.getActionRequest().getOption(PokerActionType.CALL).getMaxAmount());
		
		assertTrue(mp[0].isActionPossible(PokerActionType.RAISE));
		assertEquals(3000, mockServerAdapter.getActionRequest().getOption(PokerActionType.RAISE).getMinAmount());
		assertEquals(4500, mockServerAdapter.getActionRequest().getOption(PokerActionType.RAISE).getMaxAmount());
		
		act(p[0], PokerActionType.CALL, 2000);
		
	}
	
	
	
	/**
	 * Mock Game is staked at 10/5'
	 */
	public void testAllInHoldemHand() {
		game.setAnteLevel(10);
		MockPlayer[] mp = testUtils.createMockPlayers(8);
		int[] p = testUtils.createPlayerIdArray(mp);
		addPlayers(game, mp);
		
		// Set initial balances
		mp[0].setBalance(40280);
		mp[1].setBalance(10000);
		
		// Force start
		game.timeout();
		// Blinds
		act(p[1], PokerActionType.SMALL_BLIND);	// player[39]
		act(p[2], PokerActionType.BIG_BLIND); 	// player[4]
		act(p[3], PokerActionType.FOLD);		// player[3]
		act(p[4], PokerActionType.RAISE, 20);	// player[32]
		act(p[5], PokerActionType.CALL);		// player[94]
		act(p[6], PokerActionType.RAISE, 210);	// player[11]
		act(p[7], PokerActionType.FOLD);		// player[66]
		act(p[0], PokerActionType.CALL);		// player[60]
		
		act(p[1], PokerActionType.RAISE, 10000);
		act(p[2], PokerActionType.CALL);
		act(p[4], PokerActionType.CALL);
		act(p[5], PokerActionType.CALL);
		act(p[6], PokerActionType.CALL);
		
		// Since all other players are all in now, p[0] should not be allowed to raise
		assertTrue(mp[0].isActionPossible(PokerActionType.CALL));
		assertFalse(mp[0].isActionPossible(PokerActionType.RAISE));
	}

}

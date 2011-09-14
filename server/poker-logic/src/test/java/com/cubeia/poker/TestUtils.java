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

package com.cubeia.poker;

import org.junit.Ignore;

import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.player.PokerPlayer;

@Ignore("not a test")
public class TestUtils {
	
	public MockPlayer[] createMockPlayers(int n) {
		return createMockPlayers(n, 5000);
	}
	
	public MockPlayer[] createMockPlayers(int n, long balance) {
		MockPlayer[] r = new MockPlayer[n];

		for (int i = 0; i < n; i++) {
			r[i] = new MockPlayer(i);
			r[i].setSeatId(i);
			r[i].setBalance(balance);
		}

		return r;		
	}	

	public int[] createPlayerIdArray(MockPlayer[] mp) {
		int[] ids = new int[mp.length];
		
		for (int i = 0; i < mp.length; i++) {
			ids[i] = mp[i].getId();
		}
		
		return ids;
	}
	
	public void addPlayers(PokerState game, PokerPlayer[] p, long startingChips) {
		for (PokerPlayer pl : p) {
			game.addPlayer(pl);
			game.addChips(pl.getId(), startingChips);
		}
	}
	
	public void addPlayers(PokerState game, PokerPlayer[] p) {
		addPlayers(game, p, 10000);
	}

	public void act(PokerState game, int playerId, PokerActionType actionType) {
		game.act(new PokerAction(playerId, actionType));
	}


}

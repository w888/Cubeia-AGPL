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

package com.cubeia.poker.rounds.blinds;

import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import org.apache.log4j.Logger;

public class WaitingForEntryBetState extends AbstractBlindsState {

    private static final long serialVersionUID = 1L;

    private static transient Logger log = Logger.getLogger(WaitingForEntryBetState.class);

    @Override
    public boolean bigBlind(int playerId, PokerContext context, BlindsRound blindsRound) {
        PokerPlayer player = context.getPlayerInCurrentHand(playerId);
        if (player.getActionRequest().isOptionEnabled(PokerActionType.BIG_BLIND)) {
            player.setHasPostedEntryBet(true);
            player.addBetOrGoAllIn(context.getSettings().getBigBlindAmount());
            blindsRound.entryBetPosted();
            return true;
        } else {
            log.info("Player " + player + " is not allowed to post big blind. Options were " + player.getActionRequest());
            return false;
        }
    }

    @Override
    public boolean declineEntryBet(int playerId, PokerContext context, BlindsRound blindsRound) {
        PokerPlayer player = context.getPlayerInCurrentHand(playerId);
        if (player.getActionRequest().isOptionEnabled(PokerActionType.DECLINE_ENTRY_BET)) {
            blindsRound.entryBetDeclined(player);
            return true;
        } else {
            log.info("Player " + player + " is not allowed to decline entry bet.");
            return false;
        }
    }

    @Override
    public boolean deadSmallBlind(int playerId, PokerContext context, BlindsRound round) {
        PokerPlayer player = context.getPlayerInCurrentHand(playerId);
        if (player.getActionRequest().isOptionEnabled(PokerActionType.DEAD_SMALL_BLIND)) {
            player.setHasPostedEntryBet(true);
            int amount = context.getSettings().getSmallBlindAmount();
            long takenAmount = player.takeChipsOrGoAllIn(amount);
            context.getPotHolder().getActivePot().bet(player, Long.valueOf(takenAmount));
            round.entryBetPosted();
            return true;
        } else {
            log.info("Player " + player + " is not allowed to post big blind. Options were " + player.getActionRequest());
            return false;
        }
    }

    @Override
    public boolean bigBlindPlusDeadSmallBlind(int playerId, PokerContext context, BlindsRound round) {
        PokerPlayer player = context.getPlayerInCurrentHand(playerId);
        if (player.getActionRequest().isOptionEnabled(PokerActionType.BIG_BLIND_PLUS_DEAD_SMALL_BLIND)) {
            player.setHasPostedEntryBet(true);
            player.addBetOrGoAllIn(context.getSettings().getBigBlindAmount());
            int deadAmount = context.getSettings().getSmallBlindAmount();
            long amountTaken = player.takeChipsOrGoAllIn(deadAmount);
            context.getPotHolder().getActivePot().bet(player, Long.valueOf(amountTaken));
            round.entryBetPosted();
            return true;
        } else {
            log.info("Player " + player + " is not allowed to post big blind plus dead small blind. Options were " + player.getActionRequest());
            return false;
        }
    }

    @Override
    public boolean timeout(PokerContext context, BlindsRound round) {
        int entryBetter = round.getPendingEntryBetterId();
        declineEntryBet(entryBetter, context, round);
        return true;
    }

}
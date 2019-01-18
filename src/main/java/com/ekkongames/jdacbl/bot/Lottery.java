package com.ekkongames.jdacbl.bot;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public final class Lottery {

    private final float winChance;
    private final String winMessage;

    /**
     * @param winChance a probability of a given message being a winner, on the interval [0, 1]
     * @param winMessage a message to display when this lottery is won
     */
    public Lottery(float winChance, String winMessage) {
        this.winChance = winChance;
        this.winMessage = winMessage;
    }

    public float getWinChance() {
        return winChance;
    }

    public String getWinMessage() {
        return winMessage;
    }
}

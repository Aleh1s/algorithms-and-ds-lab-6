package org.example.model.game;

import org.example.model.game.card.Card;

import java.util.LinkedList;
import java.util.Objects;

public class Entry {

    private final LinkedList<Card> defending;
    private final LinkedList<Card> hitting;
    private boolean isBeaten;

    public Entry() {
        this.defending = new LinkedList<>();
        this.hitting = new LinkedList<>();
    }

    private Entry(LinkedList<Card> defending, LinkedList<Card> hitting) {
        this.defending = defending;
        this.hitting = hitting;
    }

    public static Entry newEntry() {
        return new Entry();
    }

    public LinkedList<Card> getDefending() {
        return defending;
    }

    public LinkedList<Card> getHitting() {
        return hitting;
    }


    public boolean isBeaten() {
        return isBeaten;
    }

    public void setBeaten(boolean beaten) {
        isBeaten = beaten;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry entry)) return false;

        if (isBeaten != entry.isBeaten) return false;
        if (!Objects.equals(defending, entry.defending)) return false;
        return Objects.equals(hitting, entry.hitting);
    }

    @Override
    public int hashCode() {
        int result = defending.hashCode();
        result = 31 * result + hitting.hashCode();
        result = 31 * result + (isBeaten ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "defending=" + defending +
                ", hitting=" + hitting +
                ", isBeaten=" + isBeaten +
                '}';
    }

    public Entry copy() {
        return new Entry(
                new LinkedList<>(this.defending),
                new LinkedList<>(this.hitting)
        );
    }
}

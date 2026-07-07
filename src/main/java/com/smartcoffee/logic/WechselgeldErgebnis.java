package com.smartcoffee.logic;

import java.util.List;

/**
 * Holds the definitive outcome of a change-making calculation sequence.
 * This immutable container encapsulates whether the transaction succeeded,
 * the specific inventory list of coins to be dispensed, and the corresponding notification state.
 *
 * @param erfolgreich        true if exact change could be successfully calculated and provisioned; false otherwise
 * @param ausgegebeneMuenzen the list of specific {@link Muenze} constants to return to the user as change
 * @param Meldung            the specific {@link ZahlungsMeldung} status describing the payment outcome
 */
public record WechselgeldErgebnis(
        boolean erfolgreich,
        List<Muenze> ausgegebeneMuenzen,
        ZahlungsMeldung Meldung)
{
}
package com.smartcoffee.logic;

/**
 * Represents the definitive status outcomes of a payment transaction processing attempt.
 * Used to deliver user-facing feedback messages depending on transaction success or failure.
 */
public enum ZahlungsMeldung
{

    ERFOLGREICH("Zahlvorgang erfolgreich"),
    ERFOLGREICH_WECHSELGELD("Zahlvorgang erfolgreich, bitte Wechselgeld entnehmen"),
    FEHLER_KEIN_WECHSELGELD("Zahlvorgang fehlgeschlagen, kein Wechselgeld vorhanden");

    /** The descriptive system text corresponding to the payment status. */
    private final String text;

    /**
     * Constructs a payment message status constant with its user-facing feedback string.
     *
     * @param text the descriptive payment feedback message
     */
    ZahlungsMeldung(String text)
    {
        this.text = text;
    }

    /**
     * Gets the user-facing description text associated with the payment outcome.
     *
     * @return the clear description text
     */
    public String getText()
    {
        return text;
    }
}

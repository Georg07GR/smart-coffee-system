package com.smartcoffee.logic;

/**
 * Represents the types of coins accepted by the coffee machine's payment system.
 * All monetary values are maintained strictly as integers in cents to eliminate
 * floating-point calculation errors during transactions.
 */
public enum Muenze
{

    CENT_1 (1, "1 Ct"),
    CENT_2 (2, "2 Ct"),
    CENT_5 (5, "5 Ct"),
    CENT_10 (10, "10 Ct"),
    CENT_20 (20, "20 Ct"),
    CENT_50 (50, "50 Ct"),
    EURO_1 (100, "1 €"),
    EURO_2 (200, "2 €");

    /** The monetary value represented strictly in cents. */
    private final int wertInCents;
    /** The user-friendly text representation displayed on the UI. */
    private final String anzeigeName;

    /**
     * Constructs a coin constant with its designated cent value and display name.
     *
     * @param wertInCents the integer value of the coin in cents
     * @param anzeigeName the formatted display string for the user interface
     */
    Muenze(int wertInCents, String anzeigeName)
    {
        this.wertInCents = wertInCents;
        this.anzeigeName = anzeigeName;
    }

    /**
     * Gets the monetary value of the coin in cents.
     *
     * @return the coin value as an integer
     */
    public int getWertInCents()
    {
        return wertInCents;
    }

    /**
     * ets the monetary value of the coin in Euro
     * @return
     */
    public double getWertInEuro()
    {
        return wertInCents / 100.0;
    }

    public static Muenze fromEuroValue(double euroValue)
    {
        int centValue = (int) Math.round(euroValue * 100);
        for (Muenze muenze : Muenze.values())
        {
            if (muenze.getWertInCents() == centValue)
            {
                return muenze;
            }
        }
        throw new IllegalArgumentException("No coin found for the given Euro value: " + euroValue);
    }


    /**
     * Gets the user-friendly display name of the coin.
     *
     * @return the formatted display name string
     */
    public String getAnzeigeName()
    {
        return anzeigeName;
    }
}

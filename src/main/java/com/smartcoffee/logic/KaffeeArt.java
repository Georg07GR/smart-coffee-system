package com.smartcoffee.logic;

/**
 * Defines the types of beverages available in the coffee machine.
 * Stores configuration details for each beverage, including display names,
 * pricing, and ingredient profiles (milk requirement).
 */
public enum KaffeeArt
{
    ESPRESSO("Espresso", 1.50, false),
    BLACK_COFFEE("Schwarzer Kaffee", 2.00, false),
    CAPPUCCINO("Cappuccino", 2.00, true),
    LATE("Late", 2.50, true),
    LATTE_MACCHIATO("Latte Macchiato", 2.50, true),
    COFFEE_WITH_MILK("Milch Kaffee", 2.00, true),
    HOT_CHOCOLATE("Heiße Schokolade", 2.50, true);

    /** The user-friendly text representation displayed on the UI. */
    private final String anzeigeName;
    /** The retail price of the beverage in Euros. */
    private final double preis;
    /** Flag indicating whether the beverage preparation requires milk powder. */
    private final boolean mitmilch;

    /**
     * Constructs a beverage configuration option.
     *
     * @param anzeigeName the formatted display string for the user interface
     * @param preis the retail price in Euros
     * @param mitmilch true if the beverage requires milk powder, false otherwise
     */
    KaffeeArt(String anzeigeName, double preis, boolean mitmilch)
    {
        this.anzeigeName = anzeigeName;
        this.preis = preis;
        this.mitmilch = mitmilch;
    }

    /**
     * Gets the user-friendly display name of the beverage.
     *
     * @return the formatted display name string
     */
    public String getAnzeigeName()
    {
        return anzeigeName;
    }

    /**
     * Gets the retail price of the beverage.
     *
     * @return the price in Euros as a double value
     */
    public double getPreis()
    {
        return preis;
    }

    /**
     * Gets the price of the beverage in cents.
     *
     * @return the price in cents as an integer
     */
    public int getPreisInCents()
    {
        return (int) Math.round(preis * 100);
    }

    /**
     * Checks if the beverage requires milk powder during preparation.
     *
     * @return true if milk powder is required, false otherwise
     */
    public boolean isMitmilch()
    {
        return mitmilch;
    }
}

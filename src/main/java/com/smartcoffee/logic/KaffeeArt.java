package com.smartcoffee.logic;

public enum KaffeeArt
{
    ESPRESSO("Espresso", 1.50, false),
    BLACK_COFFEE("Schwarzer Kaffee", 2.00, true),
    CAPPUCCINO("Cappuccino", 2.00, false),
    LATE("Late", 2.50, false),
    LATTE_MACCHIATO("Latte Macchiato", 2.50, true),
    COFFEE_WITH_MILK("Milch Kaffee", 2.00, true),
    HOT_CHOCOLATE("Heiße Schokolade", 2.50, true);


    private final String anzeigeName;
    private final double preis;
    private final boolean mitmilch;

    KaffeeArt(String anzeigeName, double preis, boolean mitmilch)
    {
        this.anzeigeName = anzeigeName;
        this.preis = preis;
        this.mitmilch = mitmilch;
    }

    public String getAnzeigeName()
    {
        return anzeigeName;
    }

    public double getPreis()
    {
        return preis;
    }

    public boolean isMitmilch()
    {
        return mitmilch;
    }
}

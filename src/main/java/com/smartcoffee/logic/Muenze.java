package com.smartcoffee.logic;

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


    private final int wertInCents;
    private final String anzeigeName;


    Muenze(int wertInCents, String anzeigeName)
    {
        this.wertInCents = wertInCents;
        this.anzeigeName = anzeigeName;
    }

    public int getWertInCents()
    {
        return wertInCents;
    }

    public String getAnzeigeName()
    {
        return anzeigeName;
    }
}

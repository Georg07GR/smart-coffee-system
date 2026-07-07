package com.smartcoffee.logic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Münzwechsler
{
    private final Map<Muenze, Integer> muenzenBestand;


    public Münzwechsler()
    {
        this.muenzenBestand = new EnumMap<>(Muenze.class);

        for (Muenze m : Muenze.values())
        {
           this.muenzenBestand.put(m, 10);
        }
    }

    public Map<Muenze, Integer> getMuenzBestand()
    {

        return muenzenBestand;
    }

    public WechselgeldErgebnis wechselgeldBerechnen(int preisInCents, List<Muenze> eingeworfen)
    {
        int summeEingeworfen = 0;
        for (Muenze m : eingeworfen)
        {
            summeEingeworfen += m.getWertInCents();
        }

        if (summeEingeworfen < preisInCents)
        {
            return new WechselgeldErgebnis(false, eingeworfen, ZahlungsMeldung.FEHLER_KEIN_WECHSELGELD);
        }
        else if (summeEingeworfen == preisInCents)
        {
            for (Muenze m : eingeworfen)
            {
                muenzenBestand.put(m, muenzenBestand.get(m) + 1);
            }
            return new WechselgeldErgebnis(true, List.of(), ZahlungsMeldung.ERFOLGREICH);
        }
        else
        {
            int wechselgeldInCents = summeEingeworfen - preisInCents;
            Map<Muenze, Integer> tempStock = new EnumMap<>(muenzenBestand);

            for (Muenze m : eingeworfen)
            {
                tempStock.put(m, tempStock.get(m) + 1);
            }

            List<Muenze> changeCoinsToReturn = new ArrayList<>();
            Muenze[] allCoins = Muenze.values();
            for (int i = allCoins.length - 1; i >= 0; i--)
            {
                Muenze coin = allCoins[i];
                int coinValue = coin.getWertInCents();

                while (wechselgeldInCents >= coinValue && tempStock.getOrDefault(coin, 0) > 0)
                {
                    wechselgeldInCents -= coinValue;
                    tempStock.put(coin, tempStock.get(coin) - 1);
                    changeCoinsToReturn.add(coin);
                }
            }

            if (wechselgeldInCents == 0)
            {
                this.muenzenBestand.clear();
                this.muenzenBestand.putAll(tempStock);
                return new WechselgeldErgebnis(true, changeCoinsToReturn, ZahlungsMeldung.ERFOLGREICH_WECHSELGELD);
            } else
            {
                return new WechselgeldErgebnis(false, eingeworfen, ZahlungsMeldung.FEHLER_KEIN_WECHSELGELD);
            }
        }
    }
}

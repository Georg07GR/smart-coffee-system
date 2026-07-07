package com.smartcoffee.logic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Verwaltet den physischen Münzbestand des Kaffeeautomaten und berechnet das Wechselgeld.
 * Die Klasse stellt sicher, dass Transaktionen atomar (rollbacksicher) durchgeführt werden.
 * Wenn ein Wechselgeld nicht passend herausgegeben werden kann, wird die Transaktion abgebrochen.
 */
public class Münzwechsler
{
    /** Der aktuelle Münzbestand des Automaten, der jede Münzart ihrer Anzahl zuordnet. */
    private final Map<Muenze, Integer> muenzenBestand;


    /**
     * Erzeugt einen neuen Münzwechsler und befüllt das interne Depot
     * initial mit jeweils 10 Münzen pro Münzsorte.
     */
    public Münzwechsler()
    {
        this.muenzenBestand = new EnumMap<>(Muenze.class);

        for (Muenze m : Muenze.values())
        {
           this.muenzenBestand.put(m, 10);
        }
    }

    /**
     * Gibt den aktuellen Münzbestand des Automaten zurück.
     *
     * @return eine Map mit dem aktuellen Bestand aller Münzen
     */
    public Map<Muenze, Integer> getMuenzBestand()
    {

        return muenzenBestand;
    }

    /**
     * Berechnet das Wechselgeld für einen Bezahlvorgang und aktualisiert bei Erfolg den Münzbestand.
     * Der Algorithmus arbeitet nach dem Greedy-Prinzip (gieriger Algorithmus) und gibt bevorzugt
     * große Münzen heraus.
     *
     * @param preisInCents   der zu zahlende Betrag für das Getränk in Cents
     * @param eingeworfen    die Liste der vom Kunden eingeworfenen Münzen
     * @return ein {@link WechselgeldErgebnis}, das den Erfolg und die herauszugebenden Münzen kapselt
     */
    public WechselgeldErgebnis wechselgeldBerechnen(int preisInCents, List<Muenze> eingeworfen)
    {
        // 1. Gesamtsumme des eingeworfenen Geldes berechnen
        int summeEingeworfen = 0;
        for (Muenze m : eingeworfen)
        {
            summeEingeworfen += m.getWertInCents();
        }

        // Fall 1: Zu wenig Geld eingeworfen -> Abbruch, Kunde erhält sein Geld zurück
        if (summeEingeworfen < preisInCents)
        {
            return new WechselgeldErgebnis(false, eingeworfen, ZahlungsMeldung.FEHLER_KEIN_WECHSELGELD);
        }
        // Fall 2: Exakt passend bezahlt -> Münzen direkt in den Bestand übernehmen, kein Wechselgeld nötig
        else if (summeEingeworfen == preisInCents)
        {
            for (Muenze m : eingeworfen)
            {
                muenzenBestand.put(m, muenzenBestand.get(m) + 1);
            }
            return new WechselgeldErgebnis(true, List.of(), ZahlungsMeldung.ERFOLGREICH);
        }
        // Fall 3: Zu viel gezahlt -> Wechselgeld-Berechnung erforderlich
        else
        {
            int wechselgeldInCents = summeEingeworfen - preisInCents;
            // Temporäres Abbild des Bestands erstellen (Sandbox für Rollback-Sicherheit)
            Map<Muenze, Integer> tempStock = new EnumMap<>(muenzenBestand);

            // Eingeworfene Münzen virtuell dem temporären Bestand hinzufügen
            for (Muenze m : eingeworfen)
            {
                tempStock.put(m, tempStock.get(m) + 1);
            }

            List<Muenze> changeCoinsToReturn = new ArrayList<>();
            Muenze[] allCoins = Muenze.values();

            // Greedy-Algorithmus: Schleife läuft rückwärts von der größten zur kleinsten Münze
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

            // Wenn das Wechselgeld exakt aufgegangen ist, wird die Transaktion festgeschrieben (Commit)
            if (wechselgeldInCents == 0)
            {
                this.muenzenBestand.clear();
                this.muenzenBestand.putAll(tempStock);
                return new WechselgeldErgebnis(true, changeCoinsToReturn, ZahlungsMeldung.ERFOLGREICH_WECHSELGELD);
            }
            // Wenn nicht passend gewechselt werden konnte -> Rollback (Echter Bestand bleibt unberührt)
            else
            {
                return new WechselgeldErgebnis(false, eingeworfen, ZahlungsMeldung.FEHLER_KEIN_WECHSELGELD);
            }
        }
    }
}

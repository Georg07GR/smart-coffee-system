package com.smartcoffee.logic;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a coffee machine controller that manages ingredient stock,
 * handles drink preparation logic, tracks usage statistics, and simulates
 * hardware wear and tear (e.g., grinder breakdown).
 */
public class Kaffeeautomat
{
    // Maximum capacities for the machine ingredients
    private final int maxKaffee = 2000;
    private final int maxMilch = 200;
    private final int maxcacaoPulver = 100;

    // Current ingredient inventory and usage tracking
    private int kaffeeBestand;
    private int milchBestand;
    private int cacaoBestand;
    private int tassen;
    private AutomatenStatus status;
    private List<String> transaktionsHistorie;

    /**
     * Initializes a new coffee machine with fully stocked ingredients,
     * zero cups served, and a ready status.
     */
    public Kaffeeautomat()
    {
        this.kaffeeBestand = maxKaffee;
        this.milchBestand = maxMilch;
        this.cacaoBestand = maxcacaoPulver;
        this.tassen = 0;
        this.status = AutomatenStatus.BEREIT;
        this.transaktionsHistorie = new ArrayList<>();
    }




    /**
     * Processes the preparation of a specified beverage.
     * Validates machine status, checks resource availability, simulates
     * potential hardware malfunction, and updates inventory/history logs.
     *
     * @param kaffeeArt The type of drink to be prepared.
     * @return true if the beverage was successfully made; false otherwise.
     */
    public boolean getraenkZubereiten(KaffeeArt kaffeeArt)
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);


        // 1. Prüfung: Ist die Maschine defekt?
        if (status == AutomatenStatus.DEFEKT)
        {
            System.out.println("Maschine ist defekt. Keine Zubereitung möglich.");
            return false;
        }

        // 2. Prüfung: Genug Kaffeebohnen?
        if (kaffeeBestand < 25)
        {
            System.out.println("Nicht genug Kaffee vorhanden.");
            status = AutomatenStatus.ZUTATEN_FEHLEN;
            return false;
        }

        // 3. Prüfung: Genug Milchpulver?
        if (kaffeeArt.isMitmilch() && milchBestand < 10)
        {
            System.out.println("Nicht genug Milch vorhanden.");
            status = AutomatenStatus.ZUTATEN_FEHLEN;
            return false;
        }

        // 4. Prüfung: Genug Kakao?
        if (kaffeeArt == KaffeeArt.HOT_CHOCOLATE && cacaoBestand < 15)
        {
            System.out.println("Nicht genug Kakao vorhanden.");
            status = AutomatenStatus.ZUTATEN_FEHLEN;
            return false;
        }

        // 5. Prüfung: Geht das Mahlwerk kaputt? (2% Chance)
        if (istMahlwerkKaputt())
        {
            System.out.println("Die Kaffeemühle ist kaputt. Maschine defekt.");
            status = AutomatenStatus.DEFEKT;
            transaktionsHistorie.add(formattedDateTime + " - DEFEKT: Mahlwerk beschädigt.");
            return false;
        }


        // Deduct resources and update statistics
        kaffeeBestand -= 25; // Jede Tasse verbraucht 25g Kaffee
        if (kaffeeArt.isMitmilch())
        {
            milchBestand -= 10; // Jede Tasse mit Milch verbraucht 10g Milchpulver
        }
        if (kaffeeArt == KaffeeArt.HOT_CHOCOLATE)
        {
            cacaoBestand -= 15; // Jede Tasse Heiße Schokolade verbraucht 15g Kakao
        }
        tassen++;
        System.out.println("Getränk wird zubereitet: " + kaffeeArt.getAnzeigeName());
        String logText = formattedDateTime + " - Getränk zubereitet: " + kaffeeArt.getAnzeigeName();
        transaktionsHistorie.add(logText);

        status = AutomatenStatus.BEREIT;



        // Print basic statistics every 20 cups
        if (tassen % 20 == 0)
        {
            System.out.println("Statistik: " + tassen + " Tassen ausgegeben.");
        }
        return true;
    }



    /**
     * Refills all ingredient inventories to their maximum capacity.
     * Resets the machine status back to ready if it was out of ingredients or broken.
     */
    public void auffuellen()
    {
        kaffeeBestand = maxKaffee;
        milchBestand = maxMilch;
        cacaoBestand = maxcacaoPulver;


        if (status == AutomatenStatus.ZUTATEN_FEHLEN || status == AutomatenStatus.DEFEKT)
        {
            status = AutomatenStatus.BEREIT;
        }
        System.out.println("Kaffeeautomat wurde vollständig aufgefüllt und gereinigt.");
    }


    /**
     * Simulates a hardware failure probability for the coffee grinder mechanism.
     *
     * @return true if the grinder breaks down (2% chance); false otherwise.
     */
    boolean istMahlwerkKaputt()
    {
        return Math.random() < 0.02; // 2% Chance, dass das Mahlwerk kaputt geht
    }



    /**
     * Gets the current level of coffee beans remaining.
     * @return Current coffee inventory in grams.
     */
    public int getKaffeeBestand()
    {
        return kaffeeBestand;
    }

    /**
     * Gets the current level of milk powder remaining.
     * @return Current milk inventory in grams.
     */
    public int getMilchBestand()
    {
        return milchBestand;
    }

    /**
     * Gets the current level of cocoa powder remaining.
     * @return Current cocoa inventory in grams.
     */
    public int getCacaoBestand()
    {
        return cacaoBestand;
    }

    /**
     * Gets the total number of cups successfully prepared.
     * @return Total cup counter.
     */
    public int getTassen()
    {
        return tassen;
    }

    /**
     * Gets the current operational status of the machine.
     * @return The current AutomatenStatus.
     */
    public AutomatenStatus getStatus()
    {
        return status;
    }

    /**
     * Retrieves the history log of all successful preparations and malfunctions.
     * @return A list containing transaction timestamps and messages.
     */
    public List<String> getTransaktionsHistorie()
    {
        return transaktionsHistorie;
    }
}

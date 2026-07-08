package com.smartcoffee.logic;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Kaffeeautomat
{
    private final int maxKaffee = 2000;
    private final int maxMilch = 200;
    private final int maxcacaoPulver = 100;


    private int kaffeeBestand;
    private int milchBestand;
    private int cacaoBestand;
    private int tassen;
    private AutomatenStatus status;
    private List<String> transaktionsHistorie;

    public Kaffeeautomat()
    {
        this.kaffeeBestand = maxKaffee;
        this.milchBestand = maxMilch;
        this.cacaoBestand = maxcacaoPulver;
        this.tassen = 0;
        this.status = AutomatenStatus.BEREIT;
        this.transaktionsHistorie = new ArrayList<>();
    }




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


        kaffeeBestand -= 25; // Jede Tasse verbraucht 25g Kaffee
        if (kaffeeArt.isMitmilch())
        {
            milchBestand -= 10; // Jede Tasse mit Milch verbraucht 10g Milchpulver
        }
        if (kaffeeArt == KaffeeArt.HOT_CHOCOLATE)
        {
            cacaoBestand -= 15; // Jede Tasse Heiße Schokolade verbraucht 15g Kakao
        }
        tassen++; // Eine Tasse wird verbraucht7
        System.out.println("Getränk wird zubereitet: " + kaffeeArt.getAnzeigeName());
        String logText = formattedDateTime + " - Getränk zubereitet: " + kaffeeArt.getAnzeigeName();
        transaktionsHistorie.add(logText);

        status = AutomatenStatus.BEREIT;



        if (tassen % 20 == 0)
        {
            System.out.println("Statistik: " + tassen + " Tassen ausgegeben.");
        }
        return true;
    }



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


    boolean istMahlwerkKaputt()
    {
        return Math.random() < 0.02; // 2% Chance, dass das Mahlwerk kaputt geht
    }



    public int getKaffeeBestand()
    {
        return kaffeeBestand;
    }

    public int getMilchBestand()
    {
        return milchBestand;
    }

    public int getCacaoBestand()
    {
        return cacaoBestand;
    }

    public int getTassen()
    {
        return tassen;
    }

    public AutomatenStatus getStatus()
    {
        return status;
    }

    public List<String> getTransaktionsHistorie()
    {
        return transaktionsHistorie;
    }

}

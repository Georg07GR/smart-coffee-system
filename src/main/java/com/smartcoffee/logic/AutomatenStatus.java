package com.smartcoffee.logic;

/**
 * Repräsentiert die verschiedenen Betriebszustände des Kaffeeautomaten.
 * Dient der Steuerung des Anwendungsablaufs (Workflow) und bestimmt,
 * welche Aktionen (z. B. Geld einwerfen, Kaffee brühen) im aktuellen Zustand zulässig sind.
 */
public enum AutomatenStatus
{
    BEREIT,
    BEZAHLVORGANG,
    BRUEHVORGANG,
    DEFEKT,
    ZUTATEN_FEHLEN;

}

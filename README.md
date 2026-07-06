# ByteSized Coffee - Smart Coffee & Payment System

![ByteSized Coffee Logo](src/main/resources/com/smartcoffee/gui/bytesized_coffee_logo.png)

Dieses Projekt umfasst die Entwicklung eines softwarebasierten Steuerungssystems für einen Kaffeeautomaten mit integriertem Bezahl- und Münzwechselsystem für die Marke **ByteSized Coffee**. Das System trennt Benutzeroberfläche (JavaFX), Geschäftslogik und Datenhaltung (SQLite).

---

## Projektphasen (nach Vorgehensmodell)

Das Projekt folgt dem klassischen Ablauf aus der Softwaretechnik, wie im Phasenmodell der Projektwoche vorgegeben:

```mermaid
graph LR
    Analyse[1. Analyse] --> Entwurf[2. Entwurf]
    Entwurf --> Implementierung[3. Implementierung]
    Implementierung --> Test[4. Test]
```

---

## 1. Analyse & Entwurf (Design Phase)

### 1.1 Funktionale Anforderungen (Analyse)
* **Kaffeezubereitung**: Auswahl aus 6 Kaffeesorten mit/ohne Milch:
  * **Espresso** (ohne Milch, 25g Bohnen, 1.50 EUR)
  * **Black Coffee** (ohne Milch, 25g Bohnen, 2.00 EUR)
  * **Coffee with Milk** (mit Milch, 25g Bohnen, 10g Milchpulver, 2.20 EUR)
  * **Cappuccino** (mit Milch, 25g Bohnen, 10g Milchpulver, 2.50 EUR)
  * **Latte** (mit Milch, 25g Bohnen, 10g Milchpulver, 2.50 EUR)
  * **Latte Macchiato** (mit Milch, 25g Bohnen, 10g Milchpulver, 2.80 EUR)
* **Bestandsprüfung**: Überprüfung der Bestände (Start: 2000g Kaffee, 200g Milch). Automatische Reduzierung bei erfolgreicher Zubereitung.
* **Bezahlsystem**: Münzeingabe (von 1 Ct bis 2 €), Wechselgeldberechnung. Speicherung der eingeworfenen Münzen zur detaillierten Transaktionsanalyse. Die eingeworfenen Münzen werden vor der Wechselgeldberechnung dem internen Bestand hinzugefügt.
* **Systemverwaltung**: Erfassung aller Bestellungen und Münzbestände in einer lokalen SQLite-Datenbank. Fehlerbehandlung bei Ressourcenmangel oder simuliertem Defekt (2% Ausfallwahrscheinlichkeit).

---

### 1.2 Datenbankdesign (ER-Modell)
Die Datenhaltung erfolgt in einer SQLite-Datenbank (`coffee_system.db`). Die Tabellenstruktur wird über das folgende Entity-Relationship-Diagramm (ERD) definiert:

```mermaid
erDiagram
    Bestellungen ||--o{ Zahlungen : "gehoert zu"
    Bestellungen {
        INTEGER id PK "Auto-Increment"
        VARCHAR kaffeeart "Sorte"
        BOOLEAN mitMilch "Ja/Nein"
        DECIMAL preis "Preis in EUR"
        TIMESTAMP zeitstempel "Datum & Uhrzeit"
    }
    Zahlungen {
        INTEGER id PK "Auto-Increment"
        INTEGER bestellung_id FK "Fremdschluessel auf Bestellungen"
        DECIMAL muenztyp "Muenze (z.B. 0.50, 1.00)"
        INTEGER anzahl "Menge der Muenzen"
    }
    Münzbestand {
        DECIMAL muenztyp PK "Münz-Nennwert"
        INTEGER anzahl "Aktueller Bestand im Wechsler"
    }
```

---

### 1.3 Systemarchitektur (Klassendiagramm)
Die Klassenstruktur basiert auf dem **Model-View-Controller (MVC) Pattern**, um GUI, Logik und Datenbank sauber zu trennen (Separation of Concerns). Die Domänentypen, Systemzustände und Rückgabewerte werden durch Enums und Records gekapselt:

```mermaid
classDiagram
    class KaffeeArt {
        <<enumeration>>
        ESPRESSO
        BLACK_COFFEE
        COFFEE_WITH_MILK
        CAPPUCCINO
        LATTE
        LATTE_MACCHIATO
        -String anzeigeName
        -double preis
        -boolean mitMilch
        +getAnzeigeName() String
        +getPreis() double
        +isMitMilch() boolean
    }

    class Muenze {
        <<enumeration>>
        CENT_1
        CENT_2
        CENT_5
        CENT_10
        CENT_20
        CENT_50
        EURO_1
        EURO_2
        -int wertInCents
        -String anzeigeName
        +getWertInCents() int
        +getAnzeigeName() String
        +getWertInEuro() double
    }

    class AutomatenStatus {
        <<enumeration>>
        BEREIT
        BEZAHLVORGANG
        BRUEHVORGANG
        DEFEKT
        ZUTATEN_FEHLEN
    }

    class ZahlungsMeldung {
        <<enumeration>>
        ERFOLGREICH
        ERFOLGREICH_WECHSELGELD
        FEHLER_KEIN_WECHSELGELD
        -String text
        +getText() String
    }

    class WechselgeldErgebnis {
        <<record>>
        +boolean erfolgreich
        +List~Muenze~ ausgegebeneMuenzen
        +ZahlungsMeldung meldung
    }

    class CurrencyUtil {
        +formatEuro(double betrag) String$
    }

    class Kaffeeautomat {
        -AutomatenStatus status
        -int kaffeeBestand
        -int milchBestand
        -int tassen
        -List~String~ transaktionsHistorie
        +getraenkZubereiten(KaffeeArt kaffee) boolean
        +auffuellen() void
        +getStatus() AutomatenStatus
        -bestandPruefen(boolean mitMilch) boolean
        -transaktionSpeichern(KaffeeArt kaffee) void
    }

    class Münzwechsler {
        -Map~Muenze, Integer~ muenzBestand
        +muenzeAnnehmen(Muenze muenze) void
        +wechselgeldBerechnen(int preisInCents, List~Muenze~ eingeworfen) WechselgeldErgebnis
        -bestandAktualisieren() void
    }

    class DatenbankManager {
        -Connection dbVerbindung
        +bestellungSpeichern(KaffeeArt kaffee) int
        +zahlungSpeichern(int bestellId, Muenze muenztyp, int anzahl) void
        +datenAbrufen() void
    }

    class GUIController {
        -Kaffeeautomat automat
        -Münzwechsler wechsler
        -DatenbankManager dbManager
        +getraenkeAnzeigen() void
        +zahlungVerwalten() void
        +fehlermeldungAnzeigen(String nachricht) void
    }

    Kaffeeautomat ..> KaffeeArt : "nutzt"
    Kaffeeautomat ..> AutomatenStatus : "nutzt"
    Münzwechsler ..> Muenze : "nutzt"
    Münzwechsler ..> WechselgeldErgebnis : "erzeugt"
    WechselgeldErgebnis ..> ZahlungsMeldung : "nutzt"
    DatenbankManager ..> KaffeeArt : "nutzt"
    DatenbankManager ..> Muenze : "nutzt"
    GUIController --> Kaffeeautomat : "steuert"
    GUIController --> Münzwechsler : "steuert"
    GUIController --> DatenbankManager : "nutzt für Persistence"
```

---

### 1.4 Ergänzende Code-Strukturierung (Clean Code & Separation)
Um die Lesbarkeit des Quellcodes zu maximieren und die Wartbarkeit zu verbessern, implementieren wir folgende Mechanismen:
1. **Externe SQL-Initialisierungsdatei (`schema.sql`)**: Die Datenbanktabellen-Erstellungsskripte liegen getrennt in `src/main/resources/com/smartcoffee/database/schema.sql`. Der `DatenbankManager` liest diese SQL-Datei beim Start ein.
2. **Java Record (`WechselgeldErgebnis`)**: Zur Kapselung des Rückgabewertes der Wechselgeld-Berechnung nutzen wir ein kompaktes Java Record, um redundanten Boilerplate-Code zu vermeiden.
3. **Formatierungsklasse (`CurrencyUtil`)**: Eine Hilfsklasse stellt die einheitliche Formatierung von Euro-Preisen über das gesamte System sicher (z.B. `1,50 €`), um Codeduplizierung im UI und Logger zu vermeiden.

---

### 1.5 GUI-Entwurf (Skizze)
Die JavaFX-Oberfläche besteht aus einem Hauptfenster und einem Zahlungsdialog mit einem modernen, dunklen Design und dem **ByteSized Coffee** Logo im Hintergrund:
1. **Hauptfenster**:
   * Getränkeauswahl (Buttons für Espresso, Black Coffee, Coffee with Milk, Cappuccino, Latte, Latte Macchiato) inklusive Preisangabe auf jedem Button.
   * Statusanzeige der Bestände als visuelle Balken mit exakter Grammangabe (z.B. `1450g / 2000g`).
   * Anzeige der insgesamt ausgegebenen Tassen.
   * Fehler-/Info-Log für Statusmeldungen (z.B. "Mahlwerk defekt!", "Bitte Bohnen auffüllen").
2. **Zahlungsfenster**:
   * Münzeinwurftasten als stilisierte, runde Münz-Icons.
   * Anzeige des eingeworfenen Betrags und des noch zu zahlenden Restbetrags.
   * Anzeige des Wechselgeldbestands im Wechsler.

---

## 2. Verwendung & Ausführung

### Voraussetzungen
* **Java SDK 21**
* **Maven 3.x**

### Anwendung starten
Im Projektverzeichnis folgenden Befehl ausführen:
```bash
mvn javafx:run
```

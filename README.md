# ByteSized Coffee - Smart Coffee & Payment System

![ByteSized Coffee Logo](src/main/resources/com/smartcoffee/gui/bytesized_coffee_logo.png)

> [!WARNING]
> **Haftungsausschluss**
> Dies ist ein Software-Prototyp. Jegliche Interaktion mit realen Münzen oder elektronischen Zahlungsgeräten ist im Rahmen dieses Projekts nicht vorgesehen und wird nicht unterstützt.

> [!NOTE]
> **Schulprojekt / Lern-Projekt**
> Dieses Projekt wurde im Rahmen einer schulischen Projektwoche entwickelt. Es dient als praktische Übung zur Anwendung von JavaFX, Software-Design-Mustern (MVC-Pattern), automatisierter Qualitätssicherung mit JUnit-Tests und Datenbank-Persistenz mit SQLite.

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
  * **Black Coffee** (ohne Milch, 25g Bohnen, 1.80 EUR)
  * **Coffee with Milk** (mit Milch, 25g Bohnen, 10g Milchpulver, 2.00 EUR)
  * **Cappuccino** (mit Milch, 25g Bohnen, 10g Milchpulver, 2.50 EUR)
  * **Latte Macchiato** (mit Milch, 25g Bohnen, 10g Milchpulver, 2.80 EUR)
  * **Hot Chocolate** (mit Milch, 15g Kakaopulver, 10g Milchpulver, 2.20 EUR)
* **Bestandsprüfung**: Überprüfung der Bestände (Start: 2000g Kaffee, 200g Milch, 100g Kakao). Automatische Reduzierung bei erfolgreicher Zubereitung.
* **Bezahlsystem**: Münzeingabe (10c, 20c, 50c, 1€, 2€), Wechselgeldberechnung. Speicherung der eingeworfenen Münzen zur detaillierten Transaktionsanalyse. Die eingeworfenen Münzen werden vor der Wechselgeldberechnung dem internen Bestand hinzugefügt.
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
    Muenzbestand {
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
        LATTE_MACCHIATO
        HOT_CHOCOLATE
        -String anzeigeName
        -double preis
        -boolean mitMilch
        +getAnzeigeName() String
        +getPreis() double
        +getPreisInCents() int
        +isMitMilch() boolean
    }

    class Muenze {
        <<enumeration>>
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
        -int cacaoBestand
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
        +getGesamtumsatz() double
        +getVerkaufsStatistik() Map~String, Integer~
    }

    class GUIController {
        -Kaffeeautomat automat
        -Münzwechsler wechsler
        -DatenbankManager dbManager
        +initialize() void
        +onCoffeeSelected(ActionEvent event) void
        +onCoinInserted(ActionEvent event) void
        +onCancelPayment(ActionEvent event) void
        +onToggleTechnicianPanel(ActionEvent event) void
        +onRefill(ActionEvent event) void
        +onRepairGrinder(ActionEvent event) void
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

### 1.5 Implementierte JavaFX Benutzeroberfläche
Die grafische Benutzeroberfläche (`main_layout.fxml` & `styles.css`) wurde als moderner, dunkler Simulator mit Premium-Asthetik umgesetzt. Sie ist in zwei funktionale Bereiche unterteilt:

1. **Kunden-Touchscreen (Linke Seite)**:
   * **Menüauswahl**: 6 stilvolle Getränkekarten mit professionellen, AI-generierten Produktfotos (Espresso, Schwarzer Kaffee, Cappuccino, Latte Macchiato, Heiße Schokolade, Milchkaffee).
   * **Bühnen-Hintergrund**: Ein großes, dezentes Branding-Wasserzeichen des ByteSized Coffee Logos im Hintergrund (`fitWidth="460"`, `opacity="0.09"`, `mouseTransparent="true"`).
   * **Münzeinwurf**: Physisch angeordneter Münzeinwurfbalken mit stilisierter, kreisrunder Münz-Tastatur (`10c`, `20c`, `50c`, `1€`, `2€`) direkt neben einem grün leuchtenden, digital-segmentierten LED-Kreditdisplay.
   * **Zubereitungs-Animation**: Während des Brühvorgangs blockiert ein transluzentes Overlay die Interaktion. Ein stilisierter Becher füllt sich in Echtzeit mit Kaffeeflüssigkeit, synchronisiert mit dem Fortschrittsbalken (Mahlen $\rightarrow$ Erhitzen & Brühen $\rightarrow$ Ausgeben $\rightarrow$ Fertig).

2. **Entwickler- & Bedienerkonsole (Rechte Seite - Ein-/Ausklappbar)**:
   * **Techniker-Drawer**: Über die Menütaste (`☰ Admin`) im Kunden-Touchscreen kann die Konsole dynamisch ein- und ausgeblendet werden. Die Fensterbreite passt sich fließend an (`720px` eingeklappt, `1080px` ausgeklappt).
   * **Zutaten-Tanks**: 3 vertikale, pillenförmige Glasröhren zur Füllstandsanzeige (Kaffeebohnen, Milchpulver, Kakaopulver). Fällt ein Bestand unter das kritische Minimum, glühen die Röhren-Ränder rot auf.
   * **Kassenstand & Münzbestand**: Ein geteiltes Side-by-Side-Panel. Links stehen Umsatz, Kasseninhalt und die Tassenzahl; rechts steht der detaillierte, vertikal ausgerichtete Münzwechslerbestand pro Münztyp (in absteigender Reihenfolge).
   * **Terminal-Konsole**: Ein retro-grün leuchtendes Log-Fenster, das live Datenbankereignisse, PRAGMA-Initialisierungen und Fehlercodes ausgibt.
   * **Bedieneraktionen**: Tasten zum manuellen Auffüllen des Automaten und zur Reparatur des Mahlwerks bei Ausfällen.

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

### Entwickler-Dokumentation (JavaDoc)
Die JavaDoc-Dokumentation für alle Klassen und Schnittstellen kann mit folgendem Befehl generiert werden:
```bash
mvn javadoc:javadoc
```
Nach erfolgreicher Generierung befindet sich die Dokumentation im Ordner `target/site/apidocs/` und kann durch Öffnen der Datei `index.html` im Webbrowser eingesehen werden.

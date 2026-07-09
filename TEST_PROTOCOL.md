# ByteSized Coffee - Test- und Verifikationsprotokoll

Dieses Dokument dient zur strukturierten Überprüfung und Qualitätssicherung des **ByteSized Coffee** Smart Coffee & Bezahlsystems. Es unterteilt sich in automatisierte Testabläufe (JUnit) und manuelle Verifikationsszenarien für die JavaFX-Benutzeroberfläche.

---

## 1. Automatisierte Testsuite (JUnit & Integration Tests)

Die automated Unit- und Integrations-Tests verifizieren die korrekte Funktionsweise der Kerngeschäftslogik, der mathematischen Wechselgeldberechnung sowie der Datenbank-Persistenz.

### Ausführung der Tests
Um die gesamte Testsuite auszuführen, führen Sie folgenden Befehl im Projektverzeichnis aus:
```bash
mvn clean test
```

### Abgedeckte Testklassen
* **`MünzwechslerTest`**:
  * Überprüfung der Münzeinzahlung und korrekten Erhöhung des Wechselgeldbestands.
  * Verifikation der Berechnung des Wechselgelds (Gieriger Algorithmus) für verschiedene Beträge.
  * Test des Fehlerfalls bei unzureichendem Wechselgeldbestand im Wechsler.
* **`KaffeeautomatTest`**:
  * Prüfung der Bestandsreduzierung bei der Kaffeezubereitung (Zutatenabbau).
  * Verifikation der Sperrung von Getränken, wenn Zutaten leer sind.
  * Test des simulierten Mahlwerk-Defekts (2% Zufallsausfall) und dessen Fehlerbehandlung.
* **`DatenbankManagerTest`**:
  * Test der automatischen Initialisierung des Datenbankschemas aus `schema.sql`.
  * Verifikation der korrekten Speicherung von Bestellungen und verknüpften Zahlungen.
  * Abfrage-Test für Umsatzstatistiken und ausgegebene Tassen-Reports.

---

## 2. Manuelle Verifikationsszenarien (GUI Testprotokoll)

Da die JavaFX-Benutzeroberfläche visuelle Interaktionen, Animationen und dynamische Fenstergrößen beinhaltet, müssen diese manuell anhand der folgenden Testfälle verifiziert werden.

| Test-ID | Testfall-Beschreibung | Schritt-für-Schritt Anleitung | Erwartetes Ergebnis | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-01** | **Standard-Kauf mit Wechselgeld** | 1. Werfen Sie `2 €` ein (Klick auf `2€` Button).<br>2. Wählen Sie **Black Coffee** (`1,80 €`). | * Die Becher-Animation startet und füllt sich mit Kaffee.<br>* Die Fortschrittsanzeige läuft von 0% auf 100%.<br>* Nach Fertigstellung wird `20c` Wechselgeld ausgegeben.<br>* Der Kassenstand im Admin-Panel erhöht sich. | `[ ]` |
| **TC-02** | **Kaufvorgang Abbrechen & Rückgabe** | 1. Werfen Sie `1 €` und `50c` ein.<br>2. Klicken Sie auf den roten **Abbrechen** Button. | * Das eingeworfene Guthaben wird auf `0,00 €` zurückgesetzt.<br>* Die eingezahlten Münzen werden virtuell wieder ausgeworfen.<br>* Die Maschine kehrt in den Status **BEREIT** zurück. | `[ ]` |
| **TC-03** | **Unzureichendes Guthaben** | 1. Werfen Sie `1 €` ein.<br>2. Wählen Sie **Cappuccino** (`2,50 €`). | * Der Brühvorgang startet nicht.<br>* Im Log-Terminal erscheint die Meldung `Selection Blocked: Cappuccino costs 2,50 € (Credit: 1,00 €)`. | `[ ]` |
| **TC-04** | **Zutatenmangel & Warnung** | 1. Öffnen Sie das Admin-Panel (`☰ Admin`).<br>2. Bereiten Sie Kaffee zu, bis ein Rohstoff (z.B. Kakao) unter das Minimum fällt. | * Der betroffene Zylinder (z.B. Kakaopulver) glüht rot auf.<br>* Getränke, die diese Zutat benötigen (z.B. Hot Chocolate), werden im Touchscreen ausgegraut und gesperrt. | `[ ]` |
| **TC-05** | **Zufälliger Defekt & Reparatur** | 1. Brühen Sie wiederholt Kaffeesorten, bis das Mahlwerk blockiert (2% Zufallschance). | * Der Becher-Dialog zeigt "Maschine DEFEKT".<br>* Ein roter Fehlereintrag erscheint im Log-Terminal.<br>* Klicken Sie im Admin-Panel auf **Repair Grinder**, um den Status wieder auf **BEREIT** zu setzen. | `[ ]` |
| **TC-06** | **Techniker-Drawer Animation** | 1. Starten Sie die Anwendung (öffnet sich in `720px` Breite).<br>2. Klicken Sie auf die Taste `☰ Admin`. | * Das Fenster vergrößert sich fließend auf `1080px` Breite.<br>* Das Techniker-Drawer-Panel schiebt sich fließend ins Bild.<br>* Erneuter Klick verkleinert das Fenster wieder auf `720px`. | `[ ]` |

---

## 3. Datenbank-Verifikation (SQL-Abfragen)

Zur manuellen Überprüfung, ob alle Transaktionen ordnungsgemäß persistiert wurden, kann die lokale SQLite-Datenbank `coffee_system.db` abgefragt werden.

### Überprüfung mittels SQL-Befehlen
Öffnen Sie die Datenbank mit einem SQLite-Viewer oder CLI-Tool und führen Sie folgende Kontrollabfragen aus:

1. **Umsatz und Bestellungen einsehen**:
   ```sql
   SELECT * FROM Bestellungen ORDER BY zeitstempel DESC;
   ```
   *Erwartung*: Jeder erfolgreiche Kauf erzeugt hier eine Zeile mit Name, Preis und Zeitstempel.

2. **Detaillierte Münzzahlungen prüfen**:
   ```sql
   SELECT b.kaffeeart, z.muenztyp, z.anzahl 
   FROM Zahlungen z 
   JOIN Bestellungen b ON z.bestellung_id = b.id;
   ```
   *Erwartung*: Zeigt genau an, mit welchen Münzen das jeweilige Getränk bezahlt wurde.

3. **Münzwechsler-Bestand auslesen**:
   ```sql
   SELECT * FROM Muenzbestand;
   ```
   *Erwartung*: Zeigt den aktuellen Echtzeit-Münzbestand innerhalb der Geldkassette an.

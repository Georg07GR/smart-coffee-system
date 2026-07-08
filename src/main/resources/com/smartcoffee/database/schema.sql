CREATE TABLE IF NOT EXISTS Bestellungen (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    kaffeeart TEXT NOT NULL,
    mit_milch INTEGER NOT NULL,
    preis REAL NOT NULL,
    zeitstempel TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Zahlungen (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bestellung_id INTEGER NOT NULL,
    muenztyp REAL NOT NULL,
    anzahl INTEGER NOT NULL,
    FOREIGN KEY (bestellung_id) REFERENCES Bestellungen(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Muenzbestand (
    muenztyp REAL PRIMARY KEY,
    anzahl INTEGER NOT NULL
);
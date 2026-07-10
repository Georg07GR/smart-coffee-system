package com.smartcoffee.gui;

import com.smartcoffee.database.DatenbankManager;
import com.smartcoffee.logic.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the JavaFX graphical interface.
 * Connects UI elements, handles user inputs, manages coin inserts, calculates change,
 * persists transactions to SQLite, and updates the operator dashboard.
 */
public class GUIController
{

    // --- FXML UI Controls ---
    @FXML private HBox mainContainer;
    @FXML private VBox customerPanel;
    @FXML private ImageView logoView;
    @FXML private Button gearButton;
    @FXML private Label creditDisplay;
    
    @FXML private Button espressoBtn;
    @FXML private Button blackCoffeeBtn;
    @FXML private Button cappuccinoBtn;
    @FXML private Button latteBtn;
    @FXML private Button cacaoBtn;
    @FXML private Button coffeeMilkBtn;

    @FXML private Label statusLabel;
    @FXML private Button cancelBtn;

    // Operator Panel Controls
    @FXML private VBox developerPanel;
    @FXML private Region coffeeFill;
    @FXML private Region milkFill;
    @FXML private Region cacaoFill;
    @FXML private Label coffeeAmount;
    @FXML private Label milkAmount;
    @FXML private Label cacaoAmount;

    @FXML private Label revenueLabel;
    @FXML private Label cashInChangerLabel;
    @FXML private Label tassenLabel;
    @FXML private Label coinBreakdownLabel;
    @FXML private TextArea terminalLog;
    @FXML private Button refillBtn;
    @FXML private Button repairBtn;

    // Brewing Overlay Controls
    @FXML private VBox brewingOverlay;
    @FXML private ProgressIndicator brewingProgress;
    @FXML private Label overlayStatusLabel;
    @FXML private Region liquidFill;

    // --- Core Logic & Database Fields ---
    private Kaffeeautomat kaffeeautomat;
    private Münzwechsler muenzwechsler;
    private DatenbankManager dbManager;
    
    private List<Muenze> eingeworfeneMuenzen;
    private int creditInCents;
    private boolean isDeveloperPanelVisible = false;

    /**
     * Initializes the controller. Called automatically after the FXML is loaded.
     */
    @FXML
    public void initialize()
    {
        // 1. Load branding logo image
        try {
            Image logo = new Image(getClass().getResourceAsStream("/com/smartcoffee/gui/bytesized_coffee_logo.png"));
            logoView.setImage(logo);
        } catch (Exception e) {
            logTerminal("Branding logo image could not be loaded: " + e.getMessage());
        }

        // 2. Instantiate core business logic and database manager
        dbManager = new DatenbankManager();
        kaffeeautomat = new Kaffeeautomat();
        muenzwechsler = new Münzwechsler();
        eingeworfeneMuenzen = new ArrayList<>();
        creditInCents = 0;

        // 3. Load persisted coin inventory from database on startup
        try
        {
            Map<Muenze, Integer> dbStock = muenzwechsler.getMuenzBestand();
            dbManager.muenzbestandLaden(dbStock);
            logTerminal("SUCCESS: Persisted coin inventory successfully loaded from database.");
        }
        catch (Exception e)
        {
            logTerminal("WARNING: Failed to load coin stock from database, using memory defaults. Info: " + e.getMessage());
        }

        // 4. Update operator gauges and stats displays
        updateResourceGauges();
        updateChangerStats();
        updateDatabaseStats();
        
        // Initialize developer panel as collapsed on startup
        developerPanel.setVisible(false);
        developerPanel.setManaged(false);
        
        // 5. System startup log message
        logTerminal("System boot completed successfully.\n" +
                    "ByteSized Coffee Machine is operational.\n" +
                    "PRAGMA foreign keys enabled.\n" +
                    "Status: BEREIT.");
    }

    /**
     * Toggles the visibility of the developer console panel on the right.
     */
    @FXML
    private void onToggleTechnicianPanel(ActionEvent event)
    {
        isDeveloperPanelVisible = !isDeveloperPanelVisible;
        developerPanel.setVisible(isDeveloperPanelVisible);
        developerPanel.setManaged(isDeveloperPanelVisible);
        
        // Dynamically resize the stage width to fit the visible panels
        javafx.stage.Stage stage = (javafx.stage.Stage) customerPanel.getScene().getWindow();
        if (isDeveloperPanelVisible)
        {
            stage.setWidth(1080); // Customer touchscreen (720) + Developer console (360)
        }
        else
        {
            stage.setWidth(720);  // Customer touchscreen only
        }
        
        logTerminal("Operator console visibility set to: " + isDeveloperPanelVisible);
    }

    /**
     * Triggered when a coin simulation button is clicked.
     */
    @FXML
    private void onCoinInserted(ActionEvent event)
    {
        Button btn = (Button) event.getSource();
        String coinName = (String) btn.getUserData();
        Muenze coin = Muenze.valueOf(coinName);

        // Add coin to current transaction credit list
        eingeworfeneMuenzen.add(coin);
        creditInCents += coin.getWertInCents();

        // Update displays
        creditDisplay.setText(formatEuro(creditInCents));
        statusLabel.setText(String.format("Münze eingeworfen: %s (Guthaben: %s)", coin.getAnzeigeName(), formatEuro(creditInCents)));
        logTerminal(String.format("Coin Inserted: %s (New Credit: %s)", coin.getAnzeigeName(), formatEuro(creditInCents)));
    }

    /**
     * Cancels the current payment transaction, refunding the inserted coins.
     */
    @FXML
    private void onCancelPayment(ActionEvent event)
    {
        if (eingeworfeneMuenzen.isEmpty())
        {
            statusLabel.setText("Kein Guthaben zum Abbrechen vorhanden.");
            return;
        }

        logTerminal(String.format("Payment Cancelled. Refunding inserted coins: %s (Total: %s)", 
                    eingeworfeneMuenzen.toString(), formatEuro(creditInCents)));
        
        // Reset transaction states
        eingeworfeneMuenzen.clear();
        creditInCents = 0;

        creditDisplay.setText("0,00 €");
        statusLabel.setText("Zahlung abgebrochen. Münzen zurückgegeben.");
    }

    /**
     * Triggered when a drink card is clicked.
     */
    @FXML
    private void onCoffeeSelected(ActionEvent event)
    {
        // 1. Determine which coffee was selected based on the button clicked
        Button clickedBtn = (Button) event.getSource();
        KaffeeArt selectedCoffee;
        if (clickedBtn == espressoBtn) selectedCoffee = KaffeeArt.ESPRESSO;
        else if (clickedBtn == blackCoffeeBtn) selectedCoffee = KaffeeArt.BLACK_COFFEE;
        else if (clickedBtn == cappuccinoBtn) selectedCoffee = KaffeeArt.CAPPUCCINO;
        else if (clickedBtn == latteBtn) selectedCoffee = KaffeeArt.LATTE_MACCHIATO;
        else if (clickedBtn == cacaoBtn) selectedCoffee = KaffeeArt.HOT_CHOCOLATE;
        else if (clickedBtn == coffeeMilkBtn) selectedCoffee = KaffeeArt.COFFEE_WITH_MILK;
        else return;

        // 2. Check if the machine is currently defective
        if (kaffeeautomat.getStatus() == AutomatenStatus.DEFEKT)
        {
            statusLabel.setText("FEHLER: Maschine defekt! Wartung erforderlich.");
            logTerminal("Selection Blocked: Machine is in DEFEKT state.");
            return;
        }

        // 3. Check if user has inserted enough credit
        int priceInCents = selectedCoffee.getPreisInCents();
        if (creditInCents < priceInCents)
        {
            statusLabel.setText(String.format("Guthaben unzureichend! Benötigt: %s", formatEuro(priceInCents)));
            logTerminal(String.format("Selection Blocked: %s costs %s (Credit: %s)", 
                        selectedCoffee.getAnzeigeName(), formatEuro(priceInCents), formatEuro(creditInCents)));
            return;
        }

        // 4. Check if ingredients are sufficient (dry-run check)
        if (!checkResourcesSufficient(selectedCoffee)) {
            statusLabel.setText("FEHLER: Zutaten fehlen! Bitte auffüllen lassen.");
            logTerminal(String.format("Selection Blocked: Missing resources to brew %s.", selectedCoffee.getAnzeigeName()));
            return;
        }

        // 5. Evaluate payment and change calculations
        logTerminal(String.format("Evaluating payment for %s...", selectedCoffee.getAnzeigeName()));
        WechselgeldErgebnis ergebnis = muenzwechsler.wechselgeldBerechnen(priceInCents, eingeworfeneMuenzen);

        if (!ergebnis.erfolgreich())
        {
            // Rollback payment - refund inserted coins
            statusLabel.setText("FEHLER: Kein passendes Wechselgeld verfügbar!");
            logTerminal("Payment Blocked: Münzwechsler failed to calculate change. Transaction rolled back.");
            onCancelPayment(null);
            return;
        }

        // Payment approved! Animate the brewing process
        animateBrewingProcess(selectedCoffee, ergebnis);
    }

    /**
     * Animates the coffee brewing process over 3 seconds without freezing the UI thread.
     */
    private void animateBrewingProcess(KaffeeArt kaffee, WechselgeldErgebnis ergebnis)
    {
        // Disable UI buttons during brewing
        setControlsEnabled(false);

        // Reset liquid level before brewing
        liquidFill.setPrefHeight(0);

        Timeline brewingTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, e ->
            {
                brewingOverlay.setVisible(true);
                brewingProgress.setProgress(0.1);
                liquidFill.setPrefHeight(0); // Start empty
                overlayStatusLabel.setText("Kaffeemühle mahlt Bohnen...");
                logTerminal("Brewing: Grinding beans...");
            }),
            new KeyFrame(Duration.seconds(1.0), e ->
            {
                brewingProgress.setProgress(0.5);
                liquidFill.setPrefHeight(20); // Cup 1/3 filled
                overlayStatusLabel.setText("Erhitzen & Brühen läuft...");
                logTerminal("Brewing: Heating water and brewing...");
            }),
            new KeyFrame(Duration.seconds(2.0), e ->
            {
                brewingProgress.setProgress(0.85);
                liquidFill.setPrefHeight(45); // Cup 3/4 filled
                overlayStatusLabel.setText(kaffee.isMitmilch() ? "Milch aufschäumen..." : "Getränk ausgeben...");
                logTerminal("Brewing: Adding froth/dispensing...");
            }),
            new KeyFrame(Duration.seconds(3.0), e ->
            {
                brewingOverlay.setVisible(false);
                setControlsEnabled(true);
                liquidFill.setPrefHeight(55); // Cup fully filled at end
                // Complete transaction and finalize brewing
                finalizeTransaction(kaffee, ergebnis);
            })
        );
        brewingTimeline.play();
    }

    /**
     * Finalizes the transaction, deducts ingredients, updates database records and GUI elements.
     */
    private void finalizeTransaction(KaffeeArt kaffee, WechselgeldErgebnis ergebnis)
    {
        // 1. Deduct ingredients from coffee machine stock
        boolean brewSuccess = kaffeeautomat.getraenkZubereiten(kaffee);

        if (!brewSuccess)
        {
            // Grinder defect triggered!
            statusLabel.setText("ALARM: Mahlwerk beschädigt! Gerät blockiert.");
            logTerminal("ALARM: Grinder broke down during preparation! Machine is now DEFEKT.");
            updateResourceGauges();
            return;
        }

        // 2. Transaction succeeded! Save order and payments to database
        try
        {
            // Write order
            int orderId = dbManager.bestellungSpeichern(kaffee);
            
            // Map payment coins to group count and insert payment
            Map<Muenze, Integer> paymentCounts = new HashMap<>();
            for (Muenze coin : eingeworfeneMuenzen)
            {
                paymentCounts.put(coin, paymentCounts.getOrDefault(coin, 0) + 1);
            }
            for (Map.Entry<Muenze, Integer> entry : paymentCounts.entrySet())
            {
                dbManager.zahlungSpeichern(orderId, entry.getKey(), entry.getValue());
            }

            // Save updated coin stocks (persisting changer state)
            dbManager.muenzbestandSpeichern(muenzwechsler.getMuenzBestand());
            logTerminal("DATABASE: Order #" + orderId + " and coin payments successfully written.");
        } catch (SQLException e) {
            logTerminal("DATABASE WARNING: Failed to write transaction details. " + e.getMessage());
        }

        // 3. Reset transaction credit variables
        eingeworfeneMuenzen.clear();
        creditInCents = 0;

        // 4. Update GUI representations
        creditDisplay.setText("0,00 €");
        statusLabel.setText(String.format("Bereitgestellt: %s. Wechselgeld erhalten: %s", 
                            kaffee.getAnzeigeName(), formatCoinsList(ergebnis.ausgegebeneMuenzen())));
        logTerminal(String.format("SUCCESS: Prepared %s. Dispensed change: %s", 
                    kaffee.getAnzeigeName(), formatCoinsList(ergebnis.ausgegebeneMuenzen())));

        updateResourceGauges();
        updateChangerStats();
        updateDatabaseStats();
    }

    /**
     * Operator refill event.
     */
    @FXML
    private void onRefill(ActionEvent event)
    {
        kaffeeautomat.auffuellen();
        updateResourceGauges();
        statusLabel.setText("Maschine wurde aufgefüllt und gereinigt.");
        logTerminal("OPERATOR ACTION: Machine refilled and cleaned. Status set to BEREIT.");
    }

    /**
     * Operator repair grinder event.
     */
    @FXML
    private void onRepairGrinder(ActionEvent event)
    {
        if (kaffeeautomat.getStatus() != AutomatenStatus.DEFEKT)
        {
            logTerminal("OPERATOR ACTION: Repair skipped (Machine is already functional).");
            return;
        }

        kaffeeautomat.auffuellen(); // Refilling resets the DEFEKT state to BEREIT
        updateResourceGauges();
        statusLabel.setText("Mahlwerk repariert. Maschine bereit.");
        logTerminal("OPERATOR ACTION: Grinder repaired. Machine set to BEREIT.");
    }

    // --- Helper Methods ---

    /**
     * Dry-run resources check.
     */
    private boolean checkResourcesSufficient(KaffeeArt kaffee)
    {
        if (kaffeeautomat.getKaffeeBestand() < 25) return false;
        if (kaffee.isMitmilch() && kaffeeautomat.getMilchBestand() < 10) return false;
        if (kaffee == KaffeeArt.HOT_CHOCOLATE && kaffeeautomat.getCacaoBestand() < 15) return false;
        return true;
    }

    /**
     * Sets buttons enabled/disabled to block input during brewing animations.
     */
    private void setControlsEnabled(boolean enabled)
    {
        espressoBtn.setDisable(!enabled);
        blackCoffeeBtn.setDisable(!enabled);
        cappuccinoBtn.setDisable(!enabled);
        latteBtn.setDisable(!enabled);
        cacaoBtn.setDisable(!enabled);
        coffeeMilkBtn.setDisable(!enabled);
        cancelBtn.setDisable(!enabled);
        gearButton.setDisable(!enabled);
    }

    /**
     * Updates the heights of the resource liquid cylinders in FXML based on current values.
     */
    private void updateResourceGauges()
    {
        int coffee = kaffeeautomat.getKaffeeBestand();
        int milk = kaffeeautomat.getMilchBestand();
        int cacao = kaffeeautomat.getCacaoBestand();

        coffeeAmount.setText(coffee + "g");
        milkAmount.setText(milk + "g");
        cacaoAmount.setText(cacao + "g");

        // Scale heights (Max heights of cylinders is 70px in FXML/CSS)
        coffeeFill.setPrefHeight(70.0 * (coffee / 2000.0));
        milkFill.setPrefHeight(70.0 * (milk / 200.0));
        cacaoFill.setPrefHeight(70.0 * (cacao / 100.0));

        // Visual warning alert (If resource is low, make the gauge borders glow orange-red)
        setResourceLowAlert(coffeeFill, coffee < 150);
        setResourceLowAlert(milkFill, milk < 30);
        setResourceLowAlert(cacaoFill, cacao < 15);
    }

    private void setResourceLowAlert(Region element, boolean isLow)
    {
        if (isLow)
        {
            element.setStyle("-fx-border-color: #ff453a; -fx-border-width: 1.5; -fx-border-radius: 10;");
        }
        else
        {
            element.setStyle("");
        }
    }

    /**
     * Updates coin stock summaries and total cash in changer representation.
     */
    private void updateChangerStats()
    {
        Map<Muenze, Integer> stock = muenzwechsler.getMuenzBestand();
        int sumCents = 0;
        StringBuilder breakdown = new StringBuilder();

        // 1. Calculate sum including all coins
        for (Muenze m : Muenze.values())
        {
            int count = stock.getOrDefault(m, 0);
            sumCents += count * m.getWertInCents();
        }

        // 2. Format list vertically in descending order, skipping 1c, 2c, 5c
        Muenze[] coins = Muenze.values();
        for (int i = coins.length - 1; i >= 0; i--)
        {
            Muenze m = coins[i];
            if (m.getWertInCents() < 10) continue; // Skip very small coins to fit UI elegantly
            int count = stock.getOrDefault(m, 0);
            breakdown.append(String.format("%-4s: %d Stk\n", m.getAnzeigeName(), count));
        }

        // Remove trailing newline
        if (breakdown.length() > 0)
        {
            breakdown.setLength(breakdown.length() - 1);
        }

        cashInChangerLabel.setText(formatEuro(sumCents));
        coinBreakdownLabel.setText(breakdown.toString());
    }

    /**
     * Loads total orders count and revenue directly from SQLite database and displays them.
     */
    private void updateDatabaseStats()
    {
        tassenLabel.setText(kaffeeautomat.getTassen() + " Tassen");
        
        try
        {
            // Load stats directly from database tables
            double revenue = dbManager.getGesamtumsatz();
            revenueLabel.setText(String.format("%.2f €", revenue));
        } catch (Exception e) {
            revenueLabel.setText("0,00 €");
        }
    }

    /**
     * Logs a message into the operators console terminal with a timestamp.
     */
    private void logTerminal(String message)
    {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        terminalLog.appendText("[" + timestamp + "] " + message + "\n");
    }

    private String formatEuro(int cents)
    {
        return String.format("%.2f €", cents / 100.0);
    }

    private String formatCoinsList(List<Muenze> coins)
    {
        if (coins.isEmpty()) return "keines";
        StringBuilder sb = new StringBuilder();
        for (Muenze m : coins)
        {
            sb.append(m.getAnzeigeName()).append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}

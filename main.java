import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;

/*
 * QueueMate: Barangay Service Queue Management System
 * A beginner-friendly Java Swing GUI prototype.
 *
 * Save as QueueMate.java
 * Compile: javac QueueMate.java
 * Run:     java QueueMate
 */

public class QueueMate extends JFrame {
    private final QueueManager queueManager = new QueueManager();

    private JLabel totalLabel;
    private JLabel waitingLabel;
    private JLabel servingLabel;
    private JLabel completedLabel;
    private JLabel currentResidentLabel;

    private DefaultTableModel waitingModel;
    private DefaultTableModel completedModel;

    private JTable waitingTable;
    private JTable completedTable;

    public QueueMate() {
        setTitle("QueueMate | Barangay Service Queue Management System");
        setSize(1050, 680);
        setMinimumSize(new Dimension(850, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load previously saved queue records when the program starts.
        try {
            queueManager.loadFromFile();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load saved queue records: " + ex.getMessage(),
                    "File Loading Error", JOptionPane.ERROR_MESSAGE);
        }

        buildInterface();
        refreshTables();
    }

    private void buildInterface() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 22, 20, 22));
        setContentPane(mainPanel);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("QueueMate");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(28, 55, 90));

        JLabel subtitle = new JLabel("Barangay Service Queue Management System");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(95, 105, 120));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        JButton addButton = createButton("+ Add Resident", new Color(35, 110, 190));
        addButton.addActionListener(e -> showAddResidentDialog());
        header.add(addButton, BorderLayout.EAST);

        mainPanel.add(header, BorderLayout.NORTH);

        // Center content
        JPanel centerPanel = new JPanel(new BorderLayout(12, 15));
        centerPanel.setOpaque(false);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryPanel.setOpaque(false);

        totalLabel = new JLabel("0");
        waitingLabel = new JLabel("0");
        servingLabel = new JLabel("0");
        completedLabel = new JLabel("0");

        summaryPanel.add(createSummaryCard("TOTAL RESIDENTS", totalLabel, new Color(45, 95, 155)));
        summaryPanel.add(createSummaryCard("WAITING", waitingLabel, new Color(210, 145, 35)));
        summaryPanel.add(createSummaryCard("NOW SERVING", servingLabel, new Color(40, 135, 100)));
        summaryPanel.add(createSummaryCard("COMPLETED", completedLabel, new Color(105, 105, 120)));

        centerPanel.add(summaryPanel, BorderLayout.NORTH);

        // Queue tables
        waitingModel = createTableModel();
        completedModel = createTableModel();

        waitingTable = new JTable(waitingModel);
        completedTable = new JTable(completedModel);
        styleTable(waitingTable);
        styleTable(completedTable);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("Waiting List", new JScrollPane(waitingTable));
        tabs.addTab("Completed Transactions", new JScrollPane(completedTable));

        JPanel tablePanel = createWhitePanel(new BorderLayout(8, 8));
        JLabel tableTitle = new JLabel("Resident Queue Records");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 17));
        tableTitle.setForeground(new Color(35, 50, 70));
        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(tabs, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);

        // Current serving and actions
        JPanel bottomPanel = new JPanel(new BorderLayout(12, 0));
        bottomPanel.setOpaque(false);

        JPanel currentPanel = createWhitePanel(new BorderLayout(5, 5));
        JLabel currentTitle = new JLabel("CURRENTLY SERVING");
        currentTitle.setFont(new Font("Arial", Font.BOLD, 11));
        currentTitle.setForeground(new Color(100, 110, 125));

        currentResidentLabel = new JLabel("No resident is being served");
        currentResidentLabel.setFont(new Font("Arial", Font.BOLD, 17));
        currentResidentLabel.setForeground(new Color(35, 75, 115));

        currentPanel.add(currentTitle, BorderLayout.NORTH);
        currentPanel.add(currentResidentLabel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.setOpaque(false);

        JButton callButton = createButton("Call Next Number", new Color(35, 135, 100));
        callButton.addActionListener(e -> callNextResident());

        JButton completeButton = createButton("Mark as Completed", new Color(85, 95, 115));
        completeButton.addActionListener(e -> markCurrentCompleted());

        actionPanel.add(callButton);
        actionPanel.add(completeButton);

        bottomPanel.add(currentPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSummaryCard(String heading, JLabel number, Color accent) {
        JPanel card = createWhitePanel(new BorderLayout(5, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel label = new JLabel(heading);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(105, 115, 130));

        number.setFont(new Font("Arial", Font.BOLD, 27));
        number.setForeground(accent);

        card.add(label, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        return card;
    }

    private JPanel createWhitePanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.BLACK);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(
                new Object[]{"Queue No.", "Resident Name", "Requested Service", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(230, 236, 243));
        table.getTableHeader().setForeground(new Color(40, 55, 75));
        table.setSelectionBackground(new Color(215, 230, 245));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 234, 240));
        table.setFillsViewportHeight(true);
    }

    private void showAddResidentDialog() {
        JTextField nameField = new JTextField(20);

        String[] services = {
                "Barangay Clearance",
                "Certificate of Residency",
                "Certificate of Indigency"
        };
        JComboBox<String> serviceBox = new JComboBox<>(services);

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 7));
        form.add(new JLabel("Resident Name:"));
        form.add(nameField);
        form.add(new JLabel("Requested Service:"));
        form.add(serviceBox);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Register New Resident",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String selectedService = (String) serviceBox.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter the resident's name.",
                        "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BarangayService service = createService(selectedService);
            QueueTicket ticket = queueManager.addResident(name, service);
            saveRecords();

            refreshTables();

            JOptionPane.showMessageDialog(this,
                    "Resident successfully registered!\n\nQueue Number: "
                            + ticket.getQueueNumber()
                            + "\nName: " + name
                            + "\nService: " + service.getServiceName(),
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Creates the correct service object. Each service overrides displayServiceDetails().
    private BarangayService createService(String serviceName) {
        if ("Barangay Clearance".equals(serviceName)) {
            return new ClearanceService();
        } else if ("Certificate of Residency".equals(serviceName)) {
            return new ResidencyService();
        } else {
            return new IndigencyService();
        }
    }

    private void callNextResident() {
        try {
            QueueTicket ticket = queueManager.callNext();

            if (ticket == null) {
                JOptionPane.showMessageDialog(this,
                        "There are no residents waiting in the queue.",
                        "Queue is Empty", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            saveRecords();
            refreshTables();
            currentResidentLabel.setText(
                    ticket.getQueueNumber() + " - " + ticket.getResident().getResidentName());

            JOptionPane.showMessageDialog(this,
                    "Now serving: " + ticket.getResident().getResidentName()
                            + "\nQueue Number: " + ticket.getQueueNumber()
                            + "\nService: " + ticket.getService().displayServiceDetails(),
                    "Call Next Resident", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Queue Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void markCurrentCompleted() {
        QueueTicket ticket = queueManager.getCurrentServing();

        if (ticket == null) {
            JOptionPane.showMessageDialog(this,
                    "There is no resident currently being served.",
                    "No Active Transaction", JOptionPane.WARNING_MESSAGE);
            return;
        }

        queueManager.markCompleted(ticket);
        saveRecords();
        refreshTables();
        currentResidentLabel.setText("No resident is being served");

        JOptionPane.showMessageDialog(this,
                "Transaction completed for " + ticket.getResident().getResidentName() + ".",
                "Transaction Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    // Saves all queue records to a text file after every change.
    private void saveRecords() {
        try {
            queueManager.saveToFile();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save queue records: " + ex.getMessage(),
                    "File Saving Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTables() {
        waitingModel.setRowCount(0);
        completedModel.setRowCount(0);

        int waitingCount = 0;
        int servingCount = 0;
        int completedCount = 0;

        for (QueueTicket ticket : queueManager.getQueueList()) {
            Object[] row = {
                    ticket.getQueueNumber(),
                    ticket.getResident().getResidentName(),
                    ticket.getService().getServiceName(),
                    ticket.getStatus()
            };

            if ("Completed".equals(ticket.getStatus())) {
                completedModel.addRow(row);
                completedCount++;
            } else {
                waitingModel.addRow(row);

                if ("Waiting".equals(ticket.getStatus())) {
                    waitingCount++;
                } else if ("Serving".equals(ticket.getStatus())) {
                    servingCount++;
                }
            }
        }

        totalLabel.setText(String.valueOf(queueManager.getQueueList().size()));
        waitingLabel.setText(String.valueOf(waitingCount));
        servingLabel.setText(String.valueOf(servingCount));
        completedLabel.setText(String.valueOf(completedCount));

        QueueTicket current = queueManager.getCurrentServing();
        if (current == null) {
            currentResidentLabel.setText("No resident is being served");
        } else {
            currentResidentLabel.setText(
                    current.getQueueNumber() + " - " + current.getResident().getResidentName());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Use the default Swing look and feel if the system style is unavailable.
            }

            new QueueMate().setVisible(true);
        });
    }
}


// ======================== OOP CLASSES ========================

class Resident {
    private final String residentId;
    private final String residentName;

    public Resident(String residentId, String residentName) {
        this.residentId = residentId;
        this.residentName = residentName;
    }

    public String getResidentId() {
        return residentId;
    }

    public String getResidentName() {
        return residentName;
    }

    public String getResidentDetails() {
        return residentId + " - " + residentName;
    }
}


class QueueTicket {
    private final int queueNumber;
    private String status;
    private final Resident resident;
    private final BarangayService service;

    public QueueTicket(int queueNumber, Resident resident, BarangayService service) {
        this.queueNumber = queueNumber;
        this.resident = resident;
        this.service = service;
        this.status = "Waiting";
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public String getStatus() {
        return status;
    }

    public Resident getResident() {
        return resident;
    }

    public BarangayService getService() {
        return service;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public String displayTicket() {
        return "Queue No. " + queueNumber + " | "
                + resident.getResidentName() + " | "
                + service.getServiceName() + " | " + status;
    }
}


abstract class BarangayService {
    private final String serviceName;
    private final String description;

    public BarangayService(String serviceName, String description) {
        this.serviceName = serviceName;
        this.description = description;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public abstract String displayServiceDetails();
}


class ClearanceService extends BarangayService {
    public ClearanceService() {
        super("Barangay Clearance", "Request for a barangay clearance.");
    }

    @Override
    public String displayServiceDetails() {
        return getServiceName() + " - " + getDescription();
    }
}


class ResidencyService extends BarangayService {
    public ResidencyService() {
        super("Certificate of Residency", "Request for proof of residency.");
    }

    @Override
    public String displayServiceDetails() {
        return getServiceName() + " - " + getDescription();
    }
}


class IndigencyService extends BarangayService {
    public IndigencyService() {
        super("Certificate of Indigency", "Request for a certificate of indigency.");
    }

    @Override
    public String displayServiceDetails() {
        return getServiceName() + " - " + getDescription();
    }
}


class QueueManager {
    // The text file is created in the same folder where the program is run.
    private static final String FILE_NAME = "queuemate_records.txt";

    private final ArrayList<QueueTicket> queueList;
    private int nextQueueNumber;
    private QueueTicket currentServing;

    public QueueManager() {
        queueList = new ArrayList<>();
        nextQueueNumber = 1;
        currentServing = null;
    }

    public QueueTicket addResident(String residentName, BarangayService service) {
        String residentId = String.format("RES-%03d", nextQueueNumber);
        Resident resident = new Resident(residentId, residentName);

        QueueTicket ticket = new QueueTicket(nextQueueNumber, resident, service);
        queueList.add(ticket);
        nextQueueNumber++;

        return ticket;
    }

    public QueueTicket callNext() {
        if (currentServing != null) {
            throw new IllegalStateException(
                    "Please mark the current resident as Completed before calling the next number.");
        }

        for (QueueTicket ticket : queueList) {
            if ("Waiting".equals(ticket.getStatus())) {
                ticket.updateStatus("Serving");
                currentServing = ticket;
                return ticket;
            }
        }

        return null;
    }

    public void markCompleted(QueueTicket ticket) {
        if (ticket != null && ticket == currentServing) {
            ticket.updateStatus("Completed");
            currentServing = null;
        }
    }

    // Writes every resident and ticket to a local text file.
    public void saveToFile() throws IOException {
        Path filePath = Paths.get(FILE_NAME);

        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath, StandardCharsets.UTF_8)) {

            for (QueueTicket ticket : queueList) {
                String encodedName = Base64.getEncoder().encodeToString(
                        ticket.getResident().getResidentName()
                                .getBytes(StandardCharsets.UTF_8));

                String serviceCode = getServiceCode(ticket.getService());

                writer.write(ticket.getQueueNumber() + "|"
                        + ticket.getStatus() + "|"
                        + ticket.getResident().getResidentId() + "|"
                        + encodedName + "|"
                        + serviceCode);
                writer.newLine();
            }
        }
    }

    // Reads saved records when QueueMate starts.
    public void loadFromFile() throws IOException {
        Path filePath = Paths.get(FILE_NAME);

        if (!Files.exists(filePath)) {
            return; // No saved file yet; start with an empty queue.
        }

        queueList.clear();
        currentServing = null;
        nextQueueNumber = 1;

        try (BufferedReader reader = Files.newBufferedReader(
                filePath, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split("\\|", -1);

                if (data.length != 5) {
                    continue; // Ignore incomplete or invalid records.
                }

                try {
                    int queueNumber = Integer.parseInt(data[0]);
                    String status = data[1];
                    String residentId = data[2];

                    String residentName = new String(
                            Base64.getDecoder().decode(data[3]),
                            StandardCharsets.UTF_8);

                    BarangayService service = createServiceFromCode(data[4]);
                    Resident resident = new Resident(residentId, residentName);
                    QueueTicket ticket = new QueueTicket(
                            queueNumber, resident, service);

                    ticket.updateStatus(status);
                    queueList.add(ticket);

                    if ("Serving".equals(status)) {
                        currentServing = ticket;
                    }

                    if (queueNumber >= nextQueueNumber) {
                        nextQueueNumber = queueNumber + 1;
                    }
                } catch (IllegalArgumentException ex) {
                    // Skip a record if its number or encoded name is invalid.
                }
            }
        }
    }

    private String getServiceCode(BarangayService service) {
        if (service instanceof ClearanceService) {
            return "CLEARANCE";
        } else if (service instanceof ResidencyService) {
            return "RESIDENCY";
        } else {
            return "INDIGENCY";
        }
    }

    private BarangayService createServiceFromCode(String code) {
        if ("CLEARANCE".equals(code)) {
            return new ClearanceService();
        } else if ("RESIDENCY".equals(code)) {
            return new ResidencyService();
        } else {
            return new IndigencyService();
        }
    }

    public ArrayList<QueueTicket> getQueueList() {
        return queueList;
    }

    public QueueTicket getCurrentServing() {
        return currentServing;
    }

    public void displayQueue() {
        for (QueueTicket ticket : queueList) {
            System.out.println(ticket.displayTicket());
        }
    }
}
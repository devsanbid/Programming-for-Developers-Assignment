// Online Ticket Booking System – Concurrency Control
// Functionality:
//  Locks (Mutex / Semaphore): Ensure multiple users don’t book the same seat simultaneously.
//  Queue (Booking Requests): Manage pending seat reservation requests.
//  Database (Shared Resource): Store and update seat availability status concurrently.
// GUI:
//  A seating chart displaying available and booked seats.
//  A queue showing pending booking requests.
//  Buttons to:
// o Book a Seat (Simulate multiple users trying to book seats).
// o Enable Concurrency Control (Optimistic or Pessimistic Locking).
// o Process Bookings (Execute transactions concurrently).
// Implementation:
// Initialization:
// 1. Generate a seating layout for a theater/train/flight with available seats.
// 2. Create a queue of booking requests from multiple users.
// 3. Allow the user to choose a concurrency control mechanism (optimistic or pessimistic locking).
// 4. Display the seat availability in the GUI.
// Booking Process:
// 1. Choose a Concurrency Control Mechanism:
// o Optimistic Locking:
//  Read seat availability → Attempt to book → Check if status changed →
// Commit or retry.
// o Pessimistic Locking:
//  Lock the seat → Process booking → Unlock after completion.
// 2. Process Booking Requests:
// o Fetch a request from the queue.
// o Apply the chosen concurrency mechanism.
// o Update the seat status safely.
// 3. Real-time GUI Updates:
// o Show updated seat availability.
// o Handle failures if a seat is already booked.
// Booking Completion:
//  If a seat is successfully booked, confirm the booking.
//  If a conflict arises, retry or notify the user.
// Data Structures:
//  Queue: Store pending booking requests before processing.
//  HashMap / Dictionary: Maintain seat availability status.
//  Mutex / Semaphore: Prevent race conditions during seat selection.
//  Thread Pool: Simulate multiple users booking seats concurrently.
// Additional Considerations:
//  Deadlock Prevention: Handle timeout or avoid circular waits.
//  Transaction Logging: Keep a record of successful and failed bookings.
//  Performance Monitoring: Display success rate, conflicts, and retries.
//  Refund and Cancellation Handling: Allow users to cancel bookings and free up seats.


//Solution
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

public class TicketBookingSystem extends JFrame {

    // Constants
    private static final int ROWS = 10;
    private static final int COLS = 12;

    // Seat status constants
    private static final int AVAILABLE = 0;
    private static final int BOOKED = 1;
    private static final int PROCESSING = 2;
    private static final int SELECTED = 3; // NEW: for manual selection

    // Colors for seat visualization
    private static final Color AVAILABLE_COLOR = Color.WHITE;
    private static final Color BOOKED_COLOR = new Color(39, 174, 96); // pleasant green for booked
    private static final Color SELECTED_COLOR = new Color(46, 204, 113); // lighter green for selection
    private static final Color PROCESSING_COLOR = new Color(241, 196, 15); // yellow

    // Core data structures
    private final Map<String, SeatInfo> seatDatabase; // Shared resource (database)
    private final BlockingQueue<BookingRequest> bookingQueue; // Queue for pending requests
    private final ReentrantReadWriteLock databaseLock; // For pessimistic locking
    private final ExecutorService threadPool; // Thread pool for concurrent processing

    // GUI
    private JButton[][] seatButtons;
    private JTextArea queueArea;
    private JTextArea logArea;
    private JLabel statsLabel;
    private JComboBox<String> lockingModeCombo;
    private JProgressBar processingBar;

    // Seats selected by user before booking
    private final Set<String> selectedSeats = ConcurrentHashMap.newKeySet();

    // Statistics
    private final AtomicInteger successfulBookings = new AtomicInteger(0);
    private final AtomicInteger failedBookings = new AtomicInteger(0);
    private final AtomicInteger conflicts = new AtomicInteger(0);
    private final AtomicInteger retries = new AtomicInteger(0);

    private volatile boolean isProcessing = false;
    private boolean useOptimisticLocking = true;

    private static class SeatInfo {
        private volatile int status;
        private volatile long version;
        private volatile String bookedBy;
        private volatile long bookingTime;
        private final ReentrantLock seatLock;

        public SeatInfo() {
            this.status = AVAILABLE;
            this.version = 0;
            this.bookedBy = null;
            this.bookingTime = 0;
            this.seatLock = new ReentrantLock();
        }

        public synchronized SeatInfo copy() {
            SeatInfo copy = new SeatInfo();
            copy.status = this.status;
            copy.version = this.version;
            copy.bookedBy = this.bookedBy;
            copy.bookingTime = this.bookingTime;
            return copy;
        }
    }

    private static class BookingRequest {
        private final String userId;
        private final String seatId;
        private final long requestTime;
        private int retryCount;

        public BookingRequest(String userId, String seatId) {
            this.userId = userId;
            this.seatId = seatId;
            this.requestTime = System.currentTimeMillis();
            this.retryCount = 0;
        }

        @Override
        public String toString() {
            return String.format("User: %s, Seat: %s, Retries: %d", userId, seatId, retryCount);
        }
    }

    public TicketBookingSystem() {
        seatDatabase = new ConcurrentHashMap<>();
        bookingQueue = new LinkedBlockingQueue<>();
        databaseLock = new ReentrantReadWriteLock();
        threadPool = Executors.newFixedThreadPool(10);

        initializeSeats();
        initializeGUI();

        setTitle("Online Ticket Booking System - Concurrency Control Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private void initializeSeats() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String seatId = String.format("%c%d", 'A' + row, col + 1);
                seatDatabase.put(seatId, new SeatInfo());
            }
        }
    }

    private void initializeGUI() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createSeatPanel(), BorderLayout.CENTER);
        mainPanel.add(createControlPanel(), BorderLayout.NORTH);
        mainPanel.add(createInfoPanel(), BorderLayout.EAST);
        mainPanel.add(createStatsPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        Timer updateTimer = new Timer(500, e -> updateGUI());
        updateTimer.start();
    }

    private JPanel createSeatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Seating Chart (White=Available, Green=Selected, Dark Green=Booked, Yellow=Processing)"));

        JPanel seatGrid = new JPanel(new GridLayout(ROWS, COLS, 2, 2));
        seatButtons = new JButton[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String seatId = String.format("%c%d", 'A' + row, col + 1);
                JButton seatButton = new JButton(seatId);
                seatButton.setPreferredSize(new Dimension(60, 40));
                seatButton.setBackground(AVAILABLE_COLOR);
                seatButton.setOpaque(true);
                seatButton.setBorderPainted(true);
                seatButton.setFont(new Font("SansSerif", Font.BOLD, 18));
                seatButton.setForeground(Color.BLACK);

                // Manual selection logic
                seatButton.addActionListener(e -> handleSeatSelection(seatId));

                seatButtons[row][col] = seatButton;
                seatGrid.add(seatButton);
            }
        }
        panel.add(seatGrid, BorderLayout.CENTER);

        JLabel stageLabel = new JLabel("STAGE", SwingConstants.CENTER);
        stageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        stageLabel.setOpaque(true);
        stageLabel.setBackground(Color.LIGHT_GRAY);
        panel.add(stageLabel, BorderLayout.NORTH);

        return panel;
    }

    // Handle manual seat selection/deselection
    private void handleSeatSelection(String seatId) {
        SeatInfo info = seatDatabase.get(seatId);
        if (info == null) return;

        synchronized (info) {
            if (info.status == AVAILABLE || info.status == SELECTED) {
                if (info.status == AVAILABLE) {
                    info.status = SELECTED;
                    selectedSeats.add(seatId);
                } else if (info.status == SELECTED) {
                    info.status = AVAILABLE;
                    selectedSeats.remove(seatId);
                }
            }
        }
        updateSeatDisplay(seatId, seatDatabase.get(seatId).status);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JLabel lockingLabel = new JLabel("Concurrency Control:");
        lockingModeCombo = new JComboBox<>(new String[]{"Optimistic Locking", "Pessimistic Locking"});
        lockingModeCombo.addActionListener(e -> {
            useOptimisticLocking = lockingModeCombo.getSelectedIndex() == 0;
            logMessage("Switched to " + lockingModeCombo.getSelectedItem());
        });

        JButton processBookingsBtn = new JButton("Process Bookings");
        processBookingsBtn.addActionListener(e -> processSelectedBookings());

        JButton simulateUsersBtn = new JButton("Simulate Multiple Users");
        simulateUsersBtn.addActionListener(e -> simulateMultipleUsers());

        JButton clearQueueBtn = new JButton("Clear Queue");
        clearQueueBtn.addActionListener(e -> clearQueue());

        JButton resetSystemBtn = new JButton("Reset System");
        resetSystemBtn.addActionListener(e -> resetSystem());

        JButton cancelBookingBtn = new JButton("Cancel Random Booking");
        cancelBookingBtn.addActionListener(e -> cancelRandomBooking());

        processingBar = new JProgressBar(0, 100);
        processingBar.setStringPainted(true);
        processingBar.setString("Ready");

        panel.add(lockingLabel);
        panel.add(lockingModeCombo);
        panel.add(simulateUsersBtn);
        panel.add(processBookingsBtn);
        panel.add(clearQueueBtn);
        panel.add(cancelBookingBtn);
        panel.add(resetSystemBtn);
        panel.add(new JLabel("Processing:"));
        panel.add(processingBar);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 600));

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(new TitledBorder("Booking Queue"));
        queueArea = new JTextArea(10, 30);
        queueArea.setEditable(false);
        queueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        queuePanel.add(new JScrollPane(queueArea), BorderLayout.CENTER);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("Transaction Log"));
        logArea = new JTextArea(15, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        logPanel.add(clearLogBtn, BorderLayout.SOUTH);

        panel.add(queuePanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new TitledBorder("Performance Statistics"));

        statsLabel = new JLabel("Ready to start booking...");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(statsLabel);

        return panel;
    }

    // Only process currently SELECTED seats!
    private void processSelectedBookings() {
        if (isProcessing) {
            JOptionPane.showMessageDialog(this, "Already processing bookings!");
            return;
        }
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No seats selected!");
            return;
        }
        isProcessing = true;
        processingBar.setString("Processing...");
        // move all selected to bookingQueue
        List<String> batch = new ArrayList<>(selectedSeats);
        selectedSeats.clear();

        for (String seatId : batch) {
            // Only push request for seats still in SELECTED state (could have changed)
            SeatInfo info = seatDatabase.get(seatId);
            if (info != null) {
                synchronized (info) {
                    if (info.status == SELECTED) {
                        String userId = "User" + (int)(Math.random() * 1000);
                        BookingRequest req = new BookingRequest(userId, seatId);
                        try {
                            bookingQueue.put(req);
                            // Set seat to PROCESSING now to prevent others from selecting
                            info.status = PROCESSING;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
        updateGUI(); // display changes

        CompletableFuture.runAsync(() -> {
            try {
                processBookingsConcurrently();
            } finally {
                isProcessing = false;
                SwingUtilities.invokeLater(() -> {
                    processingBar.setValue(0);
                    processingBar.setString("Completed");
                });
            }
        });
    }

    // For simulation, the seats are added directly to bookingQueue as before
    private void simulateMultipleUsers() {
        Random random = new Random();
        int numRequests = 20 + random.nextInt(30); // 20-50 requests

        for (int i = 0; i < numRequests; i++) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);
            String seatId = String.format("%c%d", 'A' + row, col + 1);
            addBookingRequest(seatId);
        }

        logMessage("Simulated " + numRequests + " booking requests from multiple users");
    }

    // For simulation, add request directly to queue, not to SELECTED status
    private void addBookingRequest(String seatId) {
        String userId = "User" + (int)(Math.random() * 1000);
        BookingRequest request = new BookingRequest(userId, seatId);
        try {
            bookingQueue.put(request);
            logMessage("Added booking request: " + request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logMessage("Failed to add booking request: " + e.getMessage());
        }
    }

    private void processBookingsConcurrently() {
        int totalRequests = bookingQueue.size();
        AtomicInteger processedRequests = new AtomicInteger(0);

        logMessage("Starting concurrent processing of " + totalRequests + " requests using "
                + (useOptimisticLocking ? "Optimistic" : "Pessimistic") + " locking");

        java.util.List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (!bookingQueue.isEmpty()) {
            BookingRequest request = bookingQueue.poll();
            if (request != null) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    if (useOptimisticLocking) {
                        processBookingOptimistic(request);
                    } else {
                        processBookingPessimistic(request);
                    }

                    int processed = processedRequests.incrementAndGet();
                    int progress = (processed * 100) / totalRequests;
                    SwingUtilities.invokeLater(() -> processingBar.setValue(progress));

                }, threadPool);

                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        logMessage("Completed processing all booking requests");
    }

    private void processBookingOptimistic(BookingRequest request) {
        final int MAX_RETRIES = 3;
        boolean success = false;

        for (int attempt = 0; attempt < MAX_RETRIES && !success; attempt++) {
            try {
                Thread.sleep(50 + (int)(Math.random() * 100));
                SeatInfo currentSeat = seatDatabase.get(request.seatId);
                if (currentSeat == null) continue;

                SeatInfo snapshot = currentSeat.copy();

                if (snapshot.status == BOOKED) {
                    failedBookings.incrementAndGet();
                    logMessage("FAILED: Seat " + request.seatId + " already booked (User: " + request.userId + ")");
                    break;
                }

                updateSeatStatus(request.seatId, PROCESSING);
                Thread.sleep(100 + (int)(Math.random() * 200));

                synchronized (currentSeat) {
                    if (currentSeat.version == snapshot.version && currentSeat.status == PROCESSING) {
                        currentSeat.status = BOOKED;
                        currentSeat.bookedBy = request.userId;
                        currentSeat.bookingTime = System.currentTimeMillis();
                        currentSeat.version++;

                        successfulBookings.incrementAndGet();
                        logMessage("SUCCESS: Seat " + request.seatId + " booked by " + request.userId);
                        success = true;
                    } else {
                        conflicts.incrementAndGet();
                        retries.incrementAndGet();
                        request.retryCount++;
                        updateSeatStatus(request.seatId, AVAILABLE);
                        logMessage("CONFLICT: Retrying booking for seat " + request.seatId + " (User: " + request.userId + ")");
                        Thread.sleep(100);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (!success) {
            updateSeatStatus(request.seatId, AVAILABLE); // Reset to available
            failedBookings.incrementAndGet();
            logMessage("FAILED: Max retries exceeded for seat " + request.seatId + " (User: " + request.userId + ")");
        }
    }

    private void processBookingPessimistic(BookingRequest request) {
        SeatInfo seatInfo = seatDatabase.get(request.seatId);
        if (seatInfo == null) return;
        try {
            if (seatInfo.seatLock.tryLock(2000, TimeUnit.MILLISECONDS)) {
                try {
                    if (seatInfo.status == BOOKED) {
                        failedBookings.incrementAndGet();
                        logMessage("FAILED: Seat " + request.seatId + " already booked (User: " + request.userId + ")");
                        return;
                    }
                    seatInfo.status = PROCESSING;
                    updateSeatDisplay(request.seatId, PROCESSING);

                    Thread.sleep(100 + (int)(Math.random() * 200));

                    seatInfo.status = BOOKED;
                    seatInfo.bookedBy = request.userId;
                    seatInfo.bookingTime = System.currentTimeMillis();
                    seatInfo.version++;

                    successfulBookings.incrementAndGet();
                    logMessage("SUCCESS: Seat " + request.seatId + " booked by " + request.userId + " (Pessimistic)");
                } finally {
                    seatInfo.seatLock.unlock();
                }
            } else {
                failedBookings.incrementAndGet();
                logMessage("TIMEOUT: Failed to acquire lock for seat " + request.seatId + " (User: " + request.userId + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failedBookings.incrementAndGet();
            logMessage("INTERRUPTED: Booking interrupted for seat " + request.seatId + " (User: " + request.userId + ")");
        }
    }

    private void updateSeatStatus(String seatId, int status) {
        SeatInfo seatInfo = seatDatabase.get(seatId);
        if (seatInfo != null) {
            synchronized (seatInfo) {
                seatInfo.status = status;
            }
            updateSeatDisplay(seatId, status);
        }
    }

    // IMPORTANT: Now supports SELECTED state
    private void updateSeatDisplay(String seatId, int status) {
        SwingUtilities.invokeLater(() -> {
            char rowChar = seatId.charAt(0);
            int colNum = Integer.parseInt(seatId.substring(1));
            int row = rowChar - 'A';
            int col = colNum - 1;

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                JButton button = seatButtons[row][col];
                switch (status) {
                    case AVAILABLE:
                        button.setBackground(AVAILABLE_COLOR);
                        button.setText(seatId);
                        button.setForeground(Color.BLACK);
                        break;
                    case SELECTED:
                        button.setBackground(SELECTED_COLOR);
                        button.setText(seatId);
                        button.setForeground(Color.BLACK);
                        break;
                    case BOOKED:
                        button.setBackground(BOOKED_COLOR);
                        button.setText("✓");
                        button.setForeground(Color.WHITE);
                        break;
                    case PROCESSING:
                        button.setBackground(PROCESSING_COLOR);
                        button.setText("...");
                        button.setForeground(Color.BLACK);
                        break;
                }
            }
        });
    }

    private void cancelRandomBooking() {
        java.util.List<String> bookedSeats = new ArrayList<>();
        for (Map.Entry<String, SeatInfo> entry : seatDatabase.entrySet()) {
            if (entry.getValue().status == BOOKED) {
                bookedSeats.add(entry.getKey());
            }
        }
        if (bookedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bookings to cancel!");
            return;
        }
        String seatToCancel = bookedSeats.get((int)(Math.random() * bookedSeats.size()));
        SeatInfo seatInfo = seatDatabase.get(seatToCancel);

        if (seatInfo != null) {
            synchronized (seatInfo) {
                String originalUser = seatInfo.bookedBy;
                seatInfo.status = AVAILABLE;
                seatInfo.bookedBy = null;
                seatInfo.bookingTime = 0;
                seatInfo.version++;
                updateSeatDisplay(seatToCancel, AVAILABLE);
                logMessage("CANCELLED: Seat " + seatToCancel + " booking cancelled (was booked by " + originalUser + ")");
            }
        }
    }

    private void clearQueue() {
        int cleared = bookingQueue.size();
        bookingQueue.clear();
        logMessage("Cleared " + cleared + " requests from queue");
    }

    private void resetSystem() {
        if (isProcessing) {
            JOptionPane.showMessageDialog(this, "Cannot reset while processing!");
            return;
        }
        for (SeatInfo seatInfo : seatDatabase.values()) {
            synchronized (seatInfo) {
                seatInfo.status = AVAILABLE;
                seatInfo.bookedBy = null;
                seatInfo.bookingTime = 0;
                seatInfo.version = 0;
            }
        }
        bookingQueue.clear();
        successfulBookings.set(0);
        failedBookings.set(0);
        conflicts.set(0);
        retries.set(0);
        logArea.setText("");
        processingBar.setValue(0);
        processingBar.setString("Ready");
        selectedSeats.clear();
        logMessage("System reset completed");
    }

    private void updateGUI() {
        for (Map.Entry<String, SeatInfo> entry : seatDatabase.entrySet()) {
            updateSeatDisplay(entry.getKey(), entry.getValue().status);
        }
        StringBuilder queueText = new StringBuilder();
        queueText.append("Pending Requests: ").append(bookingQueue.size()).append("\n\n");
        Object[] queueArray = bookingQueue.toArray();
        for (int i = 0; i < Math.min(queueArray.length, 10); i++) {
            queueText.append(i + 1).append(". ").append(queueArray[i]).append("\n");
        }
        if (queueArray.length > 10) {
            queueText.append("... and ").append(queueArray.length - 10).append(" more");
        }
        queueArea.setText(queueText.toString());

        int total = successfulBookings.get() + failedBookings.get();
        double successRate = total > 0 ? (successfulBookings.get() * 100.0 / total) : 0;
        String statsText = String.format(
            "Success: %d | Failed: %d | Conflicts: %d | Retries: %d | Success Rate: %.1f%% | Queue: %d",
            successfulBookings.get(),
            failedBookings.get(),
            conflicts.get(),
            retries.get(),
            successRate,
            bookingQueue.size()
        );
        statsLabel.setText(statsText);
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            logArea.append(timestamp + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void cleanup() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            TicketBookingSystem system = new TicketBookingSystem();
            Runtime.getRuntime().addShutdownHook(new Thread(system::cleanup));
            system.setVisible(true);
        });
    }
}

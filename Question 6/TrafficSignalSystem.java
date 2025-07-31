// Traffic Signal Management System (Multithreaded)
// Functionality:
//  Queue (FIFO Scheduling): Manage vehicles at a traffic signal.
//  Priority Queue (Emergency Vehicles): Give priority to ambulances and fire trucks.
//  Multithreading:
// o Separate threads for traffic light changes, vehicle movement, and emergency
// handling.
// GUI:
//  An animated traffic intersection.
//  A queue showing waiting vehicles.
//  Buttons to:
// o Change Traffic Signal (Simulates signal changes in real-time).
// o Add Vehicles (Continuously add vehicles with a thread).
// o Enable Emergency Mode (Emergency vehicle gets priority in multithreaded execution).
// Implementation:
//  Main thread: Handles GUI and user inputs.
//  Traffic light thread: Changes signals at fixed intervals.
//  Vehicle queue thread: Processes vehicles using FIFO and priority queue logic.
// Data Structures:
//  Queue: Regular vehicle queue.
//  Priority Queue: Emergency vehicle handling.
// Multithreading Benefits:
//  Vehicles move in real-time without blocking GUI updates.
//  Traffic lights operate independently of vehicle movement.


//Solution
import java.awt.*;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * Traffic Signal Management System with Multithreading
 * Features: FIFO Queue, Priority Queue for Emergency Vehicles, Real-time Animation
 * Threads: Main GUI, Traffic Light Controller, Vehicle Movement, Emergency Handler
 */
public class TrafficSignalSystem extends JFrame {
    
    // Constants
    private static final int INTERSECTION_SIZE = 400;
    private static final int LANE_WIDTH = 80;
    private static final int VEHICLE_SIZE = 30;
    private static final int MAX_VEHICLES_PER_LANE = 8;
    
    // Traffic Light States
    private enum TrafficLight {
        RED, YELLOW, GREEN
    }
    
    // Vehicle Types
    private enum VehicleType {
        CAR(1, Color.BLUE),
        TRUCK(2, Color.GRAY),
        AMBULANCE(10, Color.WHITE),
        FIRE_TRUCK(10, Color.RED);
        
        final int priority;
        final Color color;
        
        VehicleType(int priority, Color color) {
            this.priority = priority;
            this.color = color;
        }
        
        boolean isEmergency() {
            return this == AMBULANCE || this == FIRE_TRUCK;
        }
    }
    
    // Directions
    private enum Direction {
        NORTH(0, -1), SOUTH(0, 1), EAST(1, 0), WEST(-1, 0);
        
        final int deltaX, deltaY;
        
        Direction(int deltaX, int deltaY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }
    }
    
    // Vehicle class
    private static class Vehicle implements Comparable<Vehicle> {
        final String id;
        final VehicleType type;
        final Direction direction;
        final long arrivalTime;
        int x, y;
        boolean isMoving = false;
        
        Vehicle(VehicleType type, Direction direction) {
            this.id = "V" + System.currentTimeMillis() % 10000;
            this.type = type;
            this.direction = direction;
            this.arrivalTime = System.currentTimeMillis();
            setInitialPosition();
        }
        
        private void setInitialPosition() {
            switch (direction) {
                case NORTH:
                    x = INTERSECTION_SIZE / 2 + LANE_WIDTH / 2;
                    y = INTERSECTION_SIZE - VEHICLE_SIZE;
                    break;
                case SOUTH:
                    x = INTERSECTION_SIZE / 2 - LANE_WIDTH / 2;
                    y = 0;
                    break;
                case EAST:
                    x = 0;
                    y = INTERSECTION_SIZE / 2 - LANE_WIDTH / 2;
                    break;
                case WEST:
                    x = INTERSECTION_SIZE - VEHICLE_SIZE;
                    y = INTERSECTION_SIZE / 2 + LANE_WIDTH / 2;
                    break;
            }
        }
        
        void move() {
            x += direction.deltaX * 5;
            y += direction.deltaY * 5;
        }
        
        boolean isOutOfBounds() {
            return x < -VEHICLE_SIZE || x > INTERSECTION_SIZE + VEHICLE_SIZE ||
                   y < -VEHICLE_SIZE || y > INTERSECTION_SIZE + VEHICLE_SIZE;
        }
        
        @Override
        public int compareTo(Vehicle other) {
            // Higher priority first (emergency vehicles)
            int priorityCompare = Integer.compare(other.type.priority, this.type.priority);
            if (priorityCompare != 0) return priorityCompare;
            
            // If same priority, FIFO (earlier arrival time first)
            return Long.compare(this.arrivalTime, other.arrivalTime);
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s, %s)", id, type, direction);
        }
    }
    
    // Core Data Structures
    private final BlockingQueue<Vehicle> northQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Vehicle> southQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Vehicle> eastQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Vehicle> westQueue = new LinkedBlockingQueue<>();
    
    private final PriorityQueue<Vehicle> emergencyQueue = new PriorityQueue<>();
    private final CopyOnWriteArrayList<Vehicle> movingVehicles = new CopyOnWriteArrayList<>();
    
    // Traffic Light State
    private volatile TrafficLight northSouthLight = TrafficLight.GREEN;
    private volatile TrafficLight eastWestLight = TrafficLight.RED;
    
    // Thread Control
    private final ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private final AtomicBoolean systemRunning = new AtomicBoolean(true);
    private final AtomicBoolean emergencyMode = new AtomicBoolean(false);
    private final AtomicBoolean autoAddVehicles = new AtomicBoolean(false);
    
    // GUI Components
    private IntersectionPanel intersectionPanel;
    private JTextArea queueArea;
    private JTextArea emergencyArea;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JLabel statsLabel;
    
    // Statistics
    private final AtomicInteger totalVehicles = new AtomicInteger(0);
    private final AtomicInteger emergencyVehicles = new AtomicInteger(0);
    private final AtomicInteger processedVehicles = new AtomicInteger(0);
    
    public TrafficSignalSystem() {
        initializeGUI();
        startThreads();
        
        setTitle("Traffic Signal Management System - Multithreaded");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void initializeGUI() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Intersection panel
        intersectionPanel = new IntersectionPanel();
        mainPanel.add(intersectionPanel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Info panel
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.EAST);
        
        // Status panel
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Start GUI update timer
        Timer guiTimer = new Timer(50, e -> {
            intersectionPanel.repaint();
            updateInfoPanels();
        });
        guiTimer.start();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton addCarBtn = new JButton("Add Car");
        addCarBtn.addActionListener(e -> addRandomVehicle(false));
        
        JButton addTruckBtn = new JButton("Add Truck");
        addTruckBtn.addActionListener(e -> addRandomVehicle(true));
        
        JButton addEmergencyBtn = new JButton("Add Emergency Vehicle");
        addEmergencyBtn.addActionListener(e -> addEmergencyVehicle());
        
        JToggleButton autoAddBtn = new JToggleButton("Auto Add Vehicles");
        autoAddBtn.addActionListener(e -> {
            autoAddVehicles.set(autoAddBtn.isSelected());
            if (autoAddBtn.isSelected()) {
                startAutoVehicleGeneration();
            }
        });
        
        JToggleButton emergencyModeBtn = new JToggleButton("Emergency Mode");
        emergencyModeBtn.addActionListener(e -> {
            emergencyMode.set(emergencyModeBtn.isSelected());
            logMessage("Emergency mode " + (emergencyModeBtn.isSelected() ? "ENABLED" : "DISABLED"));
        });
        
        JButton changeSignalBtn = new JButton("Manual Signal Change");
        changeSignalBtn.addActionListener(e -> manualSignalChange());
        
        JButton clearAllBtn = new JButton("Clear All");
        clearAllBtn.addActionListener(e -> clearAllVehicles());
        
        panel.add(addCarBtn);
        panel.add(addTruckBtn);
        panel.add(addEmergencyBtn);
        panel.add(autoAddBtn);
        panel.add(emergencyModeBtn);
        panel.add(changeSignalBtn);
        panel.add(clearAllBtn);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 600));
        
        // Queue panel
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(new TitledBorder("Vehicle Queues"));
        queueArea = new JTextArea(12, 30);
        queueArea.setEditable(false);
        queueArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        queuePanel.add(new JScrollPane(queueArea), BorderLayout.CENTER);
        
        // Emergency panel
        JPanel emergencyPanel = new JPanel(new BorderLayout());
        emergencyPanel.setBorder(new TitledBorder("Emergency Queue"));
        emergencyArea = new JTextArea(6, 30);
        emergencyArea.setEditable(false);
        emergencyArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        emergencyArea.setBackground(new Color(255, 240, 240));
        emergencyPanel.add(new JScrollPane(emergencyArea), BorderLayout.CENTER);
        
        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("System Log"));
        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        logPanel.add(clearLogBtn, BorderLayout.SOUTH);
        
        panel.add(queuePanel, BorderLayout.NORTH);
        panel.add(emergencyPanel, BorderLayout.CENTER);
        panel.add(logPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        statusLabel = new JLabel("System Status: Running | Lights: NS-Green, EW-Red");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        statsLabel = new JLabel("Total: 0 | Emergency: 0 | Processed: 0");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(statsLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void startThreads() {
        // Traffic Light Controller Thread
        threadPool.submit(() -> {
            while (systemRunning.get()) {
                try {
                    // Normal cycle: 5 seconds green, 2 seconds yellow, 5 seconds red
                    if (!emergencyMode.get()) {
                        Thread.sleep(5000); // Green light duration
                        changeToYellow();
                        Thread.sleep(2000); // Yellow light duration
                        switchLights();
                    } else {
                        // In emergency mode, prioritize emergency vehicles
                        handleEmergencyMode();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        // Vehicle Movement Thread
        threadPool.submit(() -> {
            while (systemRunning.get()) {
                try {
                    processVehicleMovement();
                    Thread.sleep(100); // Movement update frequency
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        // Vehicle Queue Processing Thread
        threadPool.submit(() -> {
            while (systemRunning.get()) {
                try {
                    processVehicleQueues();
                    Thread.sleep(200); // Queue processing frequency
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        // Emergency Vehicle Handler Thread
        threadPool.submit(() -> {
            while (systemRunning.get()) {
                try {
                    processEmergencyQueue();
                    Thread.sleep(100); // Emergency processing frequency
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    private void processVehicleMovement() {
        for (Vehicle vehicle : movingVehicles) {
            vehicle.move();
            if (vehicle.isOutOfBounds()) {
                movingVehicles.remove(vehicle);
                processedVehicles.incrementAndGet();
                logMessage("Vehicle " + vehicle.id + " exited intersection");
            }
        }
    }
    
    private void processVehicleQueues() {
        // Process vehicles based on traffic light state
        if (northSouthLight == TrafficLight.GREEN) {
            processDirectionQueue(northQueue, Direction.NORTH);
            processDirectionQueue(southQueue, Direction.SOUTH);
        }
        
        if (eastWestLight == TrafficLight.GREEN) {
            processDirectionQueue(eastQueue, Direction.EAST);
            processDirectionQueue(westQueue, Direction.WEST);
        }
    }
    
    private void processDirectionQueue(BlockingQueue<Vehicle> queue, Direction direction) {
        Vehicle vehicle = queue.poll();
        if (vehicle != null && canVehicleMove(direction)) {
            vehicle.isMoving = true;
            movingVehicles.add(vehicle);
            logMessage("Vehicle " + vehicle.id + " started moving " + direction);
        } else if (vehicle != null) {
            // Put back if can't move
            try {
                queue.put(vehicle);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void processEmergencyQueue() {
        if (!emergencyQueue.isEmpty() && emergencyMode.get()) {
            Vehicle emergency = emergencyQueue.poll();
            if (emergency != null) {
                // Emergency vehicles get immediate priority
                emergency.isMoving = true;
                movingVehicles.add(emergency);
                logMessage("EMERGENCY: " + emergency.id + " given immediate priority!");
                
                // Force traffic light change for emergency vehicle
                if (emergency.direction == Direction.NORTH || emergency.direction == Direction.SOUTH) {
                    northSouthLight = TrafficLight.GREEN;
                    eastWestLight = TrafficLight.RED;
                } else {
                    eastWestLight = TrafficLight.GREEN;
                    northSouthLight = TrafficLight.RED;
                }
            }
        }
    }
    
    private boolean canVehicleMove(Direction direction) {
        // Check if intersection is clear enough
        long movingInSameDirection = movingVehicles.stream()
            .filter(v -> v.direction == direction)
            .count();
        return movingInSameDirection < 2; // Limit concurrent vehicles per direction
    }
    
    private void handleEmergencyMode() throws InterruptedException {
        if (!emergencyQueue.isEmpty()) {
            // Shorter cycles in emergency mode
            Thread.sleep(1000);
        } else {
            // Normal operation but faster
            Thread.sleep(3000);
            changeToYellow();
            Thread.sleep(1000);
            switchLights();
        }
    }
    
    private void changeToYellow() {
        if (northSouthLight == TrafficLight.GREEN) {
            northSouthLight = TrafficLight.YELLOW;
        }
        if (eastWestLight == TrafficLight.GREEN) {
            eastWestLight = TrafficLight.YELLOW;
        }
        logMessage("Traffic lights changed to YELLOW");
    }
    
    private void switchLights() {
        if (northSouthLight == TrafficLight.YELLOW) {
            northSouthLight = TrafficLight.RED;
            eastWestLight = TrafficLight.GREEN;
        } else if (eastWestLight == TrafficLight.YELLOW) {
            eastWestLight = TrafficLight.RED;
            northSouthLight = TrafficLight.GREEN;
        }
        logMessage("Traffic lights switched: NS-" + northSouthLight + ", EW-" + eastWestLight);
    }
    
    private void manualSignalChange() {
        switchLights();
        logMessage("Manual signal change triggered");
    }
    
    private void addRandomVehicle(boolean isTruck) {
        Random random = new Random();
        Direction[] directions = Direction.values();
        Direction direction = directions[random.nextInt(directions.length)];
        
        VehicleType type = isTruck ? VehicleType.TRUCK : VehicleType.CAR;
        Vehicle vehicle = new Vehicle(type, direction);
        
        addVehicleToQueue(vehicle);
        totalVehicles.incrementAndGet();
        logMessage("Added " + type + " " + vehicle.id + " going " + direction);
    }
    
    private void addEmergencyVehicle() {
        Random random = new Random();
        Direction[] directions = Direction.values();
        Direction direction = directions[random.nextInt(directions.length)];
        
        VehicleType type = random.nextBoolean() ? VehicleType.AMBULANCE : VehicleType.FIRE_TRUCK;
        Vehicle vehicle = new Vehicle(type, direction);
        
        emergencyQueue.offer(vehicle);
        totalVehicles.incrementAndGet();
        emergencyVehicles.incrementAndGet();
        logMessage("EMERGENCY: Added " + type + " " + vehicle.id + " going " + direction);
    }
    
    private void addVehicleToQueue(Vehicle vehicle) {
        try {
            switch (vehicle.direction) {
                case NORTH:
                    if (northQueue.size() < MAX_VEHICLES_PER_LANE) northQueue.put(vehicle);
                    break;
                case SOUTH:
                    if (southQueue.size() < MAX_VEHICLES_PER_LANE) southQueue.put(vehicle);
                    break;
                case EAST:
                    if (eastQueue.size() < MAX_VEHICLES_PER_LANE) eastQueue.put(vehicle);
                    break;
                case WEST:
                    if (westQueue.size() < MAX_VEHICLES_PER_LANE) westQueue.put(vehicle);
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void startAutoVehicleGeneration() {
        threadPool.submit(() -> {
            Random random = new Random();
            while (autoAddVehicles.get() && systemRunning.get()) {
                try {
                    Thread.sleep(1000 + random.nextInt(2000)); // Random interval 1-3 seconds
                    if (random.nextDouble() < 0.1) { // 10% chance for emergency
                        addEmergencyVehicle();
                    } else {
                        addRandomVehicle(random.nextBoolean());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    private void clearAllVehicles() {
        northQueue.clear();
        southQueue.clear();
        eastQueue.clear();
        westQueue.clear();
        emergencyQueue.clear();
        movingVehicles.clear();
        logMessage("All vehicles cleared from system");
    }
    
    private void updateInfoPanels() {
        // Update queue display
        StringBuilder queueText = new StringBuilder();
        queueText.append("NORTH (").append(northQueue.size()).append("): ");
        northQueue.forEach(v -> queueText.append(v.id).append(" "));
        queueText.append("\n\nSOUTH (").append(southQueue.size()).append("): ");
        southQueue.forEach(v -> queueText.append(v.id).append(" "));
        queueText.append("\n\nEAST (").append(eastQueue.size()).append("): ");
        eastQueue.forEach(v -> queueText.append(v.id).append(" "));
        queueText.append("\n\nWEST (").append(westQueue.size()).append("): ");
        westQueue.forEach(v -> queueText.append(v.id).append(" "));
        queueText.append("\n\nMOVING (").append(movingVehicles.size()).append("): ");
        movingVehicles.forEach(v -> queueText.append(v.id).append(" "));
        
        queueArea.setText(queueText.toString());
        
        // Update emergency display
        StringBuilder emergencyText = new StringBuilder();
        emergencyText.append("Priority Queue (").append(emergencyQueue.size()).append("):\n");
        emergencyQueue.forEach(v -> emergencyText.append(v.toString()).append("\n"));
        emergencyArea.setText(emergencyText.toString());
        
        // Update status
        String lightStatus = "NS-" + northSouthLight + ", EW-" + eastWestLight;
        statusLabel.setText("System Status: Running | Lights: " + lightStatus + 
                           (emergencyMode.get() ? " | EMERGENCY MODE" : ""));
        
        statsLabel.setText("Total: " + totalVehicles.get() + 
                          " | Emergency: " + emergencyVehicles.get() + 
                          " | Processed: " + processedVehicles.get());
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            logArea.append(timestamp + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // Custom panel for intersection visualization
    private class IntersectionPanel extends JPanel {
        public IntersectionPanel() {
            setPreferredSize(new Dimension(INTERSECTION_SIZE, INTERSECTION_SIZE));
            setBackground(Color.DARK_GRAY);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            drawRoads(g2d);
            drawTrafficLights(g2d);
            drawVehicles(g2d);
            drawQueuedVehicles(g2d);
        }
        
        private void drawRoads(Graphics2D g2d) {
            // Road surface
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, INTERSECTION_SIZE/2 - LANE_WIDTH, INTERSECTION_SIZE, LANE_WIDTH * 2);
            g2d.fillRect(INTERSECTION_SIZE/2 - LANE_WIDTH, 0, LANE_WIDTH * 2, INTERSECTION_SIZE);
            
            // Lane markings
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10}, 0));
            
            // Horizontal lane divider
            g2d.drawLine(0, INTERSECTION_SIZE/2, INTERSECTION_SIZE, INTERSECTION_SIZE/2);
            
            // Vertical lane divider
            g2d.drawLine(INTERSECTION_SIZE/2, 0, INTERSECTION_SIZE/2, INTERSECTION_SIZE);
        }
        
        private void drawTrafficLights(Graphics2D g2d) {
            int lightSize = 20;
            int lightX = INTERSECTION_SIZE/2 - LANE_WIDTH - 30;
            int lightY = INTERSECTION_SIZE/2 - LANE_WIDTH - 30;
            
            // North-South lights
            g2d.setColor(Color.BLACK);
            g2d.fillRect(lightX, lightY, lightSize, lightSize * 3);
            
            g2d.setColor(northSouthLight == TrafficLight.RED ? Color.RED : Color.DARK_GRAY);
            g2d.fillOval(lightX + 2, lightY + 2, lightSize - 4, lightSize - 4);
            
            g2d.setColor(northSouthLight == TrafficLight.YELLOW ? Color.YELLOW : Color.DARK_GRAY);
            g2d.fillOval(lightX + 2, lightY + lightSize + 2, lightSize - 4, lightSize - 4);
            
            g2d.setColor(northSouthLight == TrafficLight.GREEN ? Color.GREEN : Color.DARK_GRAY);
            g2d.fillOval(lightX + 2, lightY + lightSize * 2 + 2, lightSize - 4, lightSize - 4);
            
            // East-West lights
            lightX = INTERSECTION_SIZE/2 + LANE_WIDTH + 10;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(lightX, lightY, lightSize, lightSize * 3);
            
            g2d.setColor(eastWestLight == TrafficLight.RED ? Color.RED : Color.DARK_GRAY);
            g2d.fillOval(lightX + 2, lightY + 2, lightSize - 4, lightSize - 4);
            
            g2d.setColor(eastWestLight == TrafficLight.YELLOW ? Color.YELLOW : Color.DARK_GRAY);
            g2d.fillOval(lightX + 2, lightY + lightSize + 2, lightSize - 4, lightSize - 4);
            
            g2d.setColor(eastWestLight == TrafficLight.GREEN ? Color.GREEN : Color.DARK_GRAY);
            g2d.fillOval(lightX + 2, lightY + lightSize * 2 + 2, lightSize - 4, lightSize - 4);
        }
        
        private void drawVehicles(Graphics2D g2d) {
            // Draw moving vehicles
            for (Vehicle vehicle : movingVehicles) {
                drawVehicle(g2d, vehicle, vehicle.x, vehicle.y);
            }
        }
        
        private void drawQueuedVehicles(Graphics2D g2d) {
            // Draw queued vehicles
            drawQueuedVehiclesInDirection(g2d, northQueue, Direction.NORTH);
            drawQueuedVehiclesInDirection(g2d, southQueue, Direction.SOUTH);
            drawQueuedVehiclesInDirection(g2d, eastQueue, Direction.EAST);
            drawQueuedVehiclesInDirection(g2d, westQueue, Direction.WEST);
            
            // Draw emergency vehicles
            int emergencyX = 10;
            int emergencyY = 10;
            for (Vehicle vehicle : emergencyQueue) {
                drawVehicle(g2d, vehicle, emergencyX, emergencyY);
                emergencyY += VEHICLE_SIZE + 5;
            }
        }
        
        private void drawQueuedVehiclesInDirection(Graphics2D g2d, BlockingQueue<Vehicle> queue, Direction direction) {
            Vehicle[] vehicles = queue.toArray(new Vehicle[0]);
            for (int i = 0; i < vehicles.length; i++) {
                Vehicle vehicle = vehicles[i];
                int queueX = vehicle.x;
                int queueY = vehicle.y;
                
                switch (direction) {
                    case NORTH:
                        queueY += (i + 1) * (VEHICLE_SIZE + 5);
                        break;
                    case SOUTH:
                        queueY -= (i + 1) * (VEHICLE_SIZE + 5);
                        break;
                    case EAST:
                        queueX -= (i + 1) * (VEHICLE_SIZE + 5);
                        break;
                    case WEST:
                        queueX += (i + 1) * (VEHICLE_SIZE + 5);
                        break;
                }
                
                drawVehicle(g2d, vehicle, queueX, queueY);
            }
        }
        
        private void drawVehicle(Graphics2D g2d, Vehicle vehicle, int x, int y) {
            g2d.setColor(vehicle.type.color);
            g2d.fillRect(x, y, VEHICLE_SIZE, VEHICLE_SIZE);
            
            // Border
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, VEHICLE_SIZE, VEHICLE_SIZE);
            
            // Vehicle ID
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 8));
            g2d.drawString(vehicle.id.substring(1), x + 2, y + 12);
            
            // Emergency indicator
            if (vehicle.type.isEmergency()) {
                g2d.setColor(Color.RED);
                g2d.fillOval(x + VEHICLE_SIZE - 8, y, 8, 8);
            }
        }
    }
    
    private void cleanup() {
        systemRunning.set(false);
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
            
            TrafficSignalSystem system = new TrafficSignalSystem();
            
            Runtime.getRuntime().addShutdownHook(new Thread(system::cleanup));
            
            system.setVisible(true);
        });
    }
}

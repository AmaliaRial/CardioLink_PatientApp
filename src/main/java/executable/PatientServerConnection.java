package executable;

import bitalino.BITalino;
import bitalino.BITalinoException;
import bitalino.BitalinoManager;
import bitalino.Frame;
import pojos.Patient;
import pojos.enums.Sex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PatientServerConnection {

    private static boolean isValidIPAddress(String ip) {
        if (ip == null) return false;
        if (ip.equalsIgnoreCase("localhost")) return true;
        String[] octets = ip.split("\\.");
        if (octets.length != 4) return false;
        for (String octet : octets) {
            try {
                int value = Integer.parseInt(octet);
                if (value < 0 || value > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateDNI(String dni) {
        if (dni == null) return false;
        return dni.matches("\\d{8}[A-Z]");
    }

    private static boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("\\d{7,15}");
    }

    private static boolean fitsInInt(String s) {
        if (s == null) return false;
        s = s.trim();
        if (!s.matches("\\d+")) return false;
        try {
            long v = Long.parseLong(s);
            return v <= Integer.MAX_VALUE && v >= Integer.MIN_VALUE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidPhoneForDb(String phone) {
        return phone != null && phone.matches("\\d{7,9}") && fitsInInt(phone);
    }

    private static boolean isValidInsuranceForDb(String insurance) {
        return insurance != null && insurance.matches("\\d{1,10}") && fitsInInt(insurance);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String serverAddress;
        int port = 0;
        String MACAddress;
        Socket socket = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        BITalino bitalino = null;
        BitalinoManager bitalinoManager = new BitalinoManager();

        // IP
        while (true) {
            System.out.println("Enter the IP address: ");
            serverAddress = scanner.nextLine().trim();
            if (isValidIPAddress(serverAddress)) break;
            System.out.println("Invalid IP address format. Please try again.");
        }

        // Port
        while (true) {
            System.out.println("Enter the port number (1024-65535): ");
            try {
                port = Integer.parseInt(scanner.nextLine().trim());
                if (port >= 1024 && port <= 65535) break;
                System.out.println("Port number must be between 1024 and 65535. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Please enter a numeric value.");
            }
        }

        // MAC
        while (true) {
            System.out.print("Enter MAC address (XX:XX:XX:XX:XX:XX): ");
            MACAddress = scanner.nextLine().trim();
            if (BitalinoManager.isValidMacAddress(MACAddress)) break;
            System.out.println("Invalid MAC address. Please enter a valid MAC address.");
        }

        try {
            socket = new Socket(serverAddress, port);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            System.out.println("Connected to " + serverAddress + " at port " + port);

            outputStream.writeUTF("Patient");
            outputStream.flush();

            boolean loggedIn = false;
            String username = null;

            while (!loggedIn) {
                System.out.println("Choose: 1) Sign up   2) Log in   (type 1 or 2)");
                String choice = scanner.nextLine().trim();
                if ("1".equals(choice)) {
                    performSignUp(scanner, outputStream, inputStream, socket);
                } else if ("2".equals(choice)) {
                    username = performLogin(scanner, outputStream, inputStream);
                    if (username != null) {
                        loggedIn = true;
                        System.out.println("Logged in as " + username);
                    }
                } else {
                    System.out.println("Invalid option. Type 1 or 2.");
                }
            }

            // Configure BITalino and recording loop
            bitalino = new BITalino();
            int samplingRate = 1000;

            boolean done = false;
            while (!done) {
                System.out.println("\nReady to record. Press ENTER to start recording or type Q + ENTER to quit.");
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("Q")) {
                    System.out.println("Quitting application.");
                    done = true;
                    break;
                }

                // Tell server we are about to start (optional)
                outputStream.writeUTF("START");
                outputStream.flush();

                try {
                    bitalino.open(MACAddress, samplingRate);
                    int[] channelsToAcquire = new int[]{1, 2}; // example channels
                    bitalino.start(channelsToAcquire);
                    System.out.println("Recording started. Press ENTER to stop recording.");
                } catch (Throwable ex) {
                    System.err.println("Error starting BITalino: " + ex.getMessage());
                    try {
                        outputStream.writeUTF("ERROR");
                        outputStream.writeUTF("BITalino open/start failed: " + ex.getMessage());
                        outputStream.flush();
                    } catch (IOException ignored) {}
                    try { bitalino.close(); } catch (Throwable ignored) {}
                    continue;
                }

                Thread stopper = new Thread(() -> {
                    try {
                        System.in.read();
                    } catch (IOException ignored) {
                    }
                });
                stopper.start();

                int blockSize = 10;
                long blockNumber = 0;
                try {
                    while (stopper.isAlive()) {
                        Frame[] frames = bitalino.read(blockSize);
                        // Send frames to server in blocks using reflection-safe helpers
                        outputStream.writeUTF("DATA_BLOCK");
                        outputStream.writeInt(frames.length);
                        for (Frame f : frames) {
                            long ts = extractTimestamp(f);
                            int[] analog = extractAnalog(f);
                            outputStream.writeLong(ts);
                            outputStream.writeInt(analog.length);
                            for (int v : analog) outputStream.writeInt(v);
                        }
                        outputStream.flush();
                        blockNumber++;
                    }
                } catch (Throwable ex) {
                    System.err.println("Error while recording/streaming frames: " + ex.getMessage());
                    try {
                        outputStream.writeUTF("ERROR");
                        outputStream.writeUTF("Recording failed: " + ex.getMessage());
                        outputStream.flush();
                    } catch (IOException ignored) {}
                } finally {
                    try {
                        bitalino.stop();
                    } catch (Throwable ignored) {}
                    try {
                        bitalino.close();
                    } catch (Throwable ignored) {}
                }

                // Send END marker
                try {
                    outputStream.writeUTF("END");
                    outputStream.flush();
                } catch (IOException e) {
                    System.err.println("Failed to send END: " + e.getMessage());
                }

                // Server ACK (best-effort)
                try {
                    socket.setSoTimeout(2000);
                    String ack = inputStream.readUTF();
                    if ("ACK".equals(ack)) {
                        String msg = inputStream.readUTF();
                        System.out.println("Server: " + msg);
                    }
                } catch (SocketTimeoutException ste) {
                    // no ack, continue
                } catch (IOException ignored) {
                } finally {
                    try { socket.setSoTimeout(0); } catch (SocketException ignored) {}
                }

                // After recording stopped: select symptoms
                sendSymptomsInteractive(scanner, outputStream, inputStream);

                System.out.println("Do you want to record again? (yes/no)");
                String again = scanner.nextLine().trim().toLowerCase();
                if (!again.equals("yes") && !again.equals("y")) {
                    done = true;
                }
            }

        } catch (Throwable e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error in the client", e);
        } finally {
            releaseResources(bitalino, socket, outputStream, scanner, inputStream);
        }
    }

    // Helper: acepta yyyy-MM-dd o dd/MM/yyyy; devuelve yyyy-MM-dd (formato que espera el servidor)
    private static String formatToJdbcDate(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String s = input.trim();
        try {
            LocalDate ld = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
            return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {}
        try {
            DateTimeFormatter alt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate ld = LocalDate.parse(s, alt);
            return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {}
        return null;
    }

    // --- Signup implementation (client-side validation + sending) ---
    private static void performSignUp(Scanner scanner, DataOutputStream out, DataInputStream in, Socket socket) {
        try {
            System.out.println("---- SIGN UP ----");

            String username;
            while (true) {
                System.out.print("Username: ");
                username = scanner.nextLine().trim();
                if (!username.isEmpty()) break;
                System.out.println("Username cannot be empty.");
            }

            String password;
            while (true) {
                System.out.print("Password: ");
                password = scanner.nextLine();
                if (password.length() >= 6) break;
                System.out.println("Password must be at least 6 characters.");
            }

            String name;
            while (true) {
                System.out.print("Name: ");
                name = scanner.nextLine().trim();
                if (!name.isEmpty() && name.matches("[a-zA-Z ]+")) break;
                System.out.println("Invalid name. Only letters and spaces.");
            }

            String surname;
            while (true) {
                System.out.print("Surname: ");
                surname = scanner.nextLine().trim();
                if (!surname.isEmpty() && surname.matches("[a-zA-Z ]+")) break;
                System.out.println("Invalid surname. Only letters and spaces.");
            }

            String birthdayInput;
            String birthdayFormatted;
            while (true) {
                System.out.print("Birthday (accepts yyyy-MM-dd or dd/MM/yyyy): ");
                birthdayInput = scanner.nextLine().trim();
                birthdayFormatted = formatToJdbcDate(birthdayInput);
                if (birthdayFormatted != null) break;
                System.out.println("Invalid date. Use yyyy-MM-dd or dd/MM/yyyy.");
            }

            Sex sexVal;
            while (true) {
                System.out.println("Please, type your sex (MALE/FEMALE):");
                String sex = scanner.nextLine().trim();
                if (sex.equalsIgnoreCase("F") || sex.equalsIgnoreCase("Female")) {
                    sexVal = Sex.FEMALE;
                    break;
                } else if (sex.equalsIgnoreCase("M") || sex.equalsIgnoreCase("Male")) {
                    sexVal = Sex.MALE;
                    break;
                } else {
                    System.err.println("Invalid Sex, please select as shown");
                }
            }

            String email;
            while (true) {
                System.out.print("Email: ");
                email = scanner.nextLine().trim();
                if (isValidEmail(email)) break;
                System.out.println("Invalid email. Try again.");
            }

            String phone;
            while (true) {
                System.out.print("Phone (digits only, 7-9 digits): ");
                phone = scanner.nextLine().trim();
                if (isValidPhoneForDb(phone)) break;
                System.out.println("Invalid phone. Enter 7-9 digits and ensure it fits server integer range.");
            }

            String dni;
            while (true) {
                System.out.print("DNI (8 digits + uppercase letter, e.g. 12345678A): ");
                dni = scanner.nextLine().trim().replaceAll("[\\s-]", "").toUpperCase();
                if (validateDNI(dni)) break;
                System.out.println("Invalid DNI. Try again.");
            }

            String insurance;
            while (true) {
                System.out.print("Insurance number (digits only, up to 10 digits): ");
                insurance = scanner.nextLine().trim();
                if (isValidInsuranceForDb(insurance)) break;
                System.out.println("Invalid insurance. Enter digits up to 10 characters and ensure it fits server integer range.");
            }

            String emergencyContact;
            while (true) {
                System.out.print("Emergency contact phone (digits only, 7-9 digits): ");
                emergencyContact = scanner.nextLine().trim();
                if (isValidPhoneForDb(emergencyContact)) break;
                System.out.println("Invalid phone. Enter 7-9 digits and ensure it fits server integer range.");
            }

            // DEBUG
            System.out.println("DEBUG: Enviando SIGNUP con campos (order):");
            System.out.println(" username=" + username);
            System.out.println(" password=(hidden)");
            System.out.println(" name=" + name);
            System.out.println(" surname=" + surname);
            System.out.println(" birthday=" + birthdayFormatted + " (yyyy-MM-dd)");
            System.out.println(" sex=" + sexVal);
            System.out.println(" email=" + email);
            System.out.println(" phone=" + phone);
            System.out.println(" dni=" + dni);
            System.out.println(" insurance=" + insurance);
            System.out.println(" emergency=" + emergencyContact);

            // Send SIGNUP
            out.writeUTF("SIGNUP");
            out.writeUTF(username);
            out.writeUTF(password);
            out.writeUTF(name);
            out.writeUTF(surname);
            out.writeUTF(birthdayFormatted);
            out.writeUTF(String.valueOf(sexVal));
            out.writeUTF(email);
            out.writeUTF(phone);
            out.writeUTF(dni);
            out.writeUTF(insurance);
            out.writeUTF(emergencyContact);
            out.flush();

            int previousTimeout = 0;
            try {
                previousTimeout = socket.getSoTimeout();
                socket.setSoTimeout(5000);
            } catch (SocketException ignored) {}

            String response;
            try {
                response = in.readUTF();
            } catch (SocketTimeoutException ste) {
                System.err.println("No response from server within timeout (5s). Revisa el servidor.");
                return;
            } catch (java.io.EOFException eof) {
                System.err.println("Server closed connection unexpectedly (EOF). Revisa logs del servidor.");
                return;
            } finally {
                try { socket.setSoTimeout(previousTimeout); } catch (SocketException ignored) {}
            }

            if ("ACK".equals(response)) {
                String msg = in.readUTF();
                System.out.println("Server: " + msg);
            } else if ("ERROR".equals(response)) {
                String msg = in.readUTF();
                System.err.println("Server error: " + msg);
            } else {
                System.out.println("Server (unexpected): " + response);
            }

        } catch (Exception e) {
            System.err.println("Error during sign-up: " + e);
            e.printStackTrace();
        }
    }

    // --- Login implementation ---
    private static String performLogin(Scanner scanner, DataOutputStream out, DataInputStream in) {
        try {
            System.out.println("---- LOG IN ----");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            out.writeUTF("LOGIN");
            out.writeUTF(username);
            out.writeUTF(password);
            out.flush();

            String response = in.readUTF();
            if ("LOGIN_RESULT".equals(response)) {
                boolean ok = in.readBoolean();
                String msg = in.readUTF();
                System.out.println("Server: " + msg);
                return ok ? username : null;
            } else {
                System.err.println("Unexpected server response: " + response);
                return null;
            }
        } catch (IOException e) {
            System.err.println("I/O error during login: " + e.getMessage());
            return null;
        }
    }

    private static void sendSymptomsInteractive(Scanner scanner, DataOutputStream out, DataInputStream in) {
        try {
            System.out.println("\nSelect symptoms from the list (IDs). Example input: 1,3,5");
            System.out.println("1 - Pain\n2 - Difficulty holding objects\n3 - Trouble breathing\n4 - Trouble swallowing\n5 - Trouble sleeping\n6 - Fatigue");
            System.out.print("Enter symptom IDs separated by commas (or leave blank for none): ");
            String line = scanner.nextLine().trim();
            String[] tokens = line.isEmpty() ? new String[0] : line.split(",");

            out.writeUTF("SYMPTOMS");
            out.writeInt(tokens.length);
            for (String t : tokens) {
                try {
                    int id = Integer.parseInt(t.trim());
                    out.writeInt(id);
                } catch (NumberFormatException nfe) {
                    out.writeInt(-1);
                }
            }
            out.writeUTF(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            out.flush();

            String response = in.readUTF();
            if ("ACK".equals(response)) {
                String msg = in.readUTF();
                System.out.println("Server: " + msg);
            } else {
                System.err.println("Server response: " + response);
            }
        } catch (IOException e) {
            System.err.println("I/O error sending symptoms: " + e.getMessage());
        }
    }

    private static void releaseResources(BITalino bitalino, Socket socket, DataOutputStream outputStream, Scanner scanner, DataInputStream inputStream) {
        if (scanner != null) {
            scanner.close();
        }
        try {
            if (bitalino != null) {
                bitalino.close();
            }
        } catch (BITalinoException e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error closing Bitalino", e);
        }

        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error closing OutputStream", e);
        }
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error closing InputStream", e);
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error closing socket", e);
        }
    }

    // Reflection-safe extractors for Frame timestamp and analog values
    private static long extractTimestamp(Frame f) {
        if (f == null) return System.currentTimeMillis();
        String[] names = {"getTimestamp", "getTime", "getDate", "getSeq", "getSequence", "timestamp"};
        for (String name : names) {
            try {
                Method m = f.getClass().getMethod(name);
                Object o = m.invoke(f);
                if (o instanceof Number) return ((Number) o).longValue();
                if (o instanceof String) {
                    try { return Long.parseLong((String) o); } catch (NumberFormatException ignored) {}
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        // fallback: current time
        return System.currentTimeMillis();
    }

    private static int[] extractAnalog(Frame f) {
        if (f == null) return new int[0];
        // First try methods returning arrays or lists
        String[] noArgNames = {"getAnalog", "getAnalogValues", "getValues", "getChannels", "getAnalogChannels", "getAnalogic"};
        for (String name : noArgNames) {
            try {
                Method m = f.getClass().getMethod(name);
                Object o = m.invoke(f);
                if (o instanceof int[]) return (int[]) o;
                if (o instanceof Integer[]) {
                    Integer[] arr = (Integer[]) o;
                    int[] res = new int[arr.length];
                    for (int i = 0; i < arr.length; i++) res[i] = arr[i] == null ? 0 : arr[i];
                    return res;
                }
                if (o instanceof List) {
                    List<?> list = (List<?>) o;
                    int[] res = new int[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        Object item = list.get(i);
                        if (item instanceof Number) res[i] = ((Number) item).intValue();
                        else {
                            try { res[i] = Integer.parseInt(String.valueOf(item)); } catch (Exception ex) { res[i] = 0; }
                        }
                    }
                    return res;
                }
                if (o instanceof Number) return new int[]{((Number) o).intValue()};
                if (o instanceof String) {
                    String s = (String) o;
                    String[] toks = s.replaceAll("[\\[\\]]", "").split("[,;]");
                    List<Integer> vals = new ArrayList<>();
                    for (String t : toks) {
                        try { vals.add(Integer.parseInt(t.trim())); } catch (Exception ex) {}
                    }
                    int[] res = new int[vals.size()];
                    for (int i = 0; i < vals.size(); i++) res[i] = vals.get(i);
                    return res;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        // Then try getAnalog(int channel) style
        try {
            Method m = null;
            for (Method mm : f.getClass().getMethods()) {
                if (mm.getName().equalsIgnoreCase("getAnalog") && mm.getParameterCount() == 1 && mm.getParameterTypes()[0] == int.class) {
                    m = mm;
                    break;
                }
            }
            if (m != null) {
                List<Integer> vals = new ArrayList<>();
                for (int ch = 0; ch < 16; ch++) { // probe up to 16 channels
                    try {
                        Object o = m.invoke(f, ch);
                        if (o instanceof Number) vals.add(((Number) o).intValue());
                        else {
                            try { vals.add(Integer.parseInt(String.valueOf(o))); } catch (Exception ex) { break; }
                        }
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        break;
                    }
                }
                int[] res = new int[vals.size()];
                for (int i = 0; i < vals.size(); i++) res[i] = vals.get(i);
                return res;
            }
        } catch (Exception ignored) {}
        // last fallback: empty array
        return new int[0];
    }
}

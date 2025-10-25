package executable;

import bitalino.BITalino;
import bitalino.BITalinoException;
import bitalino.BitalinoManager;
import bitalino.Frame;
import pojos.Patient;
import common.enums.Sex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PatientServerConnection {

    private static boolean isValidIPAddress(String ip) {
        if(ip.equalsIgnoreCase("localhost")){
            return true;
        } else {
            // Divide the ip by the .
            String[] octets = ip.split("\\.");
            // There need to be 4 octets
            if (octets.length != 4) {
                return false;
            }
            // check the octect
            for (String octet : octets) {
                try {
                    int value = Integer.parseInt(octet);
                    // Check if it is between the correct numbers
                    if (value < 0 || value > 255) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // if can´t be parsed is oncorrect
                    return false;
                }
            }
            // if everything seems fine the ip is correct
            return true;
        }
    }

    private static boolean validateDNI(String dni){
        return dni.matches("\\d{8}[A-Z]");
    }


    private static boolean isValidEmail(String email) {
        if (email == null) return false;
        // Simple RFC-like regex, enough for validation step
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        // Spanish-style 9 digits or generic 7-15 digits;
        return phone.matches("\\d{7,15}");
    }

    public static void main(String[] args) {
        // IP address of the server and port
        String DNI = "";
        String respond;
        Scanner scanner = new Scanner(System.in);
        String serverAddress = null;
        int port = 0;
        String MACAddress = null;
        Socket socket = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        BITalino bitalino = null;
        BitalinoManager bitalinoManager = new BitalinoManager();

        while (true) {
            System.out.println("Enter the IP address: ");
            serverAddress = scanner.nextLine();
            if (isValidIPAddress(serverAddress)) {
                break;
            } else {
                System.out.println("Invalid IP address format. Please try again.");
            }
        }
        while (true) {
            System.out.println("Enter the port number (1024-65535): ");
            try {
                port = Integer.parseInt(scanner.nextLine());
                if (port >= 1024 && port <= 65535) {
                    break;
                } else {
                    System.out.println("Port number must be between 1024 and 65535. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Please enter a numeric value.");
            }
        }

        //MAC address
        while (true) {
            System.out.print("Enter MAC address (XX:XX:XX:XX:XX:XX): ");
            MACAddress = scanner.nextLine();
            if (BitalinoManager.isValidMacAddress(MACAddress)) {
                break;
            } else {
                System.out.println("Invalid MAC address. Please enter a valid MAC address.");
            }
        }

        try {
            // Connect to server
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
                    performSignUp(scanner, outputStream, inputStream);
                } else if ("2".equals(choice)) {
                    username = performLogin(scanner, outputStream, inputStream);
                    if (username != null) {
                        loggedIn = true;
                        System.out.println("Logged in as: " + username);
                    } else {
                        System.out.println("Login failed. Try again or sign up.");
                    }
                } else {
                    System.out.println("Invalid option. Type 1 or 2.");
                }
            }

            // Configure MAC address of the bitalino
            bitalino = new BITalino();
            int samplingRate = 1000;

            // After login: only two options: Start recording (press ENTER) or Quit (type Q)
            boolean done = false;
            while (!done) {
                System.out.println("\nReady to record. Press ENTER to start recording or type Q + ENTER to quit.");
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("Q")) {
                    System.out.println("Quitting application.");
                    done = true;
                    break;
                }
                // User pressed ENTER (or something else not Q) -> start recording
                // Tell server we are about to start (optional)
                outputStream.writeUTF("START");
                outputStream.flush();

                // open bitalino and start
                try {
                    bitalino.open(MACAddress, samplingRate);
                    int[] channelsToAcquire = new int[]{1, 2}; // EMG, ECG
                    bitalino.start(channelsToAcquire);
                    System.out.println("Recording started. Press ENTER to stop recording.");
                } catch (Throwable ex) {
                    System.err.println("Error starting BITalino: " + ex.getMessage());
                    outputStream.writeUTF("ERROR");
                    outputStream.writeUTF("BITalino open/start failed: " + ex.getMessage());
                    outputStream.flush();
                    // attempt to close and continue
                    try { bitalino.close(); } catch (Throwable ignored) {}
                    continue;
                }

                // Recording loop: read until user presses ENTER again
                Thread stopper = new Thread(() -> {
                    try {
                        System.in.read(); // waits for any Enter press
                    } catch (IOException ignored) {
                    }
                });
                stopper.start();

                int blockSize = 10;
                long blockNumber = 0;
                try {
                    while (stopper.isAlive()) {
                        Frame[] frames = bitalino.read(blockSize);
                        for (Frame f : frames) {
                            // Send a DATA command followed by ints
                            outputStream.writeUTF("DATA");
                            outputStream.writeInt((int) blockNumber);
                            outputStream.writeInt(f.seq);
                            outputStream.writeInt(f.analog[1]); // ECG
                            outputStream.writeInt(f.analog[2]); // EDA
                            outputStream.flush();
                            blockNumber++;
                        }
                    }
                } catch (Throwable ex) {
                    System.err.println("Error while recording/streaming frames: " + ex.getMessage());
                    // send an ERROR marker to server
                    try {
                        outputStream.writeUTF("ERROR");
                        outputStream.writeUTF("Exception during recording: " + ex.getMessage());
                        outputStream.flush();
                    } catch (IOException ignored) {}
                } finally {
                    // Stop bitalino
                    try {
                        bitalino.stop();
                        bitalino.close();
                    } catch (Throwable ignored) {}
                }

                // Send END marker
                outputStream.writeUTF("END");
                outputStream.flush();

                // Server should ACK and/or save data.
                // Wait for ACK (if server sends one)
                try {
                    String serverResponse = inputStream.readUTF(); // could be "ACK" or message
                    System.out.println("Server: " + serverResponse);
                } catch (IOException e) {
                    System.out.println("No ACK received (server may have disconnected).");
                }

                // After recording stopped: select symptoms (only now)
                sendSymptomsInteractive(scanner, outputStream, inputStream);

                // After symptoms, ask user if they want to record again or quit
                System.out.println("Do you want to record again? (yes/no)");
                String again = scanner.nextLine().trim().toLowerCase();
                if (!again.equals("yes") && !again.equals("y")) {
                    done = true;
                }
            } // end while done


        }catch (Throwable e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error in the client", e);
        } finally {
            releaseResources(bitalino, socket, outputStream, scanner,inputStream);
        }

    }

    // --- Signup implementation (client-side validation + sending) ---
    private static void performSignUp(Scanner scanner, DataOutputStream out, DataInputStream in) {
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

            String birthday;
            while (true) {
                System.out.print("Birthday (yyyy-MM-dd): ");
                birthday = scanner.nextLine().trim();
                // basic format check
                if (birthday.matches("\\d{4}-\\d{2}-\\d{2}")) break;
                System.out.println("Invalid format. Use yyyy-MM-dd.");
            }

            String sex;
            Sex sexVal;
            while (true) {
                System.out.println("Please, type your sex (MALE/FEMALE):");
                sex =  scanner.nextLine().trim();
                if (sex.equalsIgnoreCase("F") || sex.equalsIgnoreCase("Female")) {
                    sexVal = Sex.FEMALE;
                    break;
                }
                else if (sex.equalsIgnoreCase("M") || sex.equalsIgnoreCase("Male")){
                    sexVal = Sex.MALE;
                    break;
                }
                else {
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
                System.out.print("Phone (digits only): ");
                phone = scanner.nextLine().trim();
                if (isValidPhone(phone)) break;
                System.out.println("Invalid phone. Enter 7-15 digits.");
            }

            String dni;
            while (true) {
                System.out.print("DNI (8 digits + uppercase letter, e.g. 12345678A): ");
                dni = scanner.nextLine().trim();
                if (validateDNI(dni)) break;
                System.out.println("Invalid DNI. Try again.");
            }

            String insurance;
            while (true) {
                System.out.print("Insurance number: ");
                insurance = scanner.nextLine().trim();
                if (!insurance.isEmpty()) break;
                System.out.println("Insurance number cannot be empty.");
            }
            String emergencyContact;
            while (true) {
                System.out.print("Emergency contact phone (digits only): ");
                emergencyContact = scanner.nextLine().trim();
                if (isValidPhone(emergencyContact)) break;
                System.out.println("Invalid phone. Enter 7-15 digits.");
            }

            // Send SIGNUP command and payload
            out.writeUTF("SIGNUP");
            out.writeUTF(username);
            out.writeUTF(password);
            out.writeUTF(name);
            out.writeUTF(surname);
            out.writeUTF(birthday);
            out.writeUTF(String.valueOf(sexVal));
            out.writeUTF(email);
            out.writeUTF(phone);
            out.writeUTF(dni);
            out.writeUTF(insurance);
            out.writeUTF(emergencyContact);
            out.flush();

            // Server response
            String response = in.readUTF();
            if ("ACK".equals(response)) {
                String msg = in.readUTF();
                System.out.println("Server: " + msg);
            } else if ("ERROR".equals(response)) {
                String msg = in.readUTF();
                System.err.println("Server error: " + msg);
            } else {
                System.out.println("Server (unexpected): " + response);
            }

        } catch (IOException e) {
            System.err.println("Error during sign-up: " + e.getMessage());
        }
    }

    // --- Login implementation ---
    // returns username if success, null otherwise
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

            String response = in.readUTF(); // expected "LOGIN_RESULT"
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
            // For a better UI you'd fetch symptom list from server. Here we show an example.
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
                    out.writeInt(-1); // placeholder invalid id
                }
            }
            // send timestamp
            out.writeUTF(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            out.flush();

            // wait for ack
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

    private static void releaseResources(BITalino bitalino, Socket socket, DataOutputStream outputStream, Scanner scanner,DataInputStream inputStream) {
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
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error closing OutputStream", e);
        }
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error closing InputStream", e);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, "Error closing socket", e);
        }
    }

}

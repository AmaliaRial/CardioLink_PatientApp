package bitalino;

import java.util.Scanner;

public class TestBitalinoManager {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BitalinoManager manager = new BitalinoManager();

        System.out.println("===== BITalino Recording Test =====");

        // Ask for patient name
        System.out.print("Enter patient name: ");
        String patientName = scanner.nextLine().trim();

        // Ask for MAC address
        System.out.print("Enter BITalino MAC address (e.g., 20:16:07:18:17:86): ");
        String macAddress = scanner.nextLine().trim();

        try {
            // Connect to the BITalino device
            manager.connect(macAddress);

            // Start recording
            manager.startRecording(patientName); // Uncomment if you have a Patient object
            System.out.println("Recording started. Press ENTER to stop...");

            // Wait for user to hit Enter
            scanner.nextLine();

            // Stop recording and disconnect
            manager.stopRecording();
            manager.disconnect();

            System.out.println("✅ Recording stopped and saved successfully.");

        } catch (BITalinoException e) {
            System.err.println("❌ BITalino error: " + e.getMessage() + " (code " + e.code + ")");
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}

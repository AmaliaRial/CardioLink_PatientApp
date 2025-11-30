package bitalino;



import executable.PatientSwing;
import pojos.Patient;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import executable.PatientServerConnection;

public class BitalinoManager {

    public static Frame[] frame;

    private BITalino bitalino;
    private volatile boolean isRecording = false;
    private Thread recordingThread;

    private static final int SAMPLING_RATE = 1000; // Sampling rate in Hz
    private static final int[] CHANNELS = {1, 2}; // Channels to acquire (A2 ECG and A3 EDA)

    public BitalinoManager() {
        bitalino = new BITalino();
    }

    // Method to validate a MAC address (e.g. "20:16:07:18:17:86")
    /**
     * Validates the format of a MAC address.
     * @param macAddress The MAC address string to validate.
     * @return true if the MAC address is valid, false otherwise.
     */
    public static boolean isValidMacAddress(String macAddress) {
        // Regular expression for MAC address format
        String macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        return macAddress != null && macAddress.matches(macPattern);
    }

    /**
     * Opens the Bluetooth connection to the BITalino device.
     * @param macAddress The MAC address of the BITalino device.
     * @throws BITalinoException if the MAC address is invalid or connection fails.
     */
    public void connect(String macAddress) throws BITalinoException {
        if (!isValidMacAddress(macAddress)) {
            throw new BITalinoException(BITalinoErrorTypes.MACADDRESS_NOT_VALID);
        }

        try {
            System.out.println("Connecting to BITalino device " + macAddress + "...");
            bitalino.open(macAddress, SAMPLING_RATE);
            System.out.println("Connected successfully.");
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
        }
    }

    /**
     * Starts recording ECG and EDA data for the specified patient.
     *
     * @throws BITalinoException if the device is not idle or other errors occur.
     */
    public synchronized void startRecordingToServer(DataOutputStream out) throws BITalinoException {
        if (isRecording) return;
        if (out == null) throw new IllegalArgumentException("Output stream null");
        isRecording = true;


        recordingThread = new Thread(() -> {
            StringBuilder ecgBuilder = new StringBuilder();
            StringBuilder edaBuilder = new StringBuilder();
            int sampleCount = 0;
            final int samplesPerPacket = SAMPLING_RATE*10; // ajustar según necesidad
            final int blockSize = 100; // cuantos frames pedir a bitalino.read()


            try {
                // Asegurarse de que el dispositivo esté en modo adquisición
                bitalino.start(CHANNELS);


                while (isRecording) {
                    Frame[] frames = bitalino.read(blockSize);
                    if (frames == null || frames.length == 0) continue;


                    for (Frame f : frames) {
                        int ecg = f.analog[0];
                        int eda = f.analog[1];


                        if (ecgBuilder.length() > 0) ecgBuilder.append(",");
                        ecgBuilder.append(ecg);


                        if (edaBuilder.length() > 0) edaBuilder.append(",");
                        edaBuilder.append(eda);


                        sampleCount++;


                        if (sampleCount >= samplesPerPacket) {
                            String dataString = "[" + ecgBuilder.toString() + ";" + edaBuilder.toString() + "]";

                            try {
                                synchronized (out) {
                                    System.out.println(dataString);
                                    //out.writeUTF("SEND_FRAGMENTS_OF_RECORDING");
                                    out.writeUTF(dataString);
                                    out.flush();
                                }
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                                // si falla la escritura, salir del bucle de grabación
                                isRecording = false;
                                break;
                            }
                            ecgBuilder.setLength(0);
                            edaBuilder.setLength(0);
                            sampleCount = 0;
                        }
                    }
                }
            } catch (BITalinoException be) {
                be.printStackTrace();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                // intentar parar el dispositivo y enviar restos pendientes
                try { bitalino.stop(); } catch (Throwable ignored) {}


                if (sampleCount > 0 && (ecgBuilder.length() > 0 || edaBuilder.length() > 0)) {
                    String dataString = "[" + ecgBuilder.toString() + ";" + edaBuilder.toString() + "]";
                    try {
                        synchronized (out) {
                            out.writeUTF(dataString);
                            out.flush();
                        }
                    } catch (IOException ignored) {}
                }


                isRecording = false;

                try {
                    out.writeUTF("STOP");
                    out.flush();
                    System.out.println("a llegado a intentar enviar stop");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }, "BITalino-Recording-Thread");


        recordingThread.setDaemon(true);
        recordingThread.start();
    }


/*
    public void startRecordingThread(DataOutputStream out) throws BITalinoException {
        if (isRecording) {
            throw new BITalinoException(BITalinoErrorTypes.DEVICE_NOT_IDLE);
        }

        isRecording = true;

        recordingThread = new Thread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            //String fileName = patient.getNamePatient() + "_ECG_EDA_" + sdf.format(new Date()) + ".txt";

            StringBuilder ecgBuilder = new StringBuilder();
            StringBuilder edaBuilder = new StringBuilder();
            int samplesPerPacket = SAMPLING_RATE * 10; // 10 segundos
            int sampleCount = 0;

            try {
                try {
                    bitalino.start(CHANNELS);
                } catch (Throwable t) {
                    throw new BITalinoException(BITalinoErrorTypes.DEVICE_NOT_IDLE);
                }

                System.out.println("Started recording on A2 (ECG) and A3 (EDA).");

                int blockSize = 1000;

                while (isRecording) {
                    Frame[] frames;
                    try {
                        frames = bitalino.read(blockSize);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        break;
                    }

                    for (Frame frame : frames) {
                        int ecg = frame.analog[0]; // ECG A2
                        int eda = frame.analog[1]; // EDA A3

                        if (ecgBuilder.length() > 0) ecgBuilder.append(",");
                        ecgBuilder.append(ecg);

                        if (edaBuilder.length() > 0) edaBuilder.append(",");
                        edaBuilder.append(eda);

                        sampleCount++;

                        if (sampleCount >= samplesPerPacket) {
                            String dataString = "[" + ecgBuilder.toString() + ";" + edaBuilder.toString() + "]";
                            try {
                                PatientSwing.sendFragmentsOfRecording(dataString, out);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // reset for next packet
                            ecgBuilder.setLength(0);
                            edaBuilder.setLength(0);
                            sampleCount = 0;
                        }
                    }
                }

            } catch (BITalinoException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    bitalino.stop();
                } catch (Throwable ignored) {}

                // Enviar resto parcial si existe
                if (sampleCount > 0 && (ecgBuilder.length() > 0 || edaBuilder.length() > 0)) {
                    String dataString = "[" + ecgBuilder.toString() + ";" + edaBuilder.toString() + "]";
                    try {
                        PatientServerConnection.sendFragmentsOfRecording(dataString, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                isRecording = false;
                //System.out.println("Recording stopped. Data saved to: " + fileName);
            }
        });

        recordingThread.start();
    }*/


    /**
     * Stops the ongoing recording safely.
     * @throws BITalinoException if the device is not in acquisition mode or other errors occur.
     */
    public synchronized void requestStopRecording() {
        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
            try {
                recordingThread.join(1500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        recordingThread = null;
    }


   public void stopRecording() throws BITalinoException {
        if (!isRecording) {
            throw new BITalinoException(BITalinoErrorTypes.DEVICE_NOT_IN_ACQUISITION_MODE);
        }

        System.out.println("Stopping recording...");
        isRecording = false;
        try {
            if (recordingThread != null) {
                recordingThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
        }
    }
    /**
     * Starts recording ECG (A2) and EDA (A3) signals.
     * Recording continues until stopRecording() is called.
     */
    public void startRecording(String patientName) throws BITalinoException {
        if (isRecording) {
            throw new BITalinoException(BITalinoErrorTypes.DEVICE_NOT_IDLE);
        }

        isRecording = true;
        recordingThread = new Thread(() -> {
            ArrayList<int[]> data = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String fileName = patientName + "_ECG_EDA_" + sdf.format(new Date()) + ".txt";

            try {
                try {
                    bitalino.start(CHANNELS);
                } catch (Throwable t) {
                    throw new BITalinoException(BITalinoErrorTypes.DEVICE_NOT_IDLE);
                }
                System.out.println("Started recording on A2 (ECG) and A3 (EDA).");

                int blockSize = 10;
                while (isRecording) {
                    Frame[] frames = bitalino.read(blockSize);

                    for (Frame frame : frames) {
                        int ecg = frame.analog[0]; // A2
                        int eda = frame.analog[1]; // A3
                        data.add(new int[]{ecg, eda});
                    }
                }

                bitalino.stop();
                saveDataToFile(fileName, data);
                System.out.println("Recording stopped and saved to " + fileName);

                // TODO: sendDataToServerDatabase(patientName, data);

            } catch (Exception e) {
                try {
                    throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
                } catch (BITalinoException ex) {
                    ex.printStackTrace();
                }
            }
        });

        recordingThread.start();
    }

    /**
     * Saves the recorded data into a .txt file.
     * @param fileName The name of the file to save data.
     * @param data The recorded data as a list of ECG and EDA pairs.
     * @throws IOException if file writing fails.
     */
    private void saveDataToFile(String fileName, ArrayList<int[]> data) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("ECG,EDA\n");
            for (int[] pair : data) {
                writer.write(pair[0] + "," + pair[1] + "\n");
            }
        }
    }

    /**
     * Disconnects from the BITalino device.
     * @throws BITalinoException if disconnection fails.
     */
    public void disconnect() throws BITalinoException {
        try {
            if (bitalino != null) {
                bitalino.close();
                System.out.println("Disconnected from BITalino.");
            }
        } catch (Exception e) {
            throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
        }
    }


    // Save the recorded data to a file
    /*private static void saveDataToFile(String fileName, ArrayList<Integer> data) throws IOException {
        try ( FileWriter writer = new FileWriter(fileName)) {
            for (Integer value : data) {
                writer.write(value + "\n");
            }
            System.out.println("Data saved with name: " + fileName);
            System.out.println("File saved in: " + new java.io.File(fileName).getAbsolutePath());
        }
    }*/


    /*
    public static void main(String[] args) {

        BITalino bitalino = null;
        try {
            bitalino = new BITalino();
            // Code to find Devices
            //Only works on some OS
            Vector<RemoteDevice> devices = bitalino.findDevices();
            System.out.println(devices);

            //You need TO CHANGE THE MAC ADDRESS
            //You should have the MAC ADDRESS in a sticker in the Bitalino
            //NEW Amalia's and Rodri's BITalino MAC Address "20:16:07:18:17:86"
            //Lorena's and Carmen's BITalino MAC Address "0C:43:14:24:78:F5"
            String macAddress = "20:16:07:18:17:86";
            
            //Sampling rate, should be 10, 100 or 1000
            int SamplingRate = 100;
            bitalino.open(macAddress, SamplingRate);

            // Start acquisition on analog channels A2 and A6
            // For example, If you want A1, A3 and A4 you should use {0,2,3}
            //Since we want ECG and EDA we use A2 and A3 therefore {1,2}
            int[] channelsToAcquire = {1, 2};
            bitalino.start(channelsToAcquire);

            //Read in total 10000000 times
            for (int j = 0; j < 10000000; j++) {

                //Each time read a block of 10 samples 
                int block_size=10;
                frame = bitalino.read(block_size);

                System.out.println("size block: " + frame.length);

                //Print the samples
                for (int i = 0; i < frame.length; i++) {
                    System.out.println((j * block_size + i) + " seq: " + frame[i].seq + " "
                            + frame[i].analog[0] + " "
                            + frame[i].analog[1] + " "
                    //  + frame[i].analog[2] + " "
                    //  + frame[i].analog[3] + " "
                    //  + frame[i].analog[4] + " "
                    //  + frame[i].analog[5]
                    );

                }
            }
            //stop acquisition
            bitalino.stop();
        } catch (BITalinoException ex) {
            Logger.getLogger(BitalinoManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Throwable ex) {
            Logger.getLogger(BitalinoManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                //close bluetooth connection
                if (bitalino != null) {
                    bitalino.close();
                }
            } catch (BITalinoException ex) {
                Logger.getLogger(BitalinoManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }*/

}

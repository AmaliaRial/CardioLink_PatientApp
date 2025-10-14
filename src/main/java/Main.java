import bitalino.BitalinoManager;
import jdbc.ConnectionManager;
import pojos.DiagnosisFile;
import pojos.Interfaces.ChartStatisticsRecordings;
import pojos.Patient;
import pojos.Interfaces.ChartAndStatisticsInterface;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ConnectionManager cm = new ConnectionManager();
        System.out.println("Base de datos inicializada correctamente.");
        cm.close();

                // 1️⃣ Crear un paciente
                Patient patient = new Patient();

                // 2️⃣ Crear el manager del BITalino
                BitalinoManager manager = new BitalinoManager();

                // 3️⃣ Conectar al BITalino (MAC ficticia, reemplaza por la tuya)
                String macAddress = "0C:43:14:24:78:F5";
                try {
                    manager.connect(macAddress);
                } catch (Exception e) {
                    System.out.println("Error al conectar con BITalino: " + e.getMessage());
                    return;
                }

                // 4️⃣ Iniciar la grabación enviando los datos al paciente
                try {
                    manager.startRecording(patient);

                    // ⏱ Aquí se podría esperar un tiempo o permitir que el usuario detenga la grabación
                    System.out.println("Grabando datos de ECG y EDA para el paciente...");
                    Thread.sleep(10000); // graba 10 segundos como ejemplo

                    // 5️⃣ Detener la grabación
                    manager.stopRecording();
                    System.out.println("Grabación detenida.");

                } catch (Exception e) {
                    System.out.println("Error durante la grabación: " + e.getMessage());
                }

                // 6️⃣ Mostrar datos finales del paciente
                System.out.println("\nDatos grabados en el paciente:");
                DiagnosisFile diag=patient.getDiagnosisList().get(0);
                String ECGrecording= diag.getSensorDataECG();
                String EDArecording= diag.getSensorDataEDA();
                System.out.println("ECG: " + ECGrecording);
                System.out.println("EDA: " + EDArecording);

                ChartAndStatisticsInterface chartStatistics = new ChartStatisticsRecordings();
                chartStatistics.showECGandEDAChartsFromStrings(ECGrecording, EDArecording, 1000);



            }
        }
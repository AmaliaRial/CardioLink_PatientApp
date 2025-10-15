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

                Patient patient = new Patient();
                BitalinoManager manager = new BitalinoManager();
                String macAddress = "0C:43:14:24:78:F5";
                try {
                    manager.connect(macAddress);
                } catch (Exception e) {
                    System.out.println("Error al conectar con BITalino: " + e.getMessage());
                    return;
                }

                try {
                    manager.startRecording(patient);


                    System.out.println("Grabando datos de ECG y EDA para el paciente...");
                    Thread.sleep(10000); // graba 10 segundos como ejemplo


                    manager.stopRecording();
                    System.out.println("Grabación detenida.");

                } catch (Exception e) {
                    System.out.println("Error durante la grabación: " + e.getMessage());
                }


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
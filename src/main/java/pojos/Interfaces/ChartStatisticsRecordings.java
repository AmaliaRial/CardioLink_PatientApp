package pojos.Interfaces;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ChartStatisticsRecordings implements ChartAndStatisticsInterface{
    @Override
    public void showECGandEDAChartsFromStrings(String ecgString, String edaString, int sampleRate) {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate debe ser mayor que 0");
        }

        int[] ecgData = parseSensorStringToIntArray(ecgString);
        int[] edaData = parseSensorStringToIntArray(edaString);

        // Máximo de muestras a mostrar (10 segundos)
        int maxSamplesECG = Math.min(ecgData.length, sampleRate * 10);
        int maxSamplesEDA = Math.min(edaData.length, sampleRate * 10);

        // Crear series y datasets
        XYSeries ecgSeries = new XYSeries("ECG");
        XYSeries edaSeries = new XYSeries("EDA");

        for (int i = 0; i < maxSamplesECG; i++) {
            double time = (double) i / sampleRate;
            ecgSeries.add(time, ecgData[i]);
        }

        for (int i = 0; i < maxSamplesEDA; i++) {
            double time = (double) i / sampleRate;
            edaSeries.add(time, edaData[i]);
        }

        XYSeriesCollection ecgDataset = new XYSeriesCollection();
        ecgDataset.addSeries(ecgSeries);

        XYSeriesCollection edaDataset = new XYSeriesCollection();
        edaDataset.addSeries(edaSeries);

        // Crear gráficos
        JFreeChart ecgChart = ChartFactory.createXYLineChart(
                "ECG Signal (max 10s)", "Time (s)", "Amplitude",
                ecgDataset, PlotOrientation.VERTICAL, false, true, false
        );

        JFreeChart edaChart = ChartFactory.createXYLineChart(
                "EDA Signal (max 10s)", "Time (s)", "Amplitude",
                edaDataset, PlotOrientation.VERTICAL, false, true, false
        );

        // Paneles y frame
        ChartPanel ecgPanel = new ChartPanel(ecgChart);
        ChartPanel edaPanel = new ChartPanel(edaChart);
        ecgPanel.setPreferredSize(new Dimension(900, 300));
        edaPanel.setPreferredSize(new Dimension(900, 300));

        JFrame frame = new JFrame("BITalino ECG/EDA Visualization");
        frame.setLayout(new GridLayout(2, 1));
        frame.add(ecgPanel);
        frame.add(edaPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    /**
     * Parsear un string en formato "v1;v2;v3;..." a un int[].
     * Ignora entradas vacías; si un token no es numérico lo salta.
     */
    private static int[] parseSensorStringToIntArray(String s) {
        if (s == null || s.trim().isEmpty()) {
            return new int[0];
        }

        String[] parts = s.split(";");
        List<Integer> list = new ArrayList<>(parts.length);

        for (String p : parts) {
            if (p == null) continue;
            String token = p.trim();
            if (token.isEmpty()) continue;
            try {
                // Reemplazar coma decimal si por error la usan (no habitual aquí)
                token = token.replace(",", "");
                int val = Integer.parseInt(token);
                list.add(val);
            } catch (NumberFormatException ex) {
                // Saltar tokens no numéricos (puedes registrar un warning si quieres)
            }
        }

        // Convertir List<Integer> a int[]
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

}

package bitalino;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SignalFilePlotter extends JFrame {

    private static final int BASE_WIDTH = 900; // Minimum window width
    private static final int BASE_HEIGHT = 300; // Height per chart

    /**
     * Constructor to create the plotting window.
     * @param filePath Path to the recorded data file.
     */
    public SignalFilePlotter(String filePath) {
        setTitle("BITalino Recorded Signals Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1));

        // Load data from file
        XYSeries ecgSeries = new XYSeries("ECG");
        XYSeries edaSeries = new XYSeries("EDA");

        try {
            loadData(filePath, ecgSeries, edaSeries);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ECG chart
        XYSeriesCollection ecgDataset = new XYSeriesCollection(ecgSeries);
        JFreeChart ecgChart = ChartFactory.createXYLineChart(
                "ECG Signal (A2)",
                "Sample",
                "ECG (ADC units)",
                ecgDataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        ((NumberAxis) ecgChart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);
        ChartPanel ecgPanel = new ChartPanel(ecgChart);
        ecgPanel.setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));


        // EDA chart
        XYSeriesCollection edaDataset = new XYSeriesCollection(edaSeries);
        JFreeChart edaChart = ChartFactory.createXYLineChart(
                "EDA Signal (A3)",
                "Sample",
                "EDA (ADC units)",
                edaDataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        ((NumberAxis) edaChart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);
        ChartPanel edaPanel = new ChartPanel(edaChart);
        edaPanel.setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));

        // Compute total samples to adjust scrollable area width
        int totalSamples = Math.max(ecgSeries.getItemCount(), edaSeries.getItemCount());
        int scrollWidth = Math.max(BASE_WIDTH, totalSamples); // Each sample = ~1 px

        ecgPanel.setPreferredSize(new Dimension(scrollWidth, BASE_HEIGHT));
        edaPanel.setPreferredSize(new Dimension(scrollWidth, BASE_HEIGHT));

        JScrollPane ecgScroll = new JScrollPane(
                ecgPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        JScrollPane edaScroll = new JScrollPane(
                edaPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        // --- Synchronized Scrolling ---
        JScrollBar ecgBar = ecgScroll.getHorizontalScrollBar();
        JScrollBar edaBar = edaScroll.getHorizontalScrollBar();

        // Link both scrollbars
        ecgBar.addAdjustmentListener(new SynchronizedScrollListener(edaBar));
        edaBar.addAdjustmentListener(new SynchronizedScrollListener(ecgBar));

        // Scroll speed
        ecgBar.setUnitIncrement(50);
        edaBar.setUnitIncrement(50);

        // Add charts to window
        add(ecgPanel);
        add(edaPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Loads ECG and EDA data from the specified file into the provided series.
     * @param filePath Path to the recorded data file.
     * @param ecgSeries Series to populate with ECG data.
     * @param edaSeries Series to populate with EDA data.
     * @throws IOException if file reading fails.
     */
    private void loadData(String filePath, XYSeries ecgSeries, XYSeries edaSeries) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int sampleIndex = 0;
            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        double ecg = Double.parseDouble(parts[0].trim());
                        double eda = Double.parseDouble(parts[1].trim());
                        ecgSeries.add(sampleIndex, ecg);
                        edaSeries.add(sampleIndex, eda);
                        sampleIndex++;
                    } catch (NumberFormatException ignored) { }
                }
            }
            System.out.println("Loaded " + sampleIndex + " samples from " + filePath);
        }
    }

    /**
     * Helper listener to synchronize scrollbars.
     * Prevents infinite adjustment loops.
     */
    private static class SynchronizedScrollListener implements AdjustmentListener {
        private final JScrollBar partnerBar;
        private boolean adjusting = false;

        public SynchronizedScrollListener(JScrollBar partnerBar) {
            this.partnerBar = partnerBar;
        }

        /** When one scrollbar is adjusted, update the partner scrollbar to match.
         * The 'adjusting' flag prevents recursive updates.
         */
        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (!adjusting && !partnerBar.getValueIsAdjusting()) {
                adjusting = true;
                partnerBar.setValue(e.getValue());
                adjusting = false;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Show a file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select BITalino Recorded File");
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));

            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                new SignalFilePlotter(selectedFile.getAbsolutePath());
            } else {
                System.out.println(" No file selected. Exiting...");
                System.exit(0);
            }
        });
    }
}

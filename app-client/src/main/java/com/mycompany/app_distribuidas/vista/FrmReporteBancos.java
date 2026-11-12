package com.mycompany.app_distribuidas.vista;

import com.google.gson.*;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class FrmReporteBancos extends JFrame {

    private JComboBox<String> tipo;
    private JTextField desde;
    private JTextField hasta;
    private JButton btnConsultar;
    private JButton btnImprimir;
    private JTable tabla;

    public FrmReporteBancos() {
        iniciarComponentes();
        setTitle("Reportes de Bancos");
        setSize(820, 550);
        setLocationRelativeTo(null);

        // Opciones para elegir entre el Reporte 1 y el Reporte 2 del PDF
        tipo.setModel(new DefaultComboBoxModel<>(new String[]{
            "Saldo en dólares por cuenta bancaria",
            "Transacciones por cuenta (Cruzado)"
        }));

        // Fechas por defecto para el rango de consulta
        desde.setText(LocalDate.now().withDayOfMonth(1).toString());
        hasta.setText(LocalDate.now().toString());

        // Evento para consultar los datos al servidor
        btnConsultar.addActionListener(e -> cargar());

        // Evento para imprimir la tabla del reporte
        btnImprimir.addActionListener(e -> {
            try {
                tabla.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }

    private void iniciarComponentes() {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelFiltros = new JPanel(new GridLayout(2, 3, 8, 6));

        tipo = new JComboBox<>();
        desde = new JTextField();
        hasta = new JTextField();

        panelFiltros.add(new JLabel("Reporte"));
        panelFiltros.add(new JLabel("Desde AAAA-MM-DD"));
        panelFiltros.add(new JLabel("Hasta AAAA-MM-DD"));
        panelFiltros.add(tipo);
        panelFiltros.add(desde);
        panelFiltros.add(hasta);

        JPanel panelBotones = new JPanel(new FlowLayout());
        btnConsultar = new JButton("Consultar");
        btnImprimir = new JButton("Imprimir");
        panelBotones.add(btnConsultar);
        panelBotones.add(btnImprimir);

        panelSuperior.add(panelFiltros, BorderLayout.NORTH);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        tabla = new JTable();
        JScrollPane scrollTabla = new JScrollPane(tabla);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelSuperior, BorderLayout.NORTH);
        getContentPane().add(scrollTabla, BorderLayout.CENTER);
        pack();
    }

    // Solicita el reporte seleccionado al servidor y llena la tabla
    private void cargar() {
        try {
            boolean cruzado = tipo.getSelectedIndex() == 1;
            String operacion = cruzado ? "cruzado" : "saldos";
            Map<String, Object> filtro = Map.of(
                "desde", desde.getText().trim(),
                "hasta", hasta.getText().trim()
            );

            JsonArray datos = ClienteWebSocket.get()
                    .solicitar("bancos.reporte." + operacion, filtro)
                    .getAsJsonObject()
                    .getAsJsonArray("items");

            DefaultTableModel modelo;

            if (!cruzado) {
                // Reporte 1: Saldo en dolares por cuenta bancaria
                modelo = new DefaultTableModel(new String[]{"Número de Cuenta", "Cliente", "Descripción", "Saldo ($)"}, 0);
                for (JsonElement el : datos) {
                    JsonObject x = el.getAsJsonObject();
                    modelo.addRow(new Object[]{
                        x.get("numeroCuenta").getAsInt(),
                        x.get("cliente").getAsString(),
                        x.get("descripcion").isJsonNull() ? "" : x.get("descripcion").getAsString(),
                        String.format("%.2f", x.get("saldo").getAsDouble())
                    });
                }
            } else {
                // Reporte 2: En filas las cuentas, en columnas las transacciones, al centro la suma
                Set<String> cuentas = new TreeSet<>();
                Set<String> transacciones = new TreeSet<>();
                Map<String, Map<String, String>> matriz = new TreeMap<>();

                for (JsonElement el : datos) {
                    JsonObject x = el.getAsJsonObject();
                    String cta = x.get("cuenta").getAsString();
                    String trans = x.get("transaccion").getAsString();
                    String valor = String.format("%.2f", x.get("total").getAsDouble());

                    cuentas.add(cta);
                    transacciones.add(trans);
                    matriz.computeIfAbsent(cta, k -> new TreeMap<>()).put(trans, valor);
                }

                List<String> columnas = new ArrayList<>();
                columnas.add("Cuenta Bancaria");
                columnas.addAll(transacciones);

                modelo = new DefaultTableModel(columnas.toArray(), 0);
                for (String cta : cuentas) {
                    List<Object> fila = new ArrayList<>();
                    fila.add(cta);
                    for (String trans : transacciones) {
                        fila.add(matriz.getOrDefault(cta, Map.of()).getOrDefault(trans, "0.00"));
                    }
                    modelo.addRow(fila.toArray());
                }
            }

            tabla.setModel(modelo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


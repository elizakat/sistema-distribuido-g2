package com.mycompany.app_distribuidas.vista;

import com.google.gson.*;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class FrmTransaccion extends JFrame {

    // Tabla para mostrar las cabeceras registradas
    private final DefaultTableModel modeloCabeceras = new DefaultTableModel(
            new String[]{"Código", "Fecha", "ID Cuenta", "Descripción"}, 0);

    // Tabla para mostrar los detalles filtrados de la cabecera activa
    private final DefaultTableModel modeloDetalles = new DefaultTableModel(
            new String[]{"ID Tipo", "Transacción", "Fecha", "Valor"}, 0);

    // Mapas para relacionar el texto del ComboBox con su ID en la base de datos
    private final Map<String, Integer> mapaCuentas = new LinkedHashMap<>();
    private final Map<String, Integer> mapaTipos = new LinkedHashMap<>();
    private final Map<Integer, String> mapaNombresTipos = new HashMap<>();

    // Componentes de la Cabecera
    private JTextField txtIdTransaccion;
    private JTextField txtFechaCabecera;
    private JComboBox<String> cmbCuenta;
    private JTextField txtDescripcionCabecera;
    private JTable tablaCabeceras;

    // Componentes del Detalle
    private JTextField txtFechaDetalle;
    private JComboBox<String> cmbTipoTransaccion;
    private JTextField txtValorDetalle;
    private JButton btnAgregarDetalle;
    private JButton btnModificarDetalle;
    private JButton btnQuitarDetalle;
    private JTable tablaDetalles;

    // Botones principales de la pantalla
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnEliminar;
    private JButton btnBuscar;

    public FrmTransaccion() {
        setTitle("Transacciones Bancarias (Cabecera y Detalle)");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        iniciarComponentes();
        cargarListasDesplegables();
        cargarCabeceras();
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(8, 8));

        // Panel superior con los campos y la tabla de cabeceras
        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.setBorder(BorderFactory.createTitledBorder("Datos de la Cabecera"));

        JPanel panelCamposCab = new JPanel(new GridLayout(2, 4, 8, 5));
        txtIdTransaccion = new JTextField();
        txtIdTransaccion.setEditable(false);
        txtFechaCabecera = new JTextField(LocalDate.now().toString());
        cmbCuenta = new JComboBox<>();
        txtDescripcionCabecera = new JTextField();

        panelCamposCab.add(new JLabel("Código (automático):"));
        panelCamposCab.add(txtIdTransaccion);
        panelCamposCab.add(new JLabel("Fecha (AAAA-MM-DD):"));
        panelCamposCab.add(txtFechaCabecera);
        panelCamposCab.add(new JLabel("Cuenta Bancaria:"));
        panelCamposCab.add(cmbCuenta);
        panelCamposCab.add(new JLabel("Descripción:"));
        panelCamposCab.add(txtDescripcionCabecera);

        panelNorte.add(panelCamposCab, BorderLayout.NORTH);

        tablaCabeceras = new JTable(modeloCabeceras);
        JScrollPane scrollCab = new JScrollPane(tablaCabeceras);
        scrollCab.setPreferredSize(new Dimension(850, 130));
        panelNorte.add(scrollCab, BorderLayout.CENTER);

        add(panelNorte, BorderLayout.NORTH);

        // Panel central con los campos y la tabla de detalles
        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        panelCentro.setBorder(BorderFactory.createTitledBorder("Líneas de Detalle (Filtradas por la cabecera activa)"));

        JPanel panelIngresoDetalle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtFechaDetalle = new JTextField(LocalDate.now().toString(), 9);
        cmbTipoTransaccion = new JComboBox<>();
        txtValorDetalle = new JTextField(8);
        btnAgregarDetalle = new JButton("Agregar Línea");
        btnModificarDetalle = new JButton("Modificar Línea");
        btnQuitarDetalle = new JButton("Quitar Línea");

        panelIngresoDetalle.add(new JLabel("Fecha:"));
        panelIngresoDetalle.add(txtFechaDetalle);
        panelIngresoDetalle.add(new JLabel("Tipo:"));
        panelIngresoDetalle.add(cmbTipoTransaccion);
        panelIngresoDetalle.add(new JLabel("Valor ($):"));
        panelIngresoDetalle.add(txtValorDetalle);
        panelIngresoDetalle.add(btnAgregarDetalle);
        panelIngresoDetalle.add(btnModificarDetalle);
        panelIngresoDetalle.add(btnQuitarDetalle);

        panelCentro.add(panelIngresoDetalle, BorderLayout.NORTH);

        tablaDetalles = new JTable(modeloDetalles);
        panelCentro.add(new JScrollPane(tablaDetalles), BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);

        // Panel inferior con las opciones insertar, guardar, modificar, eliminar y buscar
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnBuscar = new JButton("Buscar");

        panelInferior.add(btnNuevo);
        panelInferior.add(btnGuardar);
        panelInferior.add(btnModificar);
        panelInferior.add(btnEliminar);
        panelInferior.add(btnBuscar);

        add(panelInferior, BorderLayout.SOUTH);

        // Eventos para manejar las lineas del detalle
        btnAgregarDetalle.addActionListener(e -> agregarLineaDetalle());
        btnModificarDetalle.addActionListener(e -> modificarLineaDetalle());
        btnQuitarDetalle.addActionListener(e -> quitarLineaDetalle());

        // Eventos para manejar la transaccion completa
        btnNuevo.addActionListener(e -> limpiarPantalla());
        btnGuardar.addActionListener(e -> enviarTransaccion("insertar"));
        btnModificar.addActionListener(e -> enviarTransaccion("actualizar"));
        btnEliminar.addActionListener(e -> eliminarTransaccion());
        btnBuscar.addActionListener(e -> buscarCabecera());

        // Al seleccionar una cabecera en la tabla superior, se filtran sus detalles abajo
        tablaCabeceras.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaCabeceras.getSelectedRow() >= 0) {
                seleccionarCabeceraActiva(tablaCabeceras.getSelectedRow());
            }
        });

        // Al seleccionar una linea del detalle, se pasan sus valores a los campos de edicion
        tablaDetalles.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaDetalles.getSelectedRow() >= 0) {
                int fila = tablaDetalles.getSelectedRow();
                cmbTipoTransaccion.setSelectedItem(modeloDetalles.getValueAt(fila, 1).toString());
                txtFechaDetalle.setText(modeloDetalles.getValueAt(fila, 2).toString());
                txtValorDetalle.setText(modeloDetalles.getValueAt(fila, 3).toString());
            }
        });
    }

    // Pide al servidor las cuentas y los tipos de transaccion para llenar los ComboBox
    private void cargarListasDesplegables() {
        try {
            JsonArray cuentas = ClienteWebSocket.get().solicitar("bancos.cuenta.listar", Map.of()).getAsJsonObject().getAsJsonArray("items");
            cmbCuenta.removeAllItems();
            mapaCuentas.clear();
            for (JsonElement e : cuentas) {
                JsonObject obj = e.getAsJsonObject();
                int id = obj.get("numeroCuenta").getAsInt();
                String texto = id + " - " + obj.get("cliente").getAsString() + " (" + obj.get("descripcion").getAsString() + ")";
                mapaCuentas.put(texto, id);
                cmbCuenta.addItem(texto);
            }

            JsonArray tipos = ClienteWebSocket.get().solicitar("bancos.tipotransaccion.listar", Map.of()).getAsJsonObject().getAsJsonArray("items");
            cmbTipoTransaccion.removeAllItems();
            mapaTipos.clear();
            mapaNombresTipos.clear();
            for (JsonElement e : tipos) {
                JsonObject obj = e.getAsJsonObject();
                int id = obj.get("idTipoTransaccion").getAsInt();
                String texto = obj.get("nombre").getAsString() + " (" + obj.get("signo").getAsString() + ")";
                mapaTipos.put(texto, id);
                mapaNombresTipos.put(id, texto);
                cmbTipoTransaccion.addItem(texto);
            }
        } catch (Exception ex) { }
    }

    // Carga todas las cabeceras en la tabla superior
    private void cargarCabeceras() {
        try {
            JsonArray arr = ClienteWebSocket.get().solicitar("bancos.transaccion.listar", Map.of()).getAsJsonObject().getAsJsonArray("items");
            modeloCabeceras.setRowCount(0);
            for (JsonElement e : arr) {
                JsonObject x = e.getAsJsonObject();
                modeloCabeceras.addRow(new Object[]{
                    x.get("idTransaccion").getAsInt(),
                    x.get("fecha").getAsString(),
                    x.get("numeroCuenta").getAsInt(),
                    x.get("descripcion").isJsonNull() ? "" : x.get("descripcion").getAsString()
                });
            }
        } catch (Exception ex) { }
    }

    // Carga en pantalla los datos de la cabecera seleccionada y filtra sus detalles
    private void seleccionarCabeceraActiva(int fila) {
        int idTrans = Integer.parseInt(modeloCabeceras.getValueAt(fila, 0).toString());
        txtIdTransaccion.setText(String.valueOf(idTrans));
        txtFechaCabecera.setText(modeloCabeceras.getValueAt(fila, 1).toString());
        int idCuenta = Integer.parseInt(modeloCabeceras.getValueAt(fila, 2).toString());
        txtDescripcionCabecera.setText(modeloCabeceras.getValueAt(fila, 3).toString());

        // Seleccionar la cuenta correspondiente en el ComboBox
        for (Map.Entry<String, Integer> entry : mapaCuentas.entrySet()) {
            if (entry.getValue() == idCuenta) {
                cmbCuenta.setSelectedItem(entry.getKey());
                break;
            }
        }

        // Consultar al servidor solo los detalles de esta cabecera activa
        try {
            JsonArray arr = ClienteWebSocket.get().solicitar("bancos.transaccion.detalles", Map.of("idTransaccion", idTrans))
                    .getAsJsonObject().getAsJsonArray("items");
            modeloDetalles.setRowCount(0);
            for (JsonElement e : arr) {
                JsonObject d = e.getAsJsonObject();
                int idTipo = d.get("idTipoTransaccion").getAsInt();
                String nombreTipo = mapaNombresTipos.getOrDefault(idTipo, String.valueOf(idTipo));
                modeloDetalles.addRow(new Object[]{
                    idTipo,
                    nombreTipo,
                    d.get("fecha").getAsString(),
                    d.get("valor").getAsDouble()
                });
            }
        } catch (Exception ex) { }
    }

    // Agrega una nueva linea a la tabla de detalles
    private void agregarLineaDetalle() {
        try {
            String tipoSelec = cmbTipoTransaccion.getSelectedItem().toString();
            int idTipo = mapaTipos.get(tipoSelec);
            String fecha = txtFechaDetalle.getText().trim();
            double valor = Double.parseDouble(txtValorDetalle.getText().trim());

            if (valor <= 0) {
                JOptionPane.showMessageDialog(this, "El valor debe ser mayor a cero.");
                return;
            }

            modeloDetalles.addRow(new Object[]{idTipo, tipoSelec, fecha, valor});
            txtValorDetalle.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ingrese un valor numérico válido.");
        }
    }

    // Modifica la linea seleccionada en la tabla de detalles
    private void modificarLineaDetalle() {
        int fila = tablaDetalles.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una línea del detalle para modificar.");
            return;
        }
        try {
            String tipoSelec = cmbTipoTransaccion.getSelectedItem().toString();
            int idTipo = mapaTipos.get(tipoSelec);
            double valor = Double.parseDouble(txtValorDetalle.getText().trim());
            if (valor <= 0) {
                JOptionPane.showMessageDialog(this, "El valor debe ser mayor a cero.");
                return;
            }
            modeloDetalles.setValueAt(idTipo, fila, 0);
            modeloDetalles.setValueAt(tipoSelec, fila, 1);
            modeloDetalles.setValueAt(txtFechaDetalle.getText().trim(), fila, 2);
            modeloDetalles.setValueAt(valor, fila, 3);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ingrese un valor numérico válido.");
        }
    }

    // Quita la linea seleccionada de la tabla de detalles
    private void quitarLineaDetalle() {
        int fila = tablaDetalles.getSelectedRow();
        if (fila >= 0) {
            modeloDetalles.removeRow(fila);
        }
    }

    // Limpia los campos para ingresar una nueva transaccion
    private void limpiarPantalla() {
        txtIdTransaccion.setText("");
        txtFechaCabecera.setText(LocalDate.now().toString());
        txtDescripcionCabecera.setText("");
        txtFechaDetalle.setText(LocalDate.now().toString());
        txtValorDetalle.setText("");
        modeloDetalles.setRowCount(0);
        tablaCabeceras.clearSelection();
        cargarListasDesplegables();
    }

    // Envia la cabecera y sus detalles al servidor para insertar o actualizar
    private void enviarTransaccion(String operacion) {
        if (modeloDetalles.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un detalle a la transacción.");
            return;
        }
        if (operacion.equals("actualizar") && txtIdTransaccion.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione primero una cabecera de la tabla para modificar.");
            return;
        }

        try {
            Map<String, Object> cabecera = new HashMap<>();
            if (!txtIdTransaccion.getText().isEmpty()) {
                cabecera.put("idTransaccion", Integer.parseInt(txtIdTransaccion.getText()));
            }
            String cuentaSelec = cmbCuenta.getSelectedItem().toString();
            cabecera.put("numeroCuenta", mapaCuentas.get(cuentaSelec));
            cabecera.put("descripcion", txtDescripcionCabecera.getText().trim());
            cabecera.put("fecha", txtFechaCabecera.getText().trim());

            List<Map<String, Object>> detalles = new ArrayList<>();
            for (int i = 0; i < modeloDetalles.getRowCount(); i++) {
                Map<String, Object> det = new HashMap<>();
                det.put("idTipoTransaccion", modeloDetalles.getValueAt(i, 0));
                det.put("fecha", modeloDetalles.getValueAt(i, 2));
                det.put("valor", modeloDetalles.getValueAt(i, 3));
                detalles.add(det);
            }

            Map<String, Object> payloadFinal = new HashMap<>();
            payloadFinal.put("cabecera", cabecera);
            payloadFinal.put("detalles", detalles);

            ClienteWebSocket.get().solicitar("bancos.transaccion." + operacion, payloadFinal);
            JOptionPane.showMessageDialog(this, "Transacción guardada correctamente.");
            cargarCabeceras();
            limpiarPantalla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Elimina la transaccion seleccionada
    private void eliminarTransaccion() {
        if (txtIdTransaccion.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una transacción de la tabla para eliminar.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar esta transacción y sus detalles?", "Confirmar", JOptionPane.YES_NO_OPTION) == 0) {
            try {
                int id = Integer.parseInt(txtIdTransaccion.getText());
                ClienteWebSocket.get().solicitar("bancos.transaccion.eliminar", Map.of("idTransaccion", id));
                cargarCabeceras();
                limpiarPantalla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Busca una cabecera por su codigo o descripcion y filtra sus detalles
    private void buscarCabecera() {
        String q = JOptionPane.showInputDialog(this, "Buscar transacción por código o descripción:");
        if (q != null) {
            for (int i = 0; i < modeloCabeceras.getRowCount(); i++) {
                String texto = modeloCabeceras.getValueAt(i, 0).toString() + " " + modeloCabeceras.getValueAt(i, 3).toString();
                if (texto.toLowerCase().contains(q.toLowerCase())) {
                    tablaCabeceras.setRowSelectionInterval(i, i);
                    tablaCabeceras.scrollRectToVisible(tablaCabeceras.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }
}

package com.mycompany.app_distribuidas.vista;

import com.google.gson.*;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class FrmCuentaBancaria extends JFrame {

    // Configuracion de la tabla para mostrar las cuentas
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Número de Cuenta", "Cliente", "Descripción"}, 0);

    public FrmCuentaBancaria() {
        initComponents();
        setTitle("Administración de Cuentas Bancarias");
        setSize(820, 490);
        setLocationRelativeTo(null);
        tabla.setModel(model);

        // Enlazar los botones con el metodo accion
        btn1.addActionListener(e -> accion("Nuevo"));
        btn2.addActionListener(e -> accion("Guardar"));
        btn3.addActionListener(e -> accion("Modificar"));
        btn4.addActionListener(e -> accion("Eliminar"));
        btn5.addActionListener(e -> accion("Buscar"));
        btn6.addActionListener(e -> accion("Recargar"));

        // Llenar los campos cuando se selecciona una fila de la tabla
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int r = tabla.getSelectedRow();
                id.setText(model.getValueAt(r, 0).toString());
                cmbCliente.setSelectedItem(model.getValueAt(r, 1).toString());
                descripcion.setText(model.getValueAt(r, 2) == null ? "" : model.getValueAt(r, 2).toString());
            }
        });

        cargarClientesCombo();
        cargar();
    }

    private void initComponents() {
        panelSuperior = new javax.swing.JPanel();
        panelCampos = new javax.swing.JPanel();
        lbl1 = new javax.swing.JLabel();
        id = new javax.swing.JTextField();
        lbl2 = new javax.swing.JLabel();

        // Lista desplegable para elegir el cliente sin digitar codigos
        cmbCliente = new javax.swing.JComboBox<>();

        lbl3 = new javax.swing.JLabel();
        descripcion = new javax.swing.JTextField();

        lblVacio = new javax.swing.JLabel();
        txtVacio = new javax.swing.JLabel();

        panelBotones = new javax.swing.JPanel();
        btn1 = new javax.swing.JButton();
        btn3 = new javax.swing.JButton();
        btn4 = new javax.swing.JButton();
        btn5 = new javax.swing.JButton();
        btn6 = new javax.swing.JButton();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        btn2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        panelSuperior.setLayout(new java.awt.BorderLayout());
        panelCampos.setLayout(new java.awt.GridLayout(4, 2));

        lbl1.setText("Número de Cuenta (automático)");
        panelCampos.add(lbl1);
        id.setEditable(false);
        panelCampos.add(id);

        lbl2.setText("Seleccione Cliente");
        panelCampos.add(lbl2);
        panelCampos.add(cmbCliente);

        lbl3.setText("Descripción");
        panelCampos.add(lbl3);
        panelCampos.add(descripcion);

        panelCampos.add(lblVacio);
        panelCampos.add(txtVacio);

        panelSuperior.add(panelCampos, java.awt.BorderLayout.NORTH);

        btn1.setText("Nuevo");
        panelBotones.add(btn1);
        btn3.setText("Modificar");
        panelBotones.add(btn3);
        btn4.setText("Eliminar");
        panelBotones.add(btn4);
        btn5.setText("Buscar");
        panelBotones.add(btn5);
        btn6.setText("Recargar");
        panelBotones.add(btn6);

        panelSuperior.add(panelBotones, java.awt.BorderLayout.SOUTH);
        scrollTabla.setViewportView(tabla);

        jLabel5.setFont(new java.awt.Font("Arial", 1, 18));
        jLabel5.setForeground(new java.awt.Color(0, 0, 102));
        jLabel5.setText("ADMINISTRACIÓN DE CUENTAS BANCARIAS");

        btn2.setText("Guardar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btn2)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(scrollTabla, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(48, 48, 48))
            .addGroup(layout.createSequentialGroup()
                .addGap(90, 90, 90)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(29, 29, 29)
                .addComponent(panelSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(scrollTabla, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }

    private javax.swing.JButton btn1;
    private javax.swing.JButton btn2;
    private javax.swing.JButton btn3;
    private javax.swing.JButton btn4;
    private javax.swing.JButton btn5;
    private javax.swing.JButton btn6;
    private javax.swing.JComboBox<String> cmbCliente;
    private javax.swing.JTextField descripcion;
    private javax.swing.JTextField id;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel lbl1;
    private javax.swing.JLabel lbl2;
    private javax.swing.JLabel lbl3;
    private javax.swing.JLabel lblVacio;
    private javax.swing.JLabel txtVacio;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCampos;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tabla;

    // Envia la peticion al servidor por WebSocket
    private JsonElement call(String op, Object datos) {
        return ClienteWebSocket.get().solicitar("bancos.cuenta." + op, datos);
    }

    // Carga la lista de clientes desde la tabla CLIENTE en el ComboBox
    private void cargarClientesCombo() {
        try {
            JsonArray a = call("clientes", Map.of()).getAsJsonObject().getAsJsonArray("items");
            cmbCliente.removeAllItems();
            for (JsonElement e : a) {
                cmbCliente.addItem(e.getAsString());
            }
        } catch (Exception ex) { }
    }

    private void limpiar() {
        id.setText("");
        descripcion.setText("");
        if (cmbCliente.getItemCount() > 0) {
            cmbCliente.setSelectedIndex(0);
        }
    }

    private Map<String, Object> datos(boolean editar) {
        Map<String, Object> x = new LinkedHashMap<>();
        if (editar && !id.getText().isEmpty()) {
            x.put("numeroCuenta", Integer.parseInt(id.getText()));
        }
        x.put("cliente", cmbCliente.getSelectedItem() == null ? "" : cmbCliente.getSelectedItem().toString());
        x.put("descripcion", descripcion.getText().trim());
        return x;
    }

    private void cargar() {
        try {
            JsonArray a = call("listar", Map.of()).getAsJsonObject().getAsJsonArray("items");
            model.setRowCount(0);
            for (JsonElement e : a) {
                JsonObject x = e.getAsJsonObject();
                model.addRow(new Object[]{
                    x.get("numeroCuenta").getAsInt(),
                    x.get("cliente").getAsString(),
                    x.get("descripcion").isJsonNull() ? "" : x.get("descripcion").getAsString()
                });
            }
        } catch (Exception ex) { }
    }

    private void accion(String t) {
        try {
            switch (t) {
                case "Nuevo" -> limpiar();
                case "Guardar" -> {
                    call("insertar", datos(false));
                    cargar();
                    limpiar();
                    JOptionPane.showMessageDialog(this, "Cuenta registrada con éxito");
                }
                case "Modificar" -> {
                    call("actualizar", datos(true));
                    cargar();
                    limpiar();
                    JOptionPane.showMessageDialog(this, "Cuenta actualizada con éxito");
                }
                case "Eliminar" -> {
                    if (JOptionPane.showConfirmDialog(this, "¿Eliminar cuenta?", "Confirmar", JOptionPane.YES_NO_OPTION) == 0) {
                        call("eliminar", Map.of("numeroCuenta", Integer.parseInt(id.getText())));
                        cargar();
                        limpiar();
                    }
                }
                case "Buscar" -> {
                    String q = JOptionPane.showInputDialog(this, "Buscar por número de cuenta o cliente");
                    if (q != null) {
                        for (int i = 0; i < model.getRowCount(); i++) {
                            if ((model.getValueAt(i, 0).toString() + model.getValueAt(i, 1)).toLowerCase().contains(q.toLowerCase())) {
                                tabla.setRowSelectionInterval(i, i);
                                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                                break;
                            }
                        }
                    }
                }
                case "Recargar" -> {
                    cargarClientesCombo();
                    cargar();
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

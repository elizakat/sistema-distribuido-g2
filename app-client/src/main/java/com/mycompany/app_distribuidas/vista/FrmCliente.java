package com.mycompany.app_distribuidas.vista;
import com.google.gson.*;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
public class FrmCliente extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "RUC", "Nombre", "Dirección"}, 0);
    public FrmCliente() {
        initComponents();
        setTitle("Clientes - Facturación");
        setSize(820, 490);
        setLocationRelativeTo(null);
        tabla.setModel(model);
        btn1.addActionListener(e -> accion("Nuevo"));
        btn2.addActionListener(e -> accion("Guardar"));
        btn3.addActionListener(e -> accion("Modificar"));
        btn4.addActionListener(e -> accion("Eliminar"));
        btn5.addActionListener(e -> accion("Buscar"));
        btn6.addActionListener(e -> accion("Recargar"));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int r = tabla.getSelectedRow();
                id.setText(model.getValueAt(r,0).toString());
                ruc.setText(model.getValueAt(r,1).toString());
                nombre.setText(model.getValueAt(r,2).toString());
                direccion.setText(model.getValueAt(r,3)==null ? "" : model.getValueAt(r,3).toString());
            }
        });
        cargar();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSuperior = new javax.swing.JPanel();
        panelCampos = new javax.swing.JPanel();
        lbl1 = new javax.swing.JLabel();
        id = new javax.swing.JTextField();
        lbl2 = new javax.swing.JLabel();
        ruc = new javax.swing.JTextField();
        lbl3 = new javax.swing.JLabel();
        nombre = new javax.swing.JTextField();
        lbl4 = new javax.swing.JLabel();
        direccion = new javax.swing.JTextField();
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

        lbl1.setText("ID (automático)");
        panelCampos.add(lbl1);

        id.setEditable(false);
        panelCampos.add(id);

        lbl2.setText("RUC");
        panelCampos.add(lbl2);
        panelCampos.add(ruc);

        lbl3.setText("Nombre");
        panelCampos.add(lbl3);
        panelCampos.add(nombre);

        lbl4.setText("Dirección");
        panelCampos.add(lbl4);
        panelCampos.add(direccion);

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

        jLabel5.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 102));
        jLabel5.setText("ADMINISTRACIÓN DE ARTÍCULOS");

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
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    }// </editor-fold>//GEN-END:initComponents
    // </editor-fold>

    // Variables declaration - do not modify
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn1;
    private javax.swing.JButton btn2;
    private javax.swing.JButton btn3;
    private javax.swing.JButton btn4;
    private javax.swing.JButton btn5;
    private javax.swing.JButton btn6;
    private javax.swing.JTextField direccion;
    private javax.swing.JTextField id;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel lbl1;
    private javax.swing.JLabel lbl2;
    private javax.swing.JLabel lbl3;
    private javax.swing.JLabel lbl4;
    private javax.swing.JTextField nombre;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCampos;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JTextField ruc;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tabla;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration

    private JsonElement call(String op,Object datos){
        return ClienteWebSocket.get().solicitar("facturacion.cliente."+op,datos);
    }
    private void limpiar(){
        for(JTextField f:new JTextField[]{
            id,ruc,nombre,direccion
        })f.setText("");
    }
    private Map<String,Object> datos(boolean editar){
        Map<String,Object> x=new LinkedHashMap<>();
        if(editar)x.put("idCliente",Integer.parseInt(id.getText()));
        x.put("ruc",ruc.getText().trim());
        x.put("nombre",nombre.getText().trim());
        x.put("direccion",direccion.getText().trim());
        return x;
    }
    private void cargar(){
        try{
            JsonArray a=call("listar",Map.of()).getAsJsonObject().getAsJsonArray("items");
            model.setRowCount(0);
            for(JsonElement e:a){
                JsonObject x=e.getAsJsonObject();
                model.addRow(new Object[]{
                    x.get("idCliente").getAsInt(),x.get("ruc").getAsString(),x.get("nombre").getAsString(),x.get("direccion").isJsonNull()?"":x.get("direccion").getAsString()
                });
            }
        }catch(Exception ex){
            error(ex);
        }
    }
    private void accion(String t){
        try{
            switch(t){
                case "Nuevo"->limpiar();
                case "Guardar"->{
                    call("crear",datos(false));
                    cargar();
                    limpiar();
                }case "Modificar"->{
                    call("actualizar",datos(true));
                    cargar();
                }case "Eliminar"->{
                    if(JOptionPane.showConfirmDialog(this,"¿Eliminar cliente?","Confirmar",JOptionPane.YES_NO_OPTION)==0){
                        call("eliminar",Map.of("idCliente",Integer.parseInt(id.getText())));
                        cargar();
                        limpiar();
                    }
                }case "Buscar"->{
                    String q=JOptionPane.showInputDialog(this,"Buscar por nombre o RUC");
                    if(q!=null)for(int i=0;
                    i<model.getRowCount();
                    i++){
                        if((model.getValueAt(i,1).toString()+model.getValueAt(i,2)).toLowerCase().contains(q.toLowerCase())){
                            tabla.setRowSelectionInterval(i,i);
                            tabla.scrollRectToVisible(tabla.getCellRect(i,0,true));
                            break;
                        }
                    }
                }case "Recargar"->cargar();
            }
        }catch(Exception ex){
            error(ex);
        }
    }
    private void error(Exception ex){
        JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }
}

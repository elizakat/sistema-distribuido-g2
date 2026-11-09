package com.mycompany.app_distribuidas.vista;
import com.google.gson.*;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
public class FrmCiudadEntrega extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Ciudad"}, 0);
    public FrmCiudadEntrega() {
        initComponents();
        setTitle("Ciudades de entrega - Facturación");
        setSize(650, 420);
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
                id.setText(model.getValueAt(tabla.getSelectedRow(),0).toString());
                nombre.setText(model.getValueAt(tabla.getSelectedRow(),1).toString());
            }
        });
        cargar();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    //GEN-BEGIN:initComponents
    private void initComponents() {
        panelSuperior = new javax.swing.JPanel();
        panelCampos = new javax.swing.JPanel();
        lbl1 = new javax.swing.JLabel();
        id = new javax.swing.JTextField();
        lbl2 = new javax.swing.JLabel();
        nombre = new javax.swing.JTextField();
        panelBotones = new javax.swing.JPanel();
        btn1 = new javax.swing.JButton();
        btn2 = new javax.swing.JButton();
        btn3 = new javax.swing.JButton();
        btn4 = new javax.swing.JButton();
        btn5 = new javax.swing.JButton();
        btn6 = new javax.swing.JButton();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.BorderLayout());
        panelSuperior.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panelSuperior, java.awt.BorderLayout.NORTH);
        panelCampos.setLayout(new java.awt.GridLayout(2, 2, 8, 6));
        panelSuperior.add(panelCampos, java.awt.BorderLayout.NORTH);
        lbl1.setText("ID (automático)");
        panelCampos.add(lbl1);
        id.setEditable(false);
        panelCampos.add(id);
        lbl2.setText("Nombre");
        panelCampos.add(lbl2);
        nombre.setEditable(true);
        panelCampos.add(nombre);
        panelBotones.setLayout(new java.awt.FlowLayout());
        panelSuperior.add(panelBotones, java.awt.BorderLayout.SOUTH);
        btn1.setText("Nuevo");
        panelBotones.add(btn1);
        btn2.setText("Guardar");
        panelBotones.add(btn2);
        btn3.setText("Modificar");
        panelBotones.add(btn3);
        btn4.setText("Eliminar");
        panelBotones.add(btn4);
        btn5.setText("Buscar");
        panelBotones.add(btn5);
        btn6.setText("Recargar");
        panelBotones.add(btn6);
        getContentPane().add(scrollTabla, java.awt.BorderLayout.CENTER);
        scrollTabla.setViewportView(tabla);
        pack();
    }
    //GEN-END:initComponents
    // </editor-fold>

    // Variables declaration - do not modify
    //GEN-BEGIN:variables
    private javax.swing.JButton btn1;
    private javax.swing.JButton btn2;
    private javax.swing.JButton btn3;
    private javax.swing.JButton btn4;
    private javax.swing.JButton btn5;
    private javax.swing.JButton btn6;
    private javax.swing.JTextField id;
    private javax.swing.JLabel lbl1;
    private javax.swing.JLabel lbl2;
    private javax.swing.JTextField nombre;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCampos;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tabla;
    //GEN-END:variables
    // End of variables declaration

    private JsonElement call(String op,Object p){
        return ClienteWebSocket.get().solicitar("facturacion.ciudad."+op,p);
    }
    private void cargar(){
        try{
            JsonArray a=call("listar",Map.of()).getAsJsonObject().getAsJsonArray("items");
            model.setRowCount(0);
            a.forEach(el->{
                JsonObject x=el.getAsJsonObject();
                model.addRow(new Object[]{
                    x.get("idCiudad").getAsInt(),x.get("nombreCiudad").getAsString()
                });
            });
        }catch(Exception ex){
            error(ex);
        }
    }
    private void accion(String a){
        try{
            switch(a){
                case "Nuevo"->{
                    id.setText("");
                    nombre.setText("");
                }case "Guardar"->{
                    call("crear",Map.of("nombreCiudad",nombre.getText().trim()));
                    cargar();
                }case "Modificar"->{
                    call("actualizar",Map.of("idCiudad",Integer.parseInt(id.getText()),"nombreCiudad",nombre.getText().trim()));
                    cargar();
                }case "Eliminar"->{
                    if(JOptionPane.showConfirmDialog(this,"¿Eliminar ciudad?","Confirmar",JOptionPane.YES_NO_OPTION)==0){
                        call("eliminar",Map.of("idCiudad",Integer.parseInt(id.getText())));
                        cargar();
                    }
                }case "Buscar"->{
                    String q=JOptionPane.showInputDialog(this,"Nombre de ciudad");
                    if(q!=null)for(int i=0;
                    i<model.getRowCount();
                    i++)if(model.getValueAt(i,1).toString().toLowerCase().contains(q.toLowerCase())){
                        tabla.setRowSelectionInterval(i,i);
                        break;
                    }
                }case "Recargar"->cargar();
            }
        }catch(Exception ex){
            error(ex);
        }
    }
    private void error(Exception e){
        JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }
}

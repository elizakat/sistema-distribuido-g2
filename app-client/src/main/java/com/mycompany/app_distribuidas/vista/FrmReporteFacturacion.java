package com.mycompany.app_distribuidas.vista;
import com.google.gson.*;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.print.*;
import java.time.LocalDate;
import java.util.*;
public class FrmReporteFacturacion extends JFrame {
    public FrmReporteFacturacion() {
        initComponents();
        setTitle("Reportes de facturación");
        setSize(820, 550);
        setLocationRelativeTo(null);
        tipo.setModel(new DefaultComboBoxModel<>(new String[]{"Ventas por ciudad", "Ventas por cliente y artículo"}));
        desde.setText(LocalDate.now().withDayOfMonth(1).toString());
        hasta.setText(LocalDate.now().toString());
        btnConsultar.addActionListener(e -> cargar());
        btnImprimir.addActionListener(e -> {
            try { tabla.print(); }
            catch (PrinterException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    //GEN-BEGIN:initComponents
    private void initComponents() {
        panelSuperior = new javax.swing.JPanel();
        panelFiltros = new javax.swing.JPanel();
        lblTipo = new javax.swing.JLabel();
        lblDesde = new javax.swing.JLabel();
        lblHasta = new javax.swing.JLabel();
        tipo = new javax.swing.JComboBox();
        desde = new javax.swing.JTextField();
        hasta = new javax.swing.JTextField();
        panelBotones = new javax.swing.JPanel();
        btnConsultar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.BorderLayout());
        panelSuperior.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panelSuperior, java.awt.BorderLayout.NORTH);
        panelFiltros.setLayout(new java.awt.GridLayout(2, 3, 8, 6));
        panelSuperior.add(panelFiltros, java.awt.BorderLayout.NORTH);
        lblTipo.setText("Reporte");
        panelFiltros.add(lblTipo);
        lblDesde.setText("Desde AAAA-MM-DD");
        panelFiltros.add(lblDesde);
        lblHasta.setText("Hasta AAAA-MM-DD");
        panelFiltros.add(lblHasta);
        panelFiltros.add(tipo);
        panelFiltros.add(desde);
        panelFiltros.add(hasta);
        panelBotones.setLayout(new java.awt.FlowLayout());
        panelSuperior.add(panelBotones, java.awt.BorderLayout.SOUTH);
        btnConsultar.setText("Consultar");
        panelBotones.add(btnConsultar);
        btnImprimir.setText("Imprimir");
        panelBotones.add(btnImprimir);
        getContentPane().add(scrollTabla, java.awt.BorderLayout.CENTER);
        scrollTabla.setViewportView(tabla);
        pack();
    }
    //GEN-END:initComponents
    // </editor-fold>

    // Variables declaration - do not modify
    //GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JTextField desde;
    private javax.swing.JTextField hasta;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tabla;
    private javax.swing.JComboBox tipo;
    //GEN-END:variables
    // End of variables declaration

    private void cargar(){
        try{
            boolean cruzado=tipo.getSelectedIndex()==1;
            JsonArray a=ClienteWebSocket.get().solicitar("facturacion.reporte."+(cruzado?"ventasClienteArticulo":"ventasPorCiudad"),Map.of("desde",desde.getText(),"hasta",hasta.getText())).getAsJsonObject().getAsJsonArray("items");
            DefaultTableModel m;
            if(!cruzado){
                m=new DefaultTableModel(new String[]{
                    "Ciudad","Total vendido"
                },0);
                a.forEach(el->{
                    JsonObject x=el.getAsJsonObject();
                    m.addRow(new Object[]{
                        x.get("ciudad").getAsString(),x.get("total").getAsBigDecimal()
                    });
                });
            }else{
                Set<String> clientes=new TreeSet<>(),articulos=new TreeSet<>();
                Map<String,Map<String,String>> valores=new TreeMap<>();
                a.forEach(el->{
                    JsonObject x=el.getAsJsonObject();
                    String art=x.get("articulo").getAsString(),cli=x.get("cliente").getAsString();
                    clientes.add(cli);
                    articulos.add(art);
                    valores.computeIfAbsent(art,k->new TreeMap<>()).put(cli,x.get("total").getAsString());
                });
                java.util.List<String> col=new ArrayList<>();
                col.add("Artículo");
                col.addAll(clientes);
                m=new DefaultTableModel(col.toArray(),0);
                for(String art:articulos){
                    java.util.List<Object> row=new ArrayList<>();
                    row.add(art);
                    for(String cli:clientes)row.add(valores.getOrDefault(art,Map.of()).getOrDefault(cli,"0.00"));
                    m.addRow(row.toArray());
                }
            }tabla.setModel(m);
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }
}

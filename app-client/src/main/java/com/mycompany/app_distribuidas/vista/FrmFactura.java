package com.mycompany.app_distribuidas.vista;
import com.google.gson.*;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.*;
import java.util.*;
public class FrmFactura extends JFrame {
    private record Item(int id, String nombre) {
        public String toString() { return nombre; }
    }
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "ID artículo", "Artículo", "Cantidad", "Precio", "Subtotal"
    }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    public FrmFactura() {
        initComponents();
        //btnAccion9.setEnabled(false); // CxC fuera de alcance.
        setTitle("Facturación - Cabecera y detalle");
        setSize(1050, 700);
        setLocationRelativeTo(null);
        fecha.setText(java.time.LocalDate.now().toString());
        cantidad.setText("1");
        clientes.setModel(new DefaultComboBoxModel<>());
        ciudades.setModel(new DefaultComboBoxModel<>());
        articulos.setModel(new DefaultComboBoxModel<>());
        tabla.setModel(model);
        btnAgregar.addActionListener(e -> agregar());
        articulos.addActionListener(e -> precio());
        btnAccion1.addActionListener(e -> accion("Nueva"));
        btnAccion2.addActionListener(e -> accion("Guardar borrador"));
        btnAccion3.addActionListener(e -> accion("Buscar factura"));
        btnAccion4.addActionListener(e -> accion("Actualizar borrador"));
        btnAccion5.addActionListener(e -> accion("Quitar línea"));
        btnAccion6.addActionListener(e -> accion("Modificar cantidad"));
        btnAccion7.addActionListener(e -> accion("Confirmar venta"));
        btnAccion8.addActionListener(e -> accion("Anular factura"));
        //btnAccion9.addActionListener(e -> accion("Saldo"));
        //btnAccion10.addActionListener(e -> accion("Reportes"));
        cargarCatalogos();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSuperior = new javax.swing.JPanel();
        panelCabeceraYLinea = new javax.swing.JPanel();
        panelCabecera = new javax.swing.JPanel();
        lblFactura1 = new javax.swing.JLabel();
        idFactura = new javax.swing.JTextField();
        lblFactura2 = new javax.swing.JLabel();
        fecha = new javax.swing.JTextField();
        lblFactura3 = new javax.swing.JLabel();
        clientes = new javax.swing.JComboBox();
        lblFactura4 = new javax.swing.JLabel();
        ciudades = new javax.swing.JComboBox();
        lblFactura5 = new javax.swing.JLabel();
        estado = new javax.swing.JLabel();
        lblFactura6 = new javax.swing.JLabel();
        total = new javax.swing.JLabel();
        panelLinea = new javax.swing.JPanel();
        lblArticulo = new javax.swing.JLabel();
        lblCantidad = new javax.swing.JLabel();
        lblPrecio = new javax.swing.JLabel();
        lblAccion = new javax.swing.JLabel();
        articulos = new javax.swing.JComboBox();
        cantidad = new javax.swing.JTextField();
        precio = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        panelAcciones = new javax.swing.JPanel();
        btnAccion1 = new javax.swing.JButton();
        btnAccion3 = new javax.swing.JButton();
        btnAccion4 = new javax.swing.JButton();
        btnAccion5 = new javax.swing.JButton();
        btnAccion6 = new javax.swing.JButton();
        btnAccion7 = new javax.swing.JButton();
        btnAccion8 = new javax.swing.JButton();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        btnAccion2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelSuperior.setLayout(new java.awt.BorderLayout());

        panelCabeceraYLinea.setLayout(new java.awt.BorderLayout());

        panelCabecera.setLayout(new java.awt.GridLayout(3, 4));

        lblFactura1.setText("ID factura");
        panelCabecera.add(lblFactura1);

        idFactura.setEditable(false);
        panelCabecera.add(idFactura);

        lblFactura2.setText("Fecha AAAA-MM-DD");
        panelCabecera.add(lblFactura2);
        panelCabecera.add(fecha);

        lblFactura3.setText("Cliente");
        panelCabecera.add(lblFactura3);
        panelCabecera.add(clientes);

        lblFactura4.setText("Ciudad");
        panelCabecera.add(lblFactura4);
        panelCabecera.add(ciudades);

        lblFactura5.setText("Estado");
        panelCabecera.add(lblFactura5);

        estado.setText("BORRADOR");
        panelCabecera.add(estado);

        lblFactura6.setText("Total $");
        panelCabecera.add(lblFactura6);

        total.setText("0.00");
        panelCabecera.add(total);

        panelCabeceraYLinea.add(panelCabecera, java.awt.BorderLayout.NORTH);

        panelLinea.setLayout(new java.awt.GridLayout(2, 4));

        lblArticulo.setText("Artículo");
        panelLinea.add(lblArticulo);

        lblCantidad.setText("Cantidad");
        panelLinea.add(lblCantidad);

        lblPrecio.setText("Precio unitario");
        panelLinea.add(lblPrecio);
        panelLinea.add(lblAccion);
        panelLinea.add(articulos);
        panelLinea.add(cantidad);

        precio.setEditable(false);
        panelLinea.add(precio);

        btnAgregar.setText("Agregar artículo");
        panelLinea.add(btnAgregar);

        panelCabeceraYLinea.add(panelLinea, java.awt.BorderLayout.CENTER);

        panelSuperior.add(panelCabeceraYLinea, java.awt.BorderLayout.NORTH);

        panelAcciones.setLayout(new java.awt.GridLayout(2, 5));

        btnAccion1.setBackground(new java.awt.Color(0, 0, 102));
        btnAccion1.setForeground(new java.awt.Color(255, 255, 255));
        btnAccion1.setText("Nueva");
        panelAcciones.add(btnAccion1);

        btnAccion3.setBackground(new java.awt.Color(0, 0, 102));
        btnAccion3.setForeground(new java.awt.Color(255, 255, 255));
        btnAccion3.setText("Buscar factura");
        panelAcciones.add(btnAccion3);

        btnAccion4.setBackground(new java.awt.Color(0, 0, 102));
        btnAccion4.setForeground(new java.awt.Color(255, 255, 255));
        btnAccion4.setText("Actualizar borrador");
        panelAcciones.add(btnAccion4);

        btnAccion5.setBackground(new java.awt.Color(0, 0, 102));
        btnAccion5.setForeground(new java.awt.Color(255, 255, 255));
        btnAccion5.setText("Quitar línea");
        panelAcciones.add(btnAccion5);

        btnAccion6.setBackground(new java.awt.Color(0, 0, 102));
        btnAccion6.setForeground(new java.awt.Color(255, 255, 255));
        btnAccion6.setText("Modificar cantidad");
        panelAcciones.add(btnAccion6);

        btnAccion7.setBackground(new java.awt.Color(0, 0, 102));
        btnAccion7.setForeground(new java.awt.Color(255, 255, 255));
        btnAccion7.setText("Confirmar venta");
        panelAcciones.add(btnAccion7);

        btnAccion8.setBackground(new java.awt.Color(0, 0, 102));
        btnAccion8.setForeground(new java.awt.Color(255, 255, 255));
        btnAccion8.setText("Anular factura");
        panelAcciones.add(btnAccion8);

        panelSuperior.add(panelAcciones, java.awt.BorderLayout.SOUTH);

        scrollTabla.setViewportView(tabla);

        btnAccion2.setText("Guardar");
        btnAccion2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAccion2ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 102));
        jLabel6.setText("FACTURACIÓN");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(24, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollTabla, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(477, 477, 477)
                        .addComponent(btnAccion2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(238, 238, 238)
                .addComponent(jLabel6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollTabla, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnAccion2)
                .addGap(14, 14, 14))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAccion2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAccion2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAccion2ActionPerformed
    // </editor-fold>

    // Variables declaration - do not modify
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox articulos;
    private javax.swing.JButton btnAccion1;
    private javax.swing.JButton btnAccion2;
    private javax.swing.JButton btnAccion3;
    private javax.swing.JButton btnAccion4;
    private javax.swing.JButton btnAccion5;
    private javax.swing.JButton btnAccion6;
    private javax.swing.JButton btnAccion7;
    private javax.swing.JButton btnAccion8;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JTextField cantidad;
    private javax.swing.JComboBox ciudades;
    private javax.swing.JComboBox clientes;
    private javax.swing.JLabel estado;
    private javax.swing.JTextField fecha;
    private javax.swing.JTextField idFactura;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lblAccion;
    private javax.swing.JLabel lblArticulo;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblFactura1;
    private javax.swing.JLabel lblFactura2;
    private javax.swing.JLabel lblFactura3;
    private javax.swing.JLabel lblFactura4;
    private javax.swing.JLabel lblFactura5;
    private javax.swing.JLabel lblFactura6;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JPanel panelAcciones;
    private javax.swing.JPanel panelCabecera;
    private javax.swing.JPanel panelCabeceraYLinea;
    private javax.swing.JPanel panelLinea;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JTextField precio;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTable tabla;
    private javax.swing.JLabel total;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration

    private JsonElement call(String a,Object p){
        return ClienteWebSocket.get().solicitar(a,p);
    }
    private void cargarCatalogos(){
        try{
            cargarCombo(clientes,"facturacion.cliente.listar","idCliente","nombre");
            cargarCombo(ciudades,"facturacion.ciudad.listar","idCiudad","nombreCiudad");
            cargarCombo(articulos,"inventario.articulo.listar","idArticulo","nombre");
            precio();
        }catch(Exception ex){
            error(ex);
        }
    }
    private void cargarCombo(JComboBox<Item> combo,String accion,String id,String nombre){
        combo.removeAllItems();
        JsonArray a=call(accion,Map.of()).getAsJsonObject().getAsJsonArray("items");
        a.forEach(el->{
            JsonObject x=el.getAsJsonObject();
            combo.addItem(new Item(x.get(id).getAsInt(),x.get(nombre).getAsString()));
        });
    }
    private void precio(){
        Item item=(Item)articulos.getSelectedItem();
        if(item==null)return;
        try{
            JsonArray a=call("inventario.articulo.listar",Map.of()).getAsJsonObject().getAsJsonArray("items");
            for(JsonElement e:a){
                JsonObject x=e.getAsJsonObject();
                if(x.get("idArticulo").getAsInt()==item.id){
                    precio.setText(x.get("precio").getAsString());
                    break;
                }
            }
        }catch(Exception e){
            error(e);
        }
    }
    private void agregar(){
        try{
            Item a=(Item)articulos.getSelectedItem();
            if(a==null)throw new IllegalStateException("No hay artículos");
            BigDecimal c=new BigDecimal(cantidad.getText());
            BigDecimal p=new BigDecimal(precio.getText());
            if(c.signum()<=0)throw new IllegalArgumentException("Cantidad positiva requerida");
            for(int i=0;
            i<model.getRowCount();
            i++)if((int)model.getValueAt(i,0)==a.id)throw new IllegalArgumentException("Artículo ya agregado");
            model.addRow(new Object[]{
                a.id,a.nombre,c,p,c.multiply(p)
            });
            total();
        }catch(Exception ex){
            error(ex);
        }
    }
    private void total(){
        BigDecimal sum=BigDecimal.ZERO;
        for(int i=0;
        i<model.getRowCount();
        i++)sum=sum.add((BigDecimal)model.getValueAt(i,4));
        total.setText(sum.setScale(2,RoundingMode.HALF_UP).toString());
    }
    private Map<String,Object> datos(boolean update){
        Item c=(Item)clientes.getSelectedItem(),ci=(Item)ciudades.getSelectedItem();
        if(c==null||ci==null)throw new IllegalArgumentException("Seleccione cliente y ciudad");
        java.util.List<Map<String,Object>> lineas=new ArrayList<>();
        for(int i=0;
        i<model.getRowCount();
        i++)lineas.add(Map.of("idArticulo",model.getValueAt(i,0),"cantidad",model.getValueAt(i,2)));
        Map<String,Object> p=new LinkedHashMap<>();
        if(update)p.put("idFactura",Integer.parseInt(idFactura.getText()));
        p.put("idCliente",c.id);
        p.put("idCiudad",ci.id);
        p.put("fechaEmision",fecha.getText());
        p.put("detalles",lineas);
        return p;
    }
    private void elegir(JComboBox<Item> cb,int id){
        for(int i=0;
        i<cb.getItemCount();
        i++)if(cb.getItemAt(i).id==id){
            cb.setSelectedIndex(i);
            return;
        }
    }
    private void cargar(int id){
        JsonObject resp=call("facturacion.factura.obtener",Map.of("idFactura",id)).getAsJsonObject();
        JsonObject f=resp.getAsJsonObject("factura");
        idFactura.setText(f.get("idFactura").getAsString());
        fecha.setText(f.get("fechaEmision").getAsString());
        estado.setText(f.get("estado").getAsString());
        elegir(clientes,f.get("idCliente").getAsInt());
        elegir(ciudades,f.get("idCiudad").getAsInt());
        model.setRowCount(0);
        resp.getAsJsonArray("detalles").forEach(el->{
            JsonObject x=el.getAsJsonObject();
            BigDecimal cant=x.get("cantidad").getAsBigDecimal(),pr=x.get("precioVenta").getAsBigDecimal();
            model.addRow(new Object[]{
                x.get("idArticulo").getAsInt(),x.get("nombreArticulo").getAsString(),cant,pr,cant.multiply(pr)
            });
        });
        total();
    }
    private int current(){
        if(idFactura.getText().isBlank())throw new IllegalArgumentException("Primero guarde o busque una factura");
        return Integer.parseInt(idFactura.getText());
    }
    private void accion(String a){
        try{
            switch(a){
                case "Nueva"->{
                    idFactura.setText("");
                    fecha.setText(java.time.LocalDate.now().toString());
                    estado.setText("BORRADOR");
                    model.setRowCount(0);
                    total();
                    cargarCatalogos();
                }case "Guardar borrador"->{
                    JsonObject resp=call("facturacion.factura.crear",datos(false)).getAsJsonObject();
                    cargar(resp.getAsJsonObject("factura").get("idFactura").getAsInt());
                }case "Actualizar borrador"->{
                    call("facturacion.factura.actualizar",datos(true));
                    cargar(current());
                }case "Buscar factura"->{
                    String n=JOptionPane.showInputDialog(this,"ID de factura a recuperar");
                    if(n!=null)cargar(Integer.parseInt(n));
                }case "Quitar línea"->{
                    if(tabla.getSelectedRow()>=0){
                        model.removeRow(tabla.getSelectedRow());
                        total();
                    }
                }case "Modificar cantidad"->{
                    int index=tabla.getSelectedRow();
                    if(index<0)throw new IllegalArgumentException("Seleccione una línea");
                    String nuevo=JOptionPane.showInputDialog(this,"Nueva cantidad",model.getValueAt(index,2));
                    if(nuevo!=null){
                        BigDecimal q=new BigDecimal(nuevo);
                        if(q.signum()<=0)throw new IllegalArgumentException("Cantidad positiva requerida");
                        model.setValueAt(q,index,2);
                        model.setValueAt(q.multiply((BigDecimal)model.getValueAt(index,3)),index,4);
                        total();
                    }
                }case "Confirmar venta"->{
                    call("facturacion.factura.confirmar",Map.of("idFactura",current()));
                    cargar(current());
                }case "Anular factura"->{
                    if(JOptionPane.showConfirmDialog(this,"¿Anular y devolver stock?","Confirmar",JOptionPane.YES_NO_OPTION)==0){
                        call("facturacion.factura.anular",Map.of("idFactura",current()));
                        cargar(current());
                    }
                }case "Saldo"->{
                    JsonObject s=call("facturacion.factura.saldo",Map.of("idFactura",current())).getAsJsonObject();
                    JOptionPane.showMessageDialog(this,"Saldo pendiente: $"+s.get("saldoPendiente"));
                }case "Reportes"->new FrmReporteFacturacion().setVisible(true);
            }
        }catch(Exception ex){
            error(ex);
        }
    }
    private void error(Exception e){
        JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }
}

package com.mycompany.app_distribuidas;

import com.mycompany.app_distribuidas.vista.FrmLogin;
import javax.swing.SwingUtilities;

/** Inicia la aplicación de escritorio (cliente WebSocket). */
public class Client {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrmLogin ventana = new FrmLogin();
            ventana.setVisible(true);
        });
    }
}

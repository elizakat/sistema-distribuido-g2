package com.mycompany.app_distribuidas.negocio;

import com.google.gson.JsonObject;
import java.util.Map;
import com.mycompany.app_distribuidas.persistencia.Database;
import java.sql.*;

public class MovimientoInventarioService {

    public Object ejecutar(
        String operacion,
        JsonObject payload)
        throws Exception {

    if (!operacion.equals("crear")) {

        return Map.of(
                "estado",
                "ok"
        );
    }

    try (Connection c =
                 Database.abrir()) {

        c.setAutoCommit(false);

        long idMovimiento =
                Database.next(
                        c,
                        "SEQ_MOVIMIENTO"
                );

        try (PreparedStatement s =
                     c.prepareStatement(
                             """
                             INSERT INTO
                             MOVIMIENTO_INVENTARIO
                             (
                                 ID_MOVIMIENTO,
                                 ID_TIPO_MOV,
                                 FECHA,
                                 OBSERVACION
                             )
                             VALUES
                             (?,?,?,?)
                             """
                     )) {

            s.setLong(
                    1,
                    idMovimiento
            );

            s.setInt(
                    2,
                    payload.get(
                            "idTipoMovimiento"
                    ).getAsInt()
            );

            s.setDate(
                    3,
                    Date.valueOf(
                            payload.get(
                                    "fecha"
                            ).getAsString()
                    )
            );

            s.setString(
                    4,
                    payload.get(
                            "observacion"
                    ).getAsString()
            );

            s.executeUpdate();
        }
var detalles =
        payload.getAsJsonArray(
                "detalles"
        );

for (var item : detalles) {

    JsonObject detalle =
            item.getAsJsonObject();

    long idDetalle =
            Database.next(
                    c,
                    "SEQ_DETALLE_MOV"
            );

    try (PreparedStatement d =
                 c.prepareStatement(
                         """
                         INSERT INTO
                         DETALLE_MOVIMIENTO
                         (
                             ID_DETALLE_MOV,
                             ID_ARTICULO,
                             ID_MOVIMIENTO,
                             CANTIDAD,
                             PRECIO
                         )
                         VALUES
                         (?,?,?,?,?)
                         """
                 )) {

        d.setLong(
                1,
                idDetalle
        );

        d.setInt(
                2,
                detalle.get(
                        "idArticulo"
                ).getAsInt()
        );

        d.setLong(
                3,
                idMovimiento
        );

        d.setDouble(
                4,
                detalle.get(
                        "cantidad"
                ).getAsDouble()
        );

        d.setDouble(
                5,
                detalle.get(
                        "precio"
                ).getAsDouble()
        );

        d.executeUpdate();
    }
}
        c.commit();

        return Map.of(
                "idMovimiento",
                idMovimiento
        );
    }
}
}
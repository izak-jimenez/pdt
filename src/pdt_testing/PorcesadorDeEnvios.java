/* Copyright (C) Josué Isaac Jiménez Ortiz - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Josué Isaac Jiménez Ortiz <izakjimenez@gmail.com>, September 2015
 */
package pdt_testing;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static pdt_testing.MainGUI.pdtDBSettings;

/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class PorcesadorDeEnvios {

    //private ArrayList ordenesDeEnvio;
    private final String tablePrefix = pdtDBSettings.getProperty("prefix", "ps_pzrqs94mkk5vk6d");
    private final String textoCdgRastreo = pdtDBSettings.getProperty("cdg_rastreo_txt", "CODIGO DE RASTREO ESTAFETA:");

    public PorcesadorDeEnvios() {
    }

    public void procesarOrdenesDeEnvio(ArrayList<FTPFile> ordenesDeEnvios) throws SAXException, ParserConfigurationException, IOException, Exception {
        int idPedido = -99;
        String guiaEmbarque = "";

        // Se itera sobre la lista de archivos FTP recibidos para crear archivos XML y procesarlos
        for (FTPFile f : ordenesDeEnvios) {
            String noGuia = "";
            File xmlStockFile = new File("C:\\PDT\\tmp\\" + f.getName());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlStockFile);
            doc.getDocumentElement().normalize();

            // Ya que se tiene el archivo XML se procesa y se obtienen el id del pedido y el no. de guía
            NodeList nodosIdPedido = doc.getElementsByTagName("PEDIDO");
            NodeList nodosGuiaEmbarque = doc.getElementsByTagName("GUIA_EMBARQUE");
            try {
                noGuia = nodosGuiaEmbarque.item(0).getTextContent().trim();
            } catch (NullPointerException ex) {
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " La guía de embarque está vacía!");
            }

            if (nodosIdPedido != null) {
                idPedido = Integer.parseInt(nodosIdPedido.item(0).getTextContent().trim());
            } else {
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " El id del pedido está vacío!");
            }

            if (nodosGuiaEmbarque != null) {
                if (noGuia == null || noGuia.isEmpty()) {
                    guiaEmbarque = "NO DISPONIBLE";
                } else {
                    guiaEmbarque = nodosGuiaEmbarque.item(0).getTextContent().trim();
                }
            } else {
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " La guía de embarque está vacía!");
            }

            // Ya que se tienen los datos necesarios, se actualiza la base de datos
            String dbServer = pdtDBSettings.getProperty("server", "localhost");
            String dbUsr = pdtDBSettings.getProperty("user", "");
            Validacion val = new Validacion();
            String pwdDB = val.validaPass(pdtDBSettings.getProperty("password", ""));
            String dbNom = pdtDBSettings.getProperty("database", "");
            DBConn db = new DBConn(dbServer, dbUsr, pwdDB, dbNom);
            db.startConnection();

            /*
             String sqlQuery = "update " + tablePrefix + "orders\n"
             + "set current_state = 4, gift_message = '" + textoCdgRastreo + " " + guiaEmbarque + "'\n"
             + "where id_order = " + idPedido;
             */
            String sqlQuery = "update " + tablePrefix + "orders a, " + tablePrefix + "order_carrier b\n"
                    + "	set a.current_state = 4, b.tracking_number = '" + guiaEmbarque + "'\n"
                    + "	where a.id_order = " + idPedido + " and b.id_order = a.id_order";

            db.getConnection().setAutoCommit(false);// Inicia la transacción
            int updateOrderShippingStatus = db.updateQuery(sqlQuery);
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Se modificaron " + updateOrderShippingStatus + " registros.");
            db.getConnection().commit();// Finaliza la transacción

            // Cierra la conexión a la db
            if (db.getConnection() != null) {
                db.getConnection().close();
            }

            // Mueve el archivo xml generado a la carpeta de procesados
            if (!(new File("C:\\PDT\\procesados\\" + f.getName())).exists()) {
                FileUtils.moveFileToDirectory(new File("C:\\PDT\\tmp\\" + f.getName()), new File("C:\\PDT\\procesados\\"), true);
            } else {
                FileUtils.deleteQuietly(new File("C:\\PDT\\procesados\\" + f.getName()));
                FileUtils.moveFileToDirectory(new File("C:\\PDT\\tmp\\" + f.getName()), new File("C:\\PDT\\procesados\\"), true);
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " El archivo fue reemplazado!");
            }
        }
    }
}

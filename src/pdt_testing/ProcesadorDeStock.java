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
import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static pdt_testing.MainGUI.pdtDBSettings;

/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class ProcesadorDeStock  extends SwingWorker<Void, String>{

    private ArrayList productoRef, productoCantidad;
    private final String tablePrefix = pdtDBSettings.getProperty("prefix", "ps_pzrqs94mkk5vk6d");
    
    public ProcesadorDeStock(){
        productoRef = new ArrayList();
        productoCantidad = new ArrayList();
    }
    
    // Parsea el documento XML del Stock Diario y actuliza la DB de acuerdo a los valores obtenidos
    public void actualizarDBPrestaShop(String archivo)  throws ParserConfigurationException, SAXException, IOException, Exception {
        File xmlStockFile = new File(archivo);
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(xmlStockFile);
        
        doc.getDocumentElement().normalize();
        
        // Obtiene todos los nodos que representan un producto <Producto>
        NodeList productos = doc.getElementsByTagName("Producto");
        for(int i=0;i<productos.getLength();i++){
            NodeList tmpList = productos.item(i).getChildNodes();
            for(int j=0;j<tmpList.getLength();j++){
                if(tmpList.item(j).getNodeName().equals("#text")){
                    // No sirve este nodo
                }
                else if(tmpList.item(j).getNodeName().equals("EAN_SKU")){
                    productoRef.add(tmpList.item(j).getTextContent());
                }
                else if(tmpList.item(j).getNodeName().equals("CANTIDAD")){
                    productoCantidad.add(tmpList.item(j).getTextContent());
                }
            }
        }
        
        // Ahora se le da el formato indicado a los SKUs ej. 35172501·#·02·#·01
        // Primero se les quita el prefijo 22 a todos los sku
        for(int i=0;i<productoRef.size();i++){
            productoRef.set(i, productoRef.get(i).toString().substring(2));
        }
        
        // Ahora se le añaden los caracteres especiales
        for(int i=0;i<productoRef.size();i++){
            String tmp = productoRef.get(i).toString();
            tmp = tmp.substring(0, tmp.length()-4) + "·#·" + tmp.substring(tmp.length()-4, tmp.length()-2) + "·#·" + tmp.substring(tmp.length()-2, tmp.length());
            productoRef.set(i, tmp);
        }
        
        // Finalmente se actualiza la base de datos de PrestaShop
        String dbServer = pdtDBSettings.getProperty("server", "localhost");
                String dbUsr = pdtDBSettings.getProperty("user", "");
                Validacion val = new Validacion();
                String pwdDB = val.validaPass(pdtDBSettings.getProperty("password", "")); //"mR1ch%2B%25O92na";
                String dbNom = pdtDBSettings.getProperty("database", "");
                DBConn db = new DBConn(dbServer, dbUsr, pwdDB, dbNom);
                db.startConnection();
                
                for(int i=0;i<productoRef.size();i++){
                    String querySelect = "Select id_product, id_product_attribute\n" +
                                         "from " + tablePrefix + "product_attribute\n" +
                                         "where reference = " + "'" + productoRef.get(i).toString() + "'";
                    
                    db.getConnection().setAutoCommit(false);// Inicia la transacción
                    ResultSet updateProductSelect = db.Query(querySelect);
                    updateProductSelect.next();
                    System.out.println("Resultset: " + updateProductSelect.getString(1) + " " + updateProductSelect.getString(2));
                    String queryUpdate = "update " + tablePrefix + "stock_available\n" +
                                         "set quantity = " + Integer.parseInt(productoCantidad.get(i).toString()) + "\n" +
                                         "where id_product = " + updateProductSelect.getString(1) + " and id_product_attribute = " + updateProductSelect.getString(2);
                    
                    int updateOrderUpdate = db.updateQuery(queryUpdate);
                    MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Registros afectados por el UPDATE: " + updateOrderUpdate);
                    String queryUpdate2 = "update " + tablePrefix + "stock_available\n" +
                                         "set quantity = quantity + " + Integer.parseInt(productoCantidad.get(i).toString()) + "\n" +
                                         "where id_product = " + updateProductSelect.getString(1) + " and id_product_attribute = 0";
                    int updateOrderUpdate2 = db.updateQuery(queryUpdate2);
                    MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Registros afectados por el UPDATE: " + updateOrderUpdate2);
                    db.getConnection().commit();// Finaliza la transacción
                }
                
                // Cierra la conexión a la db
                if(db.getConnection() != null){
                    db.getConnection().close();
                }
        
        System.out.println(productoRef);
        System.out.println(productoCantidad);
    }

    @Override
    protected Void doInBackground() throws Exception {
        return null;
    }
}

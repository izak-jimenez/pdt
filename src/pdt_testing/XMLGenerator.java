/* Copyright (C) Josué Isaac Jiménez Ortiz - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Josué Isaac Jiménez Ortiz <izakjimenez@gmail.com>, September 2015
 */
package pdt_testing;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static pdt_testing.MainGUI.pdtDBSettings;
import static pdt_testing.MainGUI.pdtFTPSettings;

/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class XMLGenerator {
    private final String tablePrefix = pdtDBSettings.getProperty("prefix", "ps_pzrqs94mkk5vk6d");

    public XMLGenerator() {
    }

    // Este método recibirá datos a partir de una búsqueda en la base de datos.
    public void generateMASDA(int totalAmountOfProducts, ArrayList originalRefs, ArrayList refs, ArrayList noms, ArrayList nomTallas, ArrayList cdgTallas, ArrayList nomCols, ArrayList cdgCols) throws UnsupportedEncodingException, Exception {
        // Se llena una lista maestra que contiene las demás listas
        ArrayList<ArrayList> listaMaestra = new ArrayList<>();
        
        // Se modifican los contenidos de la lista de refs para cumplir con los requerimientos de formato en el doc XML
        ArrayList eanSku = new ArrayList();
        ArrayList codMod = new ArrayList();
        for(Object ref : refs){
            eanSku.add("22" + ref.toString());
            codMod.add(ref.toString().substring(0, ref.toString().length()-4));
        }
        
        listaMaestra.add(eanSku); // EAN SKU
        listaMaestra.add(noms); // DESCRIPCION
        listaMaestra.add(codMod); // CODIGO MODELO
        listaMaestra.add(noms); // NOMBRE MODELO
        listaMaestra.add(cdgCols); // CODIGO COLOR
        listaMaestra.add(nomCols); // NOMBRE COLOR
        listaMaestra.add(cdgTallas); // CODIGO TALLA
        listaMaestra.add(nomTallas); // NOMBRE TALLA

        // Identificadores para los detalles de productos.
        String ean_sku_str = "EAN_SKU";
        String descStr = "DESCRIPCION";
        String cdgFamStr = "CODIGO_FAMILIA"; // NO SE USA
        String nomFamStr = "NOMBRE_FAMILIA"; // NO SE USA
        String cdgModStr = "CODIGO_MODELO";
        String nomModStr = "NOMBRE_MODELO";
        String cdgColStr = "CODIGO_COLOR";
        String nomColStr = "NOMBRE_COLOR";
        String cdgTallaStr = "CODIGO_TALLA";
        String nomTallaStr = "NOMBRE_TALLA";
        String cdgTempStr = "CODIGO_TEMPORADA"; // NO SE USA
        String nomTempStr = "NOMBRE_TEMPORADA"; // NO SE USA
        try {
            // Crea un documento
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Crea la raíz del XML
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElementNS("http://www.pieldetoro.com/", "ns0:DT_MaestroArticulos");
            doc.appendChild(rootElement);

            // Cada producto se denota con una etiqueta de <Producto>
            // Cada producto tiene 8 nodos hijos
            for (int i = 0; i < totalAmountOfProducts; i++) {
                // Elementos 'Producto'.
                Element product = doc.createElement("Producto");
                rootElement.appendChild(product);

                // Se llena cada producto con los datos correspondientes.
                // EAN/SKU
                Element ean_sku = doc.createElement(ean_sku_str);
                product.appendChild(ean_sku);

                // DESCRIPCION
                Element description = doc.createElement(descStr);
                product.appendChild(description);

                /* CODIGO_FAMILIA
                Element cdgFam = doc.createElement(cdgFamStr);
                product.appendChild(cdgFam);

                // NOMBRE_FAMILIA
                Element nomFam = doc.createElement(nomFamStr);
                product.appendChild(nomFam);
                */
                
                // CODIGO_MODELO
                Element cdgMod = doc.createElement(cdgModStr);
                product.appendChild(cdgMod);

                // NOMBRE_MODELO
                Element nomMod = doc.createElement(nomModStr);
                product.appendChild(nomMod);

                // CODIGO_COLOR
                Element cdgCol = doc.createElement(cdgColStr);
                product.appendChild(cdgCol);

                // NOMBRE_COLOR
                Element nomCol = doc.createElement(nomColStr);
                product.appendChild(nomCol);

                // CODIGO_TALLA
                Element cdgTalla = doc.createElement(cdgTallaStr);
                product.appendChild(cdgTalla);

                // NOMBRE_TALLA
                Element nomTalla = doc.createElement(nomTallaStr);
                product.appendChild(nomTalla);

                /* CODIGO_TEMPORADA
                Element cdgTemp = doc.createElement(cdgTempStr);
                product.appendChild(cdgTemp);

                // NOMBRE_TEMPORADA
                Element nomTemp = doc.createElement(nomTempStr);
                product.appendChild(nomTemp);
                */
            }

            // Cada nodo se llena con los datos correspondientes.
            NodeList productList = doc.getElementsByTagName("Producto");
            for (int i = 0; i < productList.getLength(); i++) {
                Node currentNode = productList.item(i);
                NodeList productDetailsList = currentNode.getChildNodes();
                for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                    // Esta línea de codigo llena el nodo con los datos que le corrresponden
                    // desde la base de datos.
                    productDetailsList.item(j).appendChild(doc.createTextNode(listaMaestra.get(j).get(i).toString()));
                }
            }

            // Escribe el contenido a un archivo XML ubicado en el servidor FTP
            FTPObject ftpObject = new FTPObject(pdtFTPSettings.getProperty("server"), pdtFTPSettings.getProperty("port"), pdtFTPSettings.getProperty("user"), pdtFTPSettings.getProperty("password"));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            doc.setXmlStandalone(true);
            DOMSource source = new DOMSource(doc);
            
            // Se obtienen el año, mes, día, hora, minuto, segundo y milisegundos para agregarlos al nombre del archivo
            Calendar calendario = Calendar.getInstance();
            String year = String.valueOf(calendario.get(Calendar.YEAR));
            String month = String.valueOf((calendario.get(Calendar.MONTH)+1));
            if(month.length() == 1){
                month = "0" + month;
            }
            String day = String.valueOf(calendario.get(Calendar.DAY_OF_MONTH));
            if(day.length() == 1){
                day = "0" + day;
            }
            String hour = String.valueOf(calendario.get(Calendar.HOUR_OF_DAY));
            String minute = String.valueOf(calendario.get(Calendar.MINUTE));
            String second = String.valueOf(calendario.get(Calendar.SECOND));
            String ms = String.valueOf(calendario.get(Calendar.MILLISECOND));
            
            String fileName = "MASDA_PDT_" + year + month + day + "_" + hour + minute + second + "_" + ms + ".xml";
            
            StreamResult result = new StreamResult(new File("C:\\PDT\\tmp\\" + fileName));
            transformer.transform(source, result);
            
            int ftpUploadResult = ftpObject.uploadFile("C:\\PDT\\tmp\\" + fileName, "/" + MainGUI.pdtFTPSettings.getProperty("masda_folder") + "/" + fileName);
            if(ftpUploadResult == 0){
                System.out.println("El archivo se subió con éxito!");
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " El archivo se subió con éxito!");
                
                // Si el archivo se subió con éxito, entonces se actualiza la base de datos con todos los nuevos productos 
                String dbServer = pdtDBSettings.getProperty("server", "localhost");
                String dbUsr = pdtDBSettings.getProperty("user", "");
                Validacion val = new Validacion();
                String pwdDB = val.validaPass(pdtDBSettings.getProperty("password", "")); //"mR1ch%2B%25O92na";
                String dbNom = pdtDBSettings.getProperty("database", "");
                DBConn db = new DBConn(dbServer, dbUsr, pwdDB, dbNom);
                db.startConnection();
                
                //String queryInsertNewProds = "";
                int contProdInsert = 0;
                
                for(int i=0;i<totalAmountOfProducts;i++){
                    System.out.println("Insertando productos en la db...");
                    String queryInsertNewProds = "insert into " + tablePrefix + "product_ctrl (reference, fch_envio, status)\n" +
                                          "values('" + originalRefs.get(i).toString() + "', CURRENT_TIMESTAMP, 0)";
                    
                    int res = db.insertQuery(queryInsertNewProds);
                    if(res>0){
                        contProdInsert++;
                    }
                }
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Se insertaron " + contProdInsert + " nuevos productos en la base de datos.");
                // Cierra la conexión a la db
                if(db.getConnection() != null){
                    db.getConnection().close();
                }
                
                if(!(new File("C:\\PDT\\procesados\\" + fileName).exists())){
                    // Ya que se subió el archivo al ftp, se mueve de tmp a procesados
                    FileUtils.moveFileToDirectory(new File("C:\\PDT\\tmp\\" + fileName), new File("C:\\PDT\\procesados\\"), true);
                }else{
                    MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Ya existe el archivo!");
                }
            }
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    // Analiza el XML del Stock Diario
    public void parseStockDiarioXML() {
        int numProductosNuevos = 0;
        //NodeList productList = doc.getElementsByTagName("Producto");
    }

    // Genera el XML de una orden
    public void generateOrder(ArrayList[] arregloMaster, int noDeOrdenes) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SQLException, UnsupportedEncodingException, Exception {
        // Identificadores para los elementos del documento XML
        String pedidoStr = "PEDIDO";
        String albaranStr = "ALBARAN";
        String fechaStr = "FECHA";
        String idClienteStr = "ID_CLIENTE";
        String clienteStr = "CLIENTE";
        String direccion1Str = "DIRECCION1";
        String direccion2Str = "DIRECCION2";
        String municipoStr = "MUNICIPIO";
        String ciudadStr = "CIUDAD";
        String estadoStr = "ESTADO";
        String paisStr = "PAIS";
        String codigoPostalStr = "CODIGO_POSTAL";
        String telefonoStr = "TELEFONO";
        String eanSkuStr = "EAN_SKU";
        String cantidadStr = "CANTIDAD";

        ArrayList sizes = arregloMaster[15];
        ArrayList<ArrayList<String>> listaProductoRef = arregloMaster[13];
        ArrayList<ArrayList<String>> listaProductoCantidad = arregloMaster[14];

        // Por cada orden que se tiene, se procesa y se genera un archivo XML
        for (int i = 0; i < noDeOrdenes; i++) {
            // Crea el documento XML
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Crea la raíz del XML
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElementNS("http://www.pieldetoro.com/", "ns0:DT_Salida");
            doc.appendChild(rootElement);

            // Crea el elemento <Salida>
            Element salida = doc.createElement("Salida");
            rootElement.appendChild(salida);

            // PEDIDO
            Element pedidoElement = doc.createElement(pedidoStr);
            salida.appendChild(pedidoElement);
            pedidoElement.appendChild(doc.createTextNode(arregloMaster[0].get(i).toString()));

            // ALBARAN
            Element albaranElement = doc.createElement(albaranStr);
            salida.appendChild(albaranElement);
            albaranElement.appendChild(doc.createTextNode(arregloMaster[0].get(i).toString()));

            // FECHA
            Element fechaElement = doc.createElement(fechaStr);
            salida.appendChild(fechaElement);
            fechaElement.appendChild(doc.createTextNode(arregloMaster[3].get(i).toString()));

            // ID_CLIENTE
            Element idClienteElement = doc.createElement(idClienteStr);
            salida.appendChild(idClienteElement);
            idClienteElement.appendChild(doc.createTextNode(arregloMaster[2].get(i).toString()));

            // CLIENTE
            Element clienteElement = doc.createElement(clienteStr);
            salida.appendChild(clienteElement);
            clienteElement.appendChild(doc.createTextNode(arregloMaster[4].get(i).toString()));

            // DIRECCION1
            Element direccion1Element = doc.createElement(direccion1Str);
            salida.appendChild(direccion1Element);
            direccion1Element.appendChild(doc.createTextNode(arregloMaster[5].get(i).toString()));

            // DIRECCION2
            Element direccion2Element = doc.createElement(direccion2Str);
            salida.appendChild(direccion2Element);
            direccion2Element.appendChild(doc.createTextNode(arregloMaster[6].get(i).toString()));

            // MUNICIPIO
            Element municipioElement = doc.createElement(municipoStr);
            salida.appendChild(municipioElement);
            municipioElement.appendChild(doc.createTextNode(arregloMaster[7].get(i).toString()));

            // CIUDAD
            Element ciudadElement = doc.createElement(ciudadStr);
            salida.appendChild(ciudadElement);
            ciudadElement.appendChild(doc.createTextNode(arregloMaster[8].get(i).toString()));

            // ESTADO
            Element estadoElement = doc.createElement(estadoStr);
            salida.appendChild(estadoElement);
            estadoElement.appendChild(doc.createTextNode(arregloMaster[9].get(i).toString()));

            // PAIS
            Element paisElement = doc.createElement(paisStr);
            salida.appendChild(paisElement);
            paisElement.appendChild(doc.createTextNode(arregloMaster[10].get(i).toString()));

            // CODIGO POSTAL
            Element codigoPostalElement = doc.createElement(codigoPostalStr);
            salida.appendChild(codigoPostalElement);
            codigoPostalElement.appendChild(doc.createTextNode(arregloMaster[11].get(i).toString()));

            // TELEFONO
            Element telefonoElement = doc.createElement(telefonoStr);
            salida.appendChild(telefonoElement);
            telefonoElement.appendChild(doc.createTextNode(arregloMaster[12].get(i).toString()));

            // Con este ciclo, se agregan los n productos que son parte de cada orden.
            for (int j = 0; j < (int) sizes.get(i); j++) {
                Element producto = doc.createElement("Producto");
                rootElement.appendChild(producto);
                
                // Crea los elementos <EAN_SKU> y <Cantidad> de cada producto asociado a la orden
                Element ean_sku = doc.createElement("EAN_SKU");
                Element cantidad = doc.createElement("CANTIDAD");
                producto.appendChild(ean_sku);
                producto.appendChild(cantidad);
                
                // Rellena los campos <EAN_SKU> y <Cantidad>
                ean_sku.appendChild(doc.createTextNode("22" + listaProductoRef.get(i).get(j).replaceAll("[#·]", "")));
                cantidad.appendChild(doc.createTextNode(listaProductoCantidad.get(i).get(j)));
            }

            // Escribe el contenido a un archivo XML ubicado en el servidor FTP
            // Se inicializa la conexión al servidor FTP
            FTPObject ftpObject = new FTPObject(pdtFTPSettings.getProperty("server"), pdtFTPSettings.getProperty("port"), pdtFTPSettings.getProperty("user"), pdtFTPSettings.getProperty("password"));
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            doc.setXmlStandalone(true);
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\PDT\\tmp\\OBDLV_PDT_" + arregloMaster[3].get(i).toString() + ".xml"));
            transformer.transform(source, result);
            
            int ftpUploadResult = ftpObject.uploadFile("C:\\PDT\\tmp\\OBDLV_PDT_" + arregloMaster[3].get(i).toString() + ".xml","/" + MainGUI.pdtFTPSettings.getProperty("orders_folder") + "/OBDLV_PDT_" + arregloMaster[3].get(i).toString() + ".xml");
            if(ftpUploadResult == 0){
                System.out.println("El archivo se subió con éxito!");
                
                // Una vez que ya se guarda el archivo XML en el servidor FTP, se actualiza su estatus y se cambia de "pagada" a "en proceso"
                String dbServer = pdtDBSettings.getProperty("server", "localhost");
                String dbUsr = pdtDBSettings.getProperty("user", "");
                Validacion val = new Validacion();
                String pwdDB = val.validaPass(pdtDBSettings.getProperty("password", "")); //"mR1ch%2B%25O92na";
                String dbNom = pdtDBSettings.getProperty("database", "");
                DBConn db = new DBConn(dbServer, dbUsr, pwdDB, dbNom);
                db.startConnection();
                
                String queryInsert = "insert into " + tablePrefix + "orders_ctrl\n" +
                               "(id_order,fecha_envio,estatus)\n" +
                               "values(" + arregloMaster[0].get(i) + ",current_date,0)";
                String queryUpdate = "update " + tablePrefix + "orders \n" +
                               "set current_state= 3\n" +
                               "where id_order= " + arregloMaster[0].get(i);
                
                db.getConnection().setAutoCommit(false);// Inicia la transacción
                int updateOrderInsert = db.updateQuery(queryInsert);
                MainGUI.logScreen.append("\nRegistros afectados por el INSERT: " + updateOrderInsert);
                int updateOrderUpdate = db.updateQuery(queryUpdate);
                MainGUI.logScreen.append("\nRegistros afectados por el UPDATE: " + updateOrderUpdate);
                db.getConnection().commit();// Finaliza la transacción
                
                // Cierra la conexión a la db
                if(db.getConnection() != null){
                    db.getConnection().close();
                }
                
                // Se mueve el archivo al directorio de archivos procesados
                if(!(new File("C:\\PDT\\procesados\\OBDLV_PDT_" + arregloMaster[3].get(i).toString() + ".xml")).exists()){
                    FileUtils.moveFileToDirectory(new File("C:\\PDT\\tmp\\OBDLV_PDT_" + arregloMaster[3].get(i).toString() + ".xml"), new File("C:\\PDT\\procesados\\"), true);
                }else{
                    MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Ya existe el archivo!");
                }
                
            }
            else{
                System.out.println("El archivo no se pudo subir!");
            }
            try {
                Files.deleteIfExists(new File("C:\\PDT\\tmp\\OBDLV_PDT_" + arregloMaster[3].get(i).toString() + ".xml").toPath());
            } catch (IOException ex) {
                Logger.getLogger(XMLGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

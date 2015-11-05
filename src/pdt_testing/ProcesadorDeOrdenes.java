/* Copyright (C) Josué Isaac Jiménez Ortiz - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Josué Isaac Jiménez Ortiz <izakjimenez@gmail.com>, September 2015
 */
package pdt_testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import static pdt_testing.MainGUI.pdtDBSettings;

/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class ProcesadorDeOrdenes extends SwingWorker<Void, String>{
    private ArrayList[] arregloMaster;
    private ArrayList idOrdenLista, idDireccionLista, idClienteLista, fechasLista, clienteNombreLista, direccion1Lista, direccion2Lista, ciudadLista, paisLista, cPLista, estadoLista, municipioLista, telefonoLista, productListsSizes;
    private ArrayList<ArrayList<String>> listaProductoRef, listaProductoCantidad;
    private Properties pdtDBSettings = new Properties();
    private final String tablePrefix = pdtDBSettings.getProperty("prefix", "ps_pzrqs94mkk5vk6d");
    
    public ProcesadorDeOrdenes() {
        try{
            File pdtDBSettingsFile = new File("C:\\PDT\\pdt_db_settings.properties");
            FileReader DBFileReader = new FileReader(pdtDBSettingsFile);
            pdtDBSettings.load(DBFileReader);
        }catch(FileNotFoundException ex){
            System.out.println("No se encontró el archivo!");
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error de entrada/salida!");
            System.out.println(ex.getMessage());
        }
    }
    
    public void procesarOrdenes(){
        ArrayList ordenes = new ArrayList();
        System.out.println( "Obteniendo órdenes de la base de datos..." );
        MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Obteniendo órdenes de la base de datos...");
        Validacion validacion = new Validacion();
                        
        // Inicia una conexión a la base de datos
        try{
            Validacion val = new Validacion();
            String dbServer = pdtDBSettings.getProperty("server", "localhost");
            String dbUsr = pdtDBSettings.getProperty("user", "");
            String pwdDB = val.validaPass(pdtDBSettings.getProperty("password", "")); //"mR1ch%2B%25O92na";
            String dbNom = pdtDBSettings.getProperty("database", "");
            DBConn db = new DBConn(dbServer, dbUsr, pwdDB, dbNom);
            db.startConnection();
            
            // Obtiene los queries requeridos para esta operación
            // Para esto, busca en los recursos de la aplicación
            
            // Este query busca las ordenes con status = 2
            /*
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("files/sql_ordenes.txt").getFile());
            StringBuilder ordenesSqlQuery = new StringBuilder();
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    ordenesSqlQuery.append(line).append("\n");
                }   
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Este query obtiene los datos de cada orden obtenida por el query anterior
            ClassLoader classLoader2 = getClass().getClassLoader();
            File file2 = new File(classLoader2.getResource("files/sql_ordenes_datos_cliente.txt").getFile());
            StringBuilder ordenesDatosSqlQuery = new StringBuilder();
            try (Scanner scanner = new Scanner(file2)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    ordenesDatosSqlQuery.append(line).append("\n");
                }   
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Este query obtiene todos los productos relacionados con cada orden
            ClassLoader classLoader3 = getClass().getClassLoader();
            File file3 = new File(classLoader3.getResource("files/sql_productos_orden.txt").getFile());
            StringBuilder ordenesProductosSqlQuery = new StringBuilder();
            try (Scanner scanner = new Scanner(file3)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    ordenesProductosSqlQuery.append(line).append("\n");
                }   
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
                    
            // Se obtienen los resultados que devuelven los queries anteriores.
            String query1 = "Select * from " + tablePrefix + "orders orders\n" +
                            "where not exists (select * from " + tablePrefix + "orders_ctrl control\n" +
                            "where orders.id_order = control.id_order)\n" +
                            "and orders.current_state = 2";
            ResultSet resultSetOrdenes = db.Query(query1);
            int noDeOrdenes = 0;
                    
            // Se almacena el id de orden, id de dirección, id de cliente y la fecha de cada orden, más algunos datos del cliente
            idOrdenLista = new ArrayList();
            idDireccionLista = new ArrayList();
            idClienteLista = new ArrayList();
            fechasLista = new ArrayList();
            clienteNombreLista = new ArrayList();
            direccion1Lista = new ArrayList();
            direccion2Lista = new ArrayList();
            municipioLista = new ArrayList();
            ciudadLista = new ArrayList();
            estadoLista = new ArrayList();
            paisLista = new ArrayList();
            cPLista = new ArrayList();
            telefonoLista = new ArrayList();
            listaProductoRef = new ArrayList<>();
            listaProductoCantidad = new ArrayList<>();
            productListsSizes = new ArrayList();
            
            // Llena las primeras 4 listas con los datos del primer query
            while(resultSetOrdenes.next()){
                noDeOrdenes++;
                idOrdenLista.add(resultSetOrdenes.getString(1));
                idDireccionLista.add(resultSetOrdenes.getString(10));
                idClienteLista.add(resultSetOrdenes.getString(7));
                
                // Se le da formato a la fecha antes de insertarla
                String tempStr = resultSetOrdenes.getString(43).replaceAll("[-:.]", "").replaceAll(" ", "");
                tempStr = tempStr.substring(0, 8) + "_" + tempStr.substring(8, 14) + "_" + tempStr.substring(14);
                fechasLista.add(tempStr);
            }
            
            // Obtiene los datos de cliente de cada orden
            for(int i=0;i<noDeOrdenes;i++){
                String query2 = "Select address.firstname, address.lastname, address.address1, address.address2,\n" +
                                "address.city, country.name, address.postcode, address.phone\n" +
                                "from " + tablePrefix + "address address , " + tablePrefix + "country_lang country\n" +
                                "where address.id_address = ? and\n" +
                                "address.id_country = country.id_country and\n" +
                                "country.id_lang = 3";
                ResultSet resultSetOrdenesDatos = db.ParametrizedQuery(query2, idDireccionLista.get(i).toString());
                
                // Por cada orden, se recibe un registro de datos del cliente que realizó la orden
                while(resultSetOrdenesDatos.next()){
                    clienteNombreLista.add(resultSetOrdenesDatos.getString(1) + " " + resultSetOrdenesDatos.getString(2));
                    direccion1Lista.add(resultSetOrdenesDatos.getString(3));
                    direccion2Lista.add(resultSetOrdenesDatos.getString(4));
                    municipioLista.add(resultSetOrdenesDatos.getString(5));
                    ciudadLista.add(resultSetOrdenesDatos.getString(5));
                    estadoLista.add(resultSetOrdenesDatos.getString(5));
                    paisLista.add(resultSetOrdenesDatos.getString(6));
                    cPLista.add(resultSetOrdenesDatos.getString(7));
                    telefonoLista.add(resultSetOrdenesDatos.getString(8));
                }
            }
            
            // Se almacenan todos los productos correspondientes a cada orden
            for(int i=0;i<noDeOrdenes;i++){
                String query3 = "Select product_reference, product_quantity from " + tablePrefix + "order_detail\n" +
                                "where id_order = ?";
                ResultSet resultSetProductosOrden = db.ParametrizedQuery(query3, idOrdenLista.get(i).toString());
                ArrayList<String> listaProductosRef = new ArrayList<>();
                ArrayList<String> listaProductosCantidad = new ArrayList<>();
                
                // Por cada orden, se recibe un registro que contiene todos los productos relacionados a esta.
                while(resultSetProductosOrden.next()){
                    listaProductosRef.add(resultSetProductosOrden.getString(1));
                    listaProductosCantidad.add(resultSetProductosOrden.getString(2));
                }
                listaProductoRef.add(listaProductosRef);
                listaProductoCantidad.add(listaProductosCantidad);
            }
            
            // Imprime los datos de la sección de <Salida> de cada orden
            for(int i=0;i<noDeOrdenes;i++){
                System.out.print(idOrdenLista.get(i) + ", ");
                System.out.print(idDireccionLista.get(i) + ", ");
                System.out.print(idClienteLista.get(i) + ", ");
                System.out.print(fechasLista.get(i) + ", ");
                System.out.print(clienteNombreLista.get(i) + ", ");
                System.out.print(direccion1Lista.get(i) + ", ");
                System.out.print(direccion2Lista.get(i) + ", ");
                System.out.print(municipioLista.get(i) + ", ");
                System.out.print(ciudadLista.get(i) + ", ");
                System.out.print(estadoLista.get(i) + ", ");
                System.out.print(paisLista.get(i) + ", ");
                System.out.print(cPLista.get(i) + ", ");
                System.out.println(telefonoLista.get(i));
            }
            
            // Imprime los datos de cada producto de cada orden
            // Aquí también se almacenan los tamaños de cada lista para usarlos al momento de crear los elementos <Productos> en el XML
            for(int i=0;i<noDeOrdenes;i++){
                productListsSizes.add(listaProductoRef.get(i).size());
                System.out.println("***Ref. de Productos de la orden no. " + i + "***");
                for(int j=0;j<listaProductoRef.get(i).size();j++){
                    System.out.println(listaProductoRef.get(i).get(j));
                }
                
                System.out.println("***Cantidad. de cada Producto de la orden no. " + i + "***");
                for(int j=0;j<listaProductoCantidad.get(i).size();j++){
                    System.out.println(listaProductoCantidad.get(i).get(j));
                }
            }
            
            // Almacena todos los datos obtenidos en un arreglo master
            arregloMaster = new ArrayList[16];
            arregloMaster[0] = idOrdenLista;
            arregloMaster[1] = idDireccionLista;
            arregloMaster[2] = idClienteLista;
            arregloMaster[3] = fechasLista;
            arregloMaster[4] = clienteNombreLista;
            arregloMaster[5] = direccion1Lista;
            arregloMaster[6] = direccion2Lista;
            arregloMaster[7] = municipioLista;
            arregloMaster[8] = ciudadLista;
            arregloMaster[9] = estadoLista;
            arregloMaster[10] = paisLista;
            arregloMaster[11] = cPLista;
            arregloMaster[12] = telefonoLista;
            arregloMaster[13] = listaProductoRef;
            arregloMaster[14] = listaProductoCantidad;
            arregloMaster[15] = productListsSizes;
            
            // Crea los archivos XML correspondientes a cada orden.
            XMLGenerator generadorXML = new XMLGenerator();
            generadorXML.generateOrder(arregloMaster, noDeOrdenes);
            
            if(db.getConnection() != null){
                db.closeConnection();
            }
            
        }catch(UnsupportedEncodingException ex){
            System.out.println("El cifrado no es soportado por la aplicación!");
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " El cifrado no es soportado por la aplicación!");
            System.out.println(ex.getMessage());
        }
        catch(SQLException ex){
            System.out.println("Error de conexión a la base de datos!");
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " " + ex.getMessage());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ProcesadorDeOrdenes.class.getName()).log(Level.SEVERE, null, ex);
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " " + ex.getMessage());
        } catch (TransformerException ex) {
            Logger.getLogger(ProcesadorDeOrdenes.class.getName()).log(Level.SEVERE, null, ex);
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(ProcesadorDeOrdenes.class.getName()).log(Level.SEVERE, null, ex);
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " " + ex.getMessage());
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        procesarOrdenes();
        return null;
    }
    
    @Override
    protected void done(){
        MainGUI.loadingIcon.setVisible(false);
    }
}

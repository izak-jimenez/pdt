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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class ProcesadorDeMasda extends SwingWorker<Void, String>{
    private Properties pdtDBSettings = new Properties();
    private static ArrayList listaRefMod, listaRef, listaIdProd, listaDesc, listaColoresNom, listaTallaNom, listaColoresCdg, listaTallaCdg, listaNomDesc, listaRefOriginal;
    private final String tablePrefix = pdtDBSettings.getProperty("prefix", "ps_pzrqs94mkk5vk6d");
    
    public ProcesadorDeMasda() throws FileNotFoundException {
        listaRef = new ArrayList<>();
        listaIdProd = new ArrayList<>();
        listaDesc = new ArrayList<>();
        listaColoresNom = new ArrayList<>();
        listaTallaNom = new ArrayList<>();
        listaColoresCdg = new ArrayList<>();
        listaTallaCdg = new ArrayList<>();
        listaNomDesc = new ArrayList<>();
        listaRefMod = new ArrayList<>();
        listaRefOriginal = new ArrayList<>();
        
        File pdtDBSettingsFile = new File("C:\\PDT\\pdt_db_settings.properties");
        FileReader DBFileReader = new FileReader(pdtDBSettingsFile);
        try {
            pdtDBSettings.load(DBFileReader);
        } catch (IOException ex) {
            Logger.getLogger(ProcesadorDeMasda.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected Void doInBackground(){
        try {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            
            // Ejecuta el proceso en un hilo nuevo para prevenir que se congele el GUI
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Obteniendo productos nuevos de la base de datos...");
            Validacion val = new Validacion();
            String dbServer = pdtDBSettings.getProperty("server", "localhost");
            String dbUsr = pdtDBSettings.getProperty("user", "");
            String pwdDB = val.validaPass(pdtDBSettings.getProperty("password", "")); //"mR1ch%2B%25O92na";
            String dbNom = pdtDBSettings.getProperty("database", "");
            DBConn db = new DBConn(dbServer, dbUsr, pwdDB, dbNom);
            db.startConnection();
            
            // Query para obtener los productos nuevos de la base de datos
            // Por alguna razón este query deja de funcionar después de la primera vez que se genera el MASDA
            String query1 = "Select a.reference, a.id_product, b.name from ps_pdt_product_attribute a, ps_pdt_product_lang b\n" +
                    "where not exists (select * from ps_pdt_product_ctrl) and\n" +
                    "a.id_product = b.id_product and b.id_lang= 3";
            
            String query2 = "Select a.reference, a.id_product, c.name from " + tablePrefix + "product_attribute a, " + tablePrefix + "product_lang c\n" +
                    "where a.reference not in (select b.reference from " + tablePrefix + "product_ctrl b)\n" +
                    "and a.id_product = c.id_product and c.id_lang= 3";
            
            ResultSet resultSetProductosNuevos = db.Query(query2);
            int numProdNuevos = 0;
            
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Procesando todos los productos nuevos");
            while(resultSetProductosNuevos.next()){
                numProdNuevos++;
                String currentRefStr = resultSetProductosNuevos.getString(1).replaceAll("[#·]", "");
                int currentRefLength = currentRefStr.length();
                listaRefOriginal.add(resultSetProductosNuevos.getString(1));
                listaRefMod.add(currentRefStr.substring(0, currentRefLength-4));
                listaRef.add(currentRefStr);
                listaIdProd.add(resultSetProductosNuevos.getString(2));
                listaDesc.add(resultSetProductosNuevos.getString(3));
                listaNomDesc.add(resultSetProductosNuevos.getString(3).replaceAll("[\\d-]", "").trim());
                listaColoresCdg.add(currentRefStr.substring(currentRefLength-2, currentRefLength));
                listaTallaCdg.add(currentRefStr.substring(currentRefLength-4, currentRefLength-2));
            }
            //System.out.println(numProdNuevos);
            // Ya que se tienen los códigos de referencia, se obtienen los strings de colores y tallas
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Procesando tallas y colores...");
            for(int i=0;i<numProdNuevos;i++){
                String stringTmp = listaRef.get(i).toString();
                if(stringTmp.length() != 0){
                    //System.out.println(stringTmp.substring(stringTmp.length()-4, stringTmp.length()-2) + " " + stringTmp.substring(stringTmp.length()-2, stringTmp.length()));
                    String queryTalla = "Select nombre from " + tablePrefix + "product_talla \n" +
                            "where numero=" + stringTmp.substring(stringTmp.length()-4, stringTmp.length()-2);
                    
                    String queryColor = "Select nombre from " + tablePrefix + "product_color\n" +
                            "where numero=" + stringTmp.substring(stringTmp.length()-2, stringTmp.length());
                    
                    ResultSet resultSetTalla = db.Query(queryTalla);
                    ResultSet resultSetColor = db.Query(queryColor);
                    
                    // Si el registro está vacío, simplemente le deja el código de la talla
                    if(resultSetTalla.next()){
                        resultSetTalla.beforeFirst();
                        while(resultSetTalla.next()){
                            listaTallaNom.add(resultSetTalla.getString(1));
                        }
                    }else{
                        listaTallaNom.add(stringTmp.substring(stringTmp.length()-4, stringTmp.length()-2));
                    }
                    
                    // Si el registro está vacío simplemente le deja el código del color
                    if(resultSetColor.next()){
                        resultSetColor.beforeFirst();
                        while(resultSetColor.next()){
                            listaColoresNom.add(resultSetColor.getString(1));
                        }
                    }else{
                        listaColoresNom.add(stringTmp.substring(stringTmp.length()-2, stringTmp.length()));
                    }
                }
            }
            //System.out.println(listaTallaNom);
            //System.out.println(listaColoresNom);
            //System.out.println(listaRef);
            //System.out.println(listaIdProd);
            //System.out.println(listaDesc);
            
            // Genera el documento XML con todos los nuevos productos
            XMLGenerator masdaGen = new XMLGenerator();
            masdaGen.generateMASDA(numProdNuevos, listaRefOriginal, listaRef, listaNomDesc, listaTallaNom, listaTallaCdg, listaColoresNom, listaColoresCdg);
            
            if(db.getConnection() != null){
                db.closeConnection();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProcesadorDeMasda.class.getName()).log(Level.SEVERE, null, ex);
            MainGUI.logScreen.append("\n" + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(ProcesadorDeMasda.class.getName()).log(Level.SEVERE, null, ex);
            MainGUI.logScreen.append("\n" + ex.getMessage());
        }
        return null;
    }
    
    @Override
    protected  void done(){
        MainGUI.loadingIcon.setVisible(false);
    }
}

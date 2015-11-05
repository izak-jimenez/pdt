/* Copyright (C) Josué Isaac Jiménez Ortiz - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Josué Isaac Jiménez Ortiz <izakjimenez@gmail.com>, September 2015
 */
package pdt_testing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class Validacion {

    public Validacion() {
    }
    
    public String validaPass(String string) throws UnsupportedEncodingException{
        // Codifica un string usando el estándar UTF-8
        String utf8EncodedString = URLEncoder.encode(string, "UTF-8");
        return utf8EncodedString;
    }
    
    public boolean validaNombreArchivoStockDiario(String nombreArchivo){
        boolean val1 = false;
        boolean val2 = false;
        boolean val3 = false;
        boolean val4 = false;
        boolean estadoValidacion = false;
        String regexPrefix = "STOCK_PDT_";
        String regexDate = "^\\d{4}\\/(0?[1-9]|1[012])\\/(0?[1-9]|[12][0-9]|3[01])_$";
        String regexTime = "^(?:(?:([01]?\\d|2[0-3]))?([0-5]?\\d))?([0-5]?\\d)_$";
        String regexMilliSec = "^\\d{3}";
        // Verifica que la lognitud de la cadena cumpla con la medida establecida de 29 caracteres (sin contar la extensión)
        if(nombreArchivo.substring(0, nombreArchivo.lastIndexOf('.')).length() == 29){
            // Divide el nobre en partes
            String prefix = nombreArchivo.substring(0,10);
            String date = nombreArchivo.substring(10,19);
            String time = nombreArchivo.substring(19,26);
            String millisec = nombreArchivo.substring(26,29);
            
            // Valida cada parte con una expresión regular
            val1 = prefix.matches(regexPrefix);
            
            // Para validar la fecha no se utiliza un regex. utiliza una función que aparte de validar el formato,
            // valida años bisiestos.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setLenient(false);
            try {
                sdf.parse(date);
                val2 = true;
            } catch (ParseException e) {
                val2 = false;
                System.out.println("Esa fecha no existe!");
            }
            
            // Valida cada parte con una expresión regular
            val3 = time.matches(regexTime);
            val4 = millisec.matches(regexMilliSec);
            
            System.out.print(prefix);
            System.out.println(" " + val1);
            System.out.print(date);
            System.out.println(" " + val2);
            System.out.print(time);
            System.out.println(" " + val3);
            System.out.print(millisec); 
            System.out.println(" " + val4);
        }
        else{
            System.out.println("El nombre del archivo no cumple con la longitud requerida!");
        }
        
        if(val1 && val2 && val3 && val4){
            estadoValidacion = true;
        }
        return estadoValidacion;
    }
}

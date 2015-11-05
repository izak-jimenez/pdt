/* Copyright (C) Josué Isaac Jiménez Ortiz - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Josué Isaac Jiménez Ortiz <izakjimenez@gmail.com>, September 2015
 */
package pdt_testing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import static pdt_testing.MainGUI.logScreen;
/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class FTPObject {
    String server, user, pass, downloadFromPath, downloadToPath, uploadFromPath, uploadToPath;
    String port;
    private FTPClient ftpClient;

    public FTPObject(String server, String port, String user, String pass) {
        ftpClient = new FTPClient();
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.downloadFromPath = "/downloads/STOCK_PDT_20150727_131700_043.xml";
        this.downloadToPath = "C:\\Users\\r\\Desktop\\test_ftp_folder\\file.xml";
        this.uploadFromPath = "C:\\Users\\r\\Desktop\\powershell_cmd_sql_servers.txt";
        this.uploadToPath = "/uploads/archivo.txt";
    }
    
    public int downloadFile(String downloadFromPath, String downloadToPath){
        int exitCode = 98; // Si regresa 0 todo salió bien
      
        try {
            ftpClient.connect(server, Integer.parseInt(port));
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteFile1 = downloadFromPath;
            File downloadFile1 = new File(downloadToPath);
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();

            if (success) {
                exitCode = 0;
            }
        } catch (NumberFormatException | IOException ex) {
            logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
        }finally{
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (Exception ex) {
                logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
            }
        }
        return exitCode;
    }
    
    public int uploadFile(String uploadFromPath, String uploadToPath){
        int exitCode = 99;
        
        try {
            ftpClient.connect(server, Integer.parseInt(port));
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            File firstLocalFile = new File(uploadFromPath);
            String firstRemoteFile = uploadToPath;
            InputStream inputStream = new FileInputStream(firstLocalFile);
            MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Iniciando proceso de subida al servidor FTP...");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                exitCode = 0;
            }
        } catch (NumberFormatException | IOException ex) {
            logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
        }
        return exitCode;
    }
    
    public int deleteFile(String folderName, String fileName){
        int exitCode = -99;
        
        try {
            ftpClient.connect(server, Integer.parseInt(port));
            ftpClient.login(user, pass);
            boolean done = ftpClient.deleteFile("/" + folderName + "/" + fileName);
            if(done){
                exitCode = 0;
            }
            ftpClient.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(FTPObject.class.getName()).log(Level.SEVERE, null, ex);
            logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
        }
        
        return exitCode;
    }
    
    public int deleteMultipleFiles(String folderName, ArrayList<File> listaArchivos){
        int exitCode = -99;
        boolean done = false;
        
        try {
            ftpClient.connect(server, Integer.parseInt(port));
            ftpClient.login(user, pass);
            
            for(File f : listaArchivos){
                done = ftpClient.deleteFile("/" + folderName + "/" + f.getName());
            }
            
            //boolean done = client.deleteFile("/downloads/" + fileName);
            if(done){
                exitCode = 0;
            }
            ftpClient.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(FTPObject.class.getName()).log(Level.SEVERE, null, ex);
            logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
        }
        
        return exitCode;
    }
    
    public ArrayList getStockDiarioFile(){
        String fileName = "";
        ArrayList resultSet = new ArrayList();
        int exitCode = -99;
        
        try {
            ftpClient.connect(server, Integer.parseInt(port));
            boolean loggedIn = ftpClient.login(user, pass);
            
            if(loggedIn){
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Conexión establecida al servidor FTP...");
                // Se obtienen todos los archivos del directorio actual y se almacenan en un arreglo.
                FTPFile[] archivosFTP = ftpClient.listFiles("/" + MainGUI.pdtFTPSettings.getProperty("stock_folder") + "/");
                for (FTPFile file : archivosFTP) {
                    System.out.println(file.getName());
                    fileName = file.getName();
                    if(file.getName().contains("STOCK_PDT")){
                        FTPObject tempFTPObj = new  FTPObject(server, port, user, pass);
                        exitCode = tempFTPObj.downloadFile("/" + MainGUI.pdtFTPSettings.getProperty("stock_folder") + "/" + file.getName(), "C:\\PDT\\tmp\\" + file.getName());
                        tempFTPObj.dc();
                    }
                }
            }
        } catch (NumberFormatException | IOException ex) {
            logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
        }
        resultSet.add(exitCode);
        resultSet.add(fileName);
        return resultSet;
    }
    
    public ArrayList<FTPFile> getEnviosFile(){
        ArrayList<FTPFile> ordenesDeEnvio = new ArrayList<>();
        
        try {
            ftpClient.connect(server, Integer.parseInt(port));
            boolean loggedIn = ftpClient.login(user, pass);
            System.out.println(loggedIn + "Me conecté!");
            if(loggedIn){
                MainGUI.logScreen.append("\n" + new Timestamp(new Date().getTime()) + " Conexión establecida al servidor FTP...");
                // Se obtienen todos los archivos del directorio actual y se almacenan en un arreglo.
                FTPFile[] archivosFTP = ftpClient.listFiles("/" + MainGUI.pdtFTPSettings.getProperty("shipping_folder") + "/");
                //System.out.println("NOMBRE" + archivosFTP[2].getName());
                for (FTPFile file : archivosFTP) {
                    System.out.println(file.getName());
                    //fileName = file.getName().trim();
                    if(file.getName().contains("OBDLV_CONF")){
                        ordenesDeEnvio.add(file);
                        FTPObject tempFTPObj = new  FTPObject(server, port, user, pass);
                        tempFTPObj.downloadFile("/" + MainGUI.pdtFTPSettings.getProperty("shipping_folder") + "/" + file.getName(), "C:\\PDT\\tmp\\" + file.getName());
                        tempFTPObj.dc();
                    }
                }
            }
        } catch (NumberFormatException | IOException ex) {
            logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
        }
        return ordenesDeEnvio;
    }
    
    public void dc(){
        try {
            ftpClient.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(FTPObject.class.getName()).log(Level.SEVERE, null, ex);
            logScreen.append("\n" + new Timestamp(new Date().getTime()) + ex.getMessage());
        }
    }
}

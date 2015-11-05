/* Copyright (C) Josué Isaac Jiménez Ortiz - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Josué Isaac Jiménez Ortiz <izakjimenez@gmail.com>, September 2015
 */
package pdt_testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 *
 * @author Josué Isaac Jiménez Ortiz
 */
public class DBConn {
    private Connection conn;
    private PreparedStatement prepStmt;
    private String server, username, password, database;
    
    public DBConn(String server, String usernanme, String password, String database) {
        this.server = server;
        this.username = usernanme;
        this.password = password;
        this.database = database;
    }
    
    public void startConnection() throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database + "?user=" + username + "&password=" + password);
    }
    
    public void closeConnection() throws SQLException{
        conn.close();
    }
    
    public ResultSet Query(String sql) throws SQLException{
        if (conn != null) {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            return resultSet;
        }else{
            return null;
        }
    }
    
    public ResultSet ParametrizedQuery(String sqlQuery, String param) throws SQLException{
        if(conn != null){
            PreparedStatement pStmt = conn.prepareStatement(sqlQuery);
            pStmt.setString(1, param);
            ResultSet resultSet = pStmt.executeQuery();
            return  resultSet;
        }
        else{
            return null;
        }
    }
    
    public int updateQuery(String sqlQuery) throws SQLException{
        if(conn != null){
            PreparedStatement pStm = conn.prepareStatement(sqlQuery);
            return pStm.executeUpdate();
        }
        else{
            return 0;
        }
    }
    
    public int insertQuery(String query) throws SQLException{
        if(conn != null){
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(query);
        }
        else{
            return 0;
        }
    }
    
    public Connection getConnection(){
        return conn;
    }
}

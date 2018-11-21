package edu.colostate.cs.cs414.StringCheese.src;

import java.sql.*;
import java.util.*;


public class DBConnection {
    static Connection con;

    public static Connection open(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            con=DriverManager.getConnection("jdbc:mysql://localhost:3306/rollerball","root","");
            //here rollerball is database name, root is username and password stringcheese
            //con=DriverManager.getConnection(
             //       "jdbc:mysql://rollerballinstance.cds1ypryhrl5.us-east-1.rds.amazonaws.com/rollerball",
             //       "root", "stringcheese");
            /*
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from emp");
            while(rs.next())
                System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
            con.close();
            */
        }catch(Exception e){
            System.out.println(e);
            System.exit(1);
        }
        return con;
    }


    public static List<Map<String, Object>> map(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        try {
            if (rs != null) {
                ResultSetMetaData meta = rs.getMetaData();
                int numColumns = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<String, Object>();
                    for (int i = 1; i <= numColumns; ++i) {
                        String name = meta.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(name, value);
                    }
                    results.add(row);
                }
            }
        } finally {
            close(rs);
        }
        return results;
    }

    public static int update(Connection connection, String sql, List<Object> parameters) throws SQLException {
        int numRowsUpdated = 0;
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);

            int i = 0;
            for (Object parameter : parameters) {
                ps.setObject(++i, parameter);
            }
            numRowsUpdated = ps.executeUpdate();
        } finally {
            close(ps);
        }
        return numRowsUpdated;
    }

    public static boolean close(Connection connection){
        try {
            if (connection != null) {
                connection.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void close(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rollback(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]){
        Connection connection = DBConnection.open();
        //System.out.println("Authenticate is "+ User.authenticate("Chris",null));
        /*
        System.out.println(User.registerUser("chris","Chris@yahoo.com","12345"));
        System.out.println(User.authenticate("chris","12345"));
      */
        User user = new User("zaira");
        System.out.println("deactivated is " + user.deactivate());
        System.out.println(user.listRegisteredUsers());

    }


}

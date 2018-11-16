package edu.colostate.cs.cs414.StringCheese.src;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.*;
import java.util.Arrays;



public class User {

    String name, email;
    static Statement stmt;
    static Connection conn;

    public User(String nickname, String email){
        this.name = nickname;
        this.email = email;
    }

    public static boolean authenticate(String name, String password){

        try {
            conn = DBConnection.open();
            stmt = conn.createStatement();
            String query = "SELECT password, salt FROM user WHERE name='" + name+"'";
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                byte[] hashedPass = rs.getBytes("password");
                byte[] salt = rs.getBytes("salt");
                byte[] encryptedAttemptedPassword = getEncryptedPassword(password, salt);
                boolean isEqual = Arrays.equals(encryptedAttemptedPassword, hashedPass);
                DBConnection.close(conn);
                System.out.println("isEqual: " + isEqual);
                return isEqual;
            }
            else{
                System.out.println("No Results Found");
                System.exit(1);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }



    public static boolean registerUser(String name, String email, String password){
        //email syntax check
        EmailValidator emailValidator = new EmailValidator();
        boolean isValidEmail = emailValidator.validateEmail(email);
        //validate name and password length
        if(isValidEmail && name.length()>=5 && password.length()>=5) {
            try {
                conn = DBConnection.open();
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO user (name, email, password, salt ) VALUES (?,?,?,?)");
                byte[] salt = generateSalt();
                byte[] hashedPassword = getEncryptedPassword(password, salt);
                String lowCaseName = name.toLowerCase();
                pstmt.setString(1,lowCaseName);
                pstmt.setString(2,email);
                ByteArrayInputStream bInput = new ByteArrayInputStream(hashedPassword);
                pstmt.setBinaryStream(3, bInput);
                bInput.close();
                ByteArrayInputStream bInput2 = new ByteArrayInputStream(salt);
                pstmt.setBinaryStream(4, bInput2);
                bInput2.close();

                try {
                    int numRecordsInserted = pstmt.executeUpdate();
                    //System.out.println(numRecordsInserted);
                    DBConnection.close(conn);
                    //validate database has new user
                    return numRecordsInserted==1;
                }catch(SQLIntegrityConstraintViolationException e){
                    System.out.println("This name or email is already registered, try again");
                    System.exit(1);
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }else{
            System.out.println("Something went wrong");
            System.out.println("Check email is valid. Nickname and Password length must be at least 5 characters");
            System.exit(1);
        }
        return false;
    }
    //validate database has new user
    //FIXME: has bugs BUT NOT SURE IF WE EVEN NEED THIS METHOD
    private static boolean isInDatabase(String name) {
        try {
            conn = DBConnection.open();
            stmt = conn.createStatement();
            String query = "SELECT COUNT(*) FROM (SELECT * FROM user WHERE name='"+name.toLowerCase()+"') AS T";
            ResultSet rs = stmt.executeQuery(query);
            int size = rs.getInt(1);
            DBConnection.close(conn);
            return size==1;
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }
    public static byte[] getEncryptedPassword(String password, byte[] salt) {
        // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
        // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
        String algorithm = "PBKDF2WithHmacSHA1";
        // SHA-1 generates 160 bit hashes, so that's what makes sense here
        int derivedKeyLength = 160;
        // The NIST recommends at least 1,000 iterations:
        // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
        int iterations = 20000;
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);
        SecretKeyFactory f = null;
        try {
            f = SecretKeyFactory.getInstance(algorithm);
            if (f==null) {System.exit(1);}
            return f.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return new byte[0];
    }
    public static byte[] generateSalt()  {
        // VERY important to use SecureRandom instead of just Random
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        return salt;
    }
}
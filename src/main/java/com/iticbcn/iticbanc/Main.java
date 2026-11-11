package com.iticbcn.iticbanc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Main {
    public static void main(String[] args) {
        HikariConfig hkcfg = new HikariConfig();
        // Parametria de la connexió
        hkcfg.setJdbcUrl("jdbc:postgresql://localhost:5432/Iticbanc");
        hkcfg.setUsername("M06ACTRA2");
        hkcfg.setPassword("ssshhh..itsasecret!");
        hkcfg.setMaximumPoolSize(5);

        // Iniciem el datasource i controlem excepcions
        try (HikariDataSource hkds = new HikariDataSource(hkcfg)) {
            try (Connection cnn = hkds.getConnection()) {
                System.out.println("Connectat!");

                // --- EXERCICI 2: Demanar dades per pantalla i inserir client ---
                Scanner scanner = new Scanner(System.in);
                System.out.println("\n--- INSERCIÓ DE NOU CLIENT ---");
                
                System.out.print("Introdueix el nom: ");
                String nom = scanner.nextLine();
                
                System.out.print("Introdueix els cognoms: ");
                String cognoms = scanner.nextLine();
                
                System.out.print("Introdueix l'ID Fiscal (DNI/NIE): ");
                String idFiscal = scanner.nextLine();

                // Afegim les dades a una llista
                List<String> dadesClient = new ArrayList<>();
                dadesClient.add(nom);
                dadesClient.add(cognoms);
                dadesClient.add(idFiscal);

                // Cridem a la funció d'inserció
                insertarClient(cnn, dadesClient);

            } catch (SQLException e) {
                System.out.println("No connectat!");
                e.printStackTrace();
            }
        }
    }

    // EXERCICI 1
    public static void readStatementsByMinAmount(Connection conn, double quantitat) throws SQLException {
        String query = "SELECT * FROM statements WHERE import >= ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDouble(1, quantitat);
            
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                String header = String.join(";", 
                    meta.getColumnName(1), 
                    meta.getColumnName(3), 
                    meta.getColumnName(8), 
                    meta.getColumnName(10)
                );
                System.out.println(header);

                while (rs.next()) {
                    String resultat = String.join(";",
                        rs.getString(1),
                        rs.getString(3),
                        rs.getString(8),
                        rs.getString(10)
                    );
                    System.out.println(resultat);
                }
            }
        }
    }

    // EXERCICI 2
    public static boolean existeixClient(Connection conn, String idFiscal) throws SQLException {
        String query = "SELECT COUNT(*) FROM clients WHERE id_fiscal = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idFiscal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static void insertarClient(Connection conn, List<String> dadesClient) {
        String nom = dadesClient.get(0);
        String cognoms = dadesClient.get(1);
        String idFiscal = dadesClient.get(2);

        try {
            if (existeixClient(conn, idFiscal)) {
                System.out.println("El client amb ID Fiscal '" + idFiscal + "' ja existeix a la base de dades. Operació cancel·lada.");
                return;
            }

            conn.setAutoCommit(false);

            String insertQuery = "INSERT INTO clients (nom, cognoms, id_fiscal) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setString(1, nom);
                ps.setString(2, cognoms);
                ps.setString(3, idFiscal);

                int filesAfectades = ps.executeUpdate();

                if (filesAfectades > 0) {
                    conn.commit();
                    System.out.println("Client creat correctament a la base de dades!");
                } else {
                    conn.rollback();
                    System.out.println("No s'ha pogut inserir el client. S'ha fet rollback.");
                }
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error durant la inserció del client. S'ha realitzat un rollback: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Error en la gestió de la connexió o transacció: " + e.getMessage());
        }
    }
}
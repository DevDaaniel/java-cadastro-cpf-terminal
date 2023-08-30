package App;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CPF {
  public static void main(String[] args) {
    DatabaseManager.initializeDatabase();
    Scanner scan = new Scanner(System.in);

    int escolha1;

    do {
      System.out.println("Você tem um CPF cadastrado? 1: Sim, 2: Não, 3: Verifique para mim");
      escolha1 = scan.nextInt();

      if (escolha1 < 1 || escolha1 > 3) {
        System.out.println("Por favor insira uma opção válida!");
      }
    } while (escolha1 < 1 || escolha1 > 3);

    switch (escolha1) {
      case 1:
        String CPF;
        boolean valido;
        do {
          scan.nextLine();
          System.out.println("Insira seu CPF (SÓ NÚMEROS!):");
          CPF = scan.nextLine();
          valido = CPF.length() == 11 && VerificarCPF.isCPF(CPF);
          if (!valido) {
            System.out.println("CPF inválido! Deve conter 11 dígitos e ser válido.");
          }
        } while (!valido);
        break;
      case 2:
        scan.nextLine();
        String cadastroCPF;
        do {
          System.out.println("Cadastre o seu CPF! (INSIRA SÓ OS NÚMEROS):");
          cadastroCPF = scan.nextLine();
        } while (cadastroCPF.length() != 11);
        DatabaseManager.saveCPF(cadastroCPF);
        break;
      case 3:
        scan.nextLine();
        System.out.println("Insira o CPF para verificar:");
        String verificarCPF = scan.nextLine();
        if (DatabaseManager.cpfCadastrado(verificarCPF)) {
          System.out.println("CPF cadastrado na base de dados.");
        } else {
          System.out.println("CPF não cadastrado na base de dados.");
        }
        break;
    }
  }
}

class VerificarCPF {
  public static boolean isCPF(String CPF) {
    if (CPF.equals("00000000000")
            || CPF.equals("11111111111")
            || CPF.equals("22222222222")
            || CPF.equals("33333333333")
            || CPF.equals("44444444444")
            || CPF.equals("55555555555")
            || CPF.equals("66666666666")
            || CPF.equals("77777777777")
            || CPF.equals("88888888888")
            || CPF.equals("99999999999")
            || (CPF.length() != 11)) {
      return false;
    }

    char dig10, dig11;
    int sm, i, r, num, peso;
    try {
      sm = 0;
      peso = 10;
      for (i = 0; i < 9; i++) {
        num = (int) (CPF.charAt(i) - 48);
        sm = sm + (num * peso);
        peso = peso - 1;
      }

      r = 11 - (sm % 11);
      if ((r == 10) || (r == 11)) dig10 = '0';
      else dig10 = (char) (r + 48);

      sm = 0;
      peso = 11;
      for (i = 0; i < 10; i++) {
        num = (int) (CPF.charAt(i) - 48);
        sm = sm + (num * peso);
        peso = peso - 1;
      }

      r = 11 - (sm % 11);
      if ((r == 10) || (r == 11)) dig11 = '0';
      else dig11 = (char) (r + 48);

      return (dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10));
    } catch (InputMismatchException erro) {
      return false;
    }
  }
}

class DatabaseManager {
  private static final String DATABASE_URL = "jdbc:sqlite:database.db";

  public static void initializeDatabase() {
    try (Connection connection = DriverManager.getConnection(DATABASE_URL);
         PreparedStatement statement = connection.prepareStatement(
                 "CREATE TABLE IF NOT EXISTS cpf (" +
                         "id INTEGER PRIMARY KEY," +
                         "cpf_number VARCHAR(11) NOT NULL);")) {
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static boolean saveCPF(String cpf) {
    try (Connection connection = DriverManager.getConnection(DATABASE_URL);
         PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO cpf (cpf_number) VALUES (?);")) {
      statement.setString(1, cpf);
      int rowsAffected = statement.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean cpfCadastrado(String cpf) {
    try (Connection connection = DriverManager.getConnection(DATABASE_URL);
         PreparedStatement statement = connection.prepareStatement(
                 "SELECT COUNT(*) FROM cpf WHERE cpf_number = ?;")) {
      statement.setString(1, cpf);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getInt(1) > 0;
      }
      return false;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}
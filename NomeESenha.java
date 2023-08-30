package App;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class NomeESenha {
  public static void main(String[] args) {
    Scanner scan = new Scanner(System.in);
    CPFManager cpfManager = new CPFManager();

    System.out.println("Bem vindo!");
    System.out.println("Escolha uma opção:");
    System.out.println("1: Cadastro");
    System.out.println("2: Login");
    int escolha = scan.nextInt();

    if (escolha == 1) {
      realizarCadastro(scan, cpfManager);
    } else if (escolha == 2) {
      realizarLogin(scan, cpfManager);
    } else {
      System.out.println("Opção inválida.");
    }
  }

  private static void realizarCadastro(Scanner scan, CPFManager cpfManager) {
    scan.nextLine();
    System.out.println("Insira o seu nome:");
    String nome = scan.nextLine();

    while (nome.length() < 4) {
      System.out.println("Insira um nome com mais de 4 caracteres por favor!");
      nome = scan.nextLine();
    }

    System.out.println("======================================");
    System.out.println("Insira o seu CPF:");
    String cpf = scan.nextLine();

    while (!cpfManager.verificarCPF(cpf)) {
      System.out.println("CPF inválido! Insira um CPF válido:");
      cpf = scan.nextLine();
    }

    System.out.println("======================================");
    System.out.println("Insira a sua senha:");
    String senha = scan.nextLine();

    while (senha.equals("123456") || senha.length() < 6 || senha.equals("654321")) {
      System.out.println("Não insira senhas tão fracas! Insira uma senha válida:");
      senha = scan.nextLine();
    }

    System.out.println("======================================");
    System.out.println("Insira a sua idade:");
    int idade = scan.nextInt();

    System.out.println("Insira o valor de dinheiro que você possui:");
    double dinheiro = scan.nextDouble();

    String hashedSenha = hashSenha(senha);

    cpfManager.cadastrarUsuario(nome, cpf, idade, dinheiro, hashedSenha);

    System.out.println("Cadastro concluído com sucesso!");
  }

  private static void realizarLogin(Scanner scan, CPFManager cpfManager) {
    scan.nextLine();
    System.out.println("Insira o nome de usuário:");
    String nomeUsuario = scan.nextLine();

    System.out.println("Insira a senha:");
    String senhaUsuario = scan.nextLine();

    if (cpfManager.logarUsuario(nomeUsuario, hashSenha(senhaUsuario))) {
      System.out.println("Login bem-sucedido!");
      System.out.println("Suas informações:");
      cpfManager.exibirInformacoes(nomeUsuario);
    } else {
      System.out.println("Login falhou. Verifique o nome de usuário e senha.");
    }
  }

  private static String hashSenha(String senha) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(senha.getBytes());
      StringBuilder hexString = new StringBuilder();

      for (byte b : hash) {
        String hex = Integer.toHexString(0xFF & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }

      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Erro ao criar hash de senha", e);
    }
  }
}

class CPFManager {
  private static final String DATABASE_URL = "jdbc:sqlite:database.db";

  public boolean verificarCPF(String CPF) {
    return true;
  }

  public void cadastrarUsuario(String nome, String cpf, int idade, double dinheiro, String senha) {
    if (verificarCPF(cpf)) {
      try (Connection connection = DriverManager.getConnection(DATABASE_URL);
           PreparedStatement statement = connection.prepareStatement(
                   "INSERT INTO users (nome, cpf, idade, dinheiro, senha) VALUES (?, ?, ?, ?, ?);")) {
        statement.setString(1, nome);
        statement.setString(2, cpf);
        statement.setInt(3, idade);
        statement.setDouble(4, dinheiro);
        statement.setString(5, senha);
        statement.executeUpdate();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("CPF inválido. Não foi possível cadastrar.");
    }
  }

  public boolean logarUsuario(String nomeUsuario, String senhaHash) {
    try (Connection connection = DriverManager.getConnection(DATABASE_URL);
         PreparedStatement statement = connection.prepareStatement(
                 "SELECT * FROM users WHERE nome = ? AND senha = ?;")) {
      statement.setString(1, nomeUsuario);
      statement.setString(2, senhaHash);
      ResultSet resultSet = statement.executeQuery();
      return resultSet.next();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void exibirInformacoes(String nomeUsuario) {
    try (Connection connection = DriverManager.getConnection(DATABASE_URL);
         PreparedStatement statement = connection.prepareStatement(
                 "SELECT * FROM users WHERE nome = ?;")) {
      statement.setString(1, nomeUsuario);
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        System.out.println("Nome: " + resultSet.getString("nome"));
        System.out.println("CPF: " + resultSet.getString("cpf"));
        System.out.println("Idade: " + resultSet.getInt("idade"));
        System.out.println("Dinheiro: " + resultSet.getDouble("dinheiro"));
      } else {
        System.out.println("Usuário não encontrado.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class Shop01 extends JFrame {
    private JPanel panel1;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton logujButton;

    private JButton regButton;
    private JButton detalicButton;
    private JTextField mAmount;
    private JButton closeButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Shop01 shop01 = new Shop01();
            shop01.setVisible(true);
        });
    }

    public Shop01() {
        super("Panel logowania");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 600);

        logujButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredUsername = textField1.getText();
                char[] enteredPasswordChars = passwordField1.getPassword();
                String enteredPassword = new String(enteredPasswordChars);

                if (checkLoginCredentials(enteredUsername, enteredPassword)) {
                    int customerType = getCustomerType(enteredUsername);
                    boolean isRegularCustomer = (customerType == 1);
                    String message = isRegularCustomer ? "Zalogowano jako stały klient hurtowy!" : "Zalogowano jako klient hurtowy!";
                    JOptionPane.showMessageDialog(Shop01.this, message);
                    Produkty produkty = new Produkty(enteredUsername, isRegularCustomer);
                    produkty.setVisible(true);
                    Shop01.this.dispose();
                } else {
                    JOptionPane.showMessageDialog(Shop01.this, "Błędne dane logowania!");
                }
            }
        });

        detalicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredUsername = "Klient detaliczny";
                String amountText = mAmount.getText();

                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(Shop01.this, "Podaj poprawną kwotę pieniędzy.");
                        return;
                    }

                    try (BufferedWriter writer = new BufferedWriter(new FileWriter("detalicClient.txt"))) {
                        writer.write(amountText);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    Produkty produkty = new Produkty(enteredUsername, false);
                    produkty.setVisible(true);
                    Shop01.this.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Shop01.this, "Podaj poprawną kwotę pieniędzy.");
                }
            }
        });
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegisterClient registerClient = new RegisterClient();
                registerClient.setVisible(true);
                Shop01.this.dispose(); // Zamknij okno logowania
            }
        });
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Shop01.this.dispose();
            }
        });
    }



    private boolean checkLoginCredentials(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("loginy.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 4) {
                    String storedUsername = credentials[0].trim();
                    String storedPassword = credentials[1].trim();
                    if (storedUsername.equals(username) && storedPassword.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private int getCustomerType(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("loginy.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 4) {
                    String storedUsername = credentials[0].trim();
                    if (storedUsername.equals(username)) {
                        return Integer.parseInt(credentials[2].trim());
                    }
                }
            }
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}
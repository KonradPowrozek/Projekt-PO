import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Shop01 extends JFrame {
    private JPanel panel1;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton logujButton;
    private JButton zalogujJakoKlientDetalicznyButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Shop01 shop01 = new Shop01();
            shop01.setVisible(true);
        });
    }

    public Shop01() {
        super("Login panel");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 500);

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
                    Shop01.this.dispose();  // Zamknij okno logowania po zalogowaniu
                } else {
                    JOptionPane.showMessageDialog(Shop01.this, "Błędne dane logowania!");
                }
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
        return -1;  // Wartość domyślna w przypadku błędu
    }
}

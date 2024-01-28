import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class RegisterClient extends JFrame {

    private JPanel panel1;
    private JTextField nameReg;
    private JTextField passwReg;
    private JTextField moneyReg;
    private JCheckBox checkBox1;
    private JButton rejestrujButton;
    private JButton backButton;

    public RegisterClient() {
        super("Panel rejestracji");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 600);

        rejestrujButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = nameReg.getText();
                String password = passwReg.getText();
                String money = moneyReg.getText();

                if (username.isEmpty() || password.isEmpty() || money.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterClient.this, "Wypełnij wszystkie pola przed rejestracją.");
                    return;
                }

                try {
                    double moneyValue = Double.parseDouble(money);
                    if (moneyValue <= 0) {
                        JOptionPane.showMessageDialog(RegisterClient.this, "Podaj poprawną kwotę pieniędzy (większą od 0).");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(RegisterClient.this, "Podaj poprawną kwotę pieniędzy.");
                    return;
                }

                int customerType = checkBox1.isSelected() ? 1 : 0; // 1 dla klienta stałego hurtowego, 0 dla klienta hurtowego

                saveClientData(username, password, customerType, money);

                JOptionPane.showMessageDialog(RegisterClient.this, "Rejestracja zakończona pomyślnie!");
            }
        });

        this.setVisible(true);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Shop01 shop01 = new Shop01();
                shop01.setVisible(true);
                RegisterClient.this.dispose();
            }
        });
    }

    private void saveClientData(String username, String password, int customerType, String money) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("loginy.txt", true))) {
            writer.write(username + "," + password + "," + customerType + "," + money + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RegisterClient registerClient = new RegisterClient();
            registerClient.setVisible(true);
        });
    }
}

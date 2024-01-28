import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class AddMoney extends JFrame {
    private JTextField addMoneyLabel;
    private JButton doładujButton;
    private JButton backButton;
    private JPanel panel1;
    private String loggedInUsername;

    public AddMoney(String loggedInUsername) {
        super("Doładowanie środków");
        this.loggedInUsername = loggedInUsername;
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        doładujButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amountToAdd = Double.parseDouble(addMoneyLabel.getText());

                    if (amountToAdd <= 0) {
                        JOptionPane.showMessageDialog(null, "Podaj poprawną kwotę do doładowania (wartość musi być większa niż 0).");
                        return;
                    }

                    // Dodawanie kwoty do aktualnego salda użytkownika
                    addMoney(loggedInUsername, amountToAdd);

                    JOptionPane.showMessageDialog(null, "Pieniądze zostały doładowane.");

                    // Po doładowaniu otwórz okno Produkty
                    Produkty produkty = new Produkty(loggedInUsername, loggedInUsername.equals("Klient detaliczny"));
                    produkty.setVisible(true);
                    AddMoney.this.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Podaj poprawną kwotę do doładowania.");
                }
            }
        });
    }

    private void addMoney(String loggedInUsername, double amountToAdd) {
        try {
            String fileName = loggedInUsername.equals("Klient detaliczny") ? "detalicClient.txt" : "loginy.txt";
            File file = new File(fileName);

            // Uzyskanie blokady dla pliku
            boolean lockAcquired = false;
            int retryCount = 0;
            while (!lockAcquired && retryCount < 3) {
                try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
                     FileLock lock = channel.lock()) {
                    // Odczyt i zapis danych
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    StringBuilder inputBuffer = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        String[] data = line.split(",");
                        if (data[0].trim().equals(loggedInUsername)) {
                            double currentMoney = Double.parseDouble(data[3].trim());
                            double newMoney = currentMoney + amountToAdd;
                            line = data[0] + "," + data[1] + "," + data[2] + "," + newMoney;
                        }
                        inputBuffer.append(line).append('\n');
                    }
                    reader.close();

                    // Zapis danych z powrotem do pliku
                    FileOutputStream fileOut = new FileOutputStream(file);
                    fileOut.write(inputBuffer.toString().getBytes());
                    fileOut.close();
                    lockAcquired = true;
                } catch (IOException ex) {
                    // Błąd uzyskania blokady, spróbuj ponownie
                    retryCount++;
                    Thread.sleep(1000); // Poczekaj przed ponowną próbą
                }
            }

            if (!lockAcquired) {
                JOptionPane.showMessageDialog(null, "Nie udało się uzyskać dostępu do pliku.");
            }
        } catch (NumberFormatException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddMoney addMoney = new AddMoney("Klient detaliczny");
            addMoney.setVisible(true);
        });
    }
}

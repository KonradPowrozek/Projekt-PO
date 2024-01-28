import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Produkty extends JFrame {
    private JPanel panel1;
    private JTable table1;
    private JLabel username;
    private JButton addButton;
    private JLabel moneyLabel;
    private JButton addMoney;
    private JButton orderButton;
    private JTable cart1;
    private JLabel wartKosz;
    private boolean isRegularCustomer;
    private double money;

    public Produkty(String loggedInUsername, boolean isRegularCustomer) {
        super("Produkty");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(600, 400);

        this.isRegularCustomer = isRegularCustomer;

        // Ustawienie nazwy zalogowanego użytkownika w etykiecie
        username.setText("Zalogowany: " + loggedInUsername);

        // Pobranie danych użytkownika i ustawienie ilości pieniędzy
        loadUserData(loggedInUsername);

        // Ustawienie nagłówków kolumn
        String[] columnNames = {"ID", "Nazwa produktu", "Cena", "Ilość w magazynie"};

        // Inicjalizacja tabeli
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table1.setModel(model);

        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = table1.getColumnModel().getColumn(i);
            column.setPreferredWidth(150); // Dostosuj szerokość kolumny do potrzeb
        }

        loadProductsFromFile(model);

        moneyLabel.setText("Stan konta: " + String.format("%.2f", money) + " zł");

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    private void loadUserData(String loggedInUsername) {
        try (BufferedReader reader = new BufferedReader(new FileReader("loginy.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4 && data[0].equals(loggedInUsername)) {
                    money = Double.parseDouble(data[3]);
                    break;
                }
            }
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    private void loadProductsFromFile(DefaultTableModel model) {
        try (BufferedReader reader = new BufferedReader(new FileReader("products.txt"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    if (!isFirstLine) {
                        if (isRegularCustomer) {
                            // Zastosuj rabat dla stałego klienta hurtowego
                            double cena = Double.parseDouble(data[2]);
                            cena *= 0.77;  // 23% rabatu
                            data[2] = String.format("%.2f", cena);
                        } else {
                            // Zastosuj rabat dla klienta hurtowego (nie stałego)
                            double cena = Double.parseDouble(data[2]);
                            cena *= 0.88;  // 12% rabatu
                            data[2] = String.format("%.2f", cena);
                        }
                        model.addRow(data);
                    }
                    isFirstLine = false;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Produkty produkty = new Produkty("PrzykładowyUzytkownik", false);
            produkty.setVisible(true);
        });
    }
}

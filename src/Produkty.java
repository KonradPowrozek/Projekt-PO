import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Produkty extends JFrame {
    private JPanel panel1;
    private JTable table1;
    private JLabel username;
    private JButton addButton;
    private JLabel moneyLabel;
    private JButton addMoney;
    private JButton orderButton;
    private JTable cart1;
    private JTextField quanityLabel;
    private JButton addToCart;
    private JLabel valueCart;
    private boolean isRegularCustomer;
    private double money;

    public Produkty(String loggedInUsername, boolean isRegularCustomer) {
        super("Produkty");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 400);

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
                // Żadne komórki nie są edytowalne
                return false;
            }
        };
        table1.setModel(model);

        // Ustawienie szerokości kolumn
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = table1.getColumnModel().getColumn(i);
            column.setPreferredWidth(150); // Dostosuj szerokość kolumny do potrzeb
        }

        // Pobranie danych produktów i ustawienie ich w tabeli
        loadProductsFromFile(model);

        // Wyświetlenie ilości pieniędzy
        moneyLabel.setText("Stan konta: " + String.format("%.2f", money) + " zł");

        // Inicjalizacja tabeli koszyka
        String[] cartColumnNames = {"ID", "Nazwa produktu", "Cena", "Ilość"};
        DefaultTableModel cartModel = new DefaultTableModel(null, cartColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Żadne komórki nie są edytowalne
                return false;
            }
        };
        cart1.setModel(cartModel);

        // Czyszczenie pliku koszyka przy starcie aplikacji
        clearCartFile();

        // Obsługa przycisku dodawania do koszyka


        // Zamykanie okna
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Zapisanie koszyka do pliku przed zamknięciem aplikacji
                saveCartToFile(cartModel);
                System.exit(0);
            }
        });
        addToCart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToCart(cartModel);
                calculateCartValue(cartModel);
                updateCartValueLabel(cartModel);
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

    private void addToCart(DefaultTableModel cartModel) {
        try {
            int selectedRow = table1.getSelectedRow();

            // Check if a row is selected and if it's a valid row index
            if (selectedRow >= 0 && selectedRow < table1.getRowCount()) {
                String id = table1.getValueAt(selectedRow, 0).toString();
                String productName = table1.getValueAt(selectedRow, 1).toString();
                String price = table1.getValueAt(selectedRow, 2).toString();
                int availableQuantity = Integer.parseInt(table1.getValueAt(selectedRow, 3).toString());

                // Check if the quantity is a valid positive number
                try {
                    // If quantity field is empty, show an error
                    if (quanityLabel.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Podaj ilość produktu.");
                        return;  // Exit the method if quantity is empty
                    }

                    int quantity = Integer.parseInt(quanityLabel.getText());

                    // If quantity is less than or equal to 0, show an error
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(this, "Podaj poprawną ilość produktu (wartość musi być większa niż 0).");
                        return;  // Exit the method if quantity is not positive
                    }

                    // Check if the quantity exceeds the available quantity in stock
                    if (quantity > availableQuantity) {
                        JOptionPane.showMessageDialog(this, "Nie wystarczająca ilość produktu na stanie.");
                        return;  // Exit the method if quantity exceeds available quantity
                    }

                    // Update the available quantity in stock
                    table1.setValueAt(availableQuantity - quantity, selectedRow, 3);

                    // Check if the product is already in the cart
                    boolean productExists = false;
                    for (int i = 0; i < cartModel.getRowCount(); i++) {
                        if (id.equals(cartModel.getValueAt(i, 0).toString())) {
                            // Update the quantity if the product exists
                            int existingQuantity = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                            cartModel.setValueAt(existingQuantity + quantity, i, 3);

                            productExists = true;
                            break;
                        }
                    }

                    // If the product is not in the cart, add a new row
                    if (!productExists) {
                        Object[] rowData = {id, productName, price, quantity};
                        cartModel.addRow(rowData);
                    }

                    // Save the cart to the file
                    saveCartToFile(cartModel);

                    // Update the cart value label
                    updateCartValueLabel(cartModel);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Wybierz poprawny produkt z tabeli przed dodaniem do koszyka.");
            }
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(this, "Wystąpił błąd. Spróbuj ponownie.");
        }
    }



    private void saveCartToFile(DefaultTableModel cartModel) {
        try (PrintWriter writer = new PrintWriter("cart.txt")) {
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                for (int j = 0; j < cartModel.getColumnCount(); j++) {
                    writer.print(cartModel.getValueAt(i, j));
                    if (j < cartModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearCartFile() {
        try (PrintWriter writer = new PrintWriter("cart.txt")) {
            // Just create an empty file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Produkty produkty = new Produkty("PrzykładowyUzytkownik", false);
            produkty.setVisible(true);
        });
    }

    private double calculateCartValue(DefaultTableModel cartModel) {
        double cartValue = 0.0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            try {
                double price = Double.parseDouble(cartModel.getValueAt(i, 2).toString());
                int quantity = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                cartValue += price * quantity;
            } catch (NumberFormatException e) {

            }
        }
        return cartValue;
    }
    private void updateCartValueLabel(DefaultTableModel cartModel) {
        double cartValue = calculateCartValue(cartModel);
        valueCart.setText("Wartość koszyka: " + String.format("%.2f zł", cartValue));
    }

}
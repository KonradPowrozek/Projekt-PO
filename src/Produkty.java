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
    private JButton backButton;
    private JButton removeCart;
    private boolean isRegularCustomer;
    private double money;
    private DefaultTableModel cartModel;


    private double cartValue = 0.0;

    public Produkty(String loggedInUsername, boolean isRegularCustomer) {
        super("Produkty");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(1400, 600);

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
        loadProductsFromFile(model, loggedInUsername);

        // Wyświetlenie ilości pieniędzy
        moneyLabel.setText("Stan konta: " + String.format("%.2f", money) + " zł");

        // Inicjalizacja tabeli koszyka
        String[] cartColumnNames = {"ID", "Nazwa produktu", "Cena", "Ilość"};
        cartModel = new DefaultTableModel(null, cartColumnNames) {
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
        addToCart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToCart(cartModel);
                calculateCartValue(cartModel);
                updateCartValueLabel(cartModel);
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Otwarcie nowego okna ProductAdd
                ProductAdd productAdd = new ProductAdd(loggedInUsername, isRegularCustomer);
                productAdd.setVisible(true);
                // Zamknięcie bieżącego okna
                Produkty.this.dispose();
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement navigation logic to go back to Shop01.java
                Shop01 shop01 = new Shop01();
                shop01.setVisible(true);

                // Close the current window
                Produkty.this.dispose();
            }
        });
        removeCart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeItemsFromCart(cartModel);
                calculateCartValue(cartModel);
                updateCartValueLabel(cartModel);
            }
        });

        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double remainingBalance = money - cartValue;

                if (remainingBalance >= 0) {
                    JOptionPane.showMessageDialog(Produkty.this, "Zamówienie złożone pomyślnie! Zapłacono " + String.format("%.2f", cartValue) + " zł. Reszta: " + String.format("%.2f", remainingBalance) + " zł.");

                    // Update user's money in the file
                    updateMoneyInFile(loggedInUsername, remainingBalance);

                    // Subtract ordered quantities from product stock
                    subtractOrderedQuantitiesFromStock();

                    // Clear the cart
                    clearCartFile();
                    cartModel.setRowCount(0);
                    updateCartValueLabel(cartModel);

                    // Update the money label
                    moneyLabel.setText("Stan konta: " + String.format("%.2f", remainingBalance) + " zł");
                    money = remainingBalance;
                } else {
                    JOptionPane.showMessageDialog(Produkty.this, "Nie masz wystarczająco środków na koncie, aby złożyć zamówienie.");
                }
            }
        });
    }

    private void subtractOrderedQuantitiesFromStock() {
        try (BufferedReader reader = new BufferedReader(new FileReader("products.txt"))) {
            StringBuilder updatedData = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) { // Assuming the file format is "name,price,quantity"
                    String productName = data[0].trim();
                    int orderedQuantity = getOrderedQuantity(productName);

                    if (orderedQuantity > 0) {
                        int currentQuantity = Integer.parseInt(data[2].trim());
                        int updatedQuantity = currentQuantity - orderedQuantity;
                        data[2] = Integer.toString(updatedQuantity);
                        line = String.join(",", data);
                    }
                }
                updatedData.append(line).append("\n");
            }

            // Update the products.txt file with updated quantities
            try (PrintWriter writer = new PrintWriter("products.txt")) {
                writer.print(updatedData);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private int getOrderedQuantity(String productName) {
        int orderedQuantity = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String name = cartModel.getValueAt(i, 1).toString(); // Assuming the product name is in the second column
            int quantity = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
            if (productName.equals(name)) {
                orderedQuantity += quantity;
            }
        }
        return orderedQuantity;
    }

    private void updateMoneyInFile(String loggedInUsername, double newMoneyValue) {
        try {
            String fileName = loggedInUsername.equals("Klient detaliczny") ? "detalicClient.txt" : "loginy.txt";
            File file = new File(fileName);

            // Create a temporary file
            File tempFile = new File("temp.txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data[0].trim().equals(loggedInUsername)) {
                        if (loggedInUsername.equals("Klient detaliczny")) {
                            // Update the money value in the first column for retail customers
                            line = Double.toString(newMoneyValue);
                        } else {
                            // Update the money value in the fourth column for wholesale customers
                            data[3] = Double.toString(newMoneyValue);
                            line = String.join(",", data);
                        }
                    }
                    writer.write(line);
                    writer.newLine();
                }
            }

            // Replace the original file with the temporary file
            if (!file.delete()) {
                System.out.println("Could not delete file");
                return;
            }
            if (!tempFile.renameTo(file)) {
                System.out.println("Could not rename file");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void removeItemsFromCart(DefaultTableModel cartModel) {
        try {
            int selectedRow = cart1.getSelectedRow();

            // Sprawdź, czy wiersz jest zaznaczony i czy indeks jest prawidłowy
            if (selectedRow >= 0 && selectedRow < cart1.getRowCount()) {
                String id = cartModel.getValueAt(selectedRow, 0).toString();
                String productName = cartModel.getValueAt(selectedRow, 1).toString();
                int quantityToRemove;

                // Sprawdź, czy pole ilości nie jest puste
                if (quanityLabel.getText().isEmpty()) {
                    quantityToRemove = 1; // Jeśli puste, usuń tylko jeden produkt
                } else {
                    quantityToRemove = Integer.parseInt(quanityLabel.getText());

                    // Jeśli ilość jest mniejsza lub równa 0, pokaż błąd
                    if (quantityToRemove <= 0) {
                        JOptionPane.showMessageDialog(this, "Podaj poprawną ilość do usunięcia (wartość musi być większa niż 0).");
                        return;
                    }
                }

                // Znajdź odpowiadający produkt w table1 i zaktualizuj jego ilość
                for (int i = 0; i < table1.getRowCount(); i++) {
                    if (id.equals(table1.getValueAt(i, 0).toString())) {
                        int availableQuantity = Integer.parseInt(table1.getValueAt(i, 3).toString());
                        table1.setValueAt(availableQuantity + quantityToRemove, i, 3);
                        break; // Znaleziono produkt, nie trzeba już dalej szukać
                    }
                }

                // Aktualizuj ilość produktów w koszyku
                int currentQuantity = Integer.parseInt(cartModel.getValueAt(selectedRow, 3).toString());
                if (quantityToRemove >= currentQuantity) {
                    cartModel.removeRow(selectedRow);
                } else {
                    cartModel.setValueAt(currentQuantity - quantityToRemove, selectedRow, 3);
                }

                // Zapisz aktualny koszyk do pliku
                saveCartToFile(cartModel);

                // Aktualizuj wartość koszyka
                calculateCartValue(cartModel);
            } else {
                JOptionPane.showMessageDialog(this, "Wybierz poprawny produkt z koszyka przed usunięciem.");
            }
        } catch (NullPointerException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Wystąpił błąd. Spróbuj ponownie.");
        }
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

        if ("Klient detaliczny".equals(loggedInUsername)) {
            try (BufferedReader reader = new BufferedReader(new FileReader("detalicClient.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Usuń białe znaki i konwertuj na liczbę
                    money = Double.parseDouble(line.trim());
                    break; // Wczytujemy tylko pierwszą linię z pliku
                }
            } catch (IOException | NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadProductsFromFile(DefaultTableModel model, String loggedInUsername) {
        try (BufferedReader reader = new BufferedReader(new FileReader("products.txt"))) {
            String line;
            int id = 1; // Starting ID

            // Dodaj nagłówki kolumn do modelu
            model.addRow(new Object[]{"ID", "Nazwa Produktu", "Cena", "Ilość w magazynie"});

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) { // Assuming the file format is "name,price,quantity"
                    double cena = Double.parseDouble(data[1].replace(',', '.'));

                    if (isRegularCustomer) {
                        // Stały klient hurtowy
                        cena *= 0.77;  // 23% rabatu
                    } else if ("Klient detaliczny".equals(loggedInUsername)) {
                        // Klient detaliczny
                        // Nie stosuj rabatu
                    } else {
                        // Klient hurtowy
                        cena *= 0.90;  // 10% rabatu
                    }

                    model.addRow(new Object[]{id, data[0], String.format("%.2f", cena).replace('.', ','), data[2]});
                    id++;
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

    private double calculateCartValue(DefaultTableModel cartModel) {
        double cartValue = 0.0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            try {
                double price = Double.parseDouble(cartModel.getValueAt(i, 2).toString().replace(',', '.'));
                int quantity = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                cartValue += price * quantity;
                valueCart.setText("Wartość koszyka: " + String.format("%.2f zł", cartValue));
            } catch (NumberFormatException e) {

            }
        }
        return cartValue;
    }

    private void updateCartValueLabel(DefaultTableModel cartModel) {
        double cartValue = calculateCartValue(cartModel);
        valueCart.setText("Wartość koszyka: " + String.format("%.2f zł", cartValue));

        // Ustawienie wartości koszyka w obiekcie Produkty
        this.cartValue = cartValue;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Produkty produkty = new Produkty("Klient detaliczny", false);
            produkty.setVisible(true);
        });
    }
}

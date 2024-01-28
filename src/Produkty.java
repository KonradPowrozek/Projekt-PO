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

        username.setText("Zalogowany: " + loggedInUsername);

        loadUserData(loggedInUsername);

        String[] columnNames = {"ID", "Nazwa produktu", "Cena", "Ilość w magazynie"};

        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table1.setModel(model);

        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = table1.getColumnModel().getColumn(i);
            column.setPreferredWidth(150);
        }

        loadProductsFromFile(model, loggedInUsername);

        moneyLabel.setText("Stan konta: " + String.format("%.2f", money) + " zł");

        String[] cartColumnNames = {"ID", "Nazwa produktu", "Cena", "Ilość"};
        cartModel = new DefaultTableModel(null, cartColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cart1.setModel(cartModel);

        clearCartFile();
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
                ProductAdd productAdd = new ProductAdd(loggedInUsername, isRegularCustomer);
                productAdd.setVisible(true);
                Produkty.this.dispose();
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Shop01 shop01 = new Shop01();
                shop01.setVisible(true);

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

                    updateMoneyInFile(loggedInUsername, remainingBalance);

                    subtractOrderedQuantitiesFromStock();

                    clearCartFile();
                    cartModel.setRowCount(0);
                    updateCartValueLabel(cartModel);

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
                if (data.length == 3) {
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
            String name = cartModel.getValueAt(i, 1).toString();
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

            File tempFile = new File("temp.txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data[0].trim().equals(loggedInUsername)) {
                        if (loggedInUsername.equals("Klient detaliczny")) {
                            line = Double.toString(newMoneyValue);
                        } else {
                            data[3] = Double.toString(newMoneyValue);
                            line = String.join(",", data);
                        }
                    }
                    writer.write(line);
                    writer.newLine();
                }
            }

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

            if (selectedRow >= 0 && selectedRow < cart1.getRowCount()) {
                String id = cartModel.getValueAt(selectedRow, 0).toString();
                String productName = cartModel.getValueAt(selectedRow, 1).toString();
                int quantityToRemove;

                if (quanityLabel.getText().isEmpty()) {
                    quantityToRemove = 1;
                } else {
                    quantityToRemove = Integer.parseInt(quanityLabel.getText());

                    if (quantityToRemove <= 0) {
                        JOptionPane.showMessageDialog(this, "Podaj poprawną ilość do usunięcia (wartość musi być większa niż 0).");
                        return;
                    }
                }

                for (int i = 0; i < table1.getRowCount(); i++) {
                    if (id.equals(table1.getValueAt(i, 0).toString())) {
                        int availableQuantity = Integer.parseInt(table1.getValueAt(i, 3).toString());
                        table1.setValueAt(availableQuantity + quantityToRemove, i, 3);
                        break;
                    }
                }

                int currentQuantity = Integer.parseInt(cartModel.getValueAt(selectedRow, 3).toString());
                if (quantityToRemove >= currentQuantity) {
                    cartModel.removeRow(selectedRow);
                } else {
                    cartModel.setValueAt(currentQuantity - quantityToRemove, selectedRow, 3);
                }

                saveCartToFile(cartModel);

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
                    money = Double.parseDouble(line.trim());
                    break;
                }
            } catch (IOException | NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadProductsFromFile(DefaultTableModel model, String loggedInUsername) {
        try (BufferedReader reader = new BufferedReader(new FileReader("products.txt"))) {
            String line;
            int id = 1;

            model.addRow(new Object[]{"ID", "Nazwa Produktu", "Cena", "Ilość w magazynie"});

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    double cena = Double.parseDouble(data[1].replace(',', '.'));

                    if (isRegularCustomer) {

                        cena *= 0.77;
                    } else if ("Klient detaliczny".equals(loggedInUsername)) {

                    } else {

                        cena *= 0.90;
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

            if (selectedRow >= 0 && selectedRow < table1.getRowCount()) {
                String id = table1.getValueAt(selectedRow, 0).toString();
                String productName = table1.getValueAt(selectedRow, 1).toString();
                String price = table1.getValueAt(selectedRow, 2).toString();
                int availableQuantity = Integer.parseInt(table1.getValueAt(selectedRow, 3).toString());

                try {
                    if (quanityLabel.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Podaj ilość produktu.");
                        return;
                    }

                    int quantity = Integer.parseInt(quanityLabel.getText());

                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(this, "Podaj poprawną ilość produktu (wartość musi być większa niż 0).");
                        return;
                    }

                    if (quantity > availableQuantity) {
                        JOptionPane.showMessageDialog(this, "Nie wystarczająca ilość produktu na stanie.");
                        return;
                    }

                    table1.setValueAt(availableQuantity - quantity, selectedRow, 3);

                    boolean productExists = false;
                    for (int i = 0; i < cartModel.getRowCount(); i++) {
                        if (id.equals(cartModel.getValueAt(i, 0).toString())) {
                            int existingQuantity = Integer.parseInt(cartModel.getValueAt(i, 3).toString());
                            cartModel.setValueAt(existingQuantity + quantity, i, 3);

                            productExists = true;
                            break;
                        }
                    }

                    if (!productExists) {
                        Object[] rowData = {id, productName, price, quantity};
                        cartModel.addRow(rowData);
                    }

                    saveCartToFile(cartModel);

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

        this.cartValue = cartValue;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Produkty produkty = new Produkty("Klient detaliczny", false);
            produkty.setVisible(true);
        });
    }
}

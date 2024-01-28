import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ProductAdd extends JFrame {
    private JPanel panel1;
    private JTextField sizeLabel;
    private JTextField priceLabel;
    private JTextField nameField;
    private JButton addProduct;
    private JButton backButton;

    public ProductAdd(String loggedInUsername, boolean isRegularCustomer) {
        super("Dodawanie produktu");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 400);

        addProduct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productName = nameField.getText();
                String productPrice = priceLabel.getText();
                String productQuantity = sizeLabel.getText();

                if (productName.isEmpty() || productPrice.isEmpty() || productQuantity.isEmpty()) {
                    JOptionPane.showMessageDialog(ProductAdd.this, "Wypełnij wszystkie pola przed dodaniem produktu.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                addNewProduct(productName, productPrice, productQuantity);
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Produkty produkty = new Produkty(loggedInUsername, isRegularCustomer);
                produkty.setVisible(true);

                ProductAdd.this.dispose();
            }
        });
    }

    private void addNewProduct(String productName, String productPrice, String productQuantity) {
        try {
            double price = Double.parseDouble(productPrice);
            int quantity = Integer.parseInt(productQuantity);

            if (price <= 0 || quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Cena i ilość muszą być większe niż 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!productQuantity.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Ilość musi być liczbą całkowitą dodatnią.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String formattedPrice = String.format("%.2f", price).replace(',', '.');

            String newProductData = String.format("%s,%s,%d", productName, formattedPrice, quantity);

            try (PrintWriter writer = new PrintWriter(new FileWriter("products.txt", true))) {
                writer.println(newProductData);
                writer.flush();
            }

            JOptionPane.showMessageDialog(this, "Product added successfully!");
        } catch (NumberFormatException | IOException ex) {
            // Display error message
            JOptionPane.showMessageDialog(this, "Error adding product. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProductAdd productAdd = new ProductAdd("PrzykładowyUzytkownik", false);
            productAdd.setVisible(true);
        });
    }
}

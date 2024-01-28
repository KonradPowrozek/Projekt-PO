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
        super("Add Product");
        this.setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 400);

        addProduct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get input values from text fields
                String productName = nameField.getText();
                String productPrice = priceLabel.getText();
                String productQuantity = sizeLabel.getText();

                // Call the method to add a new product
                addNewProduct(productName, productPrice, productQuantity);
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Produkty produkty = new Produkty(loggedInUsername, isRegularCustomer);
                produkty.setVisible(true);

                // Close the current window
                ProductAdd.this.dispose();
            }
        });
    }

    private void addNewProduct(String productName, String productPrice, String productQuantity) {
        try {
            // Parse input values
            double price = Double.parseDouble(productPrice);
            int quantity = Integer.parseInt(productQuantity);

            // Format the price with a dot instead of a comma
            String formattedPrice = String.format("%.2f", price).replace(',', '.');

            // Create the new product data
            String newProductData = String.format("%s,%s,%d", productName, formattedPrice, quantity);

            // Append the new product to the products.txt file
            try (PrintWriter writer = new PrintWriter(new FileWriter("products.txt", true))) {
                writer.println(newProductData);
                writer.flush();
            }

            // Display success message
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

package saucedemotests.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import saucedemotests.core.SauceDemoBaseWebTest;
import saucedemotests.enums.TestData;

import java.util.List;

public class ProductsTests extends SauceDemoBaseWebTest {
    public final String BACKPACK_TITLE = "Sauce Labs Backpack";
    public final String SHIRT_TITLE = "Sauce Labs Bolt T-Shirt";
    public final String BIKE_LIGHT = "Sauce Labs Bike Light";

    private final By priceItems = By.className("inventory_item_price");
    private final By confirmMessage = By.className("complete-header");

    @BeforeEach
    public void beforeTest(){
        // Authenticate with Standard user
        loginPage.navigate();
        loginPage.submitLoginForm(TestData.STANDARD_USER_USERNAME.getValue(),
                TestData.STANDARD_USER_PASSWORD.getValue());
        inventoryPage.waitForPageTitle();
    }

    @Test
    public void productAddedToShoppingCart_when_addToCart(){
        // Add products to shopping cart
        inventoryPage.addProductsByTitle(BACKPACK_TITLE, SHIRT_TITLE);

        // Go to shopping cart
        inventoryPage.clickShoppingCartLink();

        // Assert Items and Totals
        var items = shoppingCartPage.getShoppingCartItems();

        Assertions.assertEquals(inventoryPage.getShoppingCartItemsNumber(),
                items.size(), "Items count not as expected");
        Assertions.assertEquals(BACKPACK_TITLE, items.get(0).getText(), "Item title not as expected");
        Assertions.assertEquals(SHIRT_TITLE, items.get(1).getText(), "Item title not as expected");
    }

    @Test
    public void userDetailsAdded_when_checkoutWithValidInformation(){
        // Add products to shopping cart
        inventoryPage.addProductsByTitle(BACKPACK_TITLE, SHIRT_TITLE);

        // Go to shopping cart
        inventoryPage.clickShoppingCartLink();

        // Go to checkout
        shoppingCartPage.clickCheckout();

        // Fill form
        checkoutYourInformationPage.fillShippingDetails("Fname", "Lname", "zip");

        // Continue
        checkoutYourInformationPage.clickContinue();

        // Assert Cart Items number
        Assertions.assertEquals(inventoryPage.getShoppingCartItemsNumber(),
                checkoutOverviewPage.getShoppingCartItems().size(), "Items count not as expected");

        // Calculate expected total cost
        List<WebElement> priceElements = driver().findElements(priceItems);
        double totalSum = priceElements.stream()
                .mapToDouble(e -> Double.parseDouble(e.getText().replace("$", "")))
                .sum();
        double totalWithTax = totalSum * 0.08 + totalSum;
        String total = String.format("Total: $%.2f", totalWithTax);

        // Assert Cart Items Titles and total cost
        Assertions.assertEquals(BACKPACK_TITLE,
                checkoutOverviewPage.getShoppingCartItems().get(0).getText(), "Item title not as expected");
        Assertions.assertEquals(SHIRT_TITLE,
                checkoutOverviewPage.getShoppingCartItems().get(1).getText(), "Item title not as expected");
        Assertions.assertEquals(total,
                checkoutOverviewPage.getTotalLabelText(), "Items total price not as expected");
    }

    @Test
    public void orderCompleted_when_addProduct_and_checkout_withConfirm(){
        // Add Backpack and T-shirt to shopping cart
        inventoryPage.addProductsByTitle(BACKPACK_TITLE, SHIRT_TITLE);

        // Click on shopping Cart
        inventoryPage.clickShoppingCartLink();

        // Go to Billing Info
        shoppingCartPage.clickCheckout();

        // Fill form
        checkoutYourInformationPage.fillShippingDetails("Fname", "Lname", "zip");

        // Continue
        checkoutYourInformationPage.clickContinue();
        checkoutOverviewPage.clickFinish();

        // Assert Items removed from Shopping Cart
        Assertions.assertEquals(inventoryPage.getShoppingCartItemsNumber(),
                checkoutOverviewPage.getShoppingCartItems().size(), "Items count not as expected");
        WebElement confirmationMessage = driver().findElement(confirmMessage);
        Assertions.assertTrue(confirmationMessage.getText().contains("Thank you for your order!"),
                "Order was not completed.");

        // Complete Order
        inventoryPage.clickShoppingCartLink();

        // Assert Shopping cart is empty
        Assertions.assertEquals(inventoryPage.getShoppingCartItemsNumber(), 0,
                "Shopping cart is not empty!");
    }
}
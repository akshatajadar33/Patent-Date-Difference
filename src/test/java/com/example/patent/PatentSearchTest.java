package com.example.patent;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

public class PatentSearchTest {

    private static WebDriver driver;
    private static PatentSearchPage patentPage;

    @BeforeClass
    public static void setup() {

        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");


        driver = new ChromeDriver(options);
        driver.manage().window().maximize();


        String url = System.getProperty("baseUrl", "https://patinformed.wipo.int/");
        driver.get(url);


        // Initialize the page object
        patentPage = new PatentSearchPage(driver);
    }

    @Test
    public void testSearchAndExtractDates() {

        SoftAssert softAssert = new SoftAssert();
        // Step 1: Search patents
        patentPage.clickSearch();
        List<WebElement> results = patentPage.getSearchResults();
        softAssert.assertTrue(!results.isEmpty(), "Expected more than 0 search results ");



        // Step 2: Accept cookies/terms if present
        patentPage.acceptTermsIfPresent();
        softAssert.assertTrue(patentPage.isResultsVisible(), "Search results should be visible after accepting terms.");


        // Step 3: Click on first result
        softAssert.assertTrue(patentPage.isFirstResultPresent(), "First search result should be present.");
        patentPage.clickFirstResult();


        // Step 4: Extract and calculate date differences from cards
        patentPage.extractFromFirstCardWithTwoDates();
        int cardsWithDates = patentPage.countCardsWithTwoOrMoreDates();
        System.out.println("Number of cards with two or more dates: " + cardsWithDates);
        softAssert.assertTrue(cardsWithDates > 0, "At least one card with two or more dates should be present.");


        softAssert.assertAll();

    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
           driver.quit();
        }
    }
}

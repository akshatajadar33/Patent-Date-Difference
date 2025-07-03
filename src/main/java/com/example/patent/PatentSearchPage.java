package com.example.patent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PatentSearchPage {

    private WebDriver driver;
    private WebDriverWait wait;

    //  Reusable Locators
    private By searchButton = By.xpath("//button[@class='margin-right']");
    private By agreeButton = By.xpath("//div[@class='buttons flex space-around']//button[@class='green']");
    private By resultsListItems = By.xpath("//table[@class='results']//tbody/descendant::tr/descendant::td//li");
    private By resultCardList = By.xpath("//ul[@class='results flex space-between']/descendant::li");
    private By fullCardItem = By.xpath("//ul[@class='results flex space-between']/descendant::li[@class='result card container showButtonsOnHover']");
    private By patentDetailRows = By.xpath(".//table[@class='patentDetails noBorder']/descendant::tr");

    // Constructor for initialising driver and wait
    public PatentSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // method for clicking on search button 
    public void clickSearch() {
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
        System.out.println("Clicked Search button");
        System.out.println("Clicked Search top");
    }

    // agree to terms and condition when pop up appears 
    public void acceptTermsIfPresent() {
        try {
            WebElement agreeBtn = wait.until(ExpectedConditions.elementToBeClickable(agreeButton));
            if (agreeBtn.isDisplayed() && agreeBtn.isEnabled()) {
                agreeBtn.click();
                System.out.println("Accepted Terms and Conditions");
            }
        } catch (Exception e) {
            System.out.println("Terms popup not present or already accepted");
        }
    }

    // get this list of the search
    public List<WebElement> getSearchResults() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(resultsListItems));
        return driver.findElements(resultsListItems);
    }

    // to check wether we are getting results or no
    public boolean isResultsVisible() {
        try {
            List<WebElement> results = driver.findElements(resultsListItems);
            return !results.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }


    // to give the first result from the result list 
    public WebElement getFirstResultElement() {
        List<WebElement> results = getSearchResults();
        if (!results.isEmpty()) {
            return results.get(0); // Return the first result element
        } else {
            System.out.println("No search results found.");
            return null;
        }
    }

    //To validate whether first result came or no
    public boolean isFirstResultPresent() {
        return getFirstResultElement() != null;
    }

    // To click the first result
    public void clickFirstResult() {
        try {
            WebElement firstResult = getFirstResultElement();
            if (firstResult != null) {
                wait.until(ExpectedConditions.elementToBeClickable(firstResult)).click();
                String resultText = firstResult.getText().trim();
                System.out.println("Clicked first search result: " + resultText);
            }
        } catch (Exception e) {
            System.out.println("Failed to click first result: " + e.getMessage());
        }
    }



    // Extract date differences from first card that has at least two dates
    public void extractFromFirstCardWithTwoDates() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(resultCardList));

        List<WebElement> cards = driver.findElements(fullCardItem);
        System.out.println("Total cards found: " + cards.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < cards.size(); i++) {
            WebElement card = cards.get(i);
            List<WebElement> rows = card.findElements(patentDetailRows);

            List<String> dateLabels = new ArrayList<>();
            List<String> dateValues = new ArrayList<>();

            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < 2) continue;

                String label = cells.get(0).getText().trim();
                String value = cells.get(1).getText().trim();

                if (label.equalsIgnoreCase("Filing date") ||
                        label.equalsIgnoreCase("Publication date") ||
                        label.equalsIgnoreCase("Grant date")) {

                    dateLabels.add(label);
                    dateValues.add(value);
                }
            }

            if (dateValues.size() >= 2) {
                System.out.println("Card index " + i + " has " + dateValues.size() + " date(s):");
                for (int j = 0; j < dateLabels.size(); j++) {
                    System.out.println(dateLabels.get(j) + ": " + dateValues.get(j));
                }

                for (int m = 0; m < dateValues.size(); m++) {
                    for (int n = m + 1; n < dateValues.size(); n++) {
                        try {
                            String cleanedDate1 = dateValues.get(m).split("\\s+")[0];
                            String cleanedDate2 = dateValues.get(n).split("\\s+")[0];

                            LocalDate date1 = LocalDate.parse(cleanedDate1, formatter);
                            LocalDate date2 = LocalDate.parse(cleanedDate2, formatter);

                            long daysBetween = Math.abs(ChronoUnit.DAYS.between(date1, date2));
                            System.out.println("Difference between " + dateLabels.get(m) + " and " + dateLabels.get(n) + ": " + daysBetween + " days");
                        } catch (Exception e) {
                            System.out.println("Error parsing dates: " + e.getMessage());
                        }
                    }
                }

                break; // Stop after first valid card
            }
        }
    }

    public int countCardsWithTwoOrMoreDates() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(resultCardList));
        List<WebElement> cards = driver.findElements(fullCardItem);

        int count = 0;
        for (WebElement card : cards) {
            List<WebElement> rows = card.findElements(patentDetailRows);
            int dateCount = 0;

            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < 2) continue;
                String label = cells.get(0).getText().trim();

                if (label.equalsIgnoreCase("Filing date") ||
                        label.equalsIgnoreCase("Publication date") ||
                        label.equalsIgnoreCase("Grant date")) {
                    dateCount++;
                }
            }
            if (dateCount >= 2) count++;
        }
        return count;
    }


}

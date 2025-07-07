package com.example.patent;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PatentSearchPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators
    private By searchButton = By.xpath("//button[@class='margin-right']");
    private By agreeButton = By.xpath("//div[@class='buttons flex space-around']//button[@class='green']");
    private By resultsListItems = By.xpath("//table[@class='results']//tbody/descendant::tr/descendant::td//li");
    private By resultCardList = By.xpath("//ul[@class='results flex space-between']/descendant::li");
    private By fullCardItem = By.xpath("//ul[@class='results flex space-between']/descendant::li[@class='result card container showButtonsOnHover']");
    private By patentDetailRows = By.xpath(".//table[@class='patentDetails noBorder']/descendant::tr");
    private By searchInput = By.xpath("//input[@class='searchField']");

    public PatentSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void clickSearch() {
        String searchText = System.getProperty("searchText");
        System.out.println("Search term from command line: " + searchText);

        if (searchText != null && !searchText.trim().isEmpty()) {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
            input.sendKeys(searchText);
        } else {
            System.out.println("No search text provided. Skipping entering text.");
        }

        try {
            WebDriverWait modalWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            modalWait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("modalsHomepage")));
            System.out.println("Modal disappeared.");
        } catch (Exception e) {
            System.out.println("Modal was not present or already gone.");
        }

        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        try {
            searchBtn.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Click intercepted. Retrying with JavaScript.");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBtn);
        }

        System.out.println("Clicked Search button");
    }

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

    public List<WebElement> getSearchResults() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(resultsListItems));
        return driver.findElements(resultsListItems);
    }

    public boolean isResultsVisible() {
        try {
            return !driver.findElements(resultsListItems).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public WebElement getFirstResultElement() {
        List<WebElement> results = getSearchResults();
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean isFirstResultPresent() {
        return getFirstResultElement() != null;
    }

    public void clickFirstResult() {
        try {
            WebElement firstResult = getFirstResultElement();
            if (firstResult != null) {
                wait.until(ExpectedConditions.elementToBeClickable(firstResult)).click();
                System.out.println("Clicked first search result: " + firstResult.getText().trim());
            }
        } catch (Exception e) {
            System.out.println("Failed to click first result: " + e.getMessage());
        }
    }

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

                            Period period = Period.between(date1, date2);

                            int years = Math.abs(period.getYears());
                            int months = Math.abs(period.getMonths());
                            int days = Math.abs(period.getDays());

                            System.out.println("Difference between " + dateLabels.get(m) + " and " + dateLabels.get(n) +
                                    ": " + years + " years, " + months + " months, " + days + " days");

                        } catch (Exception e) {
                            System.out.println("Error parsing or comparing dates: " + e.getMessage());
                        }
                    }
                }

                // ✅ If it's the first card (index 0), break early
                if (i == 0) {
                    System.out.println("Card index 0 has two or more dates. Exiting early.");
                    return;
                }
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

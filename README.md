 Patent Date Difference Automation

This project automates the extraction and comparison of patent dates from [Pat-INFORMED](https://patinformed.wipo.int/) using Java and Selenium WebDriver.

Objective

- Automatically navigate to Pat-INFORMED.
- Click on the first search result from the homepage.
- Extract `Filing date`, `Publication date`, and `Grant date` from the first card.
- Calculate the difference (in days) between each pair of available dates.
- If fewer than two dates are present in the current card, move to the next one.

Technologies Used

- Java
- Selenium WebDriver
- TestNG
- ChromeDriver

Setup Instructions

 Prerequisites

- Java JDK 8 or above
- Chrome browser installed
- ChromeDriver in your system PATH
- TestNG set up in your IDE (IntelliJ, Eclipse, etc.)

 Steps

1. Clone the repository or copy the project folder.
2. Open the project in your IDE.
3. Install dependencies .
4. Run the test:
    - Open `PatentSearchTest.java`
    - Right-click → `Run as TestNG Test`

Features

- Uses Chrome in Incognito mode
- Explicit waits are used — no `Thread.sleep`
- Handles missing or dynamic popups (terms popup is handled optionally)
- Cleans and parses dates even with annotations (e.g., `(expected)`)
-  Skips cards with missing date values

Note on Terms/Consent Popups

Tested this automation in multiple browsers but did not encounter the cookie/terms popup consistently.  
As a result, the script includes optional handling for it, but it might not always appear during execution.

 


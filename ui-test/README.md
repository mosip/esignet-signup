# 🧪 eSignet singup UI Automation Framework

## 🚀 Overview

This project is a UI automation testing framework for **eSignet Signup**, built using **Cucumber**, **TestNG**, and **Selenium WebDriver**, with support for **BrowserStack** and **parallel execution**.

## 🔍 What is eSignet?

eSignet is a reference identity and authentication platform developed under the [MOSIP](https://www.mosip.io) project. It demonstrates how authentication and consent mechanisms can be implemented for foundational ID systems.

This framework enables automated testing of eSignet's signup UI features and flows across multiple browsers and devices to ensure consistent and reliable behavior.

---

## 🧪 Features

- ✅ Cucumber BDD support (`.feature` files)
- ✅ TestNG parallel execution
- ✅ BrowserStack integration for cross-browser/device testing
- ✅ Seamless switching between local and cloud runs
- ✅ Configurable via `config.properties`
- ✅ Generates reports in **HTML**, **JSON**, and **Extent** format
- ✅ Automatically picks up scenarios for different browsers using scenario names or tags

---

## ⚙️ Technologies Used

- Java
- Maven
- Selenium WebDriver
- Cucumber
- TestNG
- BrowserStack (optional)
- Extent Reports

---

## 📁 Project Structure

```
project-root/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── base/            # Base classes (WebDriver setup)
│   │   │   ├── constants/       # Class for constants
│   │   │   ├── pages/           # Page Object classes
│   │   │   ├── stepdefinitions/ # Cucumber step definitions
│   │   │   ├── runners/         # TestNG-Cucumber runner classes
│   │   │   ├── utils/           # Utility classes
│   │   └── resources/
│   │       ├── featurefiles/    # Cucumber feature files
│   │       ├── config.properties# Central config file
│   │       ├── config.properties# Central config file
│   │       ├── extend.properties# Extend report property file
│   └── test/
│ 
├── testNgXmlFiles/             # Optional TestNG XML suites
├── pom.xml
└── README.md
```

---

## 🔧 Configuration (`config.properties`)

```properties
baseurl = https://healthservices.es-qa.mosip.net/
signup.portal.url=https://signup.es-qa.mosip.net/
smtp.url=https://smtp.es-qa.mosip.net/
runOnBrowserStack=true/false
runMultipleBrowsers=true/false
threadCount=3
browser=chrome                # Used when runMultipleBrowsers is false
browsers=chrome,edge          # Used when runMultipleBrowsers is true
browserstack_username=<your_browserstack_username>
browserstack_access_key=<your_browserstack_key>
```
Automation scripts rely on **MOSIP_SIGNUP_IDENTIFIER_REGEX** for input validation during signup flows.
Ensure that **MOSIP_SIGNUP_IDENTIFIER_REGEX** is explicitly defined in the configuration.

---

## 🧱 Pre-requisites

### Common Requirements
- JDK 21
- Maven 3.6.0 or higher
- BrowserStack account (for cross-browser testing)

### Windows
- Git Bash 2.18.0 or higher
- Ensure `settings.xml` is present in the `.m2` folder

### Linux
- Ensure `settings.xml` is in:
  - Maven's default `/conf` directory
  - `/usr/local/maven/conf/`

---

## 🚀 Getting Started

### 1. Access the Test Automation Code

#### 📥 Via Browser
1. Clone or download from [GitHub](https://github.com/mosip/esignet-signup)
2. Unzip contents locally
3. Open terminal (Linux) or command prompt (Windows)

#### 🐙 Via Git Bash
```bash
git clone https://github.com/mosip/esignet-signup
```

---

### 2. Build the Project

Navigate to the test directory:

```bash
cd ui-test
```

Build the project:

```bash
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

---

### 3. Execute the Test Automation Suite

#### ▶️ Using JAR
```bash
cd target/
java -jar -Denv.endpoint="$ENV_ENDPOINT" uitest-esignet-*.jar
```

#### 🧩 Using Eclipse IDE

1. **Install Eclipse**  
   [Download Eclipse](https://www.eclipse.org/downloads/)

2. **Import Maven Project**  
   - `File > Import > Maven > Existing Maven Projects`  
   - Select the `ui-test` folder

3. **Build the Project**  
   - Right-click project > `Maven > Update Project`

4. **Configure Run**  
   - `Run > Run Configurations > Java Application > New`  
   - **Main class**: `runners.Runner`  
   - **VM Arguments**:
     ```bash
     -Denv.endpoint=<base_env>
     ```

5. **Run the Configuration**  
   - Click **Run** or **Debug** (for breakpoints)

6. **View Test Results**  
   - Results will appear in `ui-test/test-output`

---

## ☁️ Run on BrowserStack

- Set `runOnBrowserStack=true` in `config.properties`
- Ensure BrowserStack credentials are correctly set

---

## 🧵 Thread Management

- Managed via `threadCount` in `config.properties`
- Threads are created dynamically for parallel execution

---

## 🧪 Tags Support

Run scenarios with specific tags:

```bash
mvn test -Dcucumber.filter.tags="@smoke"
```

Or configure in runner class:

```java
@CucumberOptions(tags = "@regression")
```

---

## 📊 Reports

- `target/cucumber.html` – Cucumber native HTML report  
- `target/cucumber.json` – JSON report  
- `test-output/extent-report.html` – Extent report

---

## 🌍 BrowserStack Notes

- Use scenario names like: `Login - chrome`, `Login - firefox`
- Or use tags like `@chrome`, `@firefox`
- The framework maps capabilities accordingly

---

## 💡 Tips

- If tests hang or report 0 tests:
  - Check scenario naming and browser mapping
  - Log scenario names using an overridden `scenarios()` method
  - Set thread name for debugging:
    ```java
    Thread.currentThread().setName(pickle.getPickle().getName());
    ```

---

## 🧾 References

- [MOSIP Docs – eSignet](https://docs.mosip.io/)
- [BrowserStack Documentation](https://www.browserstack.com/docs)
- [Cucumber with TestNG Guide](https://cucumber.io/docs/testng-integration/)

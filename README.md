# CPT111 Group 25 Project - Movie Recommendation and Tracker

## About the Project
This is the Coursework 3 submission for our group. We built a Movie Recommendation and Tracker App using Java and JavaFX. 

The main idea is to help users manage their movie watchlists. We also implemented a recommendation algorithm that suggests movies based on genres and user ratings.

The final score of this assignment is 81, ranking 2nd in the ICS major.

## Requirements
To run this project, you need:
1.  **IntelliJ IDEA** (Recommended).
2.  **Java 17** or higher.
3.  The **JavaFX SDK** is already included in the folder `javafx-sdk-21.0.9`, so you don't need to download it separately.

## How to Run (Important!)

**Method 1: Run in IntelliJ (Easiest way)**
We strongly recommend running this inside IntelliJ IDEA to avoid path issues.

1.  Open the folder `CW3-Ver6` as a project in IntelliJ.
2.  Go to **File -> Project Structure -> Libraries**.
3.  Add the `lib` folder inside `javafx-sdk-21.0.9`.
4.  Find `AppLauncher.java` in `src/main/java/movietracker/`.
5.  Right-click and Run.

**Method 2: Command Line**
If you want to run it from the terminal, please make sure you are in the root directory.

* **Compile:**
    ```
    javac --module-path "javafx-sdk-21.0.9\lib" --add-modules javafx.controls,javafx.fxml -sourcepath src\main\java -d out src\main\java\movietracker\*.java
    ```
    *(Note: The `-sourcepath` flag ensures it finds files in subfolders like `ui` and `backend` automatically)*

* **Run:**
    ```
    java --module-path "javafx-sdk-21.0.9\lib" --add-modules javafx.controls,javafx.fxml -cp out movietracker.AppLauncher
    ```

**Note on Resources:**
You need to copy the resource files (images, fxml) to the `out` folder.
```
xcopy /E /I /Y src\main\resources out
```
(If you don't do this, images or pages won't load!)

## Features We Implemented
* **User Account Levels (OOP)**: 
    *   **Basic User**: Standard access with a recommendation limit of 6 movies.
    *   **Premium User**: Enhanced privileges with a limit of 16 movies and **Password Hashing**.
* **Security**: Implemented a custom hashing algorithm (Salt + Shift) for Premium User passwords to ensure data safety.
* **Smart Recommendations**: The engine analyzes user History to calculate genre scores and recommends Top-N movies accordingly.
* **MVC & FXML**: The application uses a strict Model-View-Controller architecture with FXML for UI separation.
* **Data Persistence**: Custom CSV parser handles data storage for Users, Watchlists, and History.

## Known Issues / Future Improvements
* **Image Caching**: Poster images may load slowly on the first launch.
* **High-DPI Scaling**: On 4K monitors, the window size might need manual adjustment.
* **Data Integrity**: Please avoid manually modifying `users.csv`, especially for Premium Users, as this may break the password hash verification.
# Creating a Flutter Project

Creating a Flutter project is straightforward. Follow these steps to set up and start a new project:

### **Prerequisites**
1. **Install Flutter SDK**:
   - Download the Flutter SDK from the [official Flutter website](https://flutter.dev).
   - Follow the installation instructions for your operating system (Windows, macOS, or Linux).
   - Add the `flutter` command to your system's PATH.
   - Run `flutter doctor` in your terminal to check for dependencies and fix any issues.

2. **Install an IDE**:
   - Install an Integrated Development Environment (IDE) like **Visual Studio Code** or **Android Studio**.
   - Add Flutter and Dart plugins/extensions to your IDE.

3. **Set up a Device/Emulator**:
   - For testing, connect a physical device via USB or set up an emulator/simulator for Android or iOS.

---

### **Creating a Flutter Project**

#### **Using Command Line**
1. Open your terminal or command prompt.
2. Navigate to the directory where you want to create the project.
3. Run the following command:
   ```bash
   flutter create project_name
   ```
   Replace `project_name` with your desired project name.

4. Navigate to the project directory:
   ```bash
   cd project_name
   ```

5. Launch the project:
   ```bash
   flutter run
   ```

---

#### **Using Android Studio**
1. Open Android Studio.
2. Go to **File > New > New Flutter Project**.
3. Select **Flutter Application** and click **Next**.
4. Configure the project name, location, and description.
5. Set the Flutter SDK path if not already set.
6. Click **Finish** to create the project.

---

#### **Using Visual Studio Code**
1. Open VS Code.
2. Open the **Command Palette** (`Ctrl+Shift+P` or `Cmd+Shift+P`).
3. Type `Flutter: New Project` and select it.
4. Choose a location for your project folder.
5. Enter the project name and press Enter.
6. VS Code will automatically create the project and open it.

---

### **Running Your Flutter Project**
1. Connect a device or start an emulator.
2. Navigate to the project folder in the terminal or use your IDE.
3. Run the app using:
   ```bash
   flutter run
   ```
   Or use the play/run button in your IDE.

Your Flutter project is now ready to develop! 🚀
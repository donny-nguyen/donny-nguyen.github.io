# Downgrade Flutter

To downgrade Flutter to a specific version, you can follow these steps:

### 1. **Check Available Versions**
   - Visit the [Flutter Releases Page](https://docs.flutter.dev/development/tools/sdk/releases) to find the version you want to downgrade to.
   - Note the version number (e.g., `3.10.5`) or the corresponding Git commit hash.

### 2. **Downgrade Using Flutter Version Command**
   If you installed Flutter using the `flutter` command-line tool, you can switch to a specific version by running:
   ```bash
   flutter downgrade <version>
   ```
   Replace `<version>` with the specific version you want (e.g., `3.10.5`).

---

### 3. **Manually Checkout a Specific Version**
   If `flutter downgrade` does not work or you need a more precise control, follow these steps:

   #### a. Navigate to Your Flutter Installation Directory
   Find where Flutter is installed on your system. For example:
   - macOS/Linux: `~/flutter` or `/usr/local/flutter`
   - Windows: `C:\flutter`

   #### b. Switch to the Specified Version
   Use Git to checkout the desired version. Run the following commands:
   ```bash
   cd <flutter-directory>
   git fetch
   git checkout <version>
   ```
   Replace `<flutter-directory>` with the path to your Flutter installation and `<version>` with the version number or Git commit hash (e.g., `3.10.5` or `v3.10.5`).

   #### c. Verify Flutter
   Now run flutter doctor -v command so flutter will download the stuff related to the particular version:
   ```bash
   flutter doctor
   ```

---

### 4. **Downgrade Using Flutter Version Management Tools (Optional)**
   Use tools like [FVM (Flutter Version Manager)](https://fvm.app/) for easier version management:
   1. Install FVM:
      ```bash
      dart pub global activate fvm
      ```
   2. Install a specific Flutter version:
      ```bash
      fvm install <version>
      ```
   3. Use the installed version in your project:
      ```bash
      fvm use <version>
      ```

### 5. **Verify the Downgrade**
   Confirm the version of Flutter you're using by running:
   ```bash
   flutter --version
   ```

These methods should help you downgrade Flutter to any specific version.
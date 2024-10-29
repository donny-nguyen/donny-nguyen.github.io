# Install Flutter and Dart

1. Download Flutter SDK:
   - Visit flutter.dev/docs/get-started/install
   - Download the Flutter SDK for our OS (Windows/macOS/Linux)
   - Extract the downloaded ZIP to a location (e.g., `C:\dev` or `~/development`)

2. Add Flutter to PATH:
   - Windows: Add `<flutter-install-location>\flutter\bin` to System Environment Variables
   - macOS/Linux: Add `export PATH="$PATH:<flutter-install-location>/flutter/bin"` to `~/.bashrc` or `~/.zshrc`

3. Run `flutter doctor` in terminal to:
   - Check dependencies
   - Install missing components
   - Verify installation

4. Install Required Tools:
   - Windows: Install Git from git-scm.com
   - macOS: Install Xcode from App Store
   - Linux: `sudo apt-get install git`

5. IDE Setup:
   ```bash
   # Install VS Code extensions:
   - Flutter
   - Dart
   ```

6. Verify Installation:
   ```bash
   flutter doctor -v     # Detailed verification
   flutter --version    # Check Flutter version
   dart --version      # Check Dart version
   ```

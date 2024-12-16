# Use Dart Console Project as a Dependency in Flutter Project 

You can use your Dart console project as a dependency in your Flutter project by adding it as a **path dependency** or by publishing it as a **package**. Here’s how to do it step-by-step:

---

### **Option 1: Add as a Path Dependency**

If your Dart console project is not published, you can reference it locally using a path.

1. **Make Sure the Dart Console Project is a Package**
   Ensure your Dart console project has a `pubspec.yaml` file and is structured as a Dart package. At a minimum, the `pubspec.yaml` should have:

   ```yaml
   name: my_dart_console_project
   description: A reusable Dart project
   version: 1.0.0
   environment:
     sdk: '>=2.19.0 <3.0.0'
   ```

2. **Add the Path Dependency**
   In your Flutter project’s `pubspec.yaml`, add your Dart console project as a dependency using the `path` field. For example:

   ```yaml
   dependencies:
     flutter:
       sdk: flutter
     my_dart_console_project:
       path: ../path_to_your_dart_console_project
   ```

   Replace `../path_to_your_dart_console_project` with the actual relative path to your Dart project.

3. **Run `flutter pub get`**
   Navigate to your Flutter project folder in the terminal and run:

   ```bash
   flutter pub get
   ```

4. **Import and Use the Dart Package**
   You can now import the library from your Dart console project in your Flutter app:

   ```dart
   import 'package:my_dart_console_project/my_dart_console_project.dart';

   void main() {
     // Call functions or classes from the Dart console project
   }
   ```

---

### **Option 2: Publish to a Private or Public Repository**

If you want the dependency to be reusable across multiple projects:

1. **Publish the Dart Project to a Git Repository**
   Push your Dart console project to a Git repository (e.g., GitHub, GitLab).

2. **Reference it in `pubspec.yaml`**
   Add the Git repository URL in your Flutter project’s `pubspec.yaml`:

   ```yaml
   dependencies:
     flutter:
       sdk: flutter
     my_dart_console_project:
       git:
         url: https://github.com/username/my_dart_console_project.git
         ref: main
   ```

3. **Run `flutter pub get`**
   Fetch the dependency:

   ```bash
   flutter pub get
   ```

4. **Import and Use the Dependency**
   Use it in the same way as the path dependency.

---

### **Option 3: Publish to Pub.dev (Optional)**
If your Dart console project is generic and reusable for others, you can publish it to **pub.dev**:
1. Follow the steps in the [Dart publishing guide](https://dart.dev/tools/pub/publishing).
2. Once published, reference it in your `pubspec.yaml` like this:

   ```yaml
   dependencies:
     my_dart_console_project: ^1.0.0
   ```

3. Run `flutter pub get`.

---

### **Debugging Tips**
- Ensure the console project doesn’t have Flutter dependencies unless needed, as Dart-only projects typically don’t require them.
- If you face dependency version conflicts, align the `environment` SDK constraints between your Flutter project and Dart console project.
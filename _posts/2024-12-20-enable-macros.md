# Enable `macros` for Dart and Flutter Projects

To enable macros for Dart and Flutter projects, you need to follow a few key steps:

## Update Flutter and Dart

First, ensure you're using the latest Flutter version that supports macros:

1. Switch to the master channel:
   ```
   flutter channel master
   flutter upgrade
   ```

2. Update your `pubspec.yaml` to use a DEV version of Dart:
   ```yaml
   environment:
     sdk: ^3.7.0-243.0.dev
   ```

## Enable Macros in Project Configuration

1. Create or update your `analysis_options.yaml` file in the project root:
   ```yaml
   analyzer:
     enable-experiment:
       - macros
   ```

## Configure VS Code for Macros

To enable macros at runtime in VS Code:

1. Create or open the `.vscode/launch.json` file in your project.

2. Add a new configuration or modify an existing one to include the macros experiment flag:

    a. For Dart CLI  project:

        ```json
        {
            "version": "0.2.0",
            "configurations": [
                {
                    "name": "Dart CLI",
                    "request": "launch",
                    "type": "dart",
                    "vmAdditionalArgs": ["--enable-experiment=macros"]
                }
            ]
        }
        ```

    b. For Flutter project:

    ```json
    {
        "version": "0.2.0",
        "configurations": [
            {
                "name": "Flutter",
                "request": "launch",
                "type": "dart",
                "toolArgs": [
                    "--enable-experiment=macros"
                ]
            }
        ]
    }
    ```

This configuration ensures that when you run or debug your Dart CLI or Flutter application from VS Code, it will enable the macros experiment.

## Additional Steps

- If you're using the `json` package for macros, add it to your `pubspec.yaml`:
  ```yaml
  dependencies:
    json: ^1.0.0
  ```
  Then run `flutter pub get` to install it.

- For Dart CLI projects, you may need to adjust the `program` field in the launch configuration to point to your main Dart file, typically `bin/main.dart`.

- In some recent Dart SDK versions, you might see this compiling error when using macros:

    ```
    The part-of directive must be the only directive in a part. Try removing the other directives, or moving them to the library for which this is a part.
    ```

    You can fix it by updating your `analysis_options.yaml` file in the project root to enable `enhanced-parts` flag along with `macros`:

    ```yaml
    analyzer:
      enable-experiment:
        - macros
        - enhanced-parts
    ```

By following these steps, you'll have macros enabled in your Dart CLI or Flutter project, both for analysis and at runtime when launching from VS Code.
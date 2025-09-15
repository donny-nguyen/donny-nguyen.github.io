# Editing Text File in Linux Terminal

To edit a text file from the command line in Linux, you use a text editor. The two most common and powerful command-line text editors are **Nano** and **Vim**.

***

### 1. Nano (Recommended for Beginners)

Nano is a simple and user-friendly editor that's great for quick changes. It displays a list of commands at the bottom of the screen, so you don't need to memorize them. It's often pre-installed on Linux systems.

**To open and edit a file with Nano:**

1.  Type `nano` followed by the filename you want to edit. If the file doesn't exist, Nano will create a new one.

    `nano filename.txt`

2.  Make your changes. You can use your arrow keys to move the cursor, and type as you would in a regular text editor.

3.  To **save** your changes, press `Ctrl + O` (the letter O). This stands for "Write Out".

4.  To **exit** Nano, press `Ctrl + X`. If you made changes you haven't saved, it will prompt you to save before exiting.

***

### 2. Vim (Powerful but Steeper Learning Curve)

Vim is a highly customizable and powerful editor favored by experienced users and system administrators. It operates in different "modes," which can be confusing at first.

**To open and edit a file with Vim:**

1.  Type `vim` followed by the filename.

    `vim filename.txt`

    Vim opens in **Normal Mode**, where you can navigate but not type.

2.  To start editing, press the letter `i` on your keyboard. This switches you to **Insert Mode**, and you'll see `-- INSERT --` at the bottom of the screen.

3.  Make your changes.

4.  To **save and exit**, you first need to return to Normal Mode. Press the `Esc` key.

5.  Then, type `:wq` and press `Enter`.
    * `:` puts you in command-line mode.
    * `w` stands for "write" (save).
    * `q` stands for "quit."

**Other useful Vim commands:**

* `:w` (save without quitting)
* `:q` (quit without saving; only works if no changes were made)
* `:q!` (force quit, discarding any changes)
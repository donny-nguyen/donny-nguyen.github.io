# View Text File in Linux Terminal

You can view a text file in a Linux terminal using a few simple commands. The most common commands are `cat`, `less`, `more`, and `head` or `tail`. Each command has a specific use case.

***

### Using `cat`

The `cat` command, short for "concatenate," displays the entire content of a file directly to the terminal screen. This is best for small files.

To use it, type `cat` followed by the filename:

`cat filename.txt`

For example, to view a file named `my_notes.txt`, you would use:

`cat my_notes.txt`

***

### Using `less`

The `less` command is a powerful **pager**, which means it displays a file page by page. This is the **recommended** command for viewing **large files** because it doesn't load the entire file into memory at once.

To use it, type `less` followed by the filename:

`less filename.txt`

Once the file is open, you can navigate using these keys:

* **Up/Down arrows** or **j/k**: Scroll one line at a time.
* **Spacebar** or **Page Down**: Scroll down one page.
* **b** or **Page Up**: Scroll up one page.
* **/**: Search forward for a specific word.
* **q**: Quit and return to the terminal prompt.

***

### Using `more`

The `more` command is similar to `less` but with fewer features. It's an older pager that also displays a file one page at a time.

To use it, type `more` followed by the filename:

`more filename.txt`

You can use the **Spacebar** to move to the next page and **q** to quit.

***

### Using `head` and `tail`

The `head` and `tail` commands are useful for viewing only the beginning or end of a file, respectively. They both show the first or last **10 lines** by default.

To view the first 10 lines of a file, use `head`:

`head filename.txt`

To view the last 10 lines of a file, use `tail`:

`tail filename.txt`

You can specify a different number of lines using the `-n` option:

* `head -n 5 filename.txt` (shows the first 5 lines)
* `tail -n 20 filename.txt` (shows the last 20 lines)
# Common File and Folder Commands in Ubuntu

Ubuntu provides many useful terminal commands for working with files and folders. These commands help you navigate directories, inspect content, create files, copy items, move items, delete items, and search for what you need.

***

### Check Your Current Folder with `pwd`

The `pwd` command stands for "print working directory." It shows the full path of the folder you are currently in.

```bash
pwd
```

Example output:

```bash
/home/donny/Documents
```

***

### List Files and Folders with `ls`

The `ls` command lists the files and folders in the current directory.

```bash
ls
```

Useful options:

```bash
ls -l      # Show detailed information
ls -a      # Show hidden files
ls -lh     # Show file sizes in a readable format
ls -la     # Show detailed information, including hidden files
```

Hidden files and folders in Linux usually start with a dot, such as `.bashrc` or `.config`.

***

### Change Folders with `cd`

The `cd` command stands for "change directory." It lets you move between folders.

```bash
cd Documents
```

Common examples:

```bash
cd ..          # Move up one folder
cd ~           # Go to your home folder
cd /var/log    # Go to an absolute path
cd -           # Go back to the previous folder
```

***

### Create Folders with `mkdir`

The `mkdir` command creates a new folder.

```bash
mkdir projects
```

To create nested folders, use the `-p` option:

```bash
mkdir -p projects/java/demo-app
```

The `-p` option creates any missing parent folders and avoids an error if the folder already exists.

***

### Create Files with `touch`

The `touch` command creates an empty file if it does not already exist.

```bash
touch notes.txt
```

You can create multiple files at once:

```bash
touch index.html style.css script.js
```

If the file already exists, `touch` updates its modified time.

***

### Copy Files and Folders with `cp`

The `cp` command copies files or folders.

Copy a file:

```bash
cp notes.txt backup-notes.txt
```

Copy a file into another folder:

```bash
cp notes.txt backups/
```

Copy a folder and everything inside it:

```bash
cp -r project project-backup
```

The `-r` option means recursive, which is required when copying folders.

***

### Move or Rename Files and Folders with `mv`

The `mv` command moves files and folders. It is also used to rename them.

Rename a file:

```bash
mv old-name.txt new-name.txt
```

Move a file into another folder:

```bash
mv notes.txt Documents/
```

Rename a folder:

```bash
mv old-folder new-folder
```

***

### Delete Files and Folders with `rm` and `rmdir`

The `rm` command deletes files.

```bash
rm notes.txt
```

Delete an empty folder with `rmdir`:

```bash
rmdir empty-folder
```

Delete a folder and everything inside it:

```bash
rm -r old-project
```

Be careful with `rm -r` because deleted files do not go to the Trash when removed from the terminal.

***

### View File Details with `stat`

The `stat` command shows detailed information about a file or folder, including size, permissions, owner, and timestamps.

```bash
stat notes.txt
```

***

### Check File and Folder Sizes with `du`

The `du` command shows disk usage.

Show the size of a folder:

```bash
du -sh project
```

Show sizes of items inside the current folder:

```bash
du -h --max-depth=1
```

Common options:

```bash
du -s    # Summary only
du -h    # Human-readable sizes
```

***

### Find Files and Folders with `find`

The `find` command searches for files and folders by name, type, size, or other conditions.

Find a file by name:

```bash
find . -name "notes.txt"
```

Find all Markdown files:

```bash
find . -name "*.md"
```

Find folders by name:

```bash
find . -type d -name "images"
```

Find files larger than 100 MB:

```bash
find . -type f -size +100M
```

In these examples, `.` means the current folder.

***

### Search Inside Files with `grep`

The `grep` command searches text inside files.

Search for a word in a file:

```bash
grep "Ubuntu" notes.txt
```

Search recursively inside a folder:

```bash
grep -R "Ubuntu" .
```

Useful options:

```bash
grep -i "ubuntu" notes.txt    # Case-insensitive search
grep -n "Ubuntu" notes.txt    # Show line numbers
grep -R "Ubuntu" folder/      # Search inside a folder recursively
```

***

### Create Links with `ln`

The `ln` command creates links between files.

Create a hard link:

```bash
ln original.txt copy-link.txt
```

Create a symbolic link, also called a symlink:

```bash
ln -s /path/to/original.txt shortcut.txt
```

Symbolic links are commonly used as shortcuts to files or folders.

***

### Compress and Extract Folders with `tar`

The `tar` command is commonly used to archive and compress folders.

Create a compressed archive:

```bash
tar -czvf project.tar.gz project/
```

Extract a compressed archive:

```bash
tar -xzvf project.tar.gz
```

Common options:

```bash
c    # Create an archive
x    # Extract an archive
z    # Use gzip compression
v    # Verbose output
f    # File name follows
```

***

### Useful Command Combinations

Here are some practical combinations you may use often:

```bash
ls -lah                         # List everything with readable sizes
mkdir -p backups/2026/june       # Create nested folders
cp -r project backups/project    # Copy a folder
mv draft.txt final.txt           # Rename a file
find . -name "*.log"             # Find all log files
du -sh .                         # Show current folder size
```

These commands cover many everyday file and folder tasks in Ubuntu. Start with `pwd`, `ls`, and `cd`, then gradually add commands like `cp`, `mv`, `rm`, `find`, and `grep` as you become more comfortable in the terminal.
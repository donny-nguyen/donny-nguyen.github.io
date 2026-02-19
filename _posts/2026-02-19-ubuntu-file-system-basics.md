# Ubuntu File System Basics

## Navigating the File System

**Basic navigation commands:**
- `pwd` — print current directory
- `ls` — list files and folders (`ls -la` for detailed view including hidden files)
- `cd folder_name` — change directory
- `cd ..` — go up one level
- `cd ~` — go to your home directory

---

## Creating Files & Folders

```bash
touch filename.txt        # Create an empty file
mkdir folder_name         # Create a folder
mkdir -p a/b/c            # Create nested folders at once
```

---

## Copying, Moving & Renaming

```bash
cp file.txt /destination/         # Copy a file
cp -r folder/ /destination/       # Copy a folder (recursive)
mv file.txt /destination/         # Move a file
mv oldname.txt newname.txt        # Rename a file or folder
```

---

## Deleting Files & Folders

```bash
rm file.txt               # Delete a file
rm -r folder_name/        # Delete a folder and its contents
rm -rf folder_name/       # Force delete (no confirmation — use carefully!)
```

---

## Viewing File Contents

```bash
cat file.txt              # Print full file content
less file.txt             # Scroll through a file (q to quit)
head -n 20 file.txt       # First 20 lines
tail -n 20 file.txt       # Last 20 lines
tail -f logfile.txt       # Live-follow a file (great for logs)
```

---

## Searching

```bash
find /path -name "*.txt"          # Find files by name
find . -type d -name "config"     # Find directories named "config"
grep -r "search_term" /path/      # Search inside files recursively
```

---

## Permissions

```bash
ls -l                             # View permissions
chmod 755 file.sh                 # Set permissions (rwx for owner, rx for others)
chmod +x script.sh                # Make a file executable
chown user:group file.txt         # Change file owner
```

Permission notation: `rwxrwxrwx` = owner / group / others, where r=4, w=2, x=1.

---

## File Size & Disk Usage

```bash
du -sh folder/            # Size of a folder
df -h                     # Disk space on all drives
ls -lh                    # Human-readable file sizes in a listing
```

---

## Graphical File Manager

If you prefer a GUI, Ubuntu comes with **Nautilus (Files)**. You can open it by:
- Clicking the **Files** icon in the dock
- Or running `nautilus` in the terminal

Press **Ctrl+H** in Nautilus to toggle hidden files (those starting with a dot).

---

**Pro tips:**
- Use **Tab** to autocomplete file/folder names in the terminal
- Use `man command` (e.g. `man ls`) to read the manual for any command
- Wildcards like `*` and `?` work in most commands (e.g. `rm *.log`)

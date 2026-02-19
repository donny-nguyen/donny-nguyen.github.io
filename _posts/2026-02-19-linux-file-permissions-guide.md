# Linux File Permissions Guide

## Understanding Permission Structure

Every file/folder has three permission sets: **owner**, **group**, and **others**. Each set has three permission types:

- **r** (read) = 4
- **w** (write) = 2
- **x** (execute) = 1

Running `ls -l` shows permissions like: `-rwxr-xr--`
- Position 1: file type (`-` = file, `d` = directory)
- Positions 2–4: owner permissions
- Positions 5–7: group permissions
- Positions 8–10: others' permissions

---

## Core Commands

### `chmod` — Change permissions

**Symbolic mode:**
```bash
chmod u+x file.txt       # Add execute for owner
chmod g-w file.txt       # Remove write for group
chmod o=r file.txt       # Set others to read-only
chmod a+r file.txt       # Add read for all (a = all)
```

**Numeric (octal) mode:**
```bash
chmod 755 file.txt       # rwxr-xr-x
chmod 644 file.txt       # rw-r--r-- (common for files)
chmod 700 folder/        # rwx------ (private folder)
chmod -R 755 folder/     # Recursive (applies to contents)
```

**Common permission combos:**
| Octal | Symbolic | Use case |
|-------|----------|----------|
| 777 | rwxrwxrwx | Full access for all (avoid!) |
| 755 | rwxr-xr-x | Public directories |
| 644 | rw-r--r-- | Regular files |
| 600 | rw------- | Private files |
| 700 | rwx------ | Private directories |

---

### `chown` — Change owner

```bash
chown alice file.txt             # Change owner
chown alice:developers file.txt  # Change owner and group
chown -R alice:alice folder/     # Recursive
```

### `chgrp` — Change group

```bash
chgrp developers file.txt
chgrp -R developers folder/
```

---

## Managing Users & Groups

```bash
# Add a user to a group
sudo usermod -aG groupname username

# Create a new group
sudo groupadd groupname

# View a user's groups
groups username

# View file permissions
ls -l file.txt
ls -ld folder/       # Show folder itself, not contents
```

---

## Special Permissions

```bash
# Setuid — run file as owner (e.g., passwd)
chmod u+s file

# Setgid — new files inherit group of directory
chmod g+s folder/

# Sticky bit — only owner can delete files (e.g., /tmp)
chmod +t folder/
```

---

## ACLs — Fine-grained Permissions

For more granular control beyond owner/group/others:

```bash
# Install
sudo apt install acl

# Grant a specific user read/write on a file
setfacl -m u:alice:rw file.txt

# Grant a group read access
setfacl -m g:developers:r folder/

# View ACLs
getfacl file.txt

# Remove ACLs
setfacl -b file.txt
```

---

**Quick rule of thumb:** Use `644` for files, `755` for directories, and restrict further (`600`/`700`) for sensitive content. Avoid `777` — it grants full access to everyone.
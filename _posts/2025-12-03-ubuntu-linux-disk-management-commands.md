# Ubuntu/Linux disk management commands

Here are the most commonly used **Ubuntu/Linux disk management commands** with a short explanation for each. These will help you check disk usage, list disks, inspect partitions, mount/unmount, and manage file systems.

---

# ✅ **Basic disk usage commands**

### **1. Show disk space usage (human-readable)**

```bash
df -h
```

### **2. Show disk usage of a directory**

```bash
du -sh /path/to/directory
```

### **3. Show usage of root folder (sorted)**

```bash
sudo du -xh / | grep '[0-9\.]\+G' | sort -hr | head -30
```

---

# ✅ **List disks and partitions**

### **4. Detailed list of disks**

```bash
lsblk
```

### **5. Show disk partitions**

```bash
sudo fdisk -l
```

### **6. List partition UUIDs**

```bash
blkid
```

### **7. Show S.M.A.R.T. disk health**

```bash
sudo smartctl -a /dev/sda
```

(Requires `sudo apt install smartmontools`)

---

# ✅ **Mounting & unmounting disks**

### **8. Mount a disk**

```bash
sudo mount /dev/sdX1 /mnt
```

### **9. Unmount a disk**

```bash
sudo umount /dev/sdX1
```

### **10. Reload all mounts from /etc/fstab**

```bash
sudo mount -a
```

---

# ✅ **Disk formatting commands**

### **11. Format as EXT4**

```bash
sudo mkfs.ext4 /dev/sdX1
```

### **12. Format as FAT32**

```bash
sudo mkfs.vfat -F 32 /dev/sdX1
```

### **13. Format as NTFS**

```bash
sudo mkfs.ntfs /dev/sdX1
```

---

# ✅ **File system checks and repair**

### **14. Check a filesystem**

```bash
sudo fsck /dev/sdX1
```

### **15. Force fsck on next reboot**

```bash
sudo touch /forcefsck
```

---

# ✅ **Disk performance**

### **16. Test disk speed**

```bash
sudo hdparm -Tt /dev/sda
```

(Requires `sudo apt install hdparm`)

---

# ✅ **Disk creation & resizing**

### **17. Partition manager (text UI)**

```bash
sudo cfdisk /dev/sda
```

### **18. Resize a partition (CLI)**

```bash
sudo parted /dev/sda
```

# Backup Ubuntu using rsync

Here's a comprehensive guide on using rsync to backup your entire Ubuntu system:

## Basic Rsync Command for Full System Backup

```bash
sudo rsync -aAXHv --delete / /path/to/backup/location/
```

### Breakdown of Flags:
- `-a`: Archive mode (preserves permissions, timestamps, etc.)
- `-A`: Preserve ACLs
- `-X`: Preserve extended attributes
- `-H`: Preserve hard links
- `-v`: Verbose output
- `--delete`: Remove files in destination that don't exist in source

## Important Considerations

### 1. Exclude Unnecessary Directories
```bash
sudo rsync -aAXHv --delete \
  --exclude={"/dev/*","/proc/*","/sys/*","/tmp/*","/run/*","/mnt/*","/media/*","/lost+found"} \
  / /path/to/backup/location/
```

### 2. For Remote Backups
```bash
sudo rsync -aAXHv --delete -e ssh \
  --exclude={"/dev/*","/proc/*","/sys/*","/tmp/*","/run/*","/mnt/*","/media/*","/lost+found"} \
  / user@remote-server:/backup/path/
```

### 3. With Progress and Logging
```bash
sudo rsync -aAXHv --progress --delete \
  --log-file=/var/log/rsync-backup.log \
  --exclude={"/dev/*","/proc/*","/sys/*","/tmp/*","/run/*","/mnt/*","/media/*","/lost+found"} \
  / /path/to/backup/location/
```

## Complete Backup Script

Create a script for automated backups:

```bash
#!/bin/bash
# backup-system.sh

BACKUP_DIR="/path/to/backup"
LOG_FILE="/var/log/system-backup.log"
DATE=$(date +%Y-%m-%d)

echo "Starting system backup: $(date)" | tee -a $LOG_FILE

sudo rsync -aAXHv --progress --delete \
  --exclude={"/dev/*","/proc/*","/sys/*","/tmp/*","/run/*","/mnt/*","/media/*","/lost+found","/home/*/.cache/*"} \
  / $BACKUP_DIR/root-backup-$DATE/ 2>&1 | tee -a $LOG_FILE

echo "Backup completed: $(date)" | tee -a $LOG_FILE
```

Make it executable:
```bash
chmod +x backup-system.sh
```

## Restoration Process

### To restore from backup:
```bash
# Boot from live USB, then:
sudo rsync -aAXHv /path/to/backup/location/ /mnt/new-root/
```

### Important restoration steps:
1. Reinstall GRUB: `sudo grub-install /dev/sdX`
2. Update GRUB config: `sudo update-grub`
3. Recreate excluded directories: `mkdir /mnt/new-root/{dev,proc,sys,tmp,run,mnt,media}`

## Additional Tips

### 1. Use --numeric-ids for cross-system backups
```bash
sudo rsync -aAXHv --numeric-ids --delete / /backup/location/
```

### 2. Compress during transfer (for remote backups)
```bash
sudo rsync -aAXHvz --delete / user@remote-server:/backup/
```

### 3. Schedule with cron
```bash
# Add to crontab: sudo crontab -e
0 2 * * 0 /path/to/backup-system.sh  # Run every Sunday at 2 AM
```

## What's Being Excluded and Why

- `/dev/`, `/proc/`, `/sys/`: Virtual filesystems
- `/tmp/`: Temporary files
- `/run/`: Runtime data
- `/mnt/`, `/media/`: Mount points
- `/lost+found`: Filesystem recovery data
- Cache directories: Save space

This method creates a bootable backup of your system that can be restored in case of failure.
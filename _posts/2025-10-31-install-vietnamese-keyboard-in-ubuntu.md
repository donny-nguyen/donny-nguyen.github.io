# Install Vietnamese Keyboard Input Method in Ubuntu

To install a Vietnamese keyboard input method in Ubuntu, here are the best options:

## Option 1: Use IBus Bamboo (Recommended)

IBus Bamboo is the most popular Vietnamese input method for Ubuntu. To install it:

```bash
sudo add-apt-repository ppa:bamboo-engine/ibus-bamboo
sudo apt-get update
sudo apt-get install ibus ibus-bamboo --install-recommends
ibus restart
```

Then configure it:
1. Go to **Settings** → **Keyboard** → **Input Sources**
2. Click the **+** button to add a new input source
3. Search for "Vietnamese" and select **Vietnamese (Bamboo)**
4. You can now switch between keyboard layouts using **Super+Space** (or the keyboard indicator in the top bar)

## Option 2: Use Built-in Vietnamese Layout

Ubuntu already has Vietnamese keyboard layouts available:

1. Open **Settings** → **Keyboard** → **Input Sources**
2. Click the **+** button
3. Search for "Vietnamese"
4. Select your preferred layout (e.g., Vietnamese, Vietnamese (VNI), Vietnamese (VIQR))

## Option 3: Use Fcitx with Unikey

If you prefer Fcitx:

```bash
sudo apt install fcitx fcitx-unikey
im-config -n fcitx
```

Log out and log back in, then configure Fcitx through its settings.

**IBus Bamboo is generally recommended** as it's actively maintained and works well with modern Ubuntu versions. It supports all popular Vietnamese typing methods (Telex, VNI, VIQR).
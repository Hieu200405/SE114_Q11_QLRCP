Generator for launcher icons

Steps to use:

1. Copy the image you uploaded in chat (Toto Cinema image) into this folder and name it `toto_cinema_source.png`.
2. Create a Python venv and install Pillow:

```bash
python -m venv .venv
source .venv/Scripts/activate   # Windows: .venv\Scripts\activate
python -m pip install --upgrade pip
python -m pip install pillow
```

3. Run the generator:

```bash
python generate_launcher_icons.py
```

4. Open the project in Android Studio; clean/rebuild. The adaptive icon XMLs have been updated to use `@drawable/toto_cinema` as the foreground. If you prefer the Image Asset wizard, you can still use it after replacing the images.

Notes:
- The script writes to `app/src/main/res/mipmap-*/ic_launcher.png` and `app/src/main/res/drawable/toto_cinema.png`.
- Keep backups of original resources in case you need to revert.

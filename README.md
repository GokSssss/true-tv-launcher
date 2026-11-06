# True TV Launcher

Everything your Android TV needs: quick access, effortless remote control, and a setup that feels like yours. Your favorite apps at your fingertips, powerful tools under the hood.

**Latest: True TV Launcher 2.2.3 + To TV 1.0.2.** Android 5.0 and newer (API 21+). Interface languages: English and Russian.

[Download APKs](https://github.com/GokSssss/true-tv-launcher/releases/latest) · [Русский](#по-русски) · [Build](#build-from-source) · [Obtainium](#obtainium)

## What you get

- A remote-friendly home screen with a configurable app grid, icon sizes, labels, themes, clock and status indicators.
- An integrated file manager and document picker: browse storage, open, copy, move, rename and share files, with the access granted by Android.
- **To TV:** share files from your Android phone to the TV over your local network. Files are saved to Downloads. Receiving runs while the TV's receive window is open; you choose whether each transfer needs confirmation.
- Optional expert tools for system apps, accessibility setup and IR diagnostics. Privileged operations require the relevant explicit ADB/root access. IR diagnostics do not apply a remote-control map to the driver.

Version 2.2.3 shows the system device name together with its model when available, and introduces a folder-with-plus-and-Wi-Fi receive icon.

## Install and send a file

1. Download `2.2.3-True-TV-Launcher.apk` and install it on the TV.
2. Download `1.0.2-To-TV.apk` and install it on your Android phone.
3. Connect the phone and TV to the same local network. The TV may use Ethernet.
4. Open the folder-with-plus-and-Wi-Fi icon beside the file manager on the TV's home screen.
5. On the phone, select a file → **Share → To TV → your TV**.

To TV appears in Android's Share menu and has no separate launcher icon. Closing the TV's receive window, pressing Home or switching apps stops receiving. Completed files remain in Downloads.

Release APKs keep the original project signing certificate, so compatible installations can be updated. Storage access, Home selection and privileged features depend on the Android version and firmware. The current release's checks and limits are recorded in [the import report](docs/GITHUB_IMPORT.md) and the historical [release passports](docs/publication/).

## Obtainium

Add this repository as a GitHub source:

```text
https://github.com/GokSssss/true-tv-launcher
```

Each release contains two different APKs. Select the application you want, or use an APK filename filter:

| Application | APK filename filter |
| --- | --- |
| True TV Launcher (TV) | `.*-True-TV-Launcher\.apk$` |
| To TV (phone) | `.*-To-TV\.apk$` |

For To TV, use the version read from its APK when available: its version is independent of the TV release tag.

## Build from source

The portable Linux build needs **JDK 21** and **Python 3**. Build tools are downloaded from pinned public URLs and checked with SHA256.

```bash
git clone https://github.com/GokSssss/true-tv-launcher.git
cd true-tv-launcher
python3 tools/build.py
python3 tools/build-phone.py
```

Output: `artifacts/2.2.3-True-TV-Launcher.apk` and `artifacts/1.0.2-To-TV.apk`.

The first TV build creates a local signing key in `.signing/`; the phone build uses the same key. A local build has a different signature from the published APKs. Updating an existing official installation requires the owner's original key, stored separately from this repository.

```bash
python3 tests/build.py
python3 tests/build-bridge.py
python3 tools/audit.py
```

The test build runs host checks and compiles the instrumentation APK. Run instrumentation only on a disposable emulator; some tests change test settings and package state. See [tests/README.md](tests/README.md). Host checks, emulator results and tests on physical hardware are distinct evidence.

A standard Gradle project is also included. It requires Gradle 8.9, JDK 17+ and Android SDK 34; a Gradle wrapper is not included. The portable build is the verified path for this import.

## По-русски

Всё, что нужно вашему Android TV: быстрый запуск, удобное управление пультом и настройка под себя. Любимые приложения под рукой, мощные инструменты — под капотом.

**True TV Launcher 2.2.3** устанавливается на телевизор. **To TV 1.0.2** — на телефон и появляется в меню «Отправить». Оба APK находятся в [последнем выпуске](https://github.com/GokSssss/true-tv-launcher/releases/latest).

Для передачи откройте на ТВ значок папки с плюсом и Wi-Fi рядом с файловым менеджером. На телефоне выберите файл → **Отправить → To TV → телевизор**. Нужна одна локальная сеть; телевизор может быть подключён кабелем. Файлы сохраняются в «Загрузки». Приём работает, пока окно ТВ открыто. Галочка «Принимать без подтверждения» действует только в этом окне.

Для Obtainium используйте ссылку на репозиторий выше и выберите APK лаунчера или телефонного помощника. Фильтры имён приведены в таблице. Сборка, тесты и происхождение кода описаны в документации.

## Project records

Project **22.1**; older records use workspace number 30. This is a Java/native Android implementation developed from an agreed behavior description. Its relationship to the earlier Simple Launcher Reborn project, implementation origins and public tool dependencies are documented in [PROVENANCE.md](docs/PROVENANCE.md).

[Project on Google Drive](https://drive.google.com/drive/folders/1HSemiU8S2f8ZPpVLLfJyvjLCbD7PATmn) · [Original 2.2.3 release](https://drive.google.com/drive/folders/1jIjWhUWxlFFX1Liu5pGeunNp_yhs1Uvb)

The GitHub source package contains the application code, build tools, tests and public documentation. Private device recordings and internal working records remain in Drive. See [the import report](docs/GITHUB_IMPORT.md) for the exact package distinction and checks.

# True TV Launcher — project 22.1

The current imported release is TV 2.2.3 / code 233 and phone To TV 1.0.2 / code 102. Both share the `bridge` and `bridge-android` sources. Public interfaces are Java/native Android Views with minSdk 21 and target/compileSdk 34.

Before publishing, re-list the live Drive project and source folders, and read the matching current passport and project journals. The source of truth is https://drive.google.com/drive/folders/1HSemiU8S2f8ZPpVLLfJyvjLCbD7PATmn . The latest direct owner instructions override historical snapshots. Preserve existing Drive IDs, sharing and immutable published APK/SOURCE files. Never publish a cached version as latest without checking for updates.

Keep source origins honest: follow docs/PROVENANCE.md; do not add original/decompiled Simple Launcher APK code, smali, decoded resources or old runtime libraries. Do not claim formal clean-room legal certification. Public build tools are pinned by URL and SHA256 in tools/bootstrap.py.

Public GitHub source excludes private owner recordings, reference photos, credentials and internal Drive working records. Preserve those in their existing private storage. Never commit signing keys, passwords, authenticated URLs, local build outputs or caches. Official signing material stays outside the repository. A local key is suitable for build verification, not for compatible official updates.

Do not change application code or version numbers for documentation-only work. Keep receiving limited to an explicitly open TV receive window; no automatic root/ADB grants, hidden background receiver or changes to other devices as part of publication. Privileged app features retain their explicit confirmations and existing limits.

Portable checks: python3 tools/build.py, python3 tools/build-phone.py, python3 tests/build.py, python3 tests/build-bridge.py, python3 tools/audit.py. Test stateful Android operations only on disposable emulators. Distinguish host checks, instrumentation compilation, emulator execution and physical-device evidence. No subagents unless the owner requests them.

For a release, verify APK package/version/signature, SOURCE manifest and SHA256/CRC, then verify remote commits and downloaded release assets. Attach original signed TV and phone APKs. Clearly distinguish the public GitHub source package from the full private Drive source archive. Preserve origins, changelogs and prior check limitations. The owner asks for a Drive project link in project progress and final messages.

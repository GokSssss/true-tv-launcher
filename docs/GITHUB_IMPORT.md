# GitHub import: 2.2.3 + To TV 1.0.2

Prepared on 6 November 2026 from the current release of project 22.1 on Google Drive. The public repository contains application code, shared bridge modules, build scripts, tests, design materials and documentation. Runtime code and resources match the original source archive.

## Package distinction

The full Drive archive also contains private photos and a video from owner devices, internal instructions, live working journals and Drive state. Those files remain in their original storage and are excluded from public Git history and release archives. Public README and agent instructions describe this repository. Historical provenance and test reports are retained.

The original Drive ZIP is immutable. Its SHA256 is `730b575b4c162364b3d288f9cb9297dc1dc75513896c0a1b897f8f19d992139c`. The separately named GitHub source ZIP and GitHub's automatic Source code archives have different content and hashes. `docs/SOURCE_INPUTS.sha256` describes this public source snapshot, not the full Drive archive.

## Official APKs

| File | SHA256 |
| --- | --- |
| `2.2.3-True-TV-Launcher.apk` | `2dfac9e4e80eaef425421b3f4f7eea3ded0084a5b05f242989c63f2c9aae2dfd` |
| `1.0.2-To-TV.apk` | `435d0c0a4cd61b53ec2362f281672d9b5644ce9c411455d545ab384a0d9528f4` |

Both original APKs have verified v1/v2 signatures and certificate SHA256 `a5a632bd6a8b8e2c3e3623ee245c3a5c3652c0c6b32751a35a49d6a9c2e8fa33`. These exact files are used for GitHub Releases. The original signing key was not downloaded.

The full original ZIP passed CRC and all 474 input-manifest checks. Public files are scanned for signing material and common credential patterns before upload. New physical-TV, root/ADB/IR-device or native Android tests are not part of this import. Historical test reports do not imply new hardware validation.

## Checks performed for the public source

Both portable Linux builds passed with JDK 21 and the pinned ECJ/D8/aapt2/apksig tools. All runtime sources, resources, tests and build scripts in `app`, `phone`, `bridge`, `bridge-android`, `tests` and `tools` were compared byte-for-byte with the full original archive.

The rebuilt TV APK matched all six entries outside `META-INF/` in the official APK; the phone APK matched all four. The full APKs differ because the local build uses a newly generated local test key. GitHub Release assets are the original APKs with the official certificate.

Host suites passed: 84 domain, 35 expert commands/RSA/ADB, 18008 geometry/RSA, 127 IR, 41 export, 62 filesystem/MIME/path, 32 restricted settings, 53 Accessibility/Home, 10 picker/IR burst, 69 defaults/power, 21 volume, 25 bridge/TLS/stream — **18567 assertions in total**. These test counts are those reported by the existing project suites. Instrumentation APK compilation passed; instrumentation was not executed during this import.

The APK audit passed: 483 own TV runtime classes, expected package/version, resource alignment and no bundled legacy runtime classes, third-party fonts or native libraries. Gradle and physical-device tests were not run.

The public source manifest was generated after documentation preparation. `v2.2.3` identifies this public snapshot. Release file hashes are listed in the attached `2.2.3-SHA256SUMS.txt`.

## Sources

- [Drive project](https://drive.google.com/drive/folders/1HSemiU8S2f8ZPpVLLfJyvjLCbD7PATmn)
- [Original release and full passport](https://drive.google.com/drive/folders/1jIjWhUWxlFFX1Liu5pGeunNp_yhs1Uvb)
- [Original full SOURCE](https://drive.google.com/file/d/1O7eP5zzNkPAxCRYmHnlruF3EOBiG05b0/view)

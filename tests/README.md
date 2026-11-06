# Проверки

`python3 tools/build.py`, затем `python3 tests/build.py` и `python3 tools/audit.py`.
Host-проверки проверяют модель настроек, закрытые команды системного менеджера,
небезопасные имена/пользователей, точное перечитывание состояния, RSA AUTH,
структуру публичного ключа ADB, каналы/контрольные суммы/exit status и тайм-аут runner.
Поддельный adbd в ExpertContractCheck не заменяет проверку Android/root-менеджера.

Нативные тесты запускать **только на отдельном одноразовом эмуляторе** Android.
После установки APK приложения и `tests/build/tests.apk`:

```
adb shell am instrument -w ru.truetv.launcher.tests/ru.truetv.launcher.UiCheck
adb shell am instrument -w ru.truetv.launcher.tests/ru.truetv.launcher.AppearanceCheck
```

UiCheck сбрасывает тестовые настройки лаунчера. AppearanceCheck проверяет реальный
фокус, радиовыбор, обе позиции Close/Restart без скрытия, темы и элементы настроек.

ExpertCheck выполняет реальные изменения **только** тестового системного пакета
`com.android.dreams.basic` у пользователя0. Нужны этот установленный пакет,
ADB эмулятора в TCP-режиме на localhost:5555 и разрешённое тестовое подключение:

```
adb tcpip 5555
adb shell am instrument -w ru.truetv.launcher.tests/ru.truetv.launcher.ExpertCheck
```

Тест использует непосредственно ExpertBackend приложения: доказывает локальность,
отклоняет защищённые Settings, отключает/включает/удаляет для пользователя/
восстанавливает безобидную заставку. После теста проверьте восстановление;
если процесс теста оборван, восстановите **на этом же эмуляторе**:

```
adb shell pm install-existing --user 0 com.android.dreams.basic
adb shell pm default-state --user 0 com.android.dreams.basic
```

Не запускать на телевизоре владельца. Это не проверка root-менеджера или OEM Grundig.
ControlCheck/ExtrasCheck относятся к отдельным функциям; их старые ограничения
не снимаются новыми проверками. Фактические прогоны и отказы среды перечислены
в паспорте и техническом журнале, без переноса PASS с предыдущего APK.

Для медленного эмулятора без KVM тестовый глобальный параметр Watchdog в Android14
называется `system_server_watchdog_timeout_ms`, а не `watchdog_timeout_millis`.
После работы удалить тестовые переопределения и остановить эмулятор. Параметр
`ro.hw_timeout_multiplier` задавать только в командной строке одноразового эмулятора.

2.0.3: `DesktopCheck` проверяет180геометрическихконфигураций иPKCS8RSAвhost. `LayoutCheck` — неразрушающаяинструментация UI/рисования/Conscrypt без ADBподключения, запускается `adb shell am instrument -w ru.truetv.launcher.tests/ru.truetv.launcher.LayoutCheck`. Финальныйпрогон18PASSнаAndroid9/API28; getUiAutomationскриншотынужноосматривать, системныйANRdialogможетперехватитьINJECT_EVENTS. ЛокальныйadbdэмуляторанеобязательнослушаетTCPдажепослеadb tcpip; отказсети неявляетсяпроверкойUIилимутаций. ОбразGoogle x86_64-28_r04.zipпроверенпоразмеру564792723иофициальномуSHA1d47a85c8f4e9fd57df97814ad8884eeb0f3a0ef0, не входит вмобильнуюсборку/SOURCE.

2.0.5: IrCheck — host-проверки разборщика NEC/неверных кадров, диапазонов, двух новых совпадений, отбрасывания истории/дубликатов, исключения адресов Phys/Uniq из отчёта, закрытого списка путей и отказа без uid0. Fake runner не подтверждает работу su или настоящего ИК-драйвера. IrUiCheck проверяет новый экран/фокус, частный черновик/подтверждение/удаление и TXT без запроса su, только на одноразовом эмуляторе. Команда: `adb shell am instrument -w ru.truetv.launcher.tests/ru.truetv.launcher.IrUiCheck`. Не запускать на устройстве владельца: тест временно меняет ir_draft и создаёт/удаляет собственный отчёт.

2.0.6: ReportCheck проверяет реальную запись во временную папку и отсутствие перезаписи, UTF8, scoped publish/rollback на Store fake. IrUiCheck теперь проверяет public Downloads/отсутствие private fallback; на одноразовомAPI28 перед прогоном разрешения READ/WRITE_EXTERNAL_STORAGE выдаются тестовой shell-командой. Это проверка положительного пути, не ручного диалога разрешений; после теста удаляется только созданный тестом файл. Android29 MediaStore adapter здесь скомпилирован, транзакционный алгоритм проверен fake Store, нативный scoped-provider требует отдельной проверки. LayoutCheck использует8dp и пишет скриншоты в layout<versionCode>.

2.2.1: текущая сессия приёма проверяется `python3 tests/session/build.py`, затем `am instrument -w ru.truetv.session.tests/ru.truetv.launcher.SessionCheck` и `.../ru.truetv.launcher.PhoneSessionCheck`. SessionCheck — реальный TV Activity/Downloads и локальный TLS-клиент; PhoneSessionCheck — реальный телефонный Share/mDNS/progress с локальным fixture receiver. Это не физическая пара. SourceProvider — только тестовый APK, не production. Старые BridgeNativeCheck/BridgePhoneCheck относятся к230: их исходники сохранены для истории, они не компилируются против удалённых permanent-receiver классов231.

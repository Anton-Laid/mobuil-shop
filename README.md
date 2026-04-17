# Universeti

Android-приложение магазина с двумя ролями: **оператор** (управление товарами и витринами) и **пользователь** (поиск товара на карте магазина).

## Технологии

- **Язык:** Java
- **Android:** minSdk 24, targetSdk 34, compileSdk 34
- **Сборка:** Android Gradle Plugin 8.1.4, Gradle 8.5, JDK 21
- **Хранение:** JSON-файлы в `filesDir` (`users.json`, `products.json`, `shelves.json`)
- **Картинки:** Glide 4.16.0 + локальное копирование из галереи в `filesDir/images/`
- **Тесты:** JUnit 4.13.2 + Robolectric 4.11.1

## Запуск

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
cd UniversetiApp
./gradlew :app:assembleDebug         # сборка APK
./gradlew :app:installDebug          # установка на подключённое устройство
./gradlew :app:testDebugUnitTest     # юнит-тесты
```

## Роли и экраны

| Экран                  | Роль         | Что делает                                                                                                                  |
| ---------------------- | ------------ | --------------------------------------------------------------------------------------------------------------------------- |
| `LoginActivity`        | все          | Вход по логину/паролю                                                                                                       |
| `RegisterActivity`     | все          | Регистрация, выбор роли                                                                                                     |
| `OperatorHomeActivity` | оператор     | Список товаров, добавление/редактирование (название, цена, фото из галереи, координаты на карте), режим перестановки витрин |
| `UserHomeActivity`     | пользователь | Поиск товара, карта магазина с маркером найденного товара, список товаров                                                   |

Выход из аккаунта — через пункт меню в тулбаре.

## Структура пакетов

```
com.example.universeti
├── model/           Product, Shelf, User — модели с toJson/fromJson
├── data/            *Repository — загрузка/сохранение JSON
└── ui/              Activity, Adapter, Dialog
```

## Карта магазина

- Координаты нормализованы в диапазон `[0..1]` (процент от размера контейнера), что делает раскладку независимой от экрана.
- Витрины (`Shelf`) — прямоугольники, товары (`Product`) — точки. При перетаскивании витрины в режиме перестановки все товары, попадающие в её границы, смещаются вместе с ней.
- Вход и касса — фиксированные маркеры.

## Тесты

29 юнит-тестов, покрывают модели и репозитории:

- `model/` — JSON-roundtrip, геттеры/сеттеры, логика `Shelf.contains`
- `data/` — персистентность, уникальность ID, дубликаты пользователей, case-insensitive логин

UI-слой (Activity) не покрыт — для него нужны instrumentation-тесты на эмуляторе.

## Известные ограничения

- Пароли хранятся в открытом виде в JSON — **только для учебных целей**, не для продакшена.
- `allowBackup=true` в манифесте — JSON с паролями попадает в авто-бэкап.
- Нет миграции схемы JSON — при изменении моделей старые данные нужно удалять вручную.

# Как запустить Universeti

Руководство по запуску приложения в **Android Studio** и **VS Code**.

---

## Требования

- **JDK 21** (поставляется с Android Studio: `/Applications/Android Studio.app/Contents/jbr/Contents/Home` на macOS)
- **Android SDK** (API 34, build-tools 34.0.0)
- **Android-устройство или эмулятор** с Android 7.0+ (API 24)

Проверить, что `local.properties` в `UniversetiApp/` содержит путь к SDK:

```properties
sdk.dir=/Users/<you>/Library/Android/sdk
```

---

## Android Studio

### 1. Открыть проект

1. Запустить **Android Studio**.
2. `File → Open…` → выбрать папку `UniversetiApp/` (именно её, не родительскую).
3. Дождаться окончания Gradle sync (внизу справа).

Если sync падает с ошибкой JDK — `File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK` → выбрать `Embedded JDK 21`.

### 2. Запустить приложение

1. Подключить устройство через USB (с включённым USB-отладкой) **или** запустить эмулятор: `Tools → Device Manager → ▶`.
2. В тулбаре выбрать конфигурацию **app** и целевое устройство.
3. Нажать **▶ Run** (Shift+F10) или **🐞 Debug** (Shift+F9).

APK соберётся, установится и запустится автоматически.

### 3. Запустить тесты

- Юнит-тесты: клик правой кнопкой по папке `app/src/test/java` → `Run 'Tests in 'java'`.
- Одиночный тест: открыть файл, клик по зелёной стрелке рядом с классом или методом.
- Отчёт: `app/build/reports/tests/testDebugUnitTest/index.html`.

---

## VS Code

Поддержка Android в VS Code ограничена — проще использовать терминал + Gradle CLI. IDE даёт подсветку Java и навигацию, полноценной сборки/дебага нативно нет.

### 1. Рекомендуемые расширения

- **Extension Pack for Java** (Microsoft) — подсветка, автокомплит, навигация
- **Gradle for Java** (Microsoft) — запуск Gradle-задач из сайдбара
- **Android iOS Emulator** (необязательно) — запуск эмулятора из VS Code

### 2. Открыть проект

```bash
code /Applications/Аpp/Universeti/app/UniversetiApp
```

Первый запуск Java-индексации может занять пару минут.

### 3. Собрать и установить APK из терминала

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
cd /Applications/Аpp/Universeti/app/UniversetiApp

./gradlew :app:assembleDebug                 # собрать APK
./gradlew :app:installDebug                  # установить на подключённое устройство
```

Запустить приложение на устройстве:

```bash
~/Library/Android/sdk/platform-tools/adb shell am start \
  -n com.example.universeti/.ui.LoginActivity
```

### 4. Логи

```bash
~/Library/Android/sdk/platform-tools/adb logcat | grep -i universeti
```

### 5. Запустить тесты

```bash
./gradlew :app:testDebugUnitTest
```

Отчёт: `app/build/reports/tests/testDebugUnitTest/index.html` — открыть в браузере.

### 6. Эмулятор из CLI

```bash
~/Library/Android/sdk/emulator/emulator -list-avds           # список AVD
~/Library/Android/sdk/emulator/emulator -avd <avd-name> &    # запустить
```

AVD создаётся один раз в Android Studio (`Tools → Device Manager`), затем используется из любой IDE.

---

## Частые проблемы

| Проблема                                        | Решение                                                                                                                        |
| ----------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `Unsupported class file major version`          | `JAVA_HOME` указывает на старую JDK. Установить JDK 21: `export JAVA_HOME=".../Android Studio.app/Contents/jbr/Contents/Home"` |
| `SDK location not found`                        | Создать `local.properties` с `sdk.dir=...`                                                                                     |
| `adb: command not found`                        | Использовать полный путь `~/Library/Android/sdk/platform-tools/adb` или добавить его в `PATH`                                  |
| Gradle sync зависает                            | `./gradlew --stop`, удалить `.gradle/`, пересинхронизировать                                                                   |
| Тест падает с `RuntimeException` в `JSONObject` | Тест использует `org.json` — добавить `@RunWith(RobolectricTestRunner.class)`                                                  |

---

## Что дальше

Описание приложения, архитектуры и структуры пакетов — в [README.md](README.md).

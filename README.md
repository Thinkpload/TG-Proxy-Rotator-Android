# TG Proxy Rotator — Android

Android-порт [TG-Proxy-Rotator](../TG-Proxy-Rotator). Загружает рабочие MTProto-прокси и применяет их в Telegram через `tg://proxy?...` deep-link.

## Текущий статус: v0 скаффолд

Реализовано:
- Загрузка прокси из тех же источников, что и Python-версия
- TCP-проверка живости (параллельная, через корутины)
- Сортировка по пингу
- UI на Jetpack Compose: список прокси + кнопка Apply → открывает Telegram

Не реализовано (в следующих итерациях):
- Фоновая ротация через WorkManager + уведомление
- Избранное, фильтры по странам/пингу
- Настройки источников
- Кэш на диск

## Сборка

### Локально

Открыть папку в Android Studio (Hedgehog+). Gradle wrapper не закоммичен — Android Studio сгенерирует его при первом импорте, либо запусти `gradle wrapper` в этой папке.

Release APK:

```powershell
.\gradlew assembleRelease
```

Готовый файл: `app/build/outputs/apk/release/app-release.apk`. Подписан debug-ключом — устанавливается через `adb install`, но в Play Store не загрузить.

Min SDK 24 (Android 7.0), target 34. Kotlin 2.0.21, AGP 8.5.0.

### GitHub Actions

Workflow [.github/workflows/build-apk.yml](.github/workflows/build-apk.yml) собирает release APK:

- **На каждый push в `main`** или ручной запуск через **Actions → Build APK → Run workflow** — APK доступен как artifact в run-е.
- **На push тега `v*`** — дополнительно создаётся GitHub Release с прикреплённым APK и авто-сгенерированными release notes.

Выпустить новую версию:

```powershell
# обнови versionCode/versionName в app/build.gradle.kts
git tag v0.2.0
git push origin v0.2.0
```

Релиз появится во вкладке **Releases** репозитория.

## Архитектура

- [ProxyRepository.kt](app/src/main/java/com/thinkpload/tgproxyrotator/ProxyRepository.kt) — fetch + check, аналог `fetch_proxies` / `filter_alive_proxies` из Python
- [MainActivity.kt](app/src/main/java/com/thinkpload/tgproxyrotator/MainActivity.kt) — Compose UI и ViewModel

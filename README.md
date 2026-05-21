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

Открыть папку в Android Studio (Hedgehog+). Gradle wrapper не закоммичен — Android Studio сгенерирует его при первом импорте, либо запусти `gradle wrapper` в этой папке.

Min SDK 24 (Android 7.0), target 34.

## Архитектура

- [ProxyRepository.kt](app/src/main/java/com/thinkpload/tgproxyrotator/ProxyRepository.kt) — fetch + check, аналог `fetch_proxies` / `filter_alive_proxies` из Python
- [MainActivity.kt](app/src/main/java/com/thinkpload/tgproxyrotator/MainActivity.kt) — Compose UI и ViewModel

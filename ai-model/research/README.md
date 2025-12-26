# NetHelt - AI-Model - generowanie danych do trenowania modelu

Z powodu braku danych rzeczywistych ruchu w sieci, oraz problemu ze znalezieniem zbiorów danych w internecie, w których znajdują się:
- stan normalny,
- stan degradacji,
- stan awarii

dane zostały spreparowane syntetycznie poprzez skrypt wykonany w `python 3.12` z użyceim bibliotek: `pandas` i `pyyaml` do pobierania konfiguracji.

## Instrukcja uruchomieniowa

Aby uruchomić generowanie danych, należy najpierw przygotować plik konfiguracyjny (`config.yaml`) znajdujący się w odpowiednim folderze.

Po skonfigurowaniu pliku, lub upewnieniu się, że konfiguracja odpowiada naszym wymaganiom, możemy uruchomić program.

Na stanowisku, gdzie uruchamiamy program musi być zainstalowany `python` w wersji 3.12 lub wyższej.

Przykładowo, jeśli chcemy wygenerować dane dla operacji `PING`, należy wywołać komendę:

```bash
python main.py --ping
```

Dostępne operacje można znaleźć pod komendą:

```bash
python main.py --help
```
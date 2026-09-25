# Stubs des Zahlungsdienstleisters

Eine Quelle für zwei Verwender: die Integrationstests (WireMock in-process,
`usingFilesUnderDirectory("wiremock")`) und der Compose-Stack (Container
`wiremock/wiremock`, Verzeichnis als Volume). Verhalten:

| Endpunkt | Bedingung | Antwort |
| --- | --- | --- |
| `POST /payments` | `method = CARD`, `amount < 9000` | `AUTHORIZED`, zufällige Referenz |
| `POST /payments` | `method = CARD`, `amount ≥ 9000` | `DECLINED` |
| `POST /payments` | `method = INSTALLMENTS` | `PENDING` – Bestätigung kommt später per Callback |
| `POST /credit-checks` | `amount ≤ 5000` | `approved: true` |
| `POST /credit-checks` | `amount > 5000` | `approved: false` |

Fehlerfälle (Timeout, HTTP 500) werden in den Integrationstests programmatisch
gestubbt, weil sie im Compose-Stack den Happy Path stören würden.

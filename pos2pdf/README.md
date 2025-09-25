# Generatore di Report PDF da JSON
Tool per generare un report PDF formattato a partire da un file JSON con un formato specifico.

## Installa le dipendenze (se non l'hai già fatto):

    pip install pandas reportlab

## Uso base:

    python json_to_pdf.py data-export.json report.pdf

### Con orientamento landscape:

    python json_to_pdf.py data-export.json report.pdf landscape

### Con orientamento landscape e numero di righe per pagina:

    python json_to_pdf.py data-export.json report.pdf landscape 20

## Struttura JSON supportata
```json
[{
  "region": "Sicilia",
  "province": "Messina", 
  "city": "Scaletta Zanclea",
  "zipCode": "20028",
  "address": "Corso Italia",
  "franchiseName": "Euronics"
}, { }]
```

## Query di estrazione dati:
```
    {"type": "PHYSICAL"}
    {"region": 1, "province": 1, "city": 1, "zipCode": 1, "address": 1, "franchiseName": 1, "_id": 0}
```
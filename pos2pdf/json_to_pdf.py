#!/usr/bin/env python3
"""
Script per convertire un file JSON in PDF con tabelle multi-pagina
Uso: python json_to_pdf.py input.json output.pdf
"""

import pandas as pd
import json
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.lib.pagesizes import landscape
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import sys
import os


def calculate_column_widths(df, page_width):
    """
    Calcola le larghezze delle colonne basandosi sul contenuto
    """
    available_width = page_width - 2 * inch  # Margini
    num_cols = len(df.columns)

    # Larghezza minima per colonna
    min_width = available_width / num_cols

    col_widths = []
    for col in df.columns:
        # Considera la lunghezza dell'header e del contenuto più lungo
        header_len = len(str(col))
        max_content_len = df[col].astype(str).str.len().max()
        content_width = max(header_len, max_content_len) * 6  # ~6 punti per carattere

        col_widths.append(max(min_width, content_width))

    # Normalizza per non superare la larghezza disponibile
    total_width = sum(col_widths)
    if total_width > available_width:
        col_widths = [w * available_width / total_width for w in col_widths]

    return col_widths


def create_table_style():
    """
    Crea lo stile per la tabella
    """
    pdfmetrics.registerFont(TTFont('Titillium-Web-Regular', 'TitilliumWeb-Regular.ttf'))
    pdfmetrics.registerFont(TTFont('Titillium-Web-Bold', 'TitilliumWeb-Bold.ttf'))

    return TableStyle([
        # Header style
        ('BACKGROUND', (0, 0), (-1, 0), colors.Color(0, 115/255, 230/255)),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('FONTNAME', (0, 0), (-1, 0), 'Titillium-Web-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 10),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),

        # Data rows style
        ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
        ('TEXTCOLOR', (0, 1), (-1, -1), colors.black),
        ('FONTNAME', (0, 1), (-1, -1), 'Titillium-Web-Regular'),
        ('FONTSIZE', (0, 1), (-1, -1), 8),
        ('ALIGN', (0, 1), (-1, -1), 'LEFT'),

        # Grid
        ('GRID', (0, 0), (-1, -1), 1, colors.black),

        # Alternating row colors
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.Color(242/255, 242/255, 242/255)]),
    ])


def split_dataframe_for_pages(df, max_rows_per_page=25):
    """
    Divide il DataFrame in chunks per le pagine
    """
    chunks = []
    for i in range(0, len(df), max_rows_per_page):
        chunk = df.iloc[i:i + max_rows_per_page].copy()
        chunks.append(chunk)
    return chunks


def wrap_text_in_cells(df, max_chars=30):
    """
    Tronca il testo nelle celle se troppo lungo
    """
    df_copy = df.copy()
    for col in df_copy.columns:
        df_copy[col] = df_copy[col].astype(str).apply(
            lambda x: x[:max_chars] + '...' if len(x) > max_chars else x
        )
    return df_copy


def json_to_pdf(json_file, pdf_file, orientation='portrait', max_rows_per_page=25):
    """
    Converte un file JSON in PDF con tabelle multi-pagina

    Args:
        json_file (str): Percorso del file JSON di input
        pdf_file (str): Percorso del file PDF di output
        orientation (str): 'portrait' o 'landscape'
        max_rows_per_page (int): Numero massimo di righe per pagina
    """

    # Leggi il JSON
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            json_data = json.load(f)

        # Converti in DataFrame
        if isinstance(json_data, list):
            # Ordina i dati per regione, provincia, città, insegna
            json_data_sorted = sorted(json_data, key=lambda x: (x.get('region', '–'), x.get('province', '–'), x.get('city', '–'), x.get('franchiseName', '–')), reverse=False)
            df = pd.DataFrame(json_data_sorted)
        elif isinstance(json_data, dict):
            # Se è un dict, prova a convertirlo in lista
            df = pd.DataFrame([json_data])
        else:
            raise ValueError("Il JSON deve essere una lista di oggetti o un singolo oggetto")

        df = df.fillna('-')  # Sostituisce i NaN, non è un semplice trattino
        print(f"Letto JSON con {len(df)} righe e {len(df.columns)} colonne")
        print(f"Colonne: {', '.join(df.columns)}")

    except json.JSONDecodeError as e:
        print(f"Errore nel parsing del JSON: {e}")
        return False
    except Exception as e:
        print(f"Errore nella lettura del JSON: {e}")
        return False

    # Imposta dimensioni e orientamento pagina
    if orientation == 'landscape':
        page_size = landscape(A4)
    else:
        page_size = A4

    # Crea il documento PDF
    doc = SimpleDocTemplate(
        pdf_file,
        pagesize=page_size,
        rightMargin=0.5 * inch,
        leftMargin=0.5 * inch,
        topMargin=0.5 * inch,
        bottomMargin=0.25 * inch
    )

    # Elementi da aggiungere al PDF
    elements = []

    # Stile per il titolo
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=16,
        spaceAfter=30,
        textColor=colors.darkblue,
    )

    # Aggiungi titolo
    #title = Paragraph(f"Lista negozi - {os.path.basename(json_file).split(".")[0]}", title_style)
    #elements.append(title)
    #elements.append(Spacer(1, 12))

    # Tronca testo troppo lungo nelle celle
    df_formatted = wrap_text_in_cells(df)

    # Dividi in pagine
    page_chunks = split_dataframe_for_pages(df_formatted, max_rows_per_page)

    print(f"Creando {len(page_chunks)} pagine...")

    for i, chunk in enumerate(page_chunks):
        print(f"Processando pagina {i + 1}/{len(page_chunks)}...")

        # Crea i dati per la tabella (header + data)
        table_header = ["Regione", "Provincia", "Città", "CAP", "Indirizzo", "Insegna"]
        table_data = [table_header] + [
            [row.get('region', '-'), row.get('province', '-'), row.get('city', '-'), row.get('zipCode', '-'),
             row.get('address', '-'), row.get('franchiseName', '-')]
            for _, row in chunk.iterrows()
        ]

        # Calcola larghezze colonne
        col_widths = calculate_column_widths(chunk, page_size[0])

        # Crea la tabella
        table = Table(table_data, colWidths=col_widths, repeatRows=1)
        table.setStyle(create_table_style())

        # Aggiungi il "nuova pagina" , solo se non è la prima
        if i > 0:
            elements.append(PageBreak())

        page_number_style = ParagraphStyle(
            'PageHeader',
            fontName='Titillium-Web-Regular',
            fontSize=6,
            textColor=colors.Color(23/255, 50/255, 77/255),
            alignment=2,  # 0=LEFT, 1=CENTER, 2=RIGHT
            spaceAfter=1,
        )
        page_number = Paragraph(f"Pagina {i + 1} di {len(page_chunks)}", page_number_style)

        elements.append(Spacer(1, 20))
        elements.append(table)
        elements.append(Spacer(1, 20))
        elements.append(page_number)

    # Genera il PDF
    try:
        doc.build(elements)
        print(f"PDF creato con successo: {pdf_file}")
        return True
    except Exception as e:
        print(f"Errore nella creazione del PDF: {e}")
        return False


def main():
    """
    Funzione main per l'uso da command line
    """
    if len(sys.argv) < 3:
        print("Uso: python json_to_pdf.py <file_input.json> <file_output.pdf> [landscape/portrait] [righe_per_pagina]")
        print("Esempio: python json_to_pdf.py dati.json report.pdf landscape 20")
        return

    csv_file = sys.argv[1]
    pdf_file = sys.argv[2]
    orientation = sys.argv[3] if len(sys.argv) > 3 else 'portrait'
    max_rows = int(sys.argv[4]) if len(sys.argv) > 4 else 25

    # Verifica che il file CSV esista
    if not os.path.exists(csv_file):
        print(f"Errore: File {csv_file} non trovato")
        return

    # Converte
    success = json_to_pdf(csv_file, pdf_file, orientation, max_rows)

    if success:
        print(f"\n✅ Conversione completata!")
        print(f"📄 Input: {csv_file}")
        print(f"📋 Output: {pdf_file}")
    else:
        print("\n❌ Errore durante la conversione")


if __name__ == "__main__":
    main()
#!/usr/bin/env python3
"""
Script per convertire un file JSON in PDF con tabelle multi-pagina
Gestisce sia punti vendita ONLINE che PHYSICAL
Uso: python json_to_pdf.py input.json output.pdf [landscape/portrait] [righe_per_pagina]
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


def calculate_column_widths(table_data, page_width):
    """
    Calcola larghezze colonne proporzionali al contenuto e header, normalizzando per non superare la larghezza disponibile
    """
    from reportlab.lib.units import inch
    available_width = page_width - 2 * inch  # Margini

    num_cols = len(table_data[0])
    max_lens = [0] * num_cols
    for row in table_data:
        for i, cell in enumerate(row):
            max_lens[i] = max(max_lens[i], len(str(cell)))

    total_len = sum(max_lens)
    if total_len == 0:
        return [available_width / num_cols] * num_cols

    # Calcola larghezze proporzionali e normalizza
    raw_widths = [(l / total_len) * available_width for l in max_lens]
    scale = available_width / sum(raw_widths)
    col_widths = [w * scale for w in raw_widths]
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


def wrap_text_in_cells(df, max_chars=50):
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
    Gestisce sia negozi ONLINE che PHYSICAL (prima ONLINE, poi PHYSICAL)

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

        # Converti in lista se necessario
        if isinstance(json_data, dict):
            json_data = [json_data]
        elif not isinstance(json_data, list):
            raise ValueError("Il JSON deve essere una lista di oggetti o un singolo oggetto")

        print(f"Letto JSON con {len(json_data)} record totali")

    except json.JSONDecodeError as e:
        print(f"Errore nel parsing del JSON: {e}")
        return False
    except Exception as e:
        print(f"Errore nella lettura del JSON: {e}")
        return False

    # Separa i dati per tipo (ONLINE e PHYSICAL)
    online_data = [item for item in json_data if item.get("type") == "ONLINE"]
    physical_data = [item for item in json_data if item.get("type") == "PHYSICAL"]

    print(f"Negozi ONLINE: {len(online_data)}")
    print(f"Negozi PHYSICAL: {len(physical_data)}")

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

    # Stili
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=16,
        spaceAfter=30,
        textColor=colors.darkblue,
    )
    page_number_style = ParagraphStyle(
        'PageHeader',
        fontName='Titillium-Web-Regular',
        fontSize=6,
        textColor=colors.Color(23/255, 50/255, 77/255),
        alignment=2,  # 0=LEFT, 1=CENTER, 2=RIGHT
        spaceAfter=1,
    )

    total_pages = 0

        # --- NEGOZI ONLINE ---
    if online_data:
        print(f"\nProcessando negozi ONLINE...")

        # Ordina per insegna
        online_data_sorted = sorted(online_data, key=lambda x: x.get('franchiseName', '-'))
        df_online = pd.DataFrame(online_data_sorted).fillna('-')
        df_online_formatted = wrap_text_in_cells(df_online)

        # Dividi in pagine
        online_chunks = split_dataframe_for_pages(df_online_formatted, max_rows_per_page)

        # Titolo Punti vendita Online
        elements.append(Paragraph("Punti vendita Online", ParagraphStyle(
        'SectionTitle',
        parent=title_style,
        spaceAfter=20
        )))

        for i, chunk in enumerate(online_chunks):
            print(f"  Pagina ONLINE {i + 1}/{len(online_chunks)}...")

            table_header = ["Insegna", "Website"]
            table_data = [table_header] + [
                [
                    row.get('franchiseName', '-'),
                    row.get('website', '-'),
                ]
                for _, row in chunk.iterrows()
            ]

            col_widths = calculate_column_widths(table_data, page_size[0])

            # Crea la tabella
            table = Table(table_data, colWidths=col_widths, repeatRows=1)
            table.setStyle(create_table_style())

            # Se non è la prima tabella, aggiungi un page break PRIMA
            if total_pages > 0:
                elements.append(PageBreak())

            total_pages += 1
            elements.append(table)

    # --- NEGOZI PHYSICAL ---
    if physical_data:
        print(f"\nProcessando negozi PHYSICAL...")

        # Ordina per insegna, regione, provincia, città.
        physical_data_sorted = sorted(
            physical_data,
            key=lambda x: (
                x.get('franchiseName', '-'),
                x.get('region', '-'),
                x.get('province', '-'),
                x.get('city', '-'),
            )
        )
        df_physical = pd.DataFrame(physical_data_sorted).fillna('-')
        df_physical_formatted = wrap_text_in_cells(df_physical)
        physical_chunks = split_dataframe_for_pages(df_physical_formatted, max_rows_per_page)

        # Titolo sezione Fisici (una volta sola, e con page break prima)
        if total_pages > 0:
            elements.append(PageBreak())
            elements.append(Paragraph("Punti vendita Fisici", ParagraphStyle(
            'SectionTitle',
            parent=title_style,
            spaceAfter=20
            )))

        for i, chunk in enumerate(physical_chunks):
            print(f"  Pagina PHYSICAL {i + 1}/{len(physical_chunks)}...")

            table_header = ["Insegna", "Regione", "Provincia", "Città", "CAP", "Indirizzo"]
            table_data = [table_header] + [
                [
                    row.get('franchiseName', '-'),
                    row.get('region', '-'),
                    row.get('province', '-'),
                    row.get('city', '-'),
                    row.get('zipCode', '-'),
                    row.get('address', '-'),
                ]
                for _, row in chunk.iterrows()
            ]

            col_widths = calculate_column_widths(table_data, page_size[0])
            table = Table(table_data, colWidths=col_widths, repeatRows=1)
            table.setStyle(create_table_style())

            # Aggiungi page break solo se non è la prima tabella fisica
            if i > 0:
                elements.append(PageBreak())

            total_pages += 1
            elements.append(table)


    # Aggiungi i numeri di pagina a tutti gli elementi
    final_elements = []
    page_counter = 1
    
    for element in elements:
        final_elements.append(element)
        
        # Aggiungi numero pagina dopo ogni tabella
        if isinstance(element, Table):
            page_number = Paragraph(
                f"Pagina {page_counter} di {total_pages}",
                page_number_style
            )
            final_elements.append(page_number)
            page_counter += 1

    # Genera il PDF
    try:
        doc.build(final_elements)
        print(f" PDF creato con successo: {pdf_file}")
        print(f" Totale pagine: {total_pages}")
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

    json_file = sys.argv[1]
    pdf_file = sys.argv[2]
    orientation = sys.argv[3] if len(sys.argv) > 3 else 'portrait'
    max_rows = int(sys.argv[4]) if len(sys.argv) > 4 else 20

    # Verifica che il file JSON esista
    if not os.path.exists(json_file):
        print(f"Errore: File {json_file} non trovato")
        return

    # Converte
    success = json_to_pdf(json_file, pdf_file, orientation, max_rows)

    if success:
        print(f" Conversione completata!")
        print(f" Input: {json_file}")
        print(f" Output: {pdf_file}")
    else:
        print("Errore durante la conversione")


if __name__ == "__main__":
    main()
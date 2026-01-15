import pandas as pd
import json
import os
from openpyxl.utils import get_column_letter

OUTPUT_FOLDER = os.path.join("testResults", "instance_1")
EXCEL_FOLDER = os.path.join("excelResults", "instance_1")
os.makedirs(EXCEL_FOLDER, exist_ok=True)

def visualize_sheet(worksheet, dataframe):
    for col_idx, col in enumerate(dataframe.columns, start=1):
        max_length = max(
            dataframe[col].astype(str).map(len).max(),
            len(col)
        ) + 2
        worksheet.column_dimensions[get_column_letter(col_idx)].width = max_length

        worksheet.sheet_view.zoomScale = 150

for filename in os.listdir(OUTPUT_FOLDER):
    json_path = os.path.join(OUTPUT_FOLDER, filename)
    with open(json_path, 'r') as json_file:
        data = json.load(json_file)

    config = data['config']
    results = data['result']
    config_df = pd.DataFrame([config])
    results_df = pd.DataFrame(results)

    excel_file_path = os.path.join(EXCEL_FOLDER, filename.replace('.json', '.xlsx'))

    with pd.ExcelWriter(excel_file_path) as writer:
        config_df.to_excel(writer, sheet_name='Config', index=False)

        worksheet = writer.sheets['Config']
        visualize_sheet(worksheet, config_df)

        for instance_idx, instance_data in results_df.groupby('instanceIdx'):
            sheet_name = f'Instance_{instance_idx}'
            instance_data.drop(columns=['instanceIdx'], inplace=True)
            instance_data.to_excel(writer, sheet_name=sheet_name, index=False)

            worksheet = writer.sheets[sheet_name]
            visualize_sheet(worksheet, instance_data)

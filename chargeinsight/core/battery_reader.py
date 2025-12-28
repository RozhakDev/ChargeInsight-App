from bs4 import BeautifulSoup
from pathlib import Path


def _parse_capacity(value: str) -> int:
    """
    Cleans and converts battery capacity strings into integers.

    This helper function removes units and formatting characters to 
    provide a pure numerical value in mWh.

    Args:
        value (str): The raw capacity string from the HTML report.

    Returns:
        int: The numerical capacity value.
    """
    return int(value.replace("mWh", "").replace(".", "").strip())


def parse_battery_report(report_path: Path) -> dict:
    """
    Extracts key battery specifications from an HTML report file.

    It scans the document for design capacity, full charge capacity, 
    and cycle counts to organize them into a clean dictionary.

    Args:
        report_path (Path): The path to the battery-report.html file.

    Returns:
        dict: A dictionary containing the structured battery data.
    """
    with open(report_path, "r", encoding="utf-8") as f:
        soup = BeautifulSoup(f, "html.parser")

    rows = soup.find_all("tr")

    data = {}

    for row in rows:
        cells = row.find_all("td")
        if len(cells) != 2:
            continue

        key = cells[0].get_text(strip=True).upper()
        value = cells[1].get_text(strip=True)

        key_handlers = {
            "DESIGN CAPACITY": lambda v: _parse_capacity(v),
            "FULL CHARGE CAPACITY": lambda v: _parse_capacity(v),
            "CYCLE COUNT": lambda v: int(v),
        }

        if key in key_handlers:
            target_key = {
                "DESIGN CAPACITY": "design_capacity_mwh",
                "FULL CHARGE CAPACITY": "full_charge_capacity_mwh",
                "CYCLE COUNT": "cycle_count",
            }[key]
            data[target_key] = key_handlers[key](value)

    return data
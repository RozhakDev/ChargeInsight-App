import subprocess
from pathlib import Path


BATTERY_REPORT_PATH = Path("battery-report.html")


def generate_battery_report(output_path: Path = BATTERY_REPORT_PATH) -> Path:
    """
    Creates a detailed HTML report of the system's battery health.

    This function uses the system's power configuration tool to generate 
    a file containing usage history and capacity data.

    Args:
        output_path (Path): The file path where the report will be saved.

    Returns:
        Path: The absolute path to the generated HTML report.

    Raises:
        RuntimeError: If the system command fails to execute.
        FileNotFoundError: If the report file is not found after execution.
    """
    cmd = [
        "powercfg",
        "/batteryreport",
        f"/output",
        str(output_path.absolute())
    ]

    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        shell=True
    )

    if result.returncode != 0:
        raise RuntimeError(f"powercfg failed: {result.stderr}")
    
    if not output_path.exists():
        raise FileNotFoundError("Battery report was not generated")
    
    return output_path
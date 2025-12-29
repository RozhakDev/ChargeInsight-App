import sys
import os
import json
from datetime import datetime
from PySide6.QtWidgets import QApplication
from PySide6.QtCore import QObject, QTimer

from chargeinsight.system.windows_power import generate_battery_report
from chargeinsight.core.battery_reader import parse_battery_report
from chargeinsight.core.health_model import calculate_health, classify_health
from chargeinsight.core.confidence import calculate_confidence
from chargeinsight.ui.main_window import MainWindow

DATA_DIR = os.path.join(os.path.expanduser("~"), ".chargeinsight")
HISTORY_FILE = os.path.join(DATA_DIR, "history.json")
REFRESH_INTERVAL_MS = 10 * 60 * 1000  # 10 minutes


def load_history():
    """
    Retrieves previous battery records from local storage.

    It attempts to read a JSON file containing historical data to build 
    the health trend visualization.

    Returns:
        list: A list of past battery report dictionaries.
    """
    if not os.path.exists(HISTORY_FILE):
        return []
    try:
        with open(HISTORY_FILE, "r") as f:
            return json.load(f)
    except (json.JSONDecodeError, FileNotFoundError):
        return []


def save_history(history):
    """
    Persists battery analysis records to a local JSON file.

    This ensures that data is stored safely in the user's directory 
    and can be reloaded the next time the app starts.

    Args:
        history (list): The collection of battery reports to be saved.
    """
    os.makedirs(DATA_DIR, exist_ok=True)
    with open(HISTORY_FILE, "w") as f:
        json.dump(history, f, indent=4)


def run_analysis():
    """
    Performs a fresh battery diagnostic and data aggregation.

    This function triggers the system report, parses the result, 
    and computes health metrics into a single structured dictionary.

    Returns:
        dict: A complete snapshot of current battery health and metadata.
    """
    report_path = generate_battery_report()
    data = parse_battery_report(report_path)
    health = calculate_health(
        data["design_capacity_mwh"],
        data["full_charge_capacity_mwh"]
    )
    status = classify_health(health)
    confidence, explanation = calculate_confidence(
        health,
        data["cycle_count"]
    )
    return {
        "timestamp": datetime.now().isoformat(),
        "health": health,
        "status": status,
        "confidence": confidence,
        "explanation": explanation,
        "cycle_count": data["cycle_count"],
        "design_capacity_mwh": data["design_capacity_mwh"],
        "full_charge_capacity_mwh": data["full_charge_capacity_mwh"],
    }


class AppManager(QObject):
    """
    Coordinates the application lifecycle and data synchronization.

    It manages the background timer for periodic updates and acts as 
    a bridge between the analysis logic and the user interface.
    """
    
    def __init__(self, app):
        """
        Sets up the application environment and initial data.

        It loads history, runs the first analysis, and prepares the 
        main window for display.

        Args:
            app (QApplication): The main Qt application instance.
        """
        super().__init__()
        self.app = app
        self.history = load_history()

        initial_report = run_analysis()
        self.history.append(initial_report)
        save_history(self.history)

        self.window = MainWindow(initial_report, self.history)

        self.timer = QTimer(self)
        self.timer.setInterval(REFRESH_INTERVAL_MS)
        self.timer.timeout.connect(self.refresh_data)
        self.timer.start()

    def refresh_data(self):
        """
        Triggers a periodic update of battery information.

        It runs a new analysis, appends it to the history, and 
        notifies the window to refresh its visual elements.
        """
        print("Refreshing battery data...")

        new_report = run_analysis()
        self.history.append(new_report)
        
        save_history(self.history)
        self.window.update_data(new_report, self.history)
        
        print("Data refreshed.")

    def start(self):
        """
        Launches the graphical interface and starts the event loop.

        This method makes the main window visible and transfers 
        control to the system's execution process.
        """
        self.window.show()
        sys.exit(self.app.exec())


def main():
    """
    Entry point for the ChargeInsight application.

    It initializes the desktop environment and starts the 
    application manager to begin monitoring.
    """
    app = QApplication(sys.argv)
    manager = AppManager(app)
    manager.start()


if __name__ == "__main__":
    main()
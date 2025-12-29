from PySide6.QtWidgets import (
    QApplication,
    QMainWindow,
    QWidget,
    QLabel,
    QVBoxLayout,
    QGridLayout,
    QFrame,
)
from PySide6.QtCore import Qt
from PySide6.QtGui import QColor, QFont
import pyqtgraph as pg
from datetime import datetime


class MainWindow(QMainWindow):
    """
    The primary graphical interface for the ChargeInsight application.

    This class sets up the visual environment, including themes, layouts, 
     and data visualization components to display battery analytics.
    """

    def __init__(self, current_report: dict, history: list):
        """
        Initializes the main window and its visual components.

        It sets up the dark-themed layout, styling, and widget structure 
        required to display the battery analysis results.

        Args:
            current_report (dict): The latest calculated battery data.
            history (list): A collection of previous battery records for the graph.
        """
        super().__init__()

        self.setWindowTitle("ChargeInsight")
        self.setMinimumSize(800, 600)

        self.setStyleSheet("""
            QMainWindow {
                background-color: #2c2f33;
                color: #ffffff;
            }
            QLabel {
                color: #ffffff;
            }
            #titleLabel {
                font-size: 32px;
                font-weight: bold;
                color: #7289da;
                margin-bottom: 20px;
            }
            #metricLabel {
                font-size: 24px;
                font-weight: bold;
            }
            #statusLabel {
                font-size: 20px;
                font-weight: bold;
            }
            #confidenceLabel {
                font-size: 18px;
                color: #99aab5;
            }
            #explanationTitle {
                font-size: 18px;
                font-weight: bold;
                color: #7289da;
            }
            #explanationLabel {
                font-size: 14px;
                color: #cccccc;
            }
            QFrame#separator {
                background-color: #4f545c;
            }
        """)

        central_widget = QWidget()
        main_layout = QGridLayout(central_widget)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(15)

        current_report_frame = QFrame()
        current_report_layout = QVBoxLayout(current_report_frame)
        current_report_layout.setContentsMargins(10, 10, 10, 10)
        current_report_frame.setStyleSheet("background-color: #36393f; border-radius: 10px;")

        self.health_label = QLabel()
        self.health_label.setObjectName("metricLabel")
        self.status_label = QLabel()
        self.status_label.setObjectName("statusLabel")
        self.confidence_label = QLabel()
        self.confidence_label.setObjectName("confidenceLabel")

        current_report_layout.addWidget(self.health_label)
        current_report_layout.addWidget(self.status_label)
        current_report_layout.addWidget(self.confidence_label)
        current_report_layout.addStretch()

        main_layout.addWidget(current_report_frame, 1, 0, 1, 1)

        explanation_frame = QFrame()
        self.explanation_layout = QVBoxLayout(explanation_frame)
        self.explanation_layout.setContentsMargins(10, 10, 10, 10)
        explanation_frame.setStyleSheet("background-color: #36393f; border-radius: 10px;")

        explanation_title = QLabel("Reasoning & Recommendations")
        explanation_title.setObjectName("explanationTitle")
        self.explanation_layout.addWidget(explanation_title)

        main_layout.addWidget(explanation_frame, 1, 1, 1, 1)

        graph_frame = QFrame()
        graph_layout = QVBoxLayout(graph_frame)
        graph_layout.setContentsMargins(10, 10, 10, 10)
        graph_frame.setStyleSheet("background-color: #36393f; border-radius: 10px;")

        self.plot_widget = pg.PlotWidget()
        self.plot_widget.setBackground('#36393f')
        self.plot_widget.setTitle("Battery Health Over Time", color="#ffffff", size="18pt")
        self.plot_widget.getAxis('left').setTextPen('#ffffff')
        self.plot_widget.getAxis('bottom').setPen('#99aab5')
        self.plot_widget.getAxis('left').setPen('#99aab5')
        self.plot_widget.showGrid(x=True, y=True, alpha=0.3)
        self.plot_widget.setLabel('left', 'Health (%)', color="#ffffff")

        self.health_curve = self.plot_widget.plot(
            pen=pg.mkPen(color='#7289da', width=2),
            symbol='o',
            symbolBrush='#7289da',
            symbolSize=8,
            name="Health"
        )
        self.plot_widget.addLegend()

        self.time_axis = pg.AxisItem(orientation='bottom')
        self.time_axis.setLabel('Date', units=None)
        self.plot_widget.setAxisItems({'bottom': self.time_axis})

        graph_layout.addWidget(self.plot_widget)
        main_layout.addWidget(graph_frame, 2, 0, 1, 2)

        self.setCentralWidget(central_widget)

        self.update_data(current_report, history)

    def update_data(self, current_report: dict, history: list):
        """
        Refreshes the dashboard with new battery information.

        This method updates the labels, applies conditional styling to the status, 
        and redraws the health trend graph using the provided history.

        Args:
            current_report (dict): The new report data to be displayed.
            history (list): The list of historical records to plot on the graph.
        """
        self.health_label.setText(f"Battery Health: {current_report['health']}%")
        self.status_label.setText(f"Status: {current_report['status']}")
        self.confidence_label.setText(f"Confidence: {current_report['confidence']}")

        if current_report['status'] == "Excellent":
            self.status_label.setStyleSheet("color: #43b581; font-size: 20px; font-weight: bold;")
        elif current_report['status'] == "Good":
            self.status_label.setStyleSheet("color: #faa61a; font-size: 20px; font-weight: bold;")
        else:
            self.status_label.setStyleSheet("color: #f04747; font-size: 20px; font-weight: bold;")

        for i in reversed(range(1, self.explanation_layout.count())):
            item = self.explanation_layout.itemAt(i)
            if item:
                widget = item.widget()
                if widget:
                    widget.deleteLater()
                self.explanation_layout.removeItem(item)

        if current_report["explanation"]:
            for reason in current_report["explanation"]:
                label = QLabel(f"- {reason}")
                label.setObjectName("explanationLabel")
                label.setWordWrap(True)
                self.explanation_layout.addWidget(label)
        else:
            label = QLabel("No specific recommendations at this time.")
            label.setObjectName("explanationLabel")
            self.explanation_layout.addWidget(label)
        self.explanation_layout.addStretch()

        if history:
            history.sort(key=lambda x: datetime.fromisoformat(x['timestamp']))
            timestamps = [datetime.fromisoformat(record['timestamp']).timestamp() for record in history]
            health_values = [record['health'] for record in history]

            self.health_curve.setData(x=timestamps, y=health_values)

            if timestamps:
                ticks = [(t, datetime.fromtimestamp(t).strftime('%Y-%m-%d')) for t in timestamps]
                self.time_axis.setTicks([ticks])
            else:
                 self.time_axis.setTicks([])
from chargeinsight.system.windows_power import generate_battery_report
from chargeinsight.core.battery_reader import parse_battery_report


def main():
    """
    Executes the complete workflow to display battery information.

    This function coordinates the generation of the system report and 
    the parsing of its data, then prints the results to the console.
    """
    report_path = generate_battery_report()
    battery_data = parse_battery_report(report_path)

    print("Battery data:")
    for k, v in battery_data.items():
        print(f"- {k}: {v}")


if __name__ == "__main__":
    main()
from chargeinsight.system.windows_power import generate_battery_report
from chargeinsight.core.battery_reader import parse_battery_report
from chargeinsight.core.health_model import calculate_health, classify_health
from chargeinsight.core.confidence import calculate_confidence


def main():
    """
    Runs the end-to-end battery diagnostic and health assessment.

    This function orchestrates the entire process: generating a system report,
    parsing raw data, calculating health metrics, and presenting a final
    analytical summary to the user.
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

    print("\nChargeInsight Report")
    print("-" * 30)
    print(f"Health        : {health}%")
    print(f"Status        : {status}")
    print(f"Confidence    : {confidence}")

    if explanation:
        print("\nReasoning:")
        for e in explanation:
            print(f"- {e}")


if __name__ == "__main__":
    main()
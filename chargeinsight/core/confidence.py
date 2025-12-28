def cycle_score(cycle_count: int) -> float:
    """
    Evaluates the battery condition based on its usage cycles.

    This function assigns a rating between 0.0 and 1.0 to reflect how 
    much the battery has been used relative to typical lifespan milestones.

    Args:
        cycle_count (int): Total number of charge-discharge cycles.

    Returns:
        float: A score representing the usage health of the battery.
    """
    cycle_ranges = [
        (1000, 0.7),
        (500, 1.0),
        (1500, 0.4),
        (float('inf'), 0.2)
    ]

    for max_cycles, score in cycle_ranges:
        if cycle_count <= max_cycles:
            return score

    return 0.2


def health_score(health_percent: float) -> float:
    """
    Normalizes the health percentage into a decimal scale.

    It converts the 0-100 percentage range into a standard 0.0 to 1.0 
    format used for internal scoring and calculations.

    Args:
        health_percent (float): The calculated battery health percentage.

    Returns:
        float: The normalized value capped between 0.0 and 1.0.
    """
    return max(0.0, min(health_percent / 100, 1.0))


def calculate_confidence(health_percent: float, cycle_count: int) -> tuple[float, list[str]]:
    """
    Estimates the reliability of the battery data through weighted reasoning.

    This function combines health and cycle scores to determine a final 
    confidence level while providing clear reasons for any degradation.

    Args:
        health_percent (float): The current health percentage of the battery.
        cycle_count (int): The total recorded charge cycles.

    Returns:
        tuple[float, list[str]]: A confidence score and a list of status explanations.
    """
    explanations = []

    h_score = health_score(health_percent)
    c_score = cycle_score(cycle_count)

    if health_percent < 70:
        explanations.append("Battery capacity dropped below 70%")

    if cycle_count > 1000:
        explanations.append("Cycle count exceeds normal lifespan")

    confidence = round(
        (0.6 * h_score) + (0.4 * c_score),
        2
    )

    return confidence, explanations
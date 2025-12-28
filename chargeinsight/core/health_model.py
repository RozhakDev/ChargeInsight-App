def calculate_health(design_capacity: int, full_capacity: int) -> float:
    """
    Computes the battery health percentage based on its capacity.

    This function compares the current full charge capacity against the 
    original design capacity to determine the wear level.

    Args:
        design_capacity (int): The original capacity when the battery was new.
        full_capacity (int): The maximum charge the battery can hold now.

    Returns:
        float: The health percentage rounded to two decimal places.

    Raises:
        ValueError: If the design capacity is zero or negative.
    """
    if design_capacity <= 0:
        raise ValueError("Invalid design capacity")
    
    return round((full_capacity / design_capacity) * 100, 2)


def classify_health(health_percent: float) -> str:
    """
    Categorizes the battery condition into human-readable states.

    It provides a simple status label based on the calculated health
    percentage to help users understand their battery's longevity.

    Args:
        health_percent (float): The battery health value from 0 to 100.

    Returns:
        str: A status string: "HEALTHY", "DEGRADING", or "WORN".
    """
    health_ranges = [
        (85, "HEALTHY"),
        (70, "DEGRADING"),
        (0, "WORN")
    ]

    for min_threshold, category in health_ranges:
        if health_percent >= min_threshold:
            return category

    return "WORN"
Estimate the calories, protein, carbs and fat for this food/nutrition entry.

NUTRITION INFORMATION:
- Food: {foodName}
- Serving Size: {servingSize}
- Meal Type: {mealType}
- Date and Time: {dateTime}
- Notes: {notes}

ASSUMPTIONS:
- Use standard nutritional databases (USDA) as reference
- If serving size seems unusual, use the closest standard serving

Return format should be a JSON object with the following structure:
{
{
  "calories": number,
    "carbs": number,
    "protein": number,
    "fat": number
  }
}

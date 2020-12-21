package com.macasaet;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day21 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day21.class.getResourceAsStream("/day-21-input.txt"))) {

            final var foods = StreamSupport.stream(spliterator, false)
                    .map(String::strip)
                    .map(Food::fromLine)
                    .collect(Collectors.toUnmodifiableSet());
            final var allergenToFood = new HashMap<String, Set<Food>>();
            final var ingredientToFood = new HashMap<String, Set<Food>>();
            for (final var food : foods) {
                for (final var allergen : food.allergens) {
                    allergenToFood.computeIfAbsent(allergen, key -> new HashSet<>()).add(food);
                }
                for (final var ingredient : food.ingredients) {
                    ingredientToFood.computeIfAbsent(ingredient, key -> new HashSet<>()).add(food);
                }
            }

            final var ingredientsThatContainNoAllergens = new HashSet<>(ingredientToFood.keySet());
            final var allergenToIngredient = new HashMap<String, Set<String>>();
            allergenToFood.entrySet().stream().forEach(entry -> {
                final var allergen = entry.getKey();
                final var appearances = entry.getValue();
                Set<String> commonIngredients = null;
                for (final var food : appearances) {
                    if (commonIngredients == null) {
                        commonIngredients = new HashSet<>(food.ingredients);
                    } else {
                        commonIngredients.retainAll(food.ingredients);
                    }
                }
                System.err.println(allergen + " may be found in: " + commonIngredients);
                allergenToIngredient.put(allergen, commonIngredients);
                ingredientsThatContainNoAllergens.removeAll(commonIngredients);
            });

            int sum = 0;
            for (final var food : foods) {
                for (final var ingredient : ingredientsThatContainNoAllergens) {
                    if (food.ingredients.contains(ingredient)) {
                        sum++;
                    }
                }
            }
            System.out.println("Part 1: " + sum);

            final var ingredientToAllergen = new HashMap<String, String>();

            final var dangerousIngredients = new HashSet<>(ingredientToFood.keySet());
            dangerousIngredients.removeAll(ingredientsThatContainNoAllergens);
            while (!dangerousIngredients.isEmpty()) {
                for (final var i = dangerousIngredients.iterator(); i.hasNext(); ) {
                    final var ingredient = i.next();
                    boolean mappedIngredient = false;
                    for (final var j = allergenToIngredient.entrySet().iterator(); j.hasNext(); ) {
                        final var entry = j.next();
                        final var allergen = entry.getKey();
                        final var ingredients = entry.getValue();
                        if (ingredients.size() == 1 && ingredients.contains(ingredient)) {
                            // this is the only ingredient known to contain this allergen
                            ingredientToAllergen.put(ingredient, allergen);
                            System.err.println("Mapping " + ingredient + " to " + allergen);
                            j.remove();
                            mappedIngredient |= true;
                            break;
                        }
                    }
                    if (mappedIngredient) {
                        for (final var entry : allergenToIngredient.entrySet()) {
                            final var ingredients = entry.getValue();
                            ingredients.remove(ingredient);
                        }
                        i.remove();
                    }
                }
            }

            final var result =
                    ingredientToAllergen
                            .entrySet()
                            .stream()
                            .sorted(Comparator.comparing(Entry::getValue))
                            .map(Entry::getKey)
                            .collect(Collectors.joining(","));
            System.out.println("Part 2: " + result);
        }
    }

    public static class Food {
        private final Set<String> ingredients;
        private final Set<String> allergens;

        public Food(final Set<String> ingredients, final Set<String> allergens) {
            this.ingredients = ingredients;
            this.allergens = allergens;
        }

        public static Food fromLine(final String line) {
            final var components = line.split("\\(contains ");
            final var ingredientsArray = components[0].strip().split(" ");
            final var allergensString = components[1].strip().replaceAll("\\)", "");
            final var allergensArray = allergensString.split(", ");
            return new Food(Set.of(ingredientsArray), Set.of(allergensArray));
        }

    }
}
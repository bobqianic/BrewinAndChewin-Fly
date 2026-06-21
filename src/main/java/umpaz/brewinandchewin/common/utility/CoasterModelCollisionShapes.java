package umpaz.brewinandchewin.common.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CoasterModelCollisionShapes {
    private static final double MODEL_Y_OFFSET = 1.0D;
    private static final Map<ResourceLocation, VoxelShape> SHAPES_BY_ITEM = new ConcurrentHashMap<>();

    private CoasterModelCollisionShapes() {
    }

    public static VoxelShape get(ItemStack stack) {
        if (stack.isEmpty()) {
            return Shapes.empty();
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return SHAPES_BY_ITEM.computeIfAbsent(itemId, CoasterModelCollisionShapes::loadShape);
    }

    private static VoxelShape loadShape(ResourceLocation itemId) {
        JsonObject displayJson = readCoasterJson(itemId);
        if (displayJson == null || !displayJson.has("item") || !itemId.equals(ResourceLocation.parse(displayJson.get("item").getAsString()))) {
            return Shapes.empty();
        }

        Bounds bounds = null;
        for (ModelPlacement model : getModels(displayJson.get("models"))) {
            bounds = merge(bounds, offset(getModelBounds(model.model()), model.offsetX(), model.offsetY(), model.offsetZ()));
        }
        if (bounds == null || bounds.isEmpty()) {
            return Shapes.empty();
        }
        return Block.box(bounds.minX, bounds.minY + MODEL_Y_OFFSET, bounds.minZ, bounds.maxX, bounds.maxY + MODEL_Y_OFFSET, bounds.maxZ);
    }

    private static JsonObject readCoasterJson(ResourceLocation itemId) {
        JsonObject displayJson = readJson("assets/" + itemId.getNamespace() + "/brewinandchewin/coaster/" + itemId.getPath() + ".json");
        if (displayJson != null) {
            return displayJson;
        }
        return readJson("assets/brewinandchewin/brewinandchewin/coaster/" + itemId.getPath() + ".json");
    }

    private static List<ModelPlacement> getModels(JsonElement modelsJson) {
        List<ModelPlacement> models = new ArrayList<>();
        if (modelsJson == null) {
            return models;
        }
        if (modelsJson.isJsonPrimitive()) {
            models.add(new ModelPlacement(ResourceLocation.parse(modelsJson.getAsString()), 0.0D, 0.0D, 0.0D));
            return models;
        }
        if (modelsJson.isJsonArray()) {
            for (JsonElement modelJson : modelsJson.getAsJsonArray()) {
                if (modelJson.isJsonPrimitive()) {
                    models.add(new ModelPlacement(ResourceLocation.parse(modelJson.getAsString()), 0.0D, 0.0D, 0.0D));
                } else if (modelJson.isJsonObject() && modelJson.getAsJsonObject().has("model")) {
                    models.add(getModelPlacement(modelJson.getAsJsonObject()));
                }
            }
            return models;
        }
        if (modelsJson.isJsonObject() && modelsJson.getAsJsonObject().has("model")) {
            models.add(getModelPlacement(modelsJson.getAsJsonObject()));
        }
        return models;
    }

    private static ModelPlacement getModelPlacement(JsonObject modelJson) {
        double[] offset = modelJson.has("offset") ? getVector(modelJson.getAsJsonArray("offset")) : new double[]{0.0D, 0.0D, 0.0D};
        return new ModelPlacement(ResourceLocation.parse(modelJson.get("model").getAsString()), offset[0], offset[1], offset[2]);
    }

    private static Bounds getModelBounds(ResourceLocation model) {
        return getModelBounds(model, new HashSet<>());
    }

    private static Bounds getModelBounds(ResourceLocation model, Set<ResourceLocation> visitedModels) {
        if (!visitedModels.add(model)) {
            return null;
        }

        JsonObject modelJson = readJson("assets/" + model.getNamespace() + "/models/" + model.getPath() + ".json");
        if (modelJson == null) {
            return null;
        }

        Bounds bounds = modelJson.has("parent") ? getModelBounds(ResourceLocation.parse(modelJson.get("parent").getAsString()), visitedModels) : null;
        if (modelJson.has("elements")) {
            for (JsonElement elementJson : modelJson.getAsJsonArray("elements")) {
                if (!elementJson.isJsonObject()) {
                    continue;
                }
                JsonObject element = elementJson.getAsJsonObject();
                if (!element.has("from") || !element.has("to")) {
                    continue;
                }
                bounds = merge(bounds, getElementBounds(element));
            }
        }
        return bounds;
    }

    private static Bounds getElementBounds(JsonObject element) {
        double[] from = getVector(element.getAsJsonArray("from"));
        double[] to = getVector(element.getAsJsonArray("to"));
        double minX = Math.min(from[0], to[0]);
        double minY = Math.min(from[1], to[1]);
        double minZ = Math.min(from[2], to[2]);
        double maxX = Math.max(from[0], to[0]);
        double maxY = Math.max(from[1], to[1]);
        double maxZ = Math.max(from[2], to[2]);

        if (!element.has("rotation")) {
            return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
        }

        JsonObject rotation = element.getAsJsonObject("rotation");
        double angle = Math.toRadians(rotation.get("angle").getAsDouble());
        String axis = rotation.get("axis").getAsString();
        double[] origin = getVector(rotation.getAsJsonArray("origin"));
        Bounds rotatedBounds = null;

        for (double x : new double[]{minX, maxX}) {
            for (double y : new double[]{minY, maxY}) {
                for (double z : new double[]{minZ, maxZ}) {
                    rotatedBounds = merge(rotatedBounds, rotate(x, y, z, angle, axis, origin));
                }
            }
        }
        return rotatedBounds;
    }

    private static Bounds rotate(double x, double y, double z, double angle, String axis, double[] origin) {
        double translatedX = x - origin[0];
        double translatedY = y - origin[1];
        double translatedZ = z - origin[2];
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double rotatedX = translatedX;
        double rotatedY = translatedY;
        double rotatedZ = translatedZ;

        switch (axis) {
            case "x" -> {
                rotatedY = translatedY * cos - translatedZ * sin;
                rotatedZ = translatedY * sin + translatedZ * cos;
            }
            case "y" -> {
                rotatedX = translatedX * cos + translatedZ * sin;
                rotatedZ = -translatedX * sin + translatedZ * cos;
            }
            case "z" -> {
                rotatedX = translatedX * cos - translatedY * sin;
                rotatedY = translatedX * sin + translatedY * cos;
            }
        }

        double finalX = rotatedX + origin[0];
        double finalY = rotatedY + origin[1];
        double finalZ = rotatedZ + origin[2];
        return new Bounds(finalX, finalY, finalZ, finalX, finalY, finalZ);
    }

    private static double[] getVector(JsonArray array) {
        return new double[]{array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble()};
    }

    private static Bounds merge(Bounds first, Bounds second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return new Bounds(
                Math.min(first.minX, second.minX),
                Math.min(first.minY, second.minY),
                Math.min(first.minZ, second.minZ),
                Math.max(first.maxX, second.maxX),
                Math.max(first.maxY, second.maxY),
                Math.max(first.maxZ, second.maxZ)
        );
    }

    private static Bounds offset(Bounds bounds, double offsetX, double offsetY, double offsetZ) {
        if (bounds == null) {
            return null;
        }
        return new Bounds(bounds.minX + offsetX, bounds.minY + offsetY, bounds.minZ + offsetZ, bounds.maxX + offsetX, bounds.maxY + offsetY, bounds.maxZ + offsetZ);
    }

    private static JsonObject readJson(String path) {
        try (InputStream stream = CoasterModelCollisionShapes.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        private boolean isEmpty() {
            return minX >= maxX || minY >= maxY || minZ >= maxZ;
        }
    }

    private record ModelPlacement(ResourceLocation model, double offsetX, double offsetY, double offsetZ) {
    }
}

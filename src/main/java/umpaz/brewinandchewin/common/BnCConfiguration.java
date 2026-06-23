package umpaz.brewinandchewin.common;

import net.minecraft.util.StringRepresentable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BnCConfiguration {
    public static final int CONFIG_VERSION = 2;
    private static final int CLIENT_CONFIG_VERSION = 2;
    private static final String COMMON_CONFIG_FILE = "brewinandchewin-common.toml";
    private static final String CLIENT_CONFIG_FILE = "brewinandchewin-client.toml";

    public static final ConfigHolder<Common> COMMON_CONFIG = ConfigHolder.of(Common.DEFAULT);
    public static final ConfigHolder<Client> CLIENT_CONFIG = ConfigHolder.of(Client.DEFAULT);

    private static Common localCommonConfig = Common.DEFAULT;

    public static void init() {
        Path configDirectory = BrewinAndChewin.getHelper().getConfigDirectory();

        localCommonConfig = loadCommon(configDirectory.resolve(COMMON_CONFIG_FILE));
        COMMON_CONFIG.set(localCommonConfig);
        CLIENT_CONFIG.set(loadClient(configDirectory.resolve(CLIENT_CONFIG_FILE)));
    }

    public static Common getLocalCommonConfig() {
        return localCommonConfig;
    }

    public static void applySyncedCommonConfig(Common common) {
        COMMON_CONFIG.set(common);
    }

    public static void resetSyncedCommonConfig() {
        COMMON_CONFIG.set(localCommonConfig);
    }

    private static Common loadCommon(Path path) {
        ReadState state = readVersioned(path, CONFIG_VERSION);
        Common common = new Common(
                new Common.Root(
                        readInt(state, "root.levelChatScramble", Common.DEFAULT.root().levelChatScramble(), 1, 10),
                        readInt(state, "root.levelSignScramble", Common.DEFAULT.root().levelSignScramble(), 1, 10),
                        readInt(state, "root.levelNameScramble", Common.DEFAULT.root().levelNameScramble(), 1, 10)
                ),
                new Common.Keg(
                        readEnum(state, "keg.kegCapacityUnit", Common.DEFAULT.keg().capacityUnit(), FluidUnit.values()),
                        readLong(state, "keg.kegCapacity", Common.DEFAULT.keg().capacity(), 1L, Long.MAX_VALUE),
                        readInt(state, "keg.kegCold", Common.DEFAULT.keg().cold(), 1, Integer.MAX_VALUE),
                        readInt(state, "keg.kegChilly", Common.DEFAULT.keg().chilly(), 1, Integer.MAX_VALUE),
                        readInt(state, "keg.kegWarm", Common.DEFAULT.keg().warm(), 1, Integer.MAX_VALUE),
                        readInt(state, "keg.kegHot", Common.DEFAULT.keg().hot(), 1, Integer.MAX_VALUE),
                        readBoolean(state, "keg.kegBiomeTemp", Common.DEFAULT.keg().biomeTemp()),
                        readBoolean(state, "keg.kegDimTemp", Common.DEFAULT.keg().dimTemp())
                ),
                new Common.RecipeBook(
                        readBoolean(state, "recipe_book.enableRecipeBookKeg", Common.DEFAULT.recipeBook().enabled())
                )
        );

        if (state.needsSave()) {
            save(path, writeCommon(common));
        }
        return common;
    }

    private static Client loadClient(Path path) {
        ReadState state = readVersioned(path, CLIENT_CONFIG_VERSION);
        Client client = new Client(
                readBoolean(state, "client.numbedHeartFlickering", Client.DEFAULT.numbedHeartFlickering()),
                readBoolean(state, "client.intoxicationFoodOverlay", Client.DEFAULT.intoxicationFoodOverlay()),
                readBoolean(state, "client.scrambleChat", Client.DEFAULT.scrambleChat()),
                readBoolean(state, "client.scrambleName", Client.DEFAULT.scrambleName()),
                readBoolean(state, "client.scrambleSign", Client.DEFAULT.scrambleSign()),
                readBoolean(state, "client.renderFluidInKeg", Client.DEFAULT.renderFluidInKeg())
        );

        if (state.needsSave()) {
            save(path, writeClient(client));
        }
        return client;
    }

    private static ReadState readVersioned(Path path, int configVersion) {
        if (!Files.exists(path)) {
            return new ReadState(Map.of(), true);
        }

        Map<String, String> values;
        try {
            values = readToml(path);
        } catch (IOException ex) {
            BrewinAndChewin.LOG.warn("Failed to read Brewin' And Chewin' config {}, using defaults.", path, ex);
            return new ReadState(Map.of(), true);
        }

        Integer version = parseInteger(values.get("version"));
        if (version == null || version != configVersion) {
            BrewinAndChewin.LOG.info("Ignoring old or incompatible Brewin' And Chewin' config {} and writing version {} defaults.", path.getFileName(), configVersion);
            return new ReadState(Map.of(), true);
        }

        return new ReadState(values, false);
    }

    private static Map<String, String> readToml(Path path) throws IOException {
        Map<String, String> values = new HashMap<>();
        String section = "";
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String trimmed = stripComment(line).trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                section = trimmed.substring(1, trimmed.length() - 1).trim();
                continue;
            }
            int separator = trimmed.indexOf('=');
            if (separator < 0) {
                continue;
            }
            String key = trimmed.substring(0, separator).trim();
            String value = trimmed.substring(separator + 1).trim();
            values.put(section.isEmpty() ? key : section + "." + key, value);
        }
        return values;
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        char quote = '\0';
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if ((c == '"' || c == '\'') && (i == 0 || line.charAt(i - 1) != '\\')) {
                if (!quoted) {
                    quoted = true;
                    quote = c;
                } else if (quote == c) {
                    quoted = false;
                }
            } else if (c == '#' && !quoted) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static int readInt(ReadState state, String key, int fallback, int min, int max) {
        Integer value = parseInteger(state.values().get(key));
        if (value == null || value < min || value > max) {
            state.markNeedsSave();
            return fallback;
        }
        return value;
    }

    private static long readLong(ReadState state, String key, long fallback, long min, long max) {
        Long value = parseLong(state.values().get(key));
        if (value == null || value < min || value > max) {
            state.markNeedsSave();
            return fallback;
        }
        return value;
    }

    private static boolean readBoolean(ReadState state, String key, boolean fallback) {
        String raw = state.values().get(key);
        if (raw == null) {
            state.markNeedsSave();
            return fallback;
        }
        String normalized = unquote(raw).toLowerCase(Locale.ROOT);
        if ("true".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized)) {
            return false;
        }
        state.markNeedsSave();
        return fallback;
    }

    private static <T extends Enum<T> & StringRepresentable> T readEnum(ReadState state, String key, T fallback, T[] values) {
        String raw = state.values().get(key);
        if (raw == null) {
            state.markNeedsSave();
            return fallback;
        }

        String normalized = unquote(raw);
        for (T value : values) {
            if (value.getSerializedName().equalsIgnoreCase(normalized) || value.name().equalsIgnoreCase(normalized)) {
                return value;
            }
        }

        state.markNeedsSave();
        return fallback;
    }

    private static Integer parseInteger(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Integer.parseInt(unquote(raw));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Long parseLong(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Long.parseLong(unquote(raw));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String unquote(String raw) {
        String trimmed = raw.trim();
        if (trimmed.length() >= 2 && ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static void save(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            BrewinAndChewin.LOG.warn("Failed to write Brewin' And Chewin' config {}.", path, ex);
        }
    }

    private static String writeCommon(Common common) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Brewin' And Chewin' common config\n");
        builder.append("# Changing version makes the mod ignore this file and write defaults.\n");
        builder.append("version = ").append(CONFIG_VERSION).append("\n\n");
        builder.append("[root]\n");
        builder.append("levelChatScramble = ").append(common.root().levelChatScramble()).append("\n");
        builder.append("levelSignScramble = ").append(common.root().levelSignScramble()).append("\n");
        builder.append("levelNameScramble = ").append(common.root().levelNameScramble()).append("\n\n");
        builder.append("[keg]\n");
        builder.append("# Valid units: \"liters\", \"millibuckets\", \"droplets\". Capacity is converted to droplets internally.\n");
        builder.append("# 1 liter = 1 millibucket = 81 droplets.\n");
        builder.append("kegCapacityUnit = ").append(quote(common.keg().capacityUnit().getSerializedName())).append("\n");
        builder.append("kegCapacity = ").append(common.keg().capacity()).append("\n");
        builder.append("kegCold = ").append(common.keg().cold()).append("\n");
        builder.append("kegChilly = ").append(common.keg().chilly()).append("\n");
        builder.append("kegWarm = ").append(common.keg().warm()).append("\n");
        builder.append("kegHot = ").append(common.keg().hot()).append("\n");
        builder.append("kegBiomeTemp = ").append(common.keg().biomeTemp()).append("\n");
        builder.append("kegDimTemp = ").append(common.keg().dimTemp()).append("\n\n");
        builder.append("[recipe_book]\n");
        builder.append("enableRecipeBookKeg = ").append(common.recipeBook().enabled()).append("\n");
        return builder.toString();
    }

    private static String writeClient(Client client) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Brewin' And Chewin' client config\n");
        builder.append("# Changing version makes the mod ignore this file and write defaults.\n");
        builder.append("version = ").append(CLIENT_CONFIG_VERSION).append("\n\n");
        builder.append("[client]\n");
        builder.append("numbedHeartFlickering = ").append(client.numbedHeartFlickering()).append("\n");
        builder.append("intoxicationFoodOverlay = ").append(client.intoxicationFoodOverlay()).append("\n");
        builder.append("scrambleChat = ").append(client.scrambleChat()).append("\n");
        builder.append("scrambleName = ").append(client.scrambleName()).append("\n");
        builder.append("scrambleSign = ").append(client.scrambleSign()).append("\n");
        builder.append("renderFluidInKeg = ").append(client.renderFluidInKeg()).append("\n");
        return builder.toString();
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public static final class ConfigHolder<T> {
        private volatile T value;

        private ConfigHolder(T value) {
            this.value = value;
        }

        public static <T> ConfigHolder<T> of(T value) {
            return new ConfigHolder<>(value);
        }

        public T get() {
            return value;
        }

        private void set(T value) {
            this.value = value;
        }
    }

    private static final class ReadState {
        private final Map<String, String> values;
        private boolean needsSave;

        private ReadState(Map<String, String> values, boolean needsSave) {
            this.values = values;
            this.needsSave = needsSave;
        }

        private Map<String, String> values() {
            return values;
        }

        private boolean needsSave() {
            return needsSave;
        }

        private void markNeedsSave() {
            needsSave = true;
        }
    }

    public record Common(Root root, Keg keg, RecipeBook recipeBook) {
        public static final Common DEFAULT = new Common(Root.DEFAULT, Keg.DEFAULT, RecipeBook.DEFAULT);

        public record Root(int levelChatScramble, int levelSignScramble, int levelNameScramble) {
            public static final Root DEFAULT = new Root(3, 3, 3);
        }

        public record Keg(FluidUnit capacityUnit, long capacity,
                          int cold, int chilly, int warm, int hot,
                          boolean biomeTemp, boolean dimTemp) {
            public static final Keg DEFAULT = new Keg(
                    FluidUnit.MILLIBUCKET,
                    1000L,
                    2, 1, 1, 2,
                    true, true
            );

            /**
             * The fluid capacity localized for the mod loader.
             */
            public long localizedCapacity() {
                return capacityUnit.convertToLoader(capacity);
            }
        }

        public record RecipeBook(boolean enabled) {
            public static final RecipeBook DEFAULT = new RecipeBook(true);
        }
    }

    public record Client(boolean numbedHeartFlickering, boolean intoxicationFoodOverlay,
                         boolean scrambleChat, boolean scrambleName, boolean scrambleSign,
                         boolean renderFluidInKeg) {
        public static final Client DEFAULT = new Client(
                true, true,
                true, true, true,
                true
        );
    }
}

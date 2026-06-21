package umpaz.brewinandchewin.common.block.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.KegBlock;
import umpaz.brewinandchewin.common.block.LargeKegBlock;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;
import umpaz.brewinandchewin.common.item.BnCBucketItem;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class KegBlockEntity extends SyncedBlockEntity implements MenuProvider, Nameable, RecipeCraftingHolder {

    public static final int LARGE_KEG_SCALE = 8;
    public static final int TEMPERATURE_SCALE = 1000;
    private static final int LARGE_KEG_TEMPERATURE_SOURCE_SCALE = 4;
    public static final int CONTAINER_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;
    public static final int INVENTORY_SIZE = OUTPUT_SLOT + 1;
    public static final int RANGE = 2;
    private static final long BURN_DRAIN_AMOUNT = 250L;
    private static final long BUCKET_TRANSFER_AMOUNT = 1000L;
    private static final long TANKARD_TRANSFER_AMOUNT = 250L;
    private static final long TINY_FLUID_REMAINDER = FluidUnit.MILLIBUCKET.convertToLoader(1L);
    private static final int BURN_DRAIN_INTERVAL = 15 * 20;
    private static final int TEMPERATURE_UPDATE_INTERVAL = 80;
    private static final int TEMPERATURE_PROGRESS_INTERVAL = 10;
    private static final int BURN_TEMPERATURE_PROGRESS_SCALE = 2;
    private static final int FERMENTATION_REGRESSION_AMOUNT = 20;

    private final AbstractedItemHandler inventory;
    private final SidedKegWrapper inputHandler;
    private final SidedKegWrapper outputHandler;
    private final AbstractedFluidTank fluidTank;
    private final KegRecipeWrapper recipeWrapper;

    private int fermentTime;
    private int fermentTimeTotal;
    private Component customName;

    private boolean deferFluidExtraction = false;
    private boolean currentlyOperating = false;
    public int kegTemperature;
    private boolean initialisedTemperature = false;
    private int targetKegTemperature;
    private int temperatureProgress;
    private int burnTime;
    private boolean touchingBurningSource;
    private boolean burnedByFire;

    protected final ContainerData kegData;
    private final Object2IntOpenHashMap<ResourceKey<Recipe<?>>> usedRecipeTracker;

    private ResourceKey<Recipe<?>> lastRecipeID;
    private boolean checkNewRecipe;

    public KegBlockEntity(BlockPos pos, BlockState state) {
        super(BnCBlockEntityTypes.KEG, pos, state);
        this.inventory = createHandler();
        this.inputHandler = BrewinAndChewin.getHelper().createSidedKegWrapper(inventory, Direction.UP);
        this.outputHandler = BrewinAndChewin.getHelper().createSidedKegWrapper(inventory, Direction.DOWN);
        this.fluidTank = createFluidTank();
        this.kegData = createIntArray();
        this.usedRecipeTracker = new Object2IntOpenHashMap<>();
        this.checkNewRecipe = true;
        this.recipeWrapper = BrewinAndChewin.getHelper().createRecipeWrapper(inventory, fluidTank);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Inventory", CompoundTag.CODEC).ifPresent(inventoryTag -> inventory.readFromNbt(inventoryTag, input.lookup()));
        input.read("FluidTank", CompoundTag.CODEC).ifPresent(tankTag -> fluidTank.readFromNbt(tankTag, input.lookup()));
        fermentTime = input.getIntOr("FermentTime", 0);
        fermentTimeTotal = input.getIntOr("FermentTimeTotal", 0);
        customName = BlockEntity.parseCustomNameSafe(input, "CustomName");
        usedRecipeTracker.clear();
        CompoundTag compoundRecipes = input.read("RecipesUsed", CompoundTag.CODEC).orElse(new CompoundTag());
        for (String key : compoundRecipes.keySet()) {
            ResourceLocation location = ResourceLocation.tryParse(key);
            if (location != null) {
                usedRecipeTracker.put(ResourceKey.create(Registries.RECIPE, location), compoundRecipes.getIntOr(key, 0));
            }
        }
        int savedTemperature = input.getIntOr("Temperature", 0);
        kegTemperature = input.getIntOr("ScaledTemperature", savedTemperature * TEMPERATURE_SCALE);
        int savedTargetTemperature = input.getIntOr("TargetTemperature", savedTemperature);
        targetKegTemperature = input.getIntOr("ScaledTargetTemperature", savedTargetTemperature * TEMPERATURE_SCALE);
        temperatureProgress = input.getIntOr("TemperatureProgress", 0);
        initialisedTemperature = true;
        burnTime = input.getIntOr("BurnTime", 0);
        checkNewRecipe = true;
    }

    public static AbstractedFluidStack getMealFromItem(ItemStack kegStack, HolderLookup.Provider provider) {
        if (!kegStack.is(BnCItems.KEG) && !kegStack.is(BnCItems.LARGE_KEG)) {
            return AbstractedFluidStack.EMPTY;
        }

        TypedEntityData<?> data = kegStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return AbstractedFluidStack.EMPTY;
        }
        CompoundTag tag = data.copyTagWithoutId();
        if (!tag.isEmpty()) {
            if (tag.contains("FluidTank")) {
                return tag.getCompound("FluidTank")
                        .map(tank -> BrewinAndChewin.getHelper().deserializeTankFluidStack(tank, provider))
                        .orElse(AbstractedFluidStack.EMPTY);
            }
        }

        return AbstractedFluidStack.EMPTY;
    }

    public static long getCapacityForItem(ItemStack kegStack) {
        long capacity = BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity();
        return kegStack.is(BnCItems.LARGE_KEG) ? capacity * LARGE_KEG_SCALE : capacity;
    }

    public AbstractedFluidStack getOutput() {
        return fluidTank.getAbstractedFluid();
    }

    public SidedKegWrapper getSidedHandler(@Nullable Direction direction) {
        if (direction == null || direction == Direction.UP)
            return inputHandler;
        return outputHandler;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Inventory", CompoundTag.CODEC, inventory.writeToNbt(level.registryAccess()));
        output.store("FluidTank", CompoundTag.CODEC, fluidTank.writeToNbt(level.registryAccess()));
        output.putInt("FermentTime", fermentTime);
        output.putInt("FermentTimeTotal", fermentTimeTotal);
        output.putInt("Temperature", toLegacyTemperature(kegTemperature));
        output.putInt("TargetTemperature", toLegacyTemperature(targetKegTemperature));
        output.putInt("ScaledTemperature", kegTemperature);
        output.putInt("ScaledTargetTemperature", targetKegTemperature);
        output.putInt("TemperatureProgress", temperatureProgress);
        output.storeNullable("CustomName", ComponentSerialization.CODEC, customName);
        output.putInt("BurnTime", burnTime);
        CompoundTag compoundRecipes = new CompoundTag();
        usedRecipeTracker.forEach((recipeId, craftedAmount) -> compoundRecipes.putInt(recipeId.location().toString(), craftedAmount));
        output.store("RecipesUsed", CompoundTag.CODEC, compoundRecipes);
    }

    private CompoundTag writeUpdateTag(CompoundTag compound, HolderLookup.Provider provider) {
        compound.put("Inventory", inventory.writeToNbt(provider));
        compound.put("FluidTank", fluidTank.writeToNbt(provider));
        compound.putInt("FermentTime", fermentTime);
        compound.putInt("FermentTimeTotal", fermentTimeTotal);
        compound.putInt("Temperature", toLegacyTemperature(kegTemperature));
        compound.putInt("TargetTemperature", toLegacyTemperature(targetKegTemperature));
        compound.putInt("ScaledTemperature", kegTemperature);
        compound.putInt("ScaledTargetTemperature", targetKegTemperature);
        compound.putInt("TemperatureProgress", temperatureProgress);
        compound.putInt("BurnTime", burnTime);
        return compound;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        CompoundTag tag = writePreservedData(new CompoundTag(), level.registryAccess());
        if (!tag.isEmpty()) {
            components.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BnCBlockEntityTypes.KEG, tag));
        }
    }

    public CompoundTag writeDrink(CompoundTag compound, HolderLookup.Provider provider) {
        if (customName != null) {
            ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), customName)
                    .resultOrPartial(BrewinAndChewin.LOG::error)
                    .ifPresent(tag -> compound.put("CustomName", tag));
        }
        if (!fluidTank.isEmpty()) {
            compound.put("FluidTank", this.fluidTank.writeToNbt(provider));
        }
        if (!compound.isEmpty()) {
            compound.putString("id", BnCBlockEntityTypes.KEG.builtInRegistryHolder().getRegisteredName());
        }
        return compound;
    }

    public CompoundTag writePreservedData(CompoundTag compound, HolderLookup.Provider provider) {
        writeDrink(compound, provider);
        if (hasInventoryContents()) {
            compound.put("Inventory", inventory.writeToNbt(provider));
        }
        if (fermentTime > 0 || fermentTimeTotal > 0) {
            compound.putInt("FermentTime", fermentTime);
            compound.putInt("FermentTimeTotal", fermentTimeTotal);
        }
        if (burnTime > 0) {
            compound.putInt("BurnTime", burnTime);
        }
        if (!usedRecipeTracker.isEmpty()) {
            CompoundTag compoundRecipes = new CompoundTag();
            usedRecipeTracker.forEach((recipeId, craftedAmount) -> compoundRecipes.putInt(recipeId.location().toString(), craftedAmount));
            compound.put("RecipesUsed", compoundRecipes);
        }
        if (!compound.isEmpty()) {
            compound.putString("id", BnCBlockEntityTypes.KEG.builtInRegistryHolder().getRegisteredName());
        }
        return compound;
    }


    public static boolean isValidTemp(int kegTemp, int want) {
        return switch (want) {
            case 1 -> kegTemp <= 1;
            case 2 -> kegTemp <= 2;
            case 3 -> kegTemp < 5 && kegTemp > 1;
            case 4 -> kegTemp >= 4;
            case 5 -> kegTemp >= 5;
            default -> false;
        };
    }

    private static int scaleTemperature(int temperature) {
        return temperature * TEMPERATURE_SCALE;
    }

    private static int toLegacyTemperature(int scaledTemperature) {
        return Math.round(scaledTemperature / (float) TEMPERATURE_SCALE);
    }

    protected boolean canFerment(KegFermentingRecipe recipe, KegBlockEntity keg) {
        if (!hasInput()) return false;
        if (level == null) return false;
        if (!isValidTemp(keg.getTemperature(), recipe.getTemperature()))
            return false; // make sure the temperature is valid
        if (!hasRequiredIngredients(recipe))
            return false;


        if (recipe.getFluidIngredient().isEmpty()) { // if the recipe does not require a fluid
            return keg.fluidTank.isEmpty(); // make sure the fluid is empty
        }
        if (!recipe.getFluidIngredient().get().ingredient().matches(keg.fluidTank.getAbstractedFluid()))
            return false; // make sure the fluid is the same
        long requiredAmount = getScaledFluidLoaderAmount(recipe.getFluidIngredient().get());
        return keg.fluidTank.getAbstractedFluid().amount() >= requiredAmount && keg.fluidTank.getAbstractedFluid().amount() % requiredAmount == 0; // make sure the fluid amount is a multiple of the recipe amount
    }

    public static void fermentingTick(Level level, BlockPos pos, BlockState state, KegBlockEntity keg) {
        boolean didInventoryChange = false;

        boolean isBurning = keg.processBurning();

        boolean didTemperatureChange = false;
        if (!isBurning) {
            if (!keg.initialisedTemperature || level.getGameTime() % TEMPERATURE_UPDATE_INTERVAL == 0) // Every 4s
                keg.updateTemperatureTarget();

            if (level.getGameTime() % TEMPERATURE_PROGRESS_INTERVAL == 0) {
                didTemperatureChange = keg.progressTemperatureTowardsTarget();
            }
        }

        if (keg.deferFluidExtraction) {
            keg.deferFluidExtraction = false;
            List<ItemStack> out = keg.extractInGui(keg.inventory.getStackInSlot(CONTAINER_SLOT), keg.inventory.getSlotLimit(OUTPUT_SLOT));
            if (!out.isEmpty())
                keg.inventory.insertItem(OUTPUT_SLOT, out.getFirst(), false);
        }


        if (keg.hasInput()) {
            Optional<RecipeHolder<KegFermentingRecipe>> recipe = keg.getMatchingRecipe(keg.recipeWrapper);
            if (recipe.isPresent()) {
                if (keg.canFerment(recipe.get().value(), keg)) {
                    didInventoryChange = keg.processFermenting(recipe.get().value(), keg);
                } else {
                    didInventoryChange = keg.regressFermentation();
                }
            } else {
                didInventoryChange = keg.regressFermentation();
            }
        } else if (keg.fermentTime > 0 || keg.fermentTimeTotal > 0) {
            didInventoryChange = keg.regressFermentation();
        }

        if (didInventoryChange || didTemperatureChange) {
            keg.inventoryChanged();
        }
    }

    private boolean processBurning() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        syncHasLiquidState();
        List<BlockPos> heatedPositions = getHeatedPositions();
        if (heatedPositions.isEmpty()) {
            boolean changed = false;
            if (touchingBurningSource) {
                touchingBurningSource = false;
                updateTemperatureTarget();
                changed = true;
            }
            if (burnTime != 0) {
                burnTime = 0;
                changed = true;
            }
            if (changed) {
                inventoryChanged();
            }
            return false;
        }

        touchingBurningSource = true;
        boolean changed = false;
        changed |= updateBurningTemperatureTarget();
        if (level.getGameTime() % TEMPERATURE_PROGRESS_INTERVAL == 0) {
            changed |= progressTemperatureTowardsTarget(getBurningTemperatureProgressScale());
        }
        spawnFlameParticles(serverLevel, heatedPositions);
        if (isHotTemperature() && !fluidTank.isEmpty()) {
            ++burnTime;
            spawnSteamParticles(serverLevel);
            if (burnTime >= BURN_DRAIN_INTERVAL) {
                fluidTank.drain(BURN_DRAIN_AMOUNT, FluidUnit.MILLIBUCKET, false);
                burnTime = 0;
                changed = true;
            }
        } else if (burnTime != 0) {
            burnTime = 0;
            changed = true;
        }

        if (changed) {
            inventoryChanged();
        }
        return true;
    }

    private boolean updateBurningTemperatureTarget() {
        return updateTemperatureTarget(scaleTemperature(BnCConfiguration.COMMON_CONFIG.get().keg().hot()));
    }

    private void syncHasLiquidState() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(KegBlock.HAS_LIQUID) && state.getValue(KegBlock.HAS_LIQUID) != !fluidTank.isEmpty()) {
            level.setBlock(worldPosition, state.setValue(KegBlock.HAS_LIQUID, !fluidTank.isEmpty()), 3);
        }
    }

    private boolean regressFermentation() {
        if (fermentTime <= 0) {
            if (fermentTimeTotal != 0) {
                fermentTimeTotal = 0;
                return true;
            }
            return false;
        }
        fermentTime = Math.max(0, fermentTime - FERMENTATION_REGRESSION_AMOUNT);
        if (fermentTime == 0) {
            fermentTimeTotal = 0;
        }
        return true;
    }

    private List<BlockPos> getHeatedPositions() {
        if (level == null) {
            return List.of();
        }

        List<BlockPos> heatedPositions = new ArrayList<>();
        for (BlockPos offset : getKegFootprintOffsets()) {
            BlockPos kegPos = worldPosition.offset(offset);
            if (isTouchingBurningSource(kegPos)) {
                heatedPositions.add(kegPos);
            }
        }
        return heatedPositions;
    }

    private boolean hasBurningSource() {
        if (level == null) {
            return false;
        }

        for (BlockPos offset : getKegFootprintOffsets()) {
            if (isTouchingBurningSource(worldPosition.offset(offset))) {
                return true;
            }
        }
        return false;
    }

    private Iterable<BlockPos> getKegFootprintOffsets() {
        BlockState state = getBlockState();
        return state.is(BnCBlocks.LARGE_KEG) ? LargeKegBlock.getFootprintOffsets(state) : List.of(BlockPos.ZERO);
    }

    private boolean isTouchingBurningSource(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (isBurningSource(level.getBlockState(pos.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    private boolean isBurningSource(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.getFluidState().is(FluidTags.LAVA);
    }

    private void spawnFlameParticles(ServerLevel serverLevel, List<BlockPos> heatedPositions) {
        if (serverLevel.random.nextInt(4) != 0) {
            return;
        }
        BlockPos pos = heatedPositions.get(serverLevel.random.nextInt(heatedPositions.size()));
        double x = pos.getX() + 0.5D + (serverLevel.random.nextDouble() * 0.8D - 0.4D);
        double y = pos.getY() + 0.35D + serverLevel.random.nextDouble() * 0.9D;
        double z = pos.getZ() + 0.5D + (serverLevel.random.nextDouble() * 0.8D - 0.4D);
        serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.02D, 0.04D, 0.02D, 0.005D);
    }

    private void spawnSteamParticles(ServerLevel serverLevel) {
        if (serverLevel.random.nextInt(10) != 0) {
            return;
        }
        BlockPos pos = getRandomKegPosition(serverLevel);
        double x = pos.getX() + 0.5D + (serverLevel.random.nextDouble() * 0.5D - 0.25D);
        double y = pos.getY() + 0.85D + serverLevel.random.nextDouble() * 0.45D;
        double z = pos.getZ() + 0.5D + (serverLevel.random.nextDouble() * 0.5D - 0.25D);
        serverLevel.sendParticles(ModParticleTypes.STEAM.get(), x, y, z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    private BlockPos getRandomKegPosition(ServerLevel serverLevel) {
        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos offset : getKegFootprintOffsets()) {
            positions.add(worldPosition.offset(offset));
        }
        return positions.get(serverLevel.random.nextInt(positions.size()));
    }

    public Optional<RecipeHolder<KegFermentingRecipe>> getRecipeWithoutTemperature() {
        if (!hasInput())
            return Optional.empty();
        Optional<RecipeHolder<KegFermentingRecipe>> recipe = getMatchingRecipe(recipeWrapper);
        if (recipe.isEmpty())
            return Optional.empty();
        if (!hasRequiredIngredients(recipe.get().value()))
            return Optional.empty();
        if (recipe.get().value().getFluidIngredient().isEmpty()) { // if the recipe does not require a fluid
            if (!fluidTank.isEmpty()) // make sure the fluid is empty
                return Optional.empty();
        } else {
            if (!recipe.get().value().getFluidIngredient().get().ingredient().matches(fluidTank.getAbstractedFluid()))
                return Optional.empty(); // make sure the fluid is the same
            long requiredAmount = getScaledFluidLoaderAmount(recipe.get().value().getFluidIngredient().get());
            if (fluidTank.getAbstractedFluid().amount() < requiredAmount || fluidTank.getAbstractedFluid().amount() % requiredAmount != 0) // make sure the fluid amount is a multiple of the recipe amount
                return Optional.empty();
        }
        return recipe;
    }

    private Optional<RecipeHolder<KegFermentingRecipe>> getMatchingRecipe(KegRecipeWrapper inventoryWrapper) {
        if (level == null) return Optional.empty();
        RecipeManager recipeManager = recipeManager();
        if (recipeManager == null) return Optional.empty();

        if (checkNewRecipe) {
            Optional<RecipeHolder<KegFermentingRecipe>> recipe = recipeManager.getRecipeFor(BnCRecipeTypes.FERMENTING, inventoryWrapper, level);
            if (recipe.isPresent()) {
                ResourceKey<Recipe<?>> newRecipeID = recipe.get().id();
                if (lastRecipeID != null && !lastRecipeID.equals(newRecipeID)) {
                    fermentTime = 0;
                }
                lastRecipeID = newRecipeID;
                return recipe;
            }
        }
        checkNewRecipe = false;

        if (lastRecipeID != null) {
            Optional<RecipeHolder<KegFermentingRecipe>> recipe = recipeManager
                    .getRecipeFor(BnCRecipeTypes.FERMENTING, inventoryWrapper, level, lastRecipeID);
            if (recipe.isPresent() && recipe.get().value().matches(inventoryWrapper, level)) {
                return recipe;
            }
        }

        return Optional.empty();
    }

    private boolean hasInput() {
        for (int i = 0; i < OUTPUT_SLOT; ++i) {
            if (!inventory.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean hasRequiredIngredients(KegFermentingRecipe recipe) {
        int scale = getFermentationScale();
        if (scale <= 1) {
            return true;
        }

        List<ItemStack> simulatedInputs = new ArrayList<>();
        for (int i = 0; i < CONTAINER_SLOT; ++i) {
            simulatedInputs.add(inventory.getStackInSlot(i).copy());
        }

        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            if (!consumeIngredient(ingredient, simulatedInputs, scale, true)) {
                return false;
            }
        }
        return true;
    }

    private void consumeIngredients(KegFermentingRecipe recipe, int scale) {
        if (scale <= 1) {
            for (int i = 0; i < CONTAINER_SLOT; ++i) {
                extractIngredientFromSlot(i, 1);
            }
            return;
        }

        List<ItemStack> liveInputs = new ArrayList<>();
        for (int i = 0; i < CONTAINER_SLOT; ++i) {
            liveInputs.add(inventory.getStackInSlot(i));
        }

        for (Ingredient ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                consumeIngredient(ingredient, liveInputs, scale, false);
            }
        }
    }

    private boolean consumeIngredient(Ingredient ingredient, List<ItemStack> inputs, int amount, boolean simulate) {
        int remaining = amount;
        for (int i = 0; i < inputs.size() && remaining > 0; ++i) {
            ItemStack slotStack = inputs.get(i);
            if (!slotStack.isEmpty() && ingredient.test(slotStack)) {
                int taken = Math.min(remaining, slotStack.getCount());
                if (simulate) {
                    slotStack.shrink(taken);
                } else {
                    extractIngredientFromSlot(i, taken);
                    inputs.set(i, inventory.getStackInSlot(i));
                }
                remaining -= taken;
            }
        }
        return remaining == 0;
    }

    private void extractIngredientFromSlot(int slot, int amount) {
        ItemStack slotStack = inventory.getStackInSlot(slot);
        if (slotStack.isEmpty()) {
            return;
        }
        ItemStack remainder = BrewinAndChewin.getHelper().getCraftingRemainingItem(slotStack);
        for (int i = 0; i < amount && !remainder.isEmpty(); ++i) {
            ejectIngredientRemainder(remainder.copy());
        }
        inventory.extractItem(slot, amount, false);
    }

    public int getFermentationScale() {
        return getBlockState().is(BnCBlocks.LARGE_KEG) ? LARGE_KEG_SCALE : 1;
    }

    private long getScaledFluidAmount(FluidIngredientWithAmount ingredient) {
        return ingredient.amount() * getFermentationScale();
    }

    private long getScaledFluidLoaderAmount(FluidIngredientWithAmount ingredient) {
        return ingredient.loaderAmount() * getFermentationScale();
    }

    private long getKegCapacity() {
        return BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity() * getFermentationScale();
    }

    private static AbstractedFluidStack scaleFluid(AbstractedFluidStack stack, int scale) {
        return new AbstractedFluidStack(stack.fluid(), stack.amount() * scale, stack.components(), stack.unit(), null);
    }

    private boolean processFermenting(KegFermentingRecipe recipe, KegBlockEntity keg) {
        if (level == null) return false;

        int scale = getFermentationScale();
        ++fermentTime;
        fermentTimeTotal = recipe.getFermentTime();
        if (fermentTime < fermentTimeTotal) {
            setChanged();
            return false;
        }


        fermentTime = 0;
        if (recipe.getResult().left().isPresent()) {
            deferFluidExtraction = true;
            keg.fluidTank.setAbstractedFluid(scaleFluid(recipe.getResult().left().get(), scale));
            if (!keg.level.isClientSide()) {
                Vec3 center = keg.getBlockPos().getCenter();
                keg.level.playSound(null, center.x(), center.y(), center.z(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6f, 0.8f);
            }
        }

        if (recipe.getResult().right().isPresent()) {
            if (recipe.getFluidIngredient().isPresent())
                keg.fluidTank.drain(getScaledFluidAmount(recipe.getFluidIngredient().get()), recipe.getFluidIngredient().get().getUnit(), false);
            ItemStack result = recipe.getResult().right().get();
            keg.inventory.insertItem(OUTPUT_SLOT, result.copyWithCount(result.getCount() * scale), false);
        }

        consumeIngredients(recipe, scale);
        return true;
    }

    public List<ItemStack> extractInGui(ItemStack slotIn, int maxTakeAmount) {
        return fluidExtract(slotIn, maxTakeAmount, true, false);
    }

    public List<ItemStack> extractInWorld(ItemStack slotIn, int maxTakeAmount, boolean isCreative) {
        return fluidExtract(slotIn, maxTakeAmount, false, isCreative);
    }

    private List<ItemStack> fluidExtract(ItemStack slotIn, int maxTakeAmount, boolean inGui, boolean isCreative) {
        if (slotIn.isEmpty() || inGui && deferFluidExtraction ||
                inGui && isOutputSlotFull())
            return List.of();

        boolean changed = false;

        List<ItemStack> outputs = new ArrayList<>();
        currentlyOperating = true;
        clearTinyFluidRemainder();

        if (tryEmptyBnCContainerIntoKeg(slotIn, maxTakeAmount, inGui, isCreative, outputs) ||
                tryFillBnCContainerFromKeg(slotIn, maxTakeAmount, inGui, isCreative, outputs)) {
            return finishFluidExtract(outputs);
        }

        Optional<KegPouringRecipe> recipe = getPouringRecipe(slotIn);
        if (recipe.isPresent() && (fluidTank.isEmpty() || recipe.get().matchesFluid(slotIn, fluidTank.getAbstractedFluid()))) { // if the recipe is present and the fluid is empty or the same
            ItemStack resultItem = recipe.get().assemble(recipeWrapper, level.registryAccess());
            if (ItemStack.isSameItem(slotIn, recipe.get().getContainer(resultItem)) && // if container is same
                    recipe.get().getLoaderAmount() <= fluidTank.getAbstractedFluid().amount() && // the amount is LTE the fluid amount
                    (!inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || ItemStack.isSameItemSameComponents(resultItem, inventory.getStackInSlot(OUTPUT_SLOT)))) { // the output slot can accept this itemaccept this item
                int containerAmount = (int) Mth.clamp(Math.min(Math.min(slotIn.getCount(), resultItem.getMaxStackSize()), maxTakeAmount), 1, fluidTank.getAbstractedFluid().amount() / recipe.get().getLoaderAmount());
                fluidTank.drain(recipe.get().getFluidAmount() * containerAmount, recipe.get().getUnit(), false);

                long overflow = containerAmount;
                while (overflow > 0 && (!inGui || outputs.isEmpty()) && !slotIn.isEmpty()) {
                    ItemStack newResult = resultItem.copyWithCount((int) Math.min(Math.min(slotIn.getCount(), maxTakeAmount), overflow));
                    outputs.add(newResult);
                    overflow -= newResult.getCount();
                    slotIn.shrink(newResult.getCount());
                }
                if (!slotIn.isEmpty())
                    outputs.add(slotIn);
                changed = true;
            } else if (recipe.filter(KegPouringRecipe::canFill).isPresent() && // if the recipe can fill
                    (recipe.get().isStrict() && ItemStack.isSameItemSameComponents(resultItem, slotIn) || !recipe.get().isStrict() && ItemStack.isSameItem(slotIn, resultItem)) && // if result is same
                    (fluidTank.isEmpty() || recipe.get().matchesFluid(slotIn, fluidTank.getAbstractedFluid()) && fluidTank.getAbstractedFluid().amount() < fluidTank.getFluidCapacity()) && // if the result can fit in the container
                    (!inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || ItemStack.isSameItemSameComponents(recipe.get().getContainer(slotIn), inventory.getStackInSlot(OUTPUT_SLOT)))) { // the output slot can accept this item
                int containerAmount = (int) Mth.clamp(Math.min(Math.min(slotIn.getCount(), recipe.get().getContainer(slotIn).getMaxStackSize()), fluidTank.getFluidCapacity() / recipe.get().getLoaderAmount()), 1, maxTakeAmount);
                AbstractedFluidStack fillFluid = getRecipeFillFluid(recipe.get(), slotIn);
                if (!fillFluid.isEmpty()) {
                    AbstractedFluidStack fillStack = new AbstractedFluidStack(fillFluid.fluid(), recipe.get().getFluidAmount() * containerAmount, fillFluid.components(), recipe.get().getUnit(), null);
                    long insertedAmount = fluidTank.fill(fillStack, true).amount();
                    containerAmount = (int) Math.min(containerAmount, insertedAmount / recipe.get().getLoaderAmount());
                    if (containerAmount > 0) {
                        fluidTank.fill(new AbstractedFluidStack(fillFluid.fluid(), recipe.get().getFluidAmount() * containerAmount, fillFluid.components(), recipe.get().getUnit(), null), false);

                        if (!isCreative) {
                            ItemStack recipeItem = recipe.get().getContainer(slotIn);
                            int overflow = containerAmount;
                            while (overflow > 0 && !slotIn.isEmpty()) {
                                ItemStack newResult = recipeItem.copyWithCount(Math.min(Math.min(slotIn.getCount(), maxTakeAmount), overflow));
                                outputs.add(newResult);
                                overflow -= newResult.getCount();
                                slotIn.shrink(newResult.getCount());
                            }
                            if (!slotIn.isEmpty())
                                outputs.add(slotIn);
                        } else {
                            outputs.add(slotIn);
                        }
                        changed = true;
                    }
                }
            }

            if (changed) {
                setChanged();
                inventoryChanged();
            }
        }

        if (!outputs.isEmpty() || changed) {
            currentlyOperating = false;
            return outputs;
        }

        // TODO: Account for stacks with multiple tanks.
        AbstractedFluidTank itemFluidContainer = BrewinAndChewin.getHelper().getFluidContainerFromItem(slotIn);

        if (itemFluidContainer != null && !slotIn.isEmpty()) {
            AbstractedFluidStack kegFluid = fluidTank.getAbstractedFluid();
            AbstractedFluidStack itemFluid = itemFluidContainer.getAbstractedFluid();
            ItemStack containerResult = BrewinAndChewin.getHelper().getCraftingRemainingItem(slotIn);
            if (containerResult.isEmpty())
                containerResult = itemFluidContainer.getContainer();

            if (!itemFluid.isEmpty() && maxTakeAmount > 0 &&
                    (kegFluid.matches(itemFluid) || kegFluid.isEmpty()) &&
                    (!inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || inventory.getStackInSlot(OUTPUT_SLOT).is(containerResult.getItem())) &&
                    canStoreContainerFluid(slotIn, itemFluid)) {
                long amountToDrain = fluidTank.getFluidCapacity() - kegFluid.amount();
                AbstractedFluidStack simulatedDrain = itemFluidContainer.drain(amountToDrain, FluidUnit.getLoaderUnit(), true);
                if (!simulatedDrain.isEmpty()) {
                    long amount = fluidTank.fill(simulatedDrain, true).amount();
                    if (amount > 0) {
                        AbstractedFluidStack drained = itemFluidContainer.drain(amount, FluidUnit.getLoaderUnit(), false);
                        if (!drained.isEmpty()) {
                            fluidTank.fill(drained, false);
                            if (!isCreative) {
                                ItemStack newResult = itemFluidContainer.getContainer();
                                if (newResult.isEmpty())
                                    newResult = containerResult.copy();
                                newResult.setCount(1);
                                outputs.add(newResult);
                                slotIn.shrink(1);
                                if (!slotIn.isEmpty())
                                    outputs.add(slotIn);
                            } else {
                                outputs.add(slotIn);
                            }
                            setChanged();
                            inventoryChanged();
                        }
                    }
                }
            } else if (!kegFluid.isEmpty()) {
                AbstractedFluidStack fillFluid = getBucketFillFluid(kegFluid);
                if (fillFluid.isEmpty())
                    return finishFluidExtract(outputs);

                ItemStack filledContainerResult = getBucketForFluid(kegFluid);
                if (filledContainerResult.isEmpty())
                    filledContainerResult = itemFluidContainer.getContainer();
                boolean outputSlotCanAccept = !inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                        (!filledContainerResult.isEmpty() && inventory.getStackInSlot(OUTPUT_SLOT).is(filledContainerResult.getItem()));

                if (outputSlotCanAccept) {
                    long amountToDrain = itemFluidContainer.getFluidCapacity();
                    if (amountToDrain > 0) {
                        itemFluidContainer = BrewinAndChewin.getHelper().getFluidContainerFromItem(slotIn.copyWithCount(1));
                        if (itemFluidContainer != null) {
                            AbstractedFluidStack simulatedDrain = fluidTank.drain(amountToDrain, FluidUnit.getLoaderUnit(), true);
                            if (simulatedDrain.isEmpty())
                                return finishFluidExtract(outputs);

                            long amount = itemFluidContainer.fill(copyWithFluid(simulatedDrain, fillFluid.fluid()), true).amount();
                            if (amount > 0) {
                                AbstractedFluidStack drained = fluidTank.drain(amount, FluidUnit.getLoaderUnit(), false);
                                if (drained.isEmpty())
                                    return finishFluidExtract(outputs);

                                AbstractedFluidStack filled = itemFluidContainer.fill(copyWithFluid(drained, fillFluid.fluid()), false);
                                if (!filled.isEmpty()) {
                                    ItemStack newResult = itemFluidContainer.getContainer();
                                    if (newResult.isEmpty())
                                        newResult = filledContainerResult.copy();
                                    newResult.setCount(1);
                                    outputs.add(newResult);
                                    slotIn.shrink(1);
                                    if (!slotIn.isEmpty())
                                        outputs.add(slotIn);
                                    setChanged();
                                    inventoryChanged();
                                }
                            }
                        }
                    }
                }
            }

        }

        return finishFluidExtract(outputs);
    }

    private List<ItemStack> finishFluidExtract(List<ItemStack> outputs) {
        currentlyOperating = false;
        return outputs;
    }

    private boolean tryEmptyBnCContainerIntoKeg(ItemStack slotIn, int maxTakeAmount, boolean inGui, boolean isCreative, List<ItemStack> outputs) {
        if (slotIn.isEmpty() || maxTakeAmount <= 0) {
            return false;
        }

        DirectContainerFluid containerFluid = getDirectContainerFluid(slotIn);
        if (containerFluid == null || !canOutputContainer(inGui, containerFluid.emptyContainer())) {
            return false;
        }

        AbstractedFluidStack transferStack = createMillibucketStack(containerFluid.fluid(), containerFluid.amount());
        AbstractedFluidStack kegFluid = fluidTank.getAbstractedFluid();
        if (!kegFluid.isEmpty() && !kegFluid.matches(transferStack)) {
            return false;
        }

        long insertedAmount = fluidTank.fill(transferStack, true).amount();
        if (insertedAmount < transferStack.unit().convertToLoader(transferStack.amount())) {
            return false;
        }

        fluidTank.fill(transferStack, false);
        addContainerResult(slotIn, containerFluid.emptyContainer(), !isCreative, outputs);
        markFluidTransferChanged();
        return true;
    }

    private boolean tryFillBnCContainerFromKeg(ItemStack slotIn, int maxTakeAmount, boolean inGui, boolean isCreative, List<ItemStack> outputs) {
        if (slotIn.isEmpty() || maxTakeAmount <= 0 || fluidTank.isEmpty()) {
            return false;
        }

        AbstractedFluidStack kegFluid = fluidTank.getAbstractedFluid();
        FilledContainerTarget target = getDirectFillTarget(slotIn, kegFluid);
        if (target == null || !canOutputContainer(inGui, target.filledContainer())) {
            return false;
        }

        long loaderAmount = kegFluid.unit().convertToLoader(kegFluid.amount());
        long loaderTarget = FluidUnit.MILLIBUCKET.convertToLoader(target.amount());
        if (loaderAmount < loaderTarget)
            return false;
        AbstractedFluidStack drained = fluidTank.drain(target.amount(), FluidUnit.MILLIBUCKET, false);
        if (drained.unit().convertToLoader(drained.amount()) < loaderTarget) {
            fluidTank.setAbstractedFluid(kegFluid);
            markFluidTransferChanged();
            return false;
        }

        addContainerResult(slotIn, target.filledContainer(), true, outputs);
        markFluidTransferChanged();
        return true;
    }

    private DirectContainerFluid getDirectContainerFluid(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BnCBucketItem bucket) {
            return new DirectContainerFluid(bucket.getFluid(), BUCKET_TRANSFER_AMOUNT, Items.BUCKET.getDefaultInstance());
        }
        if (item instanceof BoozeItem booze) {
            return new DirectContainerFluid(booze.getFluid(), TANKARD_TRANSFER_AMOUNT, BnCItems.TANKARD.getDefaultInstance());
        }
        return null;
    }

    private FilledContainerTarget getDirectFillTarget(ItemStack stack, AbstractedFluidStack kegFluid) {
        if (stack.is(Items.BUCKET)) {
            ItemStack bucketStack = getBucketForFluid(kegFluid);
            if (bucketStack.isEmpty()) {
                return null;
            }
            return new FilledContainerTarget(bucketStack, BUCKET_TRANSFER_AMOUNT);
        }
        if (stack.is(BnCItems.TANKARD)) {
            ItemStack drink = getDrinkForFluid(kegFluid);
            if (drink.isEmpty()) {
                return null;
            }
            return new FilledContainerTarget(drink, TANKARD_TRANSFER_AMOUNT);
        }
        return null;
    }

    private ItemStack getDrinkForFluid(AbstractedFluidStack fluidStack) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BoozeItem booze && booze.getFluid().isSame(fluidStack.fluid())) {
                return item.getDefaultInstance();
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack getBucketForFluid(AbstractedFluidStack kegFluid) {
        AbstractedFluidStack fillFluid = getBucketFillFluid(kegFluid);
        if (fillFluid.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack recipeBucket = getBucketRecipeOutput(fillFluid);
        if (!recipeBucket.isEmpty()) {
            return recipeBucket;
        }

        Item bucketItem = fillFluid.fluid().getBucket();
        if (bucketItem != Items.AIR && bucketItem != Items.BUCKET) {
            return bucketItem.getDefaultInstance();
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BnCBucketItem bucket && bucket.getFluid().isSame(fillFluid.fluid())) {
                return item.getDefaultInstance();
            }
        }

        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fillFluid.fluid());
        if (BrewinAndChewin.MODID.equals(fluidId.getNamespace())) {
            Optional<Item> bucketItemById = BuiltInRegistries.ITEM.getOptional(BrewinAndChewin.asResource(fluidId.getPath() + "_bucket"));
            if (bucketItemById.isPresent()) {
                return bucketItemById.get().getDefaultInstance();
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack getBucketRecipeOutput(AbstractedFluidStack fluid) {
        return getPouringRecipes().stream()
                .filter(recipe -> recipe.matchesFluid(ItemStack.EMPTY, fluid))
                .map(KegPouringRecipe::getOutput)
                .filter(this::leavesEmptyBucket)
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    private boolean leavesEmptyBucket(ItemStack stack) {
        ItemStack remainder = BrewinAndChewin.getHelper().getCraftingRemainingItem(stack);
        return remainder.is(Items.BUCKET);
    }

    private AbstractedFluidStack createMillibucketStack(Fluid fluid, long amount) {
        return new AbstractedFluidStack(fluid, amount, DataComponentMap.EMPTY, FluidUnit.MILLIBUCKET, null);
    }

    public static boolean isDirectKegFluidContainer(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BnCBucketItem || item instanceof BoozeItem || stack.is(Items.BUCKET) || stack.is(BnCItems.TANKARD)) {
            return true;
        }

        AbstractedFluidTank itemFluidContainer = BrewinAndChewin.getHelper().getFluidContainerFromItem(stack);
        if (itemFluidContainer == null) {
            return false;
        }

        AbstractedFluidStack itemFluid = itemFluidContainer.getAbstractedFluid();
        return !itemFluid.isEmpty() && isDirectlyStorableFluid(itemFluid.fluid());
    }

    private boolean canOutputContainer(boolean inGui, ItemStack result) {
        return !inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                ItemStack.isSameItemSameComponents(result, inventory.getStackInSlot(OUTPUT_SLOT));
    }

    private void addContainerResult(ItemStack slotIn, ItemStack result, boolean consumeInput, List<ItemStack> outputs) {
        if (!consumeInput) {
            outputs.add(slotIn);
            return;
        }

        outputs.add(result.copyWithCount(1));
        slotIn.shrink(1);
        if (!slotIn.isEmpty()) {
            outputs.add(slotIn);
        }
    }

    private void markFluidTransferChanged() {
        setChanged();
        inventoryChanged();
    }

    private void clearTinyFluidRemainder() {
        AbstractedFluidStack stack = fluidTank.getAbstractedFluid();
        if (!stack.isEmpty() && stack.unit().convertToLoader(stack.amount()) < TINY_FLUID_REMAINDER) {
            fluidTank.setAbstractedFluid(AbstractedFluidStack.EMPTY);
            markFluidTransferChanged();
        }
    }

    private boolean isOutputSlotFull() {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.getCount() >= Math.min(inventory.getSlotLimit(OUTPUT_SLOT), output.getMaxStackSize());
    }

    private record DirectContainerFluid(Fluid fluid, long amount, ItemStack emptyContainer) {
    }

    private record FilledContainerTarget(ItemStack filledContainer, long amount) {
    }

    private boolean hasCompatiblePouringRecipe(ItemStack slot, AbstractedFluidStack fluid) {
        return getPouringRecipes().stream()
                .anyMatch(pouringRecipe -> pouringRecipe.matchesFluid(slot, fluid));
    }

    private boolean canStoreContainerFluid(ItemStack slot, AbstractedFluidStack fluid) {
        if (hasCompatiblePouringRecipe(slot, fluid))
            return true;

        if (isDirectlyStorableFluid(fluid.fluid()))
            return true;

        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid.fluid());
        return BrewinAndChewin.MODID.equals(fluidId.getNamespace());
    }

    private static boolean isDirectlyStorableFluid(Fluid fluid) {
        if (BnCFluids.HONEY != null && fluid.isSame(BnCFluids.HONEY)) {
            return true;
        }
        if (BnCFluids.FLOWING_HONEY != null && fluid.isSame(BnCFluids.FLOWING_HONEY)) {
            return true;
        }

        Fluid milk = BrewinAndChewin.getHelper().getMilkFluid();
        if (milk != null && fluid.isSame(milk)) {
            return true;
        }
        Fluid flowingMilk = BrewinAndChewin.getHelper().getFlowingMilkFluid();
        return flowingMilk != null && fluid.isSame(flowingMilk);
    }

    private AbstractedFluidStack getBucketFillFluid(AbstractedFluidStack kegFluid) {
        Fluid createHoney = BrewinAndChewin.getHelper().getCreateHoneyFluid();
        if (createHoney != null && kegFluid.fluid().isSame(BnCFluids.HONEY))
            return copyWithFluid(kegFluid, createHoney);
        return kegFluid;
    }

    private AbstractedFluidStack copyWithFluid(AbstractedFluidStack stack, Fluid fluid) {
        return new AbstractedFluidStack(fluid, stack.amount(), stack.components(), stack.unit(), null);
    }

    private AbstractedFluidStack getRecipeFillFluid(KegPouringRecipe recipe, ItemStack slot) {
        AbstractedFluidTank itemFluidContainer = BrewinAndChewin.getHelper().getFluidContainerFromItem(slot);
        if (itemFluidContainer != null) {
            AbstractedFluidStack itemFluid = itemFluidContainer.getAbstractedFluid();
            if (!itemFluid.isEmpty() && recipe.matchesFluid(slot, itemFluid))
                return itemFluid;
        }
        return recipe.getFluid(slot);
    }


    public Optional<KegPouringRecipe> getPouringRecipe(ItemStack slot) {
        return getPouringRecipes().stream()
                .sorted(Comparator.comparingInt(value -> value.isStrict() ? 0 : 1))
                .filter(r -> {
                    boolean containerCheck = false;
                    boolean resultCheck = false;
                    boolean fluidCheck = false;
                    if (r.isStrict() && ItemStack.isSameItemSameComponents(r.getContainer(), slot) || !r.isStrict() && (r.getContainer().getItem() == slot.getItem()))
                        containerCheck = true;
                    if (!containerCheck && r.canFill() && (r.isStrict() && ItemStack.isSameItemSameComponents(r.assemble(recipeWrapper, level.registryAccess()), slot) || !r.isStrict() && r.assemble(recipeWrapper, level.registryAccess()).getItem() == slot.getItem()))
                        resultCheck = true;
                    if (recipeWrapper.getFluid().isEmpty() || r.matchesFluid(slot, recipeWrapper.getFluid()))
                        fluidCheck = true;
                    return (containerCheck || resultCheck) && fluidCheck;
                })
                .findFirst();
    }

    private List<KegPouringRecipe> getPouringRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) {
            return List.of();
        }
        return BnCRecipeUtils.getRecipes(manager, BnCRecipeTypes.KEG_POURING).stream()
                .map(RecipeHolder::value)
                .toList();
    }

    public void updateTemperature() {
        updateTemperatureTarget();
        boolean changed = progressTemperatureTowardsTarget();
        if (changed) {
            inventoryChanged();
        }
    }

    public boolean updateTemperatureTarget() {
        return updateTemperatureTarget(calculateTargetTemperature());
    }

    private boolean updateTemperatureTarget(int temp) {
        if (initialisedTemperature && temp == targetKegTemperature) {
            return false;
        }
        initialisedTemperature = true;
        targetKegTemperature = temp;
        setChanged();
        return true;
    }

    private int calculateTargetTemperature() {
        int heat = 0;
        int cold = 0;
        for (BlockPos sourcePos : getTemperatureSamplePositions()) {
            BlockState sourceState = level.getBlockState(sourcePos);
            if (sourceState.is(ModTags.HEAT_SOURCES) && isActiveTemperatureSource(sourceState)) {
                ++heat;
            }
            if (sourceState.is(BnCTags.Blocks.FREEZE_SOURCES) && isActiveTemperatureSource(sourceState)) {
                ++cold;
            }
        }

        int temp = (heat - cold) * TEMPERATURE_SCALE / getTemperatureSourceScale();

        if (BnCConfiguration.COMMON_CONFIG.get().keg().biomeTemp()) {
            Holder<Biome> biome = level.getBiome(worldPosition);
            if (biome.isBound()) {
                float biomeTemperature = biome.value().getBaseTemperature();
                if (biomeTemperature <= 0) {
                    temp -= TEMPERATURE_SCALE;
                } else if (biomeTemperature == 2) {
                    temp += TEMPERATURE_SCALE;
                }
            }
        }

        if (BnCConfiguration.COMMON_CONFIG.get().keg().dimTemp() && level.dimensionType().ultraWarm())
            temp += 2 * TEMPERATURE_SCALE;

        if (hasBurningSource()) {
            temp = Math.max(temp, scaleTemperature(BnCConfiguration.COMMON_CONFIG.get().keg().hot()));
        }

        return temp;
    }

    private static boolean isActiveTemperatureSource(BlockState state) {
        return !state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT);
    }

    private boolean progressTemperatureTowardsTarget() {
        return progressTemperatureTowardsTarget(getTemperatureProgressScale());
    }

    private boolean progressTemperatureTowardsTarget(int progressScale) {
        if (kegTemperature == targetKegTemperature) {
            if (temperatureProgress != 0) {
                temperatureProgress = 0;
                setChanged();
            }
            return false;
        }

        ++temperatureProgress;
        if (temperatureProgress < progressScale) {
            setChanged();
            return false;
        }

        temperatureProgress = 0;
        int temperatureDifference = targetKegTemperature - kegTemperature;
        int temperatureStep = Math.min(TEMPERATURE_SCALE, Math.abs(temperatureDifference));
        kegTemperature += Integer.signum(temperatureDifference) * temperatureStep;
        return true;
    }

    private Set<BlockPos> getTemperatureSamplePositions() {
        Set<BlockPos> positions = new HashSet<>();
        for (BlockPos offset : getKegFootprintOffsets()) {
            BlockPos kegPos = worldPosition.offset(offset);
            for (int x = -RANGE; x <= RANGE; x++) {
                for (int y = -RANGE; y <= RANGE; y++) {
                    for (int z = -RANGE; z <= RANGE; z++) {
                        positions.add(kegPos.offset(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    private int getTemperatureSourceScale() {
        return getBlockState().is(BnCBlocks.LARGE_KEG) ? LARGE_KEG_TEMPERATURE_SOURCE_SCALE : 1;
    }

    private int getTemperatureProgressScale() {
        return getTemperatureProgressScale(touchingBurningSource);
    }

    private int getBurningTemperatureProgressScale() {
        return getTemperatureProgressScale(true);
    }

    private int getTemperatureProgressScale(boolean burning) {
        int progressScale = burning ? BURN_TEMPERATURE_PROGRESS_SCALE : TEMPERATURE_UPDATE_INTERVAL / TEMPERATURE_PROGRESS_INTERVAL;
        return progressScale * (getBlockState().is(BnCBlocks.LARGE_KEG) ? LARGE_KEG_SCALE : 1);
    }

    private boolean isHotTemperature() {
        return kegTemperature >= scaleTemperature(BnCConfiguration.COMMON_CONFIG.get().keg().hot());
    }

    public int getTemperature() {
        if (kegTemperature <= -scaleTemperature(BnCConfiguration.COMMON_CONFIG.get().keg().cold())) {
            return 1;
        } else if (kegTemperature <= -scaleTemperature(BnCConfiguration.COMMON_CONFIG.get().keg().chilly())) {
            return 2;
        } else if (kegTemperature < scaleTemperature(BnCConfiguration.COMMON_CONFIG.get().keg().warm())) {
            return 3;
        } else if (kegTemperature < scaleTemperature(BnCConfiguration.COMMON_CONFIG.get().keg().hot())) {
            return 4;
        } else {
            return 5;
        }
    }

    protected void ejectIngredientRemainder(ItemStack remainderStack) {
        Direction direction = getBlockState().getValue(KegBlock.FACING).getCounterClockWise();
        double x = worldPosition.getX() + 0.5 + (direction.getStepX() * 0.25);
        double y = worldPosition.getY() + 0.7;
        double z = worldPosition.getZ() + 0.5 + (direction.getStepZ() * 0.25);
        ItemUtils.spawnItemEntity(level, remainderStack, x, y, z,
                direction.getStepX() * 0.08F, 0.25F, direction.getStepZ() * 0.08F);
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        if (recipe != null) {
            ResourceKey<Recipe<?>> recipeID = recipe.id();
            usedRecipeTracker.addTo(recipeID, 1);
        }
    }

    @Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> items) {
        List<RecipeHolder<?>> usedRecipes = getUsedRecipesAndPopExperience(player.level(), player.position());
        player.awardRecipes(usedRecipes);
        usedRecipeTracker.clear();
    }

    public List<RecipeHolder<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
        List<RecipeHolder<?>> list = Lists.newArrayList();

        for (Object2IntMap.Entry<ResourceKey<Recipe<?>>> entry : usedRecipeTracker.object2IntEntrySet()) {
            ((ServerLevel) level).recipeAccess().byKey(entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                splitAndSpawnExperience((ServerLevel) level, pos, entry.getIntValue(), ((KegFermentingRecipe) recipe.value()).getExperience());
            });
        }

        return list;
    }

    private static void splitAndSpawnExperience(ServerLevel level, Vec3 pos, int craftedAmount, float experience) {
        int expTotal = Mth.floor((float) craftedAmount * experience);
        float expFraction = Mth.frac((float) craftedAmount * experience);
        if (expFraction > 0.0F && Math.random() < (double) expFraction) {
            ++expTotal;
        }

        ExperienceOrb.award(level, pos, expTotal);
    }

    public AbstractedItemHandler getInventory() {
        return inventory;
    }

    public AbstractedFluidTank getFluidTank() {
        return fluidTank;
    }

    public NonNullList<ItemStack> getDroppableInventory() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < INVENTORY_SIZE; ++i) {
            drops.add(inventory.getStackInSlot(i));
        }
        return drops;
    }

    private boolean hasInventoryContents() {
        for (int i = 0; i < INVENTORY_SIZE; ++i) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldLoseContentsOnRemoval() {
        return burnedByFire;
    }

    public void markBurnedByFire() {
        burnedByFire = true;
    }

    @Override
    public Component getName() {
        return customName != null ? customName : BnCTextUtils.getTranslation("container.keg");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(Component name) {
        customName = name;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
        return new KegMenu(id, player, this, kegData);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return writeUpdateTag(new CompoundTag(), provider);
    }

    public void setBlockEntityData(ItemStack stack, HolderLookup.Provider provider) {
        CompoundTag tag = writePreservedData(new CompoundTag(), provider);
        if (tag.isEmpty()) {
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
        } else {
            stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BnCBlockEntityTypes.KEG, tag));
        }
    }

    private RecipeManager recipeManager() {
        return level instanceof ServerLevel serverLevel ? serverLevel.recipeAccess() : null;
    }

    private AbstractedItemHandler createHandler() {
        return BrewinAndChewin.getHelper().createKegInventory(INVENTORY_SIZE, (handler, slot) -> {
            if (!getLevel().isClientSide() && (slot == CONTAINER_SLOT || slot == OUTPUT_SLOT) && !currentlyOperating) {
                deferFluidExtraction = true;
            }
            if (slot >= 0 && slot < OUTPUT_SLOT) {
                checkNewRecipe = true;
            }
            inventoryChanged();
        });
    }

    private AbstractedFluidTank createFluidTank() {
        return BrewinAndChewin.getHelper().createKegTank(getKegCapacity(), () -> {
            AbstractedItemHandler handler = KegBlockEntity.this.inventory;
            if (!getLevel().isClientSide() && !currentlyOperating && !deferFluidExtraction) {
                List<ItemStack> out = KegBlockEntity.this.extractInGui(handler.getStackInSlot(CONTAINER_SLOT), handler.getSlotLimit(OUTPUT_SLOT));
                if (!out.isEmpty())
                    handler.insertItem(OUTPUT_SLOT, out.get(0), false);
            }
            inventoryChanged();
            checkNewRecipe = true;
            syncHasLiquidState();
        });
    }

    private ContainerData createIntArray() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> KegBlockEntity.this.fermentTime;
                    case 1 -> KegBlockEntity.this.fermentTimeTotal;
                    case 2 -> KegBlockEntity.this.getTemperature();
                    case 3 -> KegBlockEntity.this.kegTemperature;
                    case 4 -> KegBlockEntity.this.targetKegTemperature;
                    case 5 -> KegBlockEntity.this.temperatureProgress;
                    case 6 -> KegBlockEntity.this.getTemperatureProgressScale();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> KegBlockEntity.this.fermentTime = value;
                    case 1 -> KegBlockEntity.this.fermentTimeTotal = value;
                }
            }

            @Override
            public int getCount() {
                return 7;
            }
        };
    }
}

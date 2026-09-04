package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Predicate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class PredicateTests {

    public static final String EMPTY = "empty_5x5";
    public static final String PREDICATE_TEST = "predicateTest";

    private static final BlockPos ONE = new BlockPos(1, 0, 1);

    private static MultiPredicate STONE_PREDICATE;
    private static MultiPredicate DIRT_PREDICATE;

    @BeforeBatch(batch = PREDICATE_TEST)
    public static void setupPredicateTests(ServerLevel level) {
        STONE_PREDICATE = Predicates.blocks(Blocks.STONE);
        DIRT_PREDICATE = Predicates.blocks(Blocks.DIRT);
    }

    // @GameTest(template = EMPTY, batch = PREDICATE_TEST)
    // public static void runPredicateTests(GameTestHelper helper) {
    // testPredicateCounts(helper);
    //
    // helper.succeed();
    // }

    @GameTest(template = EMPTY, batch = PREDICATE_TEST)
    public static void testAndPredicateLogic(GameTestHelper helper) {
        Object2IntMap<Block> toPlace = new Object2IntArrayMap<>(2);
        toPlace.put(Blocks.STONE, 4);
        toPlace.put(Blocks.DIRT, 4);

        var extension = new HelperExtension(helper);
        extension.setPos(ONE);
        final BlockPos anchor = extension.getRelativePos();
        extension.placeBlocks(2, 4, anchor, pos -> {
            if (toPlace.getInt(Blocks.STONE) > 0) {
                extension.setBlock(Blocks.STONE);
                toPlace.merge(Blocks.STONE, -1, Integer::sum);
            } else if (toPlace.getInt(Blocks.DIRT) > 0) {
                extension.setBlock(Blocks.DIRT);
                toPlace.merge(Blocks.DIRT, -1, Integer::sum);
            }
        });

        MultiPredicate combined = STONE_PREDICATE.and(DIRT_PREDICATE).withMaxCount(4);

        // test
        Predicate<PredicateContext> testingFunction = ctx -> {
            BasePredicate predicate = combined.getPredicateAtPos(ctx);
            return predicate != null && predicate.checkMaxCount(ctx);
        };

        boolean passed;
        // 4 stone, 4 dirt
        passed = extension.testMove(2, 4, anchor, testingFunction);
        helper.assertFalse(passed, "predicate did not fail as expected");
        extension.clearCounts();
        // 2 stone, 2 dirt
        passed = extension.testMove(2, 2, anchor, testingFunction);
        helper.assertTrue(passed, "predicate did not pass as expected");
    }

    @GameTest(template = EMPTY, batch = PREDICATE_TEST)
    public static void testXorPredicateLogic(GameTestHelper helper) {
        var extension = new HelperExtension(helper);
        extension.placeBlocks(2, 1, ONE, pos -> {
            if (pos.getX() == 0) {
                extension.setBlock(Blocks.STONE);
            } else {
                extension.setBlock(Blocks.DIRT);
            }
        });

        MultiPredicate combined = STONE_PREDICATE.xor(DIRT_PREDICATE);
        boolean passed;
        passed = extension.testMove(2, 1, ONE, ctx -> {
            BasePredicate predicate = combined.getPredicateAtPos(ctx);
            return predicate != null && predicate.checkMaxCount(ctx);
        });
        helper.assertFalse(passed, "predicate did not fail as expected");
    }

    // private static void testPredicateCounts(GameTestHelper helper) {
    // MultiPredicate stonePredicate = Predicates.blocks(Blocks.STONE);
    // MultiPredicate dirtPredicate = Predicates.blocks(Blocks.DIRT);
    // MultiPredicate andPredicate = stonePredicate.and(dirtPredicate).withMaxCount(4);
    // MultiPredicate orPredicate = stonePredicate.or(dirtPredicate);
    // MultiPredicate xorPredicate = stonePredicate.xor(dirtPredicate);
    // setPos(ONE);
    // BlockPos anchor = getRelativePos();
    //
    // Object2IntMap<Block> toPlace = new Object2IntArrayMap<>(2);
    // toPlace.put(Blocks.STONE, 4);
    // toPlace.put(Blocks.DIRT, 4);
    //
    // testMove(2, 4, anchor, () -> {
    // if (toPlace.getInt(Blocks.STONE) > 0) {
    // setBlock(Blocks.STONE);
    // toPlace.merge(Blocks.STONE, -1, Integer::sum);
    // } else if (toPlace.getInt(Blocks.DIRT) > 0) {
    // setBlock(Blocks.DIRT);
    // toPlace.merge(Blocks.DIRT, -1, Integer::sum);
    // }
    // return true;
    // });
    //
    // // test
    // BooleanSupplier testingFunction = toTestFunction(andPredicate);
    //
    // boolean passed;
    // // 4 stone, 4 dirt
    // passed = testMove(2, 4, anchor, testingFunction);
    // helper.assertFalse(passed, "predicate did not fail as expected");
    // clearCounts();
    // // 2 stone, 2 dirt
    // passed = testMove(2, 2, anchor, testingFunction);
    // helper.assertTrue(passed, "predicate did not pass as expected");
    //
    // // test xor
    // testingFunction = toTestFunction(xorPredicate);
    // passed = testMove(2, 4, anchor, testingFunction);
    // helper.assertFalse(passed, "predicate did not fail as expected");
    // }
    //
    // @NotNull
    // private static BooleanSupplier toTestFunction(MultiPredicate andPredicate) {
    // return () -> {
    // BasePredicate predicate = andPredicate.getPredicateAtPos(ctx);
    // return predicate != null && predicate.checkMaxCount(ctx);
    // };
    // }

    @AfterBatch(batch = PREDICATE_TEST)
    public static void disposePredicateTests(ServerLevel level) {
        // ctx = null;
    }

    private static class HelperExtension {

        @Getter
        final GameTestHelper helper;
        @Getter
        final PredicateContext ctx;

        @Getter
        BlockPos absolutePos;
        @Getter
        BlockPos relativePos;

        HelperExtension(GameTestHelper helper) {
            this.helper = helper;
            this.ctx = new PredicateContext(null);
        }

        private boolean testMove(int xMax, int zMax, BlockPos anchor, Predicate<PredicateContext> tester) {
            for (int x = 0; x < xMax; x++) {
                for (int z = 0; z < zMax; z++) {
                    BlockPos offset = anchor.offset(x, 0, z);
                    setPos(offset);
                    if (!tester.test(this.ctx)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private void placeBlocks(int xMax, int zMax, BlockPos anchor, Consumer<BlockPos> consumer) {
            for (int x = 0; x < xMax; x++) {
                for (int z = 0; z < zMax; z++) {
                    BlockPos offset = anchor.offset(x, 0, z);
                    setPos(offset);
                    consumer.accept(offset);
                }
            }
        }

        private void move(Direction dir) {
            this.setPos(getRelativePos().relative(dir));
        }

        private void setPos(BlockPos pos) {
            this.relativePos = pos;
            this.absolutePos = helper.absolutePos(pos);
            this.ctx.updatePos(absolutePos);
        }

        private void setBlock(Block block) {
            this.helper.setBlock(this.relativePos, block);
            this.ctx.updatePos(this.absolutePos);
        }

        private void clearCounts() {
            this.ctx.clearGlobalCounts();
            this.ctx.clearLayerCounts();
        }
    }
}

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import lombok.Getter;

import java.util.function.BooleanSupplier;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class PredicateTests {

    private static PredicateContext ctx;
    private static GameTestHelper testHelper;
    private static final BlockPos ONE = new BlockPos(1, 0, 1);

    @Getter
    private static BlockPos relativePos;
    @Getter
    private static BlockPos absolutePos;

    @BeforeBatch(batch = "predicateTest")
    public static void setupPredicateTests(ServerLevel level) {
        ctx = new PredicateContext(null);
        ctx.updateLevel(level);
    }

    @GameTest(template = "empty_5x5", batch = "predicateTest")
    public static void runPredicateTests(GameTestHelper helper) {
        testHelper = helper;

        clearCounts();
        testPredicateCounts(helper);

        helper.succeed();
    }

    private static void testPredicateCounts(GameTestHelper helper) {
        MultiPredicate stonePredicate = Predicates.blocks(Blocks.STONE);
        MultiPredicate dirtPredicate = Predicates.blocks(Blocks.DIRT);
        MultiPredicate combined = stonePredicate.and(dirtPredicate).withMaxCount(4);
        setPos(ONE);
        BlockPos anchor = getRelativePos();

        // fill blocks
        for (int i = 0; i < 4; i++) {
            setBlock(Blocks.STONE);
            move(Direction.NORTH);
        }
        move(Direction.WEST);
        for (int i = 0; i < 4; i++) {
            move(Direction.SOUTH);
            setBlock(Blocks.DIRT);
        }

        // test
        BooleanSupplier testingFunction = () -> {
            BasePredicate predicate = combined.getPredicateAtPos(ctx);
            return predicate != null && combined.testMaxCount(predicate, ctx);
        };

        boolean passed;
        // 4 stone, 4 dirt
        passed = testMove(2, 4, anchor, testingFunction);
        helper.assertFalse(passed, "predicate did not fail as expected");
        clearCounts();
        // 2 stone, 2 dirt
        passed = testMove(2, 2, anchor, testingFunction);
        helper.assertTrue(passed, "predicate did not pass as expected");
    }

    private static boolean testMove(int xMax, int zMax, BlockPos anchor, BooleanSupplier tester) {
        for (int x = 0; x < xMax; x++) {
            for (int z = 0; z < zMax; z++) {
                BlockPos offset = anchor.offset(x, 0, z);
                setPos(offset);
                if (!tester.getAsBoolean()) {
                    return false;
                }
            }
        }
        return true;
    }

    @AfterBatch(batch = "predicateTest")
    public static void disposePredicateTests(ServerLevel level) {
        testHelper = null;
        ctx = null;
    }

    private static void setBlock(Block block) {
        testHelper.setBlock(getRelativePos(), block);
        updateContext();
    }

    private static BlockState getBlockState() {
        return ctx.state();
    }

    private static void move(Direction direction) {
        setPos(getRelativePos().relative(direction));
    }

    private static void setPos(int x, int y, int z) {
        setPos(new BlockPos(x, y, z));
    }

    private static void setPos(BlockPos pos) {
        relativePos = pos;
        absolutePos = testHelper.absolutePos(pos);
        ctx.updatePos(absolutePos);
    }

    private static void updateContext() {
        ctx.updatePos(ctx.pos());
    }

    private static void clearCounts() {
        ctx.clearLayerCounts();
        ctx.clearGlobalCounts();
    }
}

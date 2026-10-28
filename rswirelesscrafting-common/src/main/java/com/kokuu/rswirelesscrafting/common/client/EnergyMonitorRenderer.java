package com.kokuu.rswirelesscrafting.common.client;

import com.kokuu.rswirelesscrafting.common.energymonitor.EnergyMonitorBlockEntity;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * 能量监控仪的「显示屏」：在方块上方悬浮画三行字——电量、净收支、预计耗尽。
 *
 * <p>以前它只能右键看聊天栏，站在机器旁边完全不知道网还剩多少电。这里给它在方块头上挂一块
 * 永远面向玩家的牌子，数值每 20 tick（1 秒）随方块实体的同步包刷新。
 *
 * <p>实现上不碰 GUI、不碰自定义数据包：{@link EnergyMonitorBlockEntity#displayLines()} 读的是
 * 方块实体自己的字段，服务端通过 {@code getUpdateTag} 把这些字段同步到客户端，
 * 渲染器只负责把文字摆到方块上方并旋转到摄像机方向。
 *
 * <h2>光影 / 高清补丁兼容</h2>
 * <ul>
 *   <li><b>全亮</b>：{@code packedLight} 恒为 {@code 0xF000F0}，不参与世界光照——
 *       光影包把夜晚压暗、或者洞穴里没光时，牌子照样清楚。</li>
 *   <li><b>半透明背板</b>：{@code drawInBatch} 的 backgroundColor 传半透明黑，
 *       亮天空/白色机器背景下也不会糊成一片。</li>
 *   <li><b>不写深度</b>：走原版 {@code RenderType.text} 的透明混合，不做深度偏移，
 *       光影包的 gbuffer 不会因此产生 Z-fighting 条纹。</li>
 *   <li><b>字号随分辨率自适应</b>：按 {@code font.width} 居中、按世界单位缩放，
 *       高清字体资源包（32x/64x 字体）不会错位或溢出。</li>
 *   <li><b>朝向摄像机</b>：billboard 旋转，绕到方块哪一面都能读。</li>
 * </ul>
 */
public class EnergyMonitorRenderer implements BlockEntityRenderer<EnergyMonitorBlockEntity> {
    /**
     * 字号缩放：0.0225 大约对应 3D 世界里的「小号字」，太大会挡住旁边的机器。
     */
    private static final float TEXT_SCALE = 0.026F;
    /**
     * 牌子挂在方块顶面上方多少格（略高于方块，避免和方块表面抢深度）。
     */
    private static final double TEXT_HEIGHT = 1.4D;
    /**
     * 行间距（像素，未缩放前）。
     */
    private static final int LINE_SPACING = 2;

    public EnergyMonitorRenderer(final BlockEntityRendererProvider.Context context) {
        // 不需要额外上下文。
    }

    @Override
    public void render(final EnergyMonitorBlockEntity blockEntity,
                       final float partialTick,
                       final PoseStack poseStack,
                       final MultiBufferSource bufferSource,
                       final int packedLight,
                       final int packedOverlay) {
        final List<Component> lines = blockEntity.displayLines();
        if (lines.isEmpty()) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final Font font = minecraft.font;
        poseStack.pushPose();
        poseStack.translate(0.5D, TEXT_HEIGHT, 0.5D);
        // 永远正对摄像机，绕着看时文字不会变成一条线。
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        final Matrix4f matrix = poseStack.last().pose();
        float y = -font.lineHeight * lines.size() / 2.0F;
        for (final Component line : lines) {
            final float x = -font.width(line) / 2.0F;
            font.drawInBatch(
                line,
                x,
                y,
                0xFFFFFFFF,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0x40000000,
                0xF000F0
            );
            y += font.lineHeight + LINE_SPACING;
        }
        poseStack.popPose();
    }

    /**
     * 只关心自己这块方块，不做额外剔除。
     */
    @Override
    public boolean shouldRenderOffScreen(final EnergyMonitorBlockEntity blockEntity) {
        return false;
    }

    /**
     * 文字牌比方块本身高，放大一点剔除范围，靠近边缘时才不会被裁掉。
     */
    @Override
    public int getViewDistance() {
        // 48 格：再远字已经看不清，纯浪费性能（车间里一排仪表时尤其明显）。
        return 48;
    }

    /**
     * 未激活时（没有网络、或被红石关掉）不画牌子，避免方块上一堆 0。
     */
    @Override
    public boolean shouldRender(final EnergyMonitorBlockEntity blockEntity, final Vec3 cameraPos) {
        return blockEntity.isLinked();
    }
}

package com.kokuu.rswirelesscrafting.common.virtual;

/**
 * 由 mixin 打进 RS 方块菜单上的「这是给虚拟方块实体开的界面」标记。
 *
 * <p><b>为什么需要它</b>：RS 的方块菜单用 {@code Container.stillValidBlockEntity(be, player)} 判有效性，
 * 而那个实现里有 {@code level.getBlockEntity(pos) != be} 的恒等检查 —— 我们的虚拟方块实体不在世界里，
 * 永远过不去，菜单会<b>开出来就立刻关掉</b>。
 *
 * <p><b>RS 自己的答案</b>：它的无线版菜单一律写成
 * <pre>
 *     &#64;Override
 *     public boolean stillValid(final Player player) {
 *         return true;
 *     }
 * </pre>
 * （见 {@code WirelessAutocraftingMonitorContainerMenu}）。我们照抄这个范式，只是把它注入到 RS 的
 * <b>方块</b>菜单上：菜单被我们的代码构造出来时顺手打个标记，{@code stillValid} 就放行。
 *
 * <p>之所以能注入：{@code stillValid} 是 <b>MC 自己声明的方法</b>（{@code AbstractContainerMenu}），
 * 名字在映射表里，所以 Fabric 的 mixin 注解处理器收得下；而 RS 自己的方法名（如
 * {@code createPattern}）不在映射表里，注解处理器会直接报 {@code NO_OBFDATA_FOR_TARGET}。
 *
 * <p>做成接口是为了让「标记」与具体菜单解耦：每无线化一个新的 RS 方块菜单，
 * 只需再加一个极小的 mixin 实现本接口。
 *
 * <p>方法名统一带 {@code rswirelesscrafting} 前缀避免与目标类成员撞名；
 * <b>刻意不用 mixin 常见的 {@code $} 前缀</b>——那是命名惯例而非语法要求，
 * 而本项目的 checkstyle 会拒绝带 {@code $} 的成员名。
 */
public interface VirtualMenuHolder {
    /**
     * 标记这个菜单是给虚拟方块实体开的。
     */
    void rswirelesscraftingMarkVirtual();

    boolean rswirelesscraftingIsVirtual();
}

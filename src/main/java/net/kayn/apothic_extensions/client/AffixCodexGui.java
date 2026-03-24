package net.kayn.apothic_extensions.client;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingScreen;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.apothic_extensions.ApothicExtensionsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AffixCodexGui implements Renderable, GuiEventListener {

    public static final ResourceLocation TEX = new ResourceLocation("apothic_extensions", "textures/gui/affix_codex_bg.png");

    public static final int WIDTH = 200;
    public static final int HEIGHT = 166;
    public static final int RARITY_BAR_Y = 4;
    public static final int RARITY_BAR_H = 18;
    public static final int DROPDOWN_Y = 25;
    public static final int DROPDOWN_H = 12;
    public static final int DROPDOWN_ITEM_H = 12;
    public static final int CONTENT_Y = 39;
    public static final int SCROLL_W = 10;
    public static final int ENTRY_H = 20;
    public static final int CONTENT_H = HEIGHT - CONTENT_Y - 5;

    public static final int MAX_AFFIX = CONTENT_H / ENTRY_H;
    public static final int MAX_DROP_VISIBLE = 6;

    public static final int LIST_X_OFF = 9;
    public static final int LIST_W = WIDTH - LIST_X_OFF - SCROLL_W - 3;

    private static final int ITEM_SIZE = 16;
    private static final int ITEM_GAP = 2;

    public static boolean wasOpen = false;
    private static float savedAffixScroll = 0f;
    private static String savedCategoryName = null;
    private static String savedRarityId = null;

    private final ReforgingScreen parent;
    private final Font font;
    public final ImageButton toggleBtn;

    private int leftPos, topPos;
    private boolean open = false;

    private List<LootCategory> categories = new ArrayList<>();
    private List<DynamicHolder<LootRarity>> rarities = new ArrayList<>();
    private List<Affix> currentAffixes = new ArrayList<>();

    private boolean dropdownOpen = false;
    private int dropdownScroll = 0;

    private LootCategory selectedCategory = null;
    private DynamicHolder<LootRarity> selectedRarity = null;

    private float affixScrollOffset = 0f;
    private int affixStartIndex = 0;
    private boolean scrollingAffixes = false;

    private int getScrollX() {
        return leftPos + WIDTH - SCROLL_W - 2;
    }

    private int getScrollY() {
        return topPos + CONTENT_Y + 1;
    }

    public AffixCodexGui(ReforgingScreen parent) {
        this.parent = parent;
        this.font = Minecraft.getInstance().font;
        recomputePos();

        this.toggleBtn = new ImageButton(parent.getGuiLeft() + ApothicExtensionsConfig.CODEX_BUTTON_X.get(), parent.getGuiTop() + ApothicExtensionsConfig.CODEX_BUTTON_Y.get(), 10, 10, 200, 0, 10, TEX, 256, 256, btn -> this.toggleVisibility(), Component.translatable("apothic_extensions.gui.show_codex")) {
            @Override
            public void setFocused(boolean f) {
            }
        };

        this.rarities = RarityRegistry.INSTANCE.getOrderedRarities().stream().filter(h -> h.isBound()).collect(Collectors.toList());
        this.categories = LootCategory.VALUES.stream().filter(c -> !c.isNone()).collect(Collectors.toList());

        affixScrollOffset = savedAffixScroll;
        if (savedCategoryName != null) selectedCategory = LootCategory.byId(savedCategoryName);
        if (savedRarityId != null) {
            var h = tryRarity(savedRarityId);
            if (h != null && h.isBound()) selectedRarity = h;
        }
        refreshAffixes();
        affixStartIndex = idx(affixScrollOffset, currentAffixes.size(), MAX_AFFIX);
    }

    private void recomputePos() {
        this.leftPos = parent.getGuiLeft() - WIDTH + 1;
        this.topPos = parent.getGuiTop();
    }

    private static int idx(float off, int total, int vis) {
        return Math.round(off * Math.max(0, total - vis));
    }

    private static DynamicHolder<LootRarity> tryRarity(String id) {
        try {
            return RarityRegistry.byLegacyId(id);
        } catch (Exception e) {
            return null;
        }
    }

    private void refreshAffixes() {
        currentAffixes.clear();
        if (selectedCategory == null || selectedRarity == null || !selectedRarity.isBound()) return;
        LootRarity rar = selectedRarity.get();
        currentAffixes = AffixRegistry.INSTANCE.getValues().stream().filter(a -> {
            try {
                return a.canApplyTo(ItemStack.EMPTY, selectedCategory, rar);
            } catch (Exception e) {
                return false;
            }
        }).sorted(Comparator.comparing(a -> {
            try {
                return a.getName(true).getString();
            } catch (Exception e) {
                return "";
            }
        })).collect(Collectors.toList());
        affixScrollOffset = 0f;
        affixStartIndex = 0;
        savedAffixScroll = 0f;
    }

    public void toggleVisibility() {
        this.open = !this.open;
        recomputePos();
        this.toggleBtn.setPosition(parent.getGuiLeft() + ApothicExtensionsConfig.CODEX_BUTTON_X.get(), parent.getGuiTop() + ApothicExtensionsConfig.CODEX_BUTTON_Y.get());
        wasOpen = this.open;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float pt) {
        this.toggleBtn.setX(parent.getGuiLeft() + ApothicExtensionsConfig.CODEX_BUTTON_X.get());
        this.toggleBtn.setY(parent.getGuiTop() + ApothicExtensionsConfig.CODEX_BUTTON_Y.get());

        if (!open) return;

        recomputePos();

        int l = this.leftPos;
        int t = this.topPos;

        gfx.blit(TEX, l, t, 0, 0, WIDTH, HEIGHT, 256, 256);

        renderRarityCircles(gfx, mouseX, mouseY);

        if (!dropdownOpen) {
            if (selectedCategory != null && selectedRarity != null && selectedRarity.isBound()) {
                renderAffixList(gfx, mouseX, mouseY);
                renderScrollbar(gfx);
            } else {
                String msg = I18n.get(selectedRarity == null ? "apothic_extensions.gui.select_rarity" : "apothic_extensions.gui.select_category");
                gfx.drawString(font, msg, l + LIST_X_OFF + (LIST_W - font.width(msg)) / 2, t + CONTENT_Y + CONTENT_H / 2 - 4, 0xFF555555, false);
            }
        }
        renderCategoryTabs(gfx, mouseX, mouseY);
        renderTooltips(gfx, mouseX, mouseY);
    }

    private void renderRarityCircles(GuiGraphics gfx, int mouseX, int mouseY) {
        if (rarities.isEmpty()) return;
        int totalW = rarities.size() * ITEM_SIZE + (rarities.size() - 1) * ITEM_GAP;
        int startX = leftPos + (WIDTH - totalW) / 2;
        int iy = topPos + RARITY_BAR_Y + (RARITY_BAR_H - ITEM_SIZE) / 2;

        for (int i = 0; i < rarities.size(); i++) {
            DynamicHolder<LootRarity> h = rarities.get(i);
            if (!h.isBound()) continue;
            LootRarity rar = h.get();
            int ix = startX + i * (ITEM_SIZE + ITEM_GAP);
            boolean sel = selectedRarity != null && selectedRarity.isBound() && selectedRarity.get() == rar;
            boolean hov = mouseX >= ix && mouseX < ix + ITEM_SIZE && mouseY >= iy && mouseY < iy + ITEM_SIZE;

            if (sel) {
                gfx.fill(ix - 1, iy - 1, ix + ITEM_SIZE + 1, iy + ITEM_SIZE + 1, 0xFF000000 | rar.getColor().getValue());
                gfx.fill(ix, iy, ix + ITEM_SIZE, iy + ITEM_SIZE, 0xFF222222);
            } else if (hov) {
                gfx.fill(ix - 1, iy - 1, ix + ITEM_SIZE + 1, iy + ITEM_SIZE + 1, 0x55FFFFFF);
            }
            gfx.renderItem(new ItemStack(rar.getMaterial()), ix, iy);
        }
    }

    private void renderCategoryTabs(GuiGraphics gfx, int mouseX, int mouseY) {
        int dx = leftPos + 4, dy = topPos + DROPDOWN_Y, dw = WIDTH - 8;
        String label = selectedCategory != null ? getCatName(selectedCategory) : I18n.get("apothic_extensions.gui.select_category");
        int labelColor = (selectedRarity != null && selectedRarity.isBound()) ? (0xFF000000 | selectedRarity.get().getColor().getValue()) : 0xFFFFFFFF;

        gfx.drawString(font, label, dx + 4, dy + (DROPDOWN_H - font.lineHeight) / 2 + 1, labelColor, false);
        gfx.blit(TEX, dx + dw - 10, dy + 2, 192, 220, 8, 8, 256, 256);

        if (dropdownOpen) {
            int visibleCount = Math.min(MAX_DROP_VISIBLE, categories.size());
            int listH = visibleCount * DROPDOWN_ITEM_H;
            int itemY = dy + DROPDOWN_H;
            gfx.fill(dx, itemY, dx + dw, itemY + listH + 1, 0xFF555555);
            gfx.fill(dx + 1, itemY, dx + dw - 1, itemY + listH, 0xFFAAAAAA);

            for (int i = 0; i < visibleCount; i++) {
                int idx = dropdownScroll + i;
                if (idx >= categories.size()) break;
                LootCategory cat = categories.get(idx);
                int iy = itemY + i * DROPDOWN_ITEM_H;
                boolean hov = mouseX >= dx && mouseX < dx + dw && mouseY >= iy && mouseY < iy + DROPDOWN_ITEM_H;
                boolean sel = cat == selectedCategory;
                gfx.blit(TEX, dx + 1, iy, 0, (hov || sel) ? 238 : 226, Math.min(dw - 2, 191), DROPDOWN_ITEM_H, 256, 256);
                if (sel) gfx.fill(dx + 1, iy, dx + 3, iy + DROPDOWN_ITEM_H, 0xFF444444);
                gfx.drawString(font, getCatName(cat), dx + 6, iy + (DROPDOWN_ITEM_H - font.lineHeight) / 2, labelColor, false);
            }
        }
    }

    private String getCatName(LootCategory cat) {
        String key = cat.getDescId();
        String t = I18n.get(key);
        return t.equals(key) ? cat.getName().substring(0, 1).toUpperCase() + cat.getName().substring(1).replace("_", " ") : t;
    }

    private void renderAffixList(GuiGraphics gfx, int mouseX, int mouseY) {
        if (selectedRarity == null || !selectedRarity.isBound()) return;
        LootRarity rar = selectedRarity.get();
        int lx = leftPos + LIST_X_OFF;
        int baseY = topPos + CONTENT_Y;
        int rarCol = 0xFF000000 | rar.getColor().getValue();

        if (currentAffixes.isEmpty()) {
            gfx.drawString(font, I18n.get("apothic_extensions.gui.no_affixes"), lx + 4, baseY + CONTENT_H / 2 - 4, 0xFF555555, false);
            return;
        }

        for (int i = 0; i < MAX_AFFIX; i++) {
            int idx = affixStartIndex + i;
            if (idx >= currentAffixes.size()) break;

            Affix affix = currentAffixes.get(idx);
            int ey = baseY + i * ENTRY_H;

            boolean hov = mouseX >= lx && mouseX < lx + LIST_W && mouseY >= ey && mouseY < ey + ENTRY_H;

            gfx.blit(TEX, lx, ey, 0, hov ? 198 : 170, LIST_W - 1, ENTRY_H, 256, 256);
            gfx.fill(lx + 1, ey + 1, lx + 3, ey + ENTRY_H - 1, rarCol);

            try {
                gfx.drawString(font, affix.getName(true), lx + 6, ey + (ENTRY_H - font.lineHeight) / 2, rarCol, true);
            } catch (Exception ignored) {
            }
        }
    }

    private static final int KNOB_SIZE = 14;

    private void renderScrollbar(GuiGraphics gfx) {
        if (currentAffixes.size() <= MAX_AFFIX) return;

        int sx = getScrollX();
        int trackT = getScrollY();
        int constrainedTrackH = CONTENT_H - 2;

        int maxIdx = currentAffixes.size() - MAX_AFFIX;
        int knobY = maxIdx > 0 ? affixStartIndex * (constrainedTrackH - KNOB_SIZE) / maxIdx : 0;

        gfx.blit(TEX, sx + (SCROLL_W - KNOB_SIZE) / 2 + 2, trackT + knobY + 1, 200, 20, KNOB_SIZE, KNOB_SIZE, 256, 256);
    }

    private void renderTooltips(GuiGraphics gfx, int mouseX, int mouseY) {
        if (rarities.isEmpty()) return;
        int totalW = rarities.size() * ITEM_SIZE + (rarities.size() - 1) * ITEM_GAP;
        int startX = leftPos + (WIDTH - totalW) / 2, cy0 = topPos + RARITY_BAR_Y;
        for (int i = 0; i < rarities.size(); i++) {
            DynamicHolder<LootRarity> h = rarities.get(i);
            if (h.isBound() && mouseX >= startX + i * (ITEM_SIZE + ITEM_GAP) && mouseX < startX + i * (ITEM_SIZE + ITEM_GAP) + ITEM_SIZE && mouseY >= cy0 && mouseY < cy0 + RARITY_BAR_H) {
                gfx.renderTooltip(font, h.get().toComponent(), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean isMouseOver(double mx, double my) {
        return open && mx >= leftPos && mx < leftPos + WIDTH && my >= topPos && my < topPos + HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!open) return false;
        int totalW = rarities.size() * ITEM_SIZE + (rarities.size() - 1) * ITEM_GAP;
        int startX = leftPos + (WIDTH - totalW) / 2, cy0 = topPos + RARITY_BAR_Y;
        for (int i = 0; i < rarities.size(); i++) {
            DynamicHolder<LootRarity> h = rarities.get(i);
            if (h.isBound() && mx >= startX + i * (ITEM_SIZE + ITEM_GAP) && mx < startX + i * (ITEM_SIZE + ITEM_GAP) + ITEM_SIZE && my >= cy0 && my < cy0 + RARITY_BAR_H) {
                selectedRarity = h;
                savedRarityId = RarityRegistry.INSTANCE.getKey(h.get()).toString();
                refreshAffixes();
                return true;
            }
        }
        int dx = leftPos + 4, dw = WIDTH - 8;
        if (mx >= dx && mx < dx + dw && my >= topPos + DROPDOWN_Y && my < topPos + DROPDOWN_Y + DROPDOWN_H) {
            dropdownOpen = !dropdownOpen;
            return true;
        }
        if (dropdownOpen) {
            int itemY = topPos + DROPDOWN_Y + DROPDOWN_H;
            for (int i = 0; i < Math.min(MAX_DROP_VISIBLE, categories.size()); i++) {
                if (mx >= dx && mx < dx + dw && my >= itemY + i * DROPDOWN_ITEM_H && my < itemY + (i + 1) * DROPDOWN_ITEM_H) {
                    selectedCategory = categories.get(dropdownScroll + i);
                    savedCategoryName = selectedCategory.getName();
                    dropdownOpen = false;
                    refreshAffixes();
                    return true;
                }
            }
            dropdownOpen = false;
            return true;
        }
        if (currentAffixes.size() > MAX_AFFIX && mx >= leftPos + WIDTH - SCROLL_W - 3 && my >= topPos + CONTENT_Y && my < topPos + CONTENT_Y + CONTENT_H) {
            scrollingAffixes = true;
            updateScroll((float) (my - topPos - CONTENT_Y) / CONTENT_H);
            return true;
        }
        return isMouseOver(mx, my);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (open && scrollingAffixes) {
            updateScroll((float) (my - topPos - CONTENT_Y) / CONTENT_H);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        scrollingAffixes = false;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (!open) return false;
        if (dropdownOpen) {
            int max = Math.max(0, categories.size() - MAX_DROP_VISIBLE);
            if (max > 0) dropdownScroll = Mth.clamp(dropdownScroll - (int) Math.signum(delta), 0, max);
            return true;
        }
        if (mx >= leftPos + LIST_X_OFF) {
            int max = Math.max(0, currentAffixes.size() - MAX_AFFIX);
            if (max > 0) updateScroll(affixScrollOffset - (float) Math.signum(delta) / max);
            return true;
        }
        return false;
    }

    private void updateScroll(float raw) {
        affixScrollOffset = Mth.clamp(raw, 0f, 1f);
        affixStartIndex = (int) (affixScrollOffset * Math.max(0, currentAffixes.size() - MAX_AFFIX) + 0.5f);
        savedAffixScroll = affixScrollOffset;
    }

    public void renderLateTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        if (!open || selectedCategory == null || selectedRarity == null || !selectedRarity.isBound()) return;
        LootRarity rar = selectedRarity.get();
        int lx    = leftPos + LIST_X_OFF;
        int baseY = topPos  + CONTENT_Y;
        for (int i = 0; i < MAX_AFFIX; i++) {
            int idx = affixStartIndex + i;
            if (idx >= currentAffixes.size()) break;
            Affix affix = currentAffixes.get(idx);
            int ey = baseY + i * ENTRY_H;
            if (mouseX >= lx && mouseX < lx + LIST_W && mouseY >= ey && mouseY < ey + ENTRY_H) {
                try {
                    java.util.List<Component> lines = new java.util.ArrayList<>();
                    lines.add(affix.getName(true).copy()
                            .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(rar.getColor())));
                    Component desc = affix.getDescription(ItemStack.EMPTY, rar, 0.5f);
                    if (desc == null || desc.getString().isBlank())
                        desc = affix.getAugmentingText(ItemStack.EMPTY, rar, 0.5f);
                    if (desc != null && !desc.getString().isBlank()) {
                        net.minecraft.network.chat.TextColor col = desc.getStyle().getColor();
                        if (col == null || col.getValue() == 0xFFFFFF)
                            desc = desc.copy().withStyle(net.minecraft.ChatFormatting.YELLOW);
                        lines.add(desc);
                    }
                    gfx.renderComponentTooltip(font, lines, mouseX, mouseY);
                } catch (Exception ignored) {}
                return;
            }
        }
    }

    @Override
    public void setFocused(boolean f) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
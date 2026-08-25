package io.github.piscescup.mcwiki.client;

import io.github.piscescup.mcwiki.wiki.WikiCategory;

/**
 * GUI text selected independently from Minecraft's global language.
 */
public final class WikiTexts {
    private WikiTexts() {
    }

    public static String text(String language, String key) {
        boolean chinese = language.equals("zh_cn");
        return switch (key) {
            case "loading" -> chinese ? "正在查询 Minecraft Wiki..." : "Searching Minecraft Wiki...";
            case "no_results" -> chinese ? "没有找到相关页面。" : "No matching page was found.";
            case "error" -> chinese ? "查询失败" : "Search failed";
            case "close" -> chinese ? "关闭" : "Close";
            case "language_set" -> chinese ? "界面语言已设置为简体中文。" : "GUI language set to English.";
            case "source" -> chinese ? "来源" : "Source";
            case "abstract" -> chinese ? "摘要" : "Abstract";
            case "reference" -> chinese ? "参考链接" : "Reference";
            case "open_source" -> chinese ? "打开原文" : "Open article";
            case "tables" -> chinese ? "附表" : "Tables";
            case "table" -> chinese ? "表" : "Table";
            case "untitled_table" -> chinese ? "未命名表格" : "Untitled table";
            case "article" -> chinese ? "正文" : "Article";
            case "previous_table" -> chinese ? "上一张" : "Previous";
            case "next_table" -> chinese ? "下一张" : "Next";
            case "entry" -> chinese ? "条目" : "Entry";
            case "field" -> chinese ? "字段" : "Field";
            case "recipe" -> chinese ? "合成配方" : "Crafting recipe";
            case "output" -> chinese ? "产物" : "Output";
            case "and_more" -> chinese ? "等可替换材料" : "and alternatives";
            default -> key;
        };
    }

    public static String category(String language, WikiCategory category) {
        if (!language.equals("zh_cn")) {
            return category.id();
        }
        return switch (category) {
            case TRADING -> "交易";
            case BREWING -> "酿造";
            case ENCHANTING -> "附魔";
            case MOB -> "生物";
            case BLOCK -> "方块";
            case ITEM -> "物品";
            case BIOME -> "生物群系";
            case EFFECT -> "状态效果";
            case CRAFTING -> "合成";
            case SMELTING -> "烧炼";
            case SMITHING -> "锻造";
            case STRUCTURE -> "结构";
            case REDSTONE -> "红石";
            case COMMAND -> "命令";
            case VERSION -> "版本";
            case TUTORIAL -> "教程";
        };
    }
}

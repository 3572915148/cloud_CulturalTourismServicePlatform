package com.jingdezhen.tourism.mcp.tool.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdezhen.tourism.entity.Product;
import com.jingdezhen.tourism.mcp.model.ToolDefinition;
import com.jingdezhen.tourism.mcp.tool.AbstractMcpTool;
import com.jingdezhen.tourism.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 旅行预算计算工具
 * 根据旅行参数计算详细的预算清单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetTravelBudgetTool extends AbstractMcpTool {
    
    private final ProductMapper productMapper;
    
    @Override
    public String getName() {
        return "get_travel_budget";
    }
    
    @Override
    public String getDescription() {
        return "根据旅行天数、人数、住宿标准、用餐标准等参数，计算详细的旅行预算，包括交通、住宿、餐饮、门票、购物等各项开支。";
    }
    
    @Override
    public ToolDefinition getDefinition() {
        Map<String, ToolDefinition.PropertySchema> properties = new LinkedHashMap<>();
        
        properties.put("days", ToolDefinition.PropertySchema.builder()
                .type("integer")
                .description("旅游天数")
                .minimum(1)
                .maximum(30)
                .build());
        
        properties.put("people", ToolDefinition.PropertySchema.builder()
                .type("integer")
                .description("旅游人数")
                .minimum(1)
                .maximum(50)
                .defaultValue(1)
                .build());
        
        properties.put("accommodationLevel", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("住宿标准")
                .enumValues(Arrays.asList("budget", "standard", "comfort", "luxury"))
                .defaultValue("standard")
                .build());
        
        properties.put("mealLevel", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("用餐标准")
                .enumValues(Arrays.asList("budget", "standard", "premium"))
                .defaultValue("standard")
                .build());
        
        properties.put("includeTransport", ToolDefinition.PropertySchema.builder()
                .type("boolean")
                .description("是否包含往返交通费用")
                .defaultValue(true)
                .build());
        
        properties.put("transportType", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("交通方式")
                .enumValues(Arrays.asList("train", "plane", "car", "bus"))
                .defaultValue("train")
                .build());
        
        properties.put("departureCity", ToolDefinition.PropertySchema.builder()
                .type("string")
                .description("出发城市")
                .build());
        
        properties.put("shoppingBudget", ToolDefinition.PropertySchema.builder()
                .type("number")
                .description("购物预算（元/人）")
                .minimum(0)
                .defaultValue(500)
                .build());
        
        properties.put("includeShopping", ToolDefinition.PropertySchema.builder()
                .type("boolean")
                .description("是否包含购物预算")
                .defaultValue(true)
                .build());
        
        ToolDefinition.InputSchema inputSchema = ToolDefinition.InputSchema.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("days"))
                .build();
        
        return ToolDefinition.builder()
                .name(getName())
                .description(getDescription())
                .inputSchema(inputSchema)
                .build();
    }
    
    @Override
    protected Object doExecute(Map<String, Object> params) {
        // 解析参数
        Integer days = getIntParam(params, "days", 3);
        Integer people = getIntParam(params, "people", 1);
        String accommodationLevel = getStringParam(params, "accommodationLevel", "standard");
        String mealLevel = getStringParam(params, "mealLevel", "standard");
        Boolean includeTransport = getBooleanParam(params, "includeTransport", true);
        String transportType = getStringParam(params, "transportType", "train");
        String departureCity = getStringParam(params, "departureCity", null);
        Double shoppingBudget = getDoubleParam(params, "shoppingBudget", 500.0);
        Boolean includeShopping = getBooleanParam(params, "includeShopping", true);
        
        log.info("计算旅行预算 - 天数: {}, 人数: {}, 住宿: {}, 用餐: {}", 
                days, people, accommodationLevel, mealLevel);
        
        // 初始化预算项目
        Map<String, Object> budget = new HashMap<>();
        List<Map<String, Object>> budgetItems = new ArrayList<>();
        
        // 1. 交通费用
        if (includeTransport) {
            Map<String, Object> transportItem = calculateTransportCost(transportType, departureCity, people);
            budgetItems.add(transportItem);
        }
        
        // 2. 住宿费用
        Map<String, Object> accommodationItem = calculateAccommodationCost(accommodationLevel, days, people);
        budgetItems.add(accommodationItem);
        
        // 3. 餐饮费用
        Map<String, Object> mealItem = calculateMealCost(mealLevel, days, people);
        budgetItems.add(mealItem);
        
        // 4. 景点门票费用
        Map<String, Object> attractionItem = calculateAttractionCost(days, people);
        budgetItems.add(attractionItem);
        
        // 5. 市内交通费用
        Map<String, Object> localTransportItem = calculateLocalTransportCost(days, people);
        budgetItems.add(localTransportItem);
        
        // 6. 购物费用
        if (includeShopping) {
            Map<String, Object> shoppingItem = new HashMap<>();
            shoppingItem.put("category", "购物");
            shoppingItem.put("description", "陶瓷工艺品、纪念品等");
            shoppingItem.put("unitPrice", shoppingBudget);
            shoppingItem.put("quantity", people);
            shoppingItem.put("subtotal", shoppingBudget * people);
            budgetItems.add(shoppingItem);
        }
        
        // 7. 其他费用（预留10%）
        double totalBeforeMisc = budgetItems.stream()
                .mapToDouble(item -> ((Number) item.get("subtotal")).doubleValue())
                .sum();
        double miscCost = totalBeforeMisc * 0.1;
        Map<String, Object> miscItem = new HashMap<>();
        miscItem.put("category", "其他");
        miscItem.put("description", "应急费用、小费等");
        miscItem.put("subtotal", miscCost);
        budgetItems.add(miscItem);
        
        // 计算总费用
        double totalCost = budgetItems.stream()
                .mapToDouble(item -> ((Number) item.get("subtotal")).doubleValue())
                .sum();
        
        // 构建返回结果
        budget.put("days", days);
        budget.put("people", people);
        budget.put("budgetItems", budgetItems);
        budget.put("totalCost", totalCost);
        budget.put("perPersonCost", totalCost / people);
        budget.put("averageDailyCost", totalCost / days);
        
        // 添加预算建议
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        // 省钱建议
        Map<String, Object> savingTip = new HashMap<>();
        savingTip.put("type", "省钱建议");
        savingTip.put("tips", Arrays.asList(
                "提前30天预订机票/火车票可节省20-40%",
                "选择工作日出行，酒店价格通常便宜30%",
                "购买景点联票可节省15-20%",
                "选择当地特色餐馆，价格实惠且地道"
        ));
        suggestions.add(savingTip);
        
        // 支出占比分析
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("type", "支出占比分析");
        Map<String, String> breakdown = new HashMap<>();
        for (Map<String, Object> item : budgetItems) {
            String category = (String) item.get("category");
            double subtotal = ((Number) item.get("subtotal")).doubleValue();
            double percentage = (subtotal / totalCost) * 100;
            breakdown.put(category, String.format("%.1f%%", percentage));
        }
        analysis.put("breakdown", breakdown);
        suggestions.add(analysis);
        
        budget.put("suggestions", suggestions);
        
        // 添加预算等级评估
        String budgetLevel;
        double perPersonPerDay = totalCost / people / days;
        if (perPersonPerDay < 300) {
            budgetLevel = "经济型";
        } else if (perPersonPerDay < 600) {
            budgetLevel = "舒适型";
        } else if (perPersonPerDay < 1000) {
            budgetLevel = "高档型";
        } else {
            budgetLevel = "豪华型";
        }
        budget.put("budgetLevel", budgetLevel);
        
        return budget;
    }
    
    /**
     * 计算交通费用
     */
    private Map<String, Object> calculateTransportCost(String transportType, String departureCity, int people) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", "往返交通");
        
        // 基础价格（往返）
        double basePrice;
        switch (transportType) {
            case "plane":
                basePrice = 1200;
                item.put("description", "往返机票");
                break;
            case "train":
                basePrice = 400;
                item.put("description", "往返火车票");
                break;
            case "car":
                basePrice = 600;
                item.put("description", "自驾油费+过路费");
                break;
            case "bus":
                basePrice = 250;
                item.put("description", "往返长途汽车");
                break;
            default:
                basePrice = 400;
                item.put("description", "往返交通");
        }
        
        item.put("unitPrice", basePrice);
        item.put("quantity", people);
        item.put("subtotal", basePrice * people);
        
        return item;
    }
    
    /**
     * 计算住宿费用
     */
    private Map<String, Object> calculateAccommodationCost(String level, int days, int people) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", "住宿");
        
        // 每晚价格
        double pricePerNight;
        switch (level) {
            case "budget":
                pricePerNight = 150;
                item.put("description", "经济型酒店/青旅 (150元/晚)");
                break;
            case "comfort":
                pricePerNight = 400;
                item.put("description", "舒适型酒店 (400元/晚)");
                break;
            case "luxury":
                pricePerNight = 800;
                item.put("description", "豪华酒店 (800元/晚)");
                break;
            default:
                pricePerNight = 250;
                item.put("description", "标准型酒店 (250元/晚)");
        }
        
        int nights = days - 1; // 住宿天数比旅行天数少1
        int rooms = (int) Math.ceil(people / 2.0); // 每间房2人
        
        item.put("unitPrice", pricePerNight);
        item.put("quantity", nights * rooms);
        item.put("subtotal", pricePerNight * nights * rooms);
        item.put("nights", nights);
        item.put("rooms", rooms);
        
        return item;
    }
    
    /**
     * 计算餐饮费用
     */
    private Map<String, Object> calculateMealCost(String level, int days, int people) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", "餐饮");
        
        // 每人每天三餐费用
        double costPerDay;
        switch (level) {
            case "budget":
                costPerDay = 60;
                item.put("description", "经济用餐 (60元/人/天)");
                break;
            case "premium":
                costPerDay = 200;
                item.put("description", "高档餐饮 (200元/人/天)");
                break;
            default:
                costPerDay = 120;
                item.put("description", "标准用餐 (120元/人/天)");
        }
        
        item.put("unitPrice", costPerDay);
        item.put("quantity", days * people);
        item.put("subtotal", costPerDay * days * people);
        
        return item;
    }
    
    /**
     * 计算景点门票费用
     */
    private Map<String, Object> calculateAttractionCost(int days, int people) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", "景点门票");
        
        // 获取平均门票价格
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCategoryId, 1L)
                .eq(Product::getStatus, 1);
        
        List<Product> attractions = productMapper.selectList(queryWrapper);
        double avgPrice = attractions.stream()
                .mapToDouble(p -> p.getPrice().doubleValue())
                .average()
                .orElse(80.0);
        
        int attractionsPerDay = 2; // 平均每天2个景点
        
        item.put("description", String.format("门票费用 (%.0f元/人/景点，每天%d个景点)", 
                avgPrice, attractionsPerDay));
        item.put("unitPrice", avgPrice);
        item.put("quantity", days * attractionsPerDay * people);
        item.put("subtotal", avgPrice * days * attractionsPerDay * people);
        
        return item;
    }
    
    /**
     * 计算市内交通费用
     */
    private Map<String, Object> calculateLocalTransportCost(int days, int people) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", "市内交通");
        item.put("description", "出租车、公交等 (50元/人/天)");
        
        double costPerDay = 50;
        
        item.put("unitPrice", costPerDay);
        item.put("quantity", days * people);
        item.put("subtotal", costPerDay * days * people);
        
        return item;
    }
    
    @Override
    public void validateParams(Map<String, Object> params) {
        Integer days = getIntParam(params, "days", null);
        if (days == null || days < 1 || days > 30) {
            throw new IllegalArgumentException("旅游天数必须在1-30天之间");
        }
        
        Integer people = getIntParam(params, "people", 1);
        if (people < 1 || people > 50) {
            throw new IllegalArgumentException("旅游人数必须在1-50人之间");
        }
    }
}


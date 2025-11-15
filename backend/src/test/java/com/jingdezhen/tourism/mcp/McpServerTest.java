package com.jingdezhen.tourism.mcp;

import com.jingdezhen.tourism.mcp.model.McpRequest;
import com.jingdezhen.tourism.mcp.model.McpResponse;
import com.jingdezhen.tourism.mcp.server.McpServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MCP Server 测试类
 */
@SpringBootTest
public class McpServerTest {
    
    @Autowired
    private McpServer mcpServer;
    
    @Test
    public void testServerInitialization() {
        assertNotNull(mcpServer);
        assertEquals(4, mcpServer.getTools().size());
        
        // 验证所有工具都已注册
        assertTrue(mcpServer.getTools().containsKey("search_attractions"));
        assertTrue(mcpServer.getTools().containsKey("recommend_daily_plan"));
        assertTrue(mcpServer.getTools().containsKey("find_accommodations"));
        assertTrue(mcpServer.getTools().containsKey("get_travel_budget"));
    }
    
    @Test
    public void testInitializeRequest() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-1");
        request.setMethod("initialize");
        request.setParams(new HashMap<>());
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-1", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testToolsListRequest() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-2");
        request.setMethod("tools/list");
        request.setParams(new HashMap<>());
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-2", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testSearchAttractionsTool() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-3");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "search_attractions");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("keyword", "陶瓷");
        arguments.put("page", 1);
        arguments.put("pageSize", 5);
        params.put("arguments", arguments);
        
        request.setParams(params);
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-3", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testRecommendDailyPlanTool() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-4");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "recommend_daily_plan");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("days", 3);
        arguments.put("budget", 2000.0);
        arguments.put("pace", "moderate");
        params.put("arguments", arguments);
        
        request.setParams(params);
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-4", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testFindAccommodationsTool() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-5");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "find_accommodations");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("region", "昌江区");
        arguments.put("maxPrice", 300.0);
        arguments.put("page", 1);
        params.put("arguments", arguments);
        
        request.setParams(params);
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-5", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testGetTravelBudgetTool() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-6");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "get_travel_budget");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("days", 3);
        arguments.put("people", 2);
        arguments.put("accommodationLevel", "standard");
        params.put("arguments", arguments);
        
        request.setParams(params);
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-6", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
    
    @Test
    public void testInvalidMethod() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-7");
        request.setMethod("invalid_method");
        request.setParams(new HashMap<>());
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-7", response.getId());
        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
    }
    
    @Test
    public void testInvalidToolName() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-8");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "invalid_tool");
        params.put("arguments", new HashMap<>());
        request.setParams(params);
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-8", response.getId());
        assertNotNull(response.getError());
        assertEquals(-32602, response.getError().getCode());
    }
    
    @Test
    public void testParameterValidation() throws Exception {
        McpRequest request = new McpRequest();
        request.setId("test-9");
        request.setMethod("tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "search_attractions");
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("page", -1); // 无效的页码
        params.put("arguments", arguments);
        
        request.setParams(params);
        
        McpResponse response = mcpServer.handleRequest(request).get();
        
        assertNotNull(response);
        assertEquals("test-9", response.getId());
        assertNotNull(response.getError());
    }
}


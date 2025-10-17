<template>
  <div class="order-manage">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">订单管理</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 24px">
      <!-- 筛选工具栏 -->
      <div class="toolbar">
        <div class="filter-group">
          <el-input 
            v-model="query.orderNo" 
            placeholder="订单号" 
            style="width: 200px"
            clearable
            @clear="fetchData"
            @keyup.enter="fetchData"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-input 
            v-model="query.contactPhone" 
            placeholder="联系电话" 
            style="width: 160px"
            clearable
            @clear="fetchData"
            @keyup.enter="fetchData"
          >
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
          <el-select 
            v-model="query.status" 
            placeholder="订单状态" 
            style="width: 140px"
            clearable
            @change="fetchData"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
            <el-option label="已退款" :value="4" />
          </el-select>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <!-- 订单列表 -->
      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180"/>
        <el-table-column label="产品" min-width="220">
          <template #default="{ row }">
            <div class="product-info">
              <img v-if="row.productImage" :src="row.productImage" class="product-img" />
              <span>{{ row.productTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center"/>
        <el-table-column label="联系人" width="120">
          <template #default="{ row }">
            <div>{{ row.contactName }}</div>
            <div class="phone">{{ row.contactPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="预订日期" width="120">
          <template #default="{ row }">
            <div>{{ row.bookingDate || '-' }}</div>
            <div class="time-slot">{{ row.bookingTime || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160"/>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="handleConfirm(row)">确认</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="handleRefund(row)">退款</el-button>
            <el-button v-if="row.status === 0 || row.status === 1" link type="danger" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :total="total"
          :page-size="query.size"
          :current-page.sync="query.current"
          @current-change="(p)=>{query.current=p;fetchData()}"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="680px">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态" :span="2">
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="产品名称" :span="2">{{ currentOrder.productTitle }}</el-descriptions-item>
          <el-descriptions-item label="产品封面" :span="2">
            <img v-if="currentOrder.productImage" :src="currentOrder.productImage" style="width: 200px; border-radius: 4px;" />
          </el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ currentOrder.price }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ currentOrder.quantity }}</el-descriptions-item>
          <el-descriptions-item label="总金额" :span="2">
            <span class="price">¥{{ currentOrder.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="预订日期">{{ currentOrder.bookingDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预订时间">{{ currentOrder.bookingTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentOrder.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.payTime" label="支付时间" :span="2">{{ currentOrder.payTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.completeTime" label="完成时间" :span="2">{{ currentOrder.completeTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.cancelTime" label="取消时间" :span="2">{{ currentOrder.cancelTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="480px">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因">
          <el-input 
            v-model="cancelForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请输入取消原因（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancel" :loading="saving">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Phone, RefreshLeft } from '@element-plus/icons-vue'
import { 
  getMerchantOrders, 
  getOrderDetail, 
  confirmOrder, 
  completeOrder, 
  cancelOrder,
  refundOrder 
} from '@/api/merchantOrder'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  status: undefined,
  orderNo: '',
  contactPhone: ''
})

const detailVisible = ref(false)
const currentOrder = ref(null)

const cancelVisible = ref(false)
const cancelForm = reactive({
  orderId: null,
  reason: ''
})
const saving = ref(false)

// 获取订单列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMerchantOrders(query)
    list.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  query.status = undefined
  query.orderNo = ''
  query.contactPhone = ''
  query.current = 1
  fetchData()
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const res = await getOrderDetail(row.id)
    currentOrder.value = res.data
    detailVisible.value = true
  } catch (err) {
    ElMessage.error(err.message || '获取订单详情失败')
  }
}

// 确认订单
const handleConfirm = (row) => {
  ElMessageBox.confirm('确认该订单已支付？', '提示', {
    type: 'info',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await confirmOrder(row.id)
      ElMessage.success('订单已确认')
      fetchData()
    } catch (err) {
      ElMessage.error(err.message || '操作失败')
    }
  })
}

// 完成订单
const handleComplete = (row) => {
  ElMessageBox.confirm('确认完成该订单？', '提示', {
    type: 'success',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await completeOrder(row.id)
      ElMessage.success('订单已完成')
      fetchData()
    } catch (err) {
      ElMessage.error(err.message || '操作失败')
    }
  })
}

// 取消订单
const handleCancel = (row) => {
  cancelForm.orderId = row.id
  cancelForm.reason = ''
  cancelVisible.value = true
}

const confirmCancel = async () => {
  saving.value = true
  try {
    await cancelOrder(cancelForm.orderId, cancelForm.reason)
    ElMessage.success('订单已取消')
    cancelVisible.value = false
    fetchData()
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    saving.value = false
  }
}

// 退款
const handleRefund = (row) => {
  ElMessageBox.confirm('确认退款该订单？退款后订单状态将变为"已退款"', '提示', {
    type: 'warning',
    confirmButtonText: '确认退款',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await refundOrder(row.id)
      ElMessage.success('退款成功')
      fetchData()
    } catch (err) {
      ElMessage.error(err.message || '操作失败')
    }
  })
}

// 状态文本
const getStatusText = (status) => {
  const map = {
    0: '待支付',
    1: '已支付',
    2: '已完成',
    3: '已取消',
    4: '已退款'
  }
  return map[status] || '未知'
}

// 状态类型
const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'info',
    4: 'danger'
  }
  return map[status] || ''
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.order-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.toolbar {
  margin-bottom: 16px;
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
}

.price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.phone {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.time-slot {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.order-detail {
  padding: 8px 0;
}
</style>

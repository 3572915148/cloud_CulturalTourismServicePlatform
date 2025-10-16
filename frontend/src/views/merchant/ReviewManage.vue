<template>
  <div class="review-manage">
    <el-page-header @back="router.back()" title="返回">
      <template #content>
        <span class="page-title">评价管理</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 24px">
      <el-empty description="评价管理功能开发中，敬请期待...">
        <el-button type="primary" @click="router.push('/merchant/dashboard')">
          返回工作台
        </el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.review-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}
</style>


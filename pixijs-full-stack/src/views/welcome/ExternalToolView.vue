<script setup>
defineProps({
  url: { type: String, required: true },
  title: { type: String, default: '' }
})
</script>

<template>
  <div class="external-tool">
    <div class="tool-header" v-if="title">
      <span class="tool-title">{{ title }}</span>
      <a class="tool-link" :href="url" target="_blank" title="在新标签页打开">🔗</a>
    </div>
    <iframe :src="url" class="tool-iframe" frameborder="0" />
  </div>
</template>

<style scoped>
.external-tool {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tool-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: #fff;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.tool-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.tool-link {
  font-size: 16px;
  text-decoration: none;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.tool-link:hover {
  opacity: 1;
}

.tool-iframe {
  flex: 1;
  width: 100%;
  border: none;
}
</style>

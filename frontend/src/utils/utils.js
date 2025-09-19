// 生成UUID
export const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c == 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

// 格式化会话标题
export const formatSessionTitle = (message) => {
  if (!message) return '新会话'
  // 截取前15个字符作为标题
  return message.length > 15 ? message.substring(0, 15) + '...' : message
}